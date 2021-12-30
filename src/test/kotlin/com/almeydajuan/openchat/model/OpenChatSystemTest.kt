package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.TestFactory.ANOTHER_USER_NAME
import com.almeydajuan.openchat.TestFactory.USER_ABOUT
import com.almeydajuan.openchat.TestFactory.USER_HOME_PAGE
import com.almeydajuan.openchat.TestFactory.USER_NAME
import com.almeydajuan.openchat.TestFactory.USER_PASSWORD
import com.almeydajuan.openchat.TestFactory.createUserNamed
import com.almeydajuan.openchat.TestUtilities
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
        assertFalse(system.hasUserNamed(USER_NAME))
        assertEquals(0, system.numberOfUsers())
    }

    @Test
    fun `can register user`() {
        val system = createSystem()
        val registeredUser = registerUser(system)

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(USER_NAME))
        assertEquals(1, system.numberOfUsers())
        assertTrue(registeredUser.isNamed(USER_NAME))
        assertEquals(USER_ABOUT, registeredUser.about)
        assertEquals(USER_HOME_PAGE, registeredUser.homePage)
        assertNotEquals(USER_HOME_PAGE + "x", registeredUser.homePage)
    }

    private fun registerUser(system: OpenChatSystem, username: String = USER_NAME): User =
            system.register(createUserNamed(username), USER_PASSWORD)

    @Test
    fun `can register many users`() {
        val system = createSystem()
        registerUser(system)
        registerUser(system, ANOTHER_USER_NAME)

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(USER_NAME))
        assertTrue(system.hasUserNamed(ANOTHER_USER_NAME))
        assertEquals(2, system.numberOfUsers())
    }

    @Test
    fun `cannot register same user twice`() {
        val system = createSystem()
        registerUser(system)

        TestUtilities.assertThrowsModelExceptionWithErrorMessage(CANNOT_REGISTER_SAME_USER_TWICE) { registerUser(system) }

        assertTrue(system.hasUsers())
        assertTrue(system.hasUserNamed(USER_NAME))
        assertEquals(1, system.numberOfUsers())
    }

    @Test
    fun `can work with authenticated user`() {
        val system = createSystem()
        registerUser(system)
        val token = Any()

        val authenticatedToken: Any = system.withAuthenticatedUserDo(USER_NAME, USER_PASSWORD, { token }) { fail() }

        assertEquals(token, authenticatedToken)
    }

    @Test
    fun `not registered user is not authenticated`() {
        val system = createSystem()

        assertCanNotAuthenticateUserWith(system, USER_PASSWORD)
    }

    private fun assertCanNotAuthenticateUserWith(system: OpenChatSystem, password: String) {
        val token = Any()
        val notAuthenticatedToken: Any = system.withAuthenticatedUserDo(USER_NAME, password, { fail() }) { token }
        assertEquals(token, notAuthenticatedToken)
    }

    @Test
    fun `cannot authenticate with invalid password`() {
        val system = createSystem()
        registerUser(system)

        assertCanNotAuthenticateUserWith(system, USER_PASSWORD + "something")
    }

    @Test
    fun `registered user can publish`() {
        val system = createSystem()
        registerUser(system)
        val publication = system.publishForUserNamed(USER_NAME, "hello")
        val timeLine = system.timeLineForUserNamed(USER_NAME)

        assertThat(timeLine).isEqualTo(listOf(publication))
    }

    @Test
    fun `no registered user cannot publish`() {
        val system = createSystem()

        TestUtilities.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.publishForUserNamed(USER_HOME_PAGE, "hello")
        }
    }

    @Test
    fun `no registered user cannot ask its timeline`() {
        val system = createSystem()
        TestUtilities.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.timeLineForUserNamed(USER_HOME_PAGE)
        }
    }

    @Test
    fun `can follow registered user`() {
        val system = createSystem()
        registerUser(system)
        val follower = registerUser(system, ANOTHER_USER_NAME)

        system.followForUserNamed(USER_NAME, ANOTHER_USER_NAME)
        val followers = system.followersOfUserNamed(USER_NAME)

        assertThat(followers).isEqualTo(listOf(follower))
    }

    @Test
    fun `can get wall of registered user`() {
        val system = createSystem()
        registerUser(system)
        registerUser(system, ANOTHER_USER_NAME)

        system.followForUserNamed(USER_NAME, ANOTHER_USER_NAME)
        val followedPublication = system.publishForUserNamed(USER_NAME, "hello")

        TestUtilities.delayOneSecond()
        val followerPublication = system.publishForUserNamed(ANOTHER_USER_NAME, "bye")
        val wall = system.wallForUserNamed(USER_NAME)

        assertThat(wall).isEqualTo(listOf(followerPublication, followedPublication))
    }

    @Test
    fun `publications have no likes when created`() {
        val system = createSystem()
        registerUser(system)

        val publication = system.publishForUserNamed(USER_NAME, "hello")

        assertEquals(0, system.likesOf(publication))
    }

    @Test
    fun `registered user can like publication`() {
        val system = createSystem()
        registerUser(system)
        registerUser(system, ANOTHER_USER_NAME)
        val publication = system.publishForUserNamed(USER_NAME, "hello")

        val likes = system.likePublication(publication, ANOTHER_USER_NAME)

        assertEquals(1, likes)
        assertEquals(1, system.likesOf(publication))
    }

    @Test
    fun `cannot like non published publication`() {
        val system = createSystem()
        val registeredUser = registerUser(system)
        val publication = Publication.madeBy(Publisher.relatedTo(registeredUser), "hello", TestUtilities.now)

        TestUtilities.assertThrowsModelExceptionWithErrorMessage(INVALID_PUBLICATION) {
            system.likePublication(publication, USER_NAME)
        }
    }

    @Test
    fun `likes by user count only once`() {
        val system = createSystem()
        registerUser(system)
        registerUser(system, ANOTHER_USER_NAME)
        val publication = system.publishForUserNamed(USER_NAME, "hello")

        system.likePublication(publication, ANOTHER_USER_NAME)
        val likes = system.likePublication(publication, ANOTHER_USER_NAME)

        assertEquals(1, likes)
        assertEquals(1, system.likesOf(publication))
    }

    @Test
    fun `not registered user cannot like publication`() {
        val system = createSystem()
        registerUser(system)
        val publication: Publication = system.publishForUserNamed(USER_NAME, "hello")

        TestUtilities.assertThrowsModelExceptionWithErrorMessage(USER_NOT_REGISTERED) {
            system.likePublication(publication, ANOTHER_USER_NAME)
        }
    }

    private fun createSystem() = OpenChatSystem()
}