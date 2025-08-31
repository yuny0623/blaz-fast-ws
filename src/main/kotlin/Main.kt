package org.razelo

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
fun main() {
    val LOCAL_HOST = "127.0.0.1"
    val PORT = 8081
    val MAX_CONCURRENT_HANDLERS = 10_000

    val serverEngine = MainServerEngine(
        host = LOCAL_HOST,
        port = PORT,
        maxConcurrentHandlers = MAX_CONCURRENT_HANDLERS
    )
    serverEngine.start()

    // JVM 종료 시 마지막으로 서버를 정리하도록 처리
    Runtime.getRuntime().addShutdownHook(
        Thread { serverEngine.close() }
    )
}