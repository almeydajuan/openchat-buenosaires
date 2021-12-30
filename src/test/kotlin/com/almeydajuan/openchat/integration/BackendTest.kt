package com.almeydajuan.openchat.integration

import com.almeydajuan.openchat.TestObjectsBucket.createJuanPerezRegistrationDto
import com.almeydajuan.openchat.TestObjectsBucket.createPepeSanchezRegistrationDto
import com.almeydajuan.openchat.TestObjectsBucket.createRegistrationDto
import com.almeydajuan.openchat.followingBodyLens
import com.almeydajuan.openchat.loginBodyLens
import com.almeydajuan.openchat.model.CANNOT_REGISTER_SAME_USER_TWICE
import com.almeydajuan.openchat.model.FollowingDto
import com.almeydajuan.openchat.model.INVALID_CREDENTIALS
import com.almeydajuan.openchat.model.LoginDto
import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.model.UserDto
import com.almeydajuan.openchat.newBackend
import com.almeydajuan.openchat.registrationBodyLens
import com.almeydajuan.openchat.userListResponseLens
import com.almeydajuan.openchat.userResponseLens
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BackendTest {

    private val backend = newBackend(RestReceptionist(OpenChatSystem()))

    @Test
    fun `backend is up`() {
        val response = backend(Request(GET, "/status"))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.bodyString()).isEqualTo("OpenChat: OK!")
    }

    @Nested
    inner class RegistrationValidation {
        @Test
        fun `register twice the same user should fail`() {
            val registrationDto = createJuanPerezRegistrationDto()
            registerUser(registrationDto)

            val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
            assertThat(registrationResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(registrationResponse.bodyString()).isEqualTo(CANNOT_REGISTER_SAME_USER_TWICE)
        }
    }

    private fun registerUser(registrationDto: RegistrationDto): UserDto {
        val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
        assertThat(registrationResponse.status).isEqualTo(CREATED)
        return userResponseLens.get(registrationResponse)
    }

    @Nested
    inner class LoginValidation {
        @Test
        fun `login user`() {
            val registrationDto = createJuanPerezRegistrationDto()
            registerUser(registrationDto)

            val loginResponse = backend(loginBodyLens.set(
                    target = Request(POST, "/login"),
                    value = LoginDto(registrationDto.username, registrationDto.password))
            )
            assertThat(loginResponse.status).isEqualTo(OK)
        }

        @Test
        fun `should fail when login with non existent user`() {
            val loginResponse = backend(loginBodyLens.set(
                    Request(POST, "/login"),
                    LoginDto("user", "password"))
            )
            assertThat(loginResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(loginResponse.bodyString()).isEqualTo(INVALID_CREDENTIALS)
        }

        @Test
        fun `should fail when login with wrong password`() {
            val registrationDto = createJuanPerezRegistrationDto()
            registerUser(registrationDto)

            val loginResponse = backend(loginBodyLens.set(
                    target = Request(POST, "/login"),
                    value = LoginDto(registrationDto.username, registrationDto.password + "sadsa"))
            )
            assertThat(loginResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(loginResponse.bodyString()).isEqualTo(INVALID_CREDENTIALS)
        }
    }

    @Nested
    inner class UsersValidation {
        @Test
        fun `find no one`() {
            val usersResponse = backend(Request(GET, "/users"))
            assertThat(usersResponse.status).isEqualTo(OK)

            val userList = userListResponseLens.get(usersResponse)
            assertThat(userList).isEmpty()
        }

        @Test
        fun `find all users`() {
            val juanPerez = registerUser(createJuanPerezRegistrationDto())
            val pepeSanchez = registerUser(createPepeSanchezRegistrationDto())

            val usersResponse = backend(Request(GET, "/users"))
            assertThat(usersResponse.status).isEqualTo(OK)

            val userList = userListResponseLens.get(usersResponse)
            assertThat(userList.size).isEqualTo(2)

            val registeredJuanPerez = userList.first()
            assertThat(registeredJuanPerez.username).isEqualTo(juanPerez.username)
            assertThat(registeredJuanPerez.about).isEqualTo(juanPerez.about)
            assertThat(registeredJuanPerez.homePage).isEqualTo(juanPerez.homePage)

            val registeredPepeSanchez = userList.last()
            assertThat(registeredPepeSanchez.username).isEqualTo(pepeSanchez.username)
            assertThat(registeredPepeSanchez.about).isEqualTo(pepeSanchez.about)
            assertThat(registeredPepeSanchez.homePage).isEqualTo(pepeSanchez.homePage)
        }
    }

    @Nested
    inner class FolloweesValidation {
        @Test
        fun `new user has no followees`() {
            val juanPerez = registerUser(createJuanPerezRegistrationDto())

            val followeesResponse = backend(Request(GET, "/followings/${juanPerez.userId}/followees"))
            assertThat(followeesResponse.status).isEqualTo(OK)

            val followees = userListResponseLens.get(followeesResponse)
            assertThat(followees).isEmpty()
        }

        @Test
        fun `find all followees for user`() {
            val juanPerez = registerUser(createJuanPerezRegistrationDto())
            val maria = registerUser(createRegistrationDto("maria"))
            val diego = registerUser(createRegistrationDto("diego"))

            addFollowing(FollowingDto(maria.userId, juanPerez.userId))
            addFollowing(FollowingDto(diego.userId, juanPerez.userId))

            val followeesResponse = backend(Request(GET, "/followings/${juanPerez.userId}/followees"))
            assertThat(followeesResponse.status).isEqualTo(OK)

            val followees = userListResponseLens.get(followeesResponse)
            assertThat(followees).hasSize(2)
            assertThat(followees).isEqualTo(listOf(maria, diego))
        }

        private fun addFollowing(followingDto: FollowingDto) {
            val followingResponse = backend(followingBodyLens.set(Request(POST, "/followings"), followingDto))
            assertThat(followingResponse.status).isEqualTo(CREATED)
        }
    }
}