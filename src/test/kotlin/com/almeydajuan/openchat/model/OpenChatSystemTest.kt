package com.almeydajuan.openchat.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OpenChatSystemTest {

    @Test
    fun `create system has no users`() {
        val system = createSystem()
        assertFalse(system.hasUsers())
        assertFalse(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME))
        assertEquals(0, system.numberOfUsers())
    }

    private fun createSystem() = OpenChatSystem()
}