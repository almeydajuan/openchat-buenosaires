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

    @Test
    fun `can register user`() {
        val system = createSystem()
        val registeredUser = registerPepeSanchez(system)

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME))
        assertEquals(1, system.numberOfUsers())
        assertTrue(registeredUser.isNamed(TestObjectsBucket.PEPE_SANCHEZ_NAME))
        assertEquals(TestObjectsBucket.PEPE_SANCHEZ_ABOUT, registeredUser.about)
        assertEquals(TestObjectsBucket.PEPE_SANCHEZ_HOME_PAGE, registeredUser.homePage)
        assertNotEquals(TestObjectsBucket.PEPE_SANCHEZ_HOME_PAGE + "x", registeredUser.homePage)
    }

    private fun registerPepeSanchez(system: OpenChatSystem): User = system.register(
            TestObjectsBucket.PEPE_SANCHEZ_NAME,
            TestObjectsBucket.PEPE_SANCHEZ_PASSWORD,
            TestObjectsBucket.PEPE_SANCHEZ_ABOUT,
            TestObjectsBucket.PEPE_SANCHEZ_HOME_PAGE
    )

    private fun createSystem() = OpenChatSystem()
}