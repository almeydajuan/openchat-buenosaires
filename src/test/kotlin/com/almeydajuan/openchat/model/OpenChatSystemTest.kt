package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.model.TestObjectsBucket.JUAN_PEREZ_NAME
import com.almeydajuan.openchat.model.TestObjectsBucket.JUAN_PEREZ_PASSWORD
import com.almeydajuan.openchat.model.TestObjectsBucket.PEPE_SANCHEZ_ABOUT
import com.almeydajuan.openchat.model.TestObjectsBucket.PEPE_SANCHEZ_HOME_PAGE
import com.almeydajuan.openchat.model.TestObjectsBucket.PEPE_SANCHEZ_NAME
import com.almeydajuan.openchat.model.TestObjectsBucket.PEPE_SANCHEZ_PASSWORD
import com.almeydajuan.openchat.model.TestObjectsBucket.createPepeSanchez
import com.almeydajuan.openchat.model.TestObjectsBucket.createUserJuanPerez
import com.almeydajuan.openchat.model.TestObjectsBucket.now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class OpenChatSystemTest {

    @Test
    fun `create system has no users`() {
        val system = createSystem()
        assertFalse(system.hasUsers())
        assertFalse(system.hasUserNamed(PEPE_SANCHEZ_NAME))
        assertEquals(0, system.numberOfUsers())
    }

    @Test
    fun `can register user`() {
        val system = createSystem()
        val registeredUser = registerPepeSanchez(system)

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(PEPE_SANCHEZ_NAME))
        assertEquals(1, system.numberOfUsers())
        assertTrue(registeredUser.isNamed(PEPE_SANCHEZ_NAME))
        assertEquals(PEPE_SANCHEZ_ABOUT, registeredUser.about)
        assertEquals(PEPE_SANCHEZ_HOME_PAGE, registeredUser.homePage)
        assertNotEquals(PEPE_SANCHEZ_HOME_PAGE + "x", registeredUser.homePage)
    }

    private fun registerPepeSanchez(system: OpenChatSystem): User = system.register(createPepeSanchez(), PEPE_SANCHEZ_PASSWORD)

    @Test
    fun `can register many users`() {
        val system = createSystem()
        registerPepeSanchez(system)
        registerJuanPerez(system)

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(PEPE_SANCHEZ_NAME))
        assertTrue(system.hasUserNamed(JUAN_PEREZ_NAME))
        assertEquals(2, system.numberOfUsers())
    }

    private fun registerJuanPerez(system: OpenChatSystem): User = system.register(createUserJuanPerez(), JUAN_PEREZ_PASSWORD)

    @Test
    fun `cannot register same user twice`() {
        val system = createSystem()
        registerPepeSanchez(system)

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(CANNOT_REGISTER_SAME_USER_TWICE) { registerPepeSanchez(system) }

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(PEPE_SANCHEZ_NAME))
        assertEquals(1, system.numberOfUsers())
    }

    @Test
    fun `can work with authenticated user`() {
        val system = createSystem()
        registerPepeSanchez(system)
        val token = Any()

        val authenticatedToken: Any = system.withAuthenticatedUserDo(
                PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_PASSWORD,
                { token }
        ) { fail() }

        assertEquals(token, authenticatedToken)
    }

    @Test
    fun `not registered user is not authenticated`() {
        val system = createSystem()

        assertCanNotAuthenticatePepeSanchezWith(system, PEPE_SANCHEZ_PASSWORD)
    }

    private fun assertCanNotAuthenticatePepeSanchezWith(system: OpenChatSystem, password: String) {
        val token = Any()
        val notAuthenticatedToken: Any = system.withAuthenticatedUserDo(
                PEPE_SANCHEZ_NAME, password,
                { fail() }
        ) { token }
        assertEquals(token, notAuthenticatedToken)
    }

    @Test
    fun `cannot authenticate with invalid password`() {
        val system = createSystem()
        registerPepeSanchez(system)

        assertCanNotAuthenticatePepeSanchezWith(system, PEPE_SANCHEZ_PASSWORD + "something")
    }

    @Test
    fun `registered user can publish`() {
        val system = createSystem()
        registerPepeSanchez(system)
        val publication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")
        val timeLine = system.timeLineForUserNamed(PEPE_SANCHEZ_NAME)

        assertThat(timeLine).isEqualTo(listOf(publication))
    }

    @Test
    fun `no registered user cannot publish`() {
        val system = createSystem()

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")
        }
    }

    @Test
    fun `no registered user cannot ask its timeline`() {
        val system = createSystem()
        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.timeLineForUserNamed(PEPE_SANCHEZ_NAME)
        }
    }

    @Test
    fun `can follow registered user`() {
        val system = createSystem()
        registerPepeSanchez(system)
        val follower = registerJuanPerez(system)

        system.followForUserNamed(PEPE_SANCHEZ_NAME, JUAN_PEREZ_NAME)
        val followers = system.followersOfUserNamed(PEPE_SANCHEZ_NAME)

        assertThat(followers).isEqualTo(listOf(follower))
    }

    @Test
    fun `can get wall of registered user`() {
        val system = createSystem()
        registerPepeSanchez(system)
        registerJuanPerez(system)

        system.followForUserNamed(PEPE_SANCHEZ_NAME, JUAN_PEREZ_NAME)
        val followedPublication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")

        TestObjectsBucket.changeNowTo(now.plusSeconds(1))
        val followerPublication = system.publishForUserNamed(JUAN_PEREZ_NAME, "bye")
        val wall = system.wallForUserNamed(PEPE_SANCHEZ_NAME)

        assertThat(wall).isEqualTo(listOf(followerPublication, followedPublication))
    }

    @Test
    fun `publications have no likes when created`() {
        val system = createSystem()
        registerPepeSanchez(system)

        val publication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")

        assertEquals(0, system.likesOf(publication))
    }

    @Test
    fun `registered user can like publication`() {
        val system = createSystem()
        registerPepeSanchez(system)
        registerJuanPerez(system)
        val publication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")

        val likes = system.likePublication(publication, JUAN_PEREZ_NAME)

        assertEquals(1, likes)
        assertEquals(1, system.likesOf(publication))
    }

    @Test
    fun `cannot like non published publication`() {
        val system = createSystem()
        val registeredUser = registerPepeSanchez(system)
        val publication = Publication.madeBy(Publisher.relatedTo(registeredUser), "hello", now)

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(INVALID_PUBLICATION) {
            system.likePublication(publication, PEPE_SANCHEZ_NAME)
        }
    }

    @Test
    fun `likes by user count only once`() {
        val system = createSystem()
        registerPepeSanchez(system)
        registerJuanPerez(system)
        val publication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")

        system.likePublication(publication, JUAN_PEREZ_NAME)
        val likes = system.likePublication(publication, JUAN_PEREZ_NAME)

        assertEquals(1, likes)
        assertEquals(1, system.likesOf(publication))
    }

    @Test
    fun `not registered user cannot like publication`() {
        val system = createSystem()
        registerPepeSanchez(system)
        val publication: Publication = system.publishForUserNamed(PEPE_SANCHEZ_NAME, "hello")

        TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.likePublication(publication, JUAN_PEREZ_NAME)
        }
    }

    private fun createSystem() = OpenChatSystem()
}