package com.almeydajuan.openchat.model

import java.time.LocalDateTime

interface Clock {
    fun now(): LocalDateTime
}

class ClockImpl : Clock {
    override fun now(): LocalDateTime = LocalDateTime.now()
}