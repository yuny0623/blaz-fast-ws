package org.razelo

import java.net.InetSocketAddress
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

data class ConnState(
    @Volatile var lastReadNanos: Long = System.nanoTime(),
    @Volatile var bytesQueued: Long = 0,
    @Volatile var closed: Boolean = false
)

class MainServerEngine(
    private val host: String,
    private val port: Int,
    maxConcurrentHandlers: Int = 10_000
): AutoCloseable {

    @Volatile private var running = false
    private val states = ConcurrentHashMap<SocketChannel, ConnState>()
    private val handlerLimiter = Semaphore(maxConcurrentHandlers)

    fun start() {
        if (running) {
            return
        }

        running = true

        val acceptorThread = Thread({
            ServerSocketChannel.open().use {
                server ->
                    server.bind(InetSocketAddress(host, port))
                    server.configureBlocking(true)

                Logger.info("bind complete")

                while(running) {
                    val socketChannel = server.accept()
                        ?: continue
                    Logger.info("we accepted socketChannel: ${socketChannel.remoteAddress}")

                    socketChannel.configureBlocking(true)
                    socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true)

                    val state = ConnState()
                    states[socketChannel] = state

                    handlerLimiter.acquire()

                    ThreadPoolConfig.vtExec.submit { // submit runnable
                        try {
                            handleConnection(socketChannel, state)
                        } catch (t: Throwable) {
                            Logger.error("error: ${t.message}")
                        } finally {
                            safeClose(socketChannel)
                            state.closed = true
                            states.remove(socketChannel)
                            handlerLimiter.release()
                        }
                    }
                }
            }
        }, "acceptor")
        acceptorThread.start()
    }

    /**
     * ByteBuf 설명
     * ByteBuf는 아래 3 포인터로 동작한다.
     * 1. capacity: 버퍼의 총 크기(불변)
     * 2. position: 다음에 읽거나 쓸 인덱스
     * 3. limit: 읽기/쓰기의 상한선
     *
     * - clear(): 쓰기 모드로 초기화 시 사용 (position = 0, limit = capacity, mark제거)
     * - flip(): 쓰기 -> 읽기 모드 전환 시 사용 (limit = position, position = 0, mark 제거)
     * - remaining(): 현재 모드에서 아직 읽거나 쓸 수 있는 바이트 수 (limit - position)
     */
    private fun handleConnection(socketChannel: SocketChannel, state: ConnState) {
        val inBuf = ByteBuffer.allocateDirect(64 * 1024)
        val outBuf = ByteBuffer.allocateDirect(64 * 1024)

        while (true) {
            inBuf.clear() // 쓰기 모드로 초기화
            val n = socketChannel.read(inBuf)
            if (n < 0) {
                return
            }
            state.lastReadNanos = System.nanoTime()

            inBuf.flip()  // 쓰기 -> 읽기 모드 전환
            val bytes = ByteArray(inBuf.remaining())
            inBuf.get(bytes)
            val text = String(bytes, Charsets.UTF_8)
            Logger.info("text: $text")

            val httpParser = HttpParser()
            httpParser.parse(text)

            outBuf.clear()
            outBuf.put(inBuf)
            outBuf.flip() // 쓰기 -> 읽기 모드 전환

            state.bytesQueued += outBuf.remaining() // remaining() == 이 커넥션에 쌓여 있는 '미전송 데이터'양 --> backpressure혹은 slow client 감지용으로 쓸 수 있습니다.
            while(outBuf.hasRemaining()) {
                socketChannel.write(outBuf)
            }
            state.bytesQueued = 0
        }
    }

    private fun safeClose(socketChannel: SocketChannel) {
        try {
            socketChannel.close()
        } catch (e: Throwable) {
            Logger.error("safeClose e: ${e.message}")
        }
    }

    // AutoCloseable 추상 메서드 구현
    override fun close() {
        running = false                             // 실행 상태 종료
        MonitorThread.monitor.shutdownNow()         // 모니터 스레드 정리
        ThreadPoolConfig.vtExec.shutdown()          // 스레드풀 정리
    }
}