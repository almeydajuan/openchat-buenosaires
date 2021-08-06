package com.almeydajuan.openchat.model

import java.time.LocalDateTime

interface Clock {
    fun now(): LocalDateTime
}