package org.razelo

enum class LoggerLevel(val level: String, val color: String) {
    INFO("INFO","\u001B[34m"),            // 파랑
    DEBUG("DEBUG","\u001B[32m"),           // 초록
    WARN("WARN","\u001B[33m"),            // 노랑
    ERROR("ERROR","\u001B[31m")            // 빨강
}