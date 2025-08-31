package org.razelo

import java.util.concurrent.Executors

object ThreadPoolConfig {
    val vtExec = Executors.newVirtualThreadPerTaskExecutor()
}