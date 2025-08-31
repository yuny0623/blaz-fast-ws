package org.razelo

class HttpParser {
    private val HTTP_METHODS = listOf("POST", "GET", "PUT", "DELETE", "OPTION")

    //            POST /api/v1/items HTTP/1.1
    //            Host: localhost:8081
    //            User-Agent: curl/8.14.1
    //            Accept: */*
    //            Content-Type: application/json
    //            Content-Length: 16
    //
    //            {name:foo,qty:3}
    fun parse(str: String): HttpParseResultDto? {
        try {
            val httpRequest = HttpParseResultDto()

            var lines = str.split("\n")

            // 첫번째 라인 처리
            val firstLine = lines[0]
            val tokens = firstLine.split(" ")
            if (tokens.size < 3) {
                Logger.error("http first line error")
                return null
            }
            if (HTTP_METHODS.contains(tokens[0])) {
                httpRequest.method = tokens[0]
                httpRequest.requestTarget = tokens[1]
                httpRequest.httpVersion = tokens[0]
            } else {
                Logger.error("no containing allowed http methods")
                return null
            }

            lines = lines.subList(1, lines.size)

            // 헤더 처리
            var bodyFlag = false
            var idx = 1
            run breaking@ {
                lines.forEach { couldBeHeader ->
                    if (couldBeHeader.isEmpty()) {
                        bodyFlag = true
                        idx++
                        return@breaking
                    }

                    val tokens = couldBeHeader.split(":")
                    if (tokens.size < 2) {
                        return@forEach
                    }
                    val key = tokens[0].trim()
                    val value = tokens[1].trim()
                    httpRequest.headers[key] = value
                    idx++
                }
            }
            lines = lines.subList(idx, lines.size)
            val requestBody = lines[0]
            httpRequest.requestBody = requestBody

            Logger.info("res: ${httpRequest}")

            return httpRequest
        } catch (e: Exception) {
            Logger.error("UnHandled Exception occur: ${e.stackTraceToString()}")
            return null
        }
    }
}


data class HttpParseResultDto(
    var method: String? = null,
    var requestTarget: String? = null,
    var httpVersion: String? = null,
    val headers: MutableMap<String, String> = mutableMapOf(),
    var requestBody: String? = null,
)