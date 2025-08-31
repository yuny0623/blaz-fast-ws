package org.razelo

import java.lang.management.ManagementFactory
import java.time.LocalDateTime

object Logger {
    private val runtime = Runtime.getRuntime()
    private val osBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    fun info(message: String) = log(LoggerLevel.INFO, message)
    fun warn(message: String) = log(LoggerLevel.WARN, message)
    fun error(message: String) = log(LoggerLevel.ERROR, message)
    fun debug(message: String) = log(LoggerLevel.DEBUG, message)

    private fun log(level: LoggerLevel, message: String) {
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMem = runtime.maxMemory() / 1024 / 1024
        val cpu = (osBean.processCpuLoad * 100).toInt()

        val now = LocalDateTime.now()
        println("${level.color}[${level.level}][${now}][CPU ${cpu}%][MEM ${usedMem}MB/${maxMem}MB] $message")
    }
}