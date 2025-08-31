package org.razelo

import java.util.concurrent.Executors

object MonitorThread {
    val monitor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "conn-monitor").apply {
            isDaemon = true
        }
    }
}