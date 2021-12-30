package com.almeydajuan.openchat.integration

import com.almeydajuan.openchat.TestObjectsBucket.createJuanPerezRegistrationDto
import com.almeydajuan.openchat.loginBodyLens
import com.almeydajuan.openchat.model.CANNOT_REGISTER_SAME_USER_TWICE
import com.almeydajuan.openchat.model.INVALID_CREDENTIALS
import com.almeydajuan.openchat.model.LoginDto
import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.newBackend
import com.almeydajuan.openchat.registrationBodyLens
import com.almeydajuan.openchat.userListResponseLens
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
            val registrationDto = registerJuanPerez()

            val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
            assertThat(registrationResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(registrationResponse.bodyString()).isEqualTo(CANNOT_REGISTER_SAME_USER_TWICE)
        }
    }

    private fun registerJuanPerez(): RegistrationDto {
        val registrationDto = createJuanPerezRegistrationDto()
        val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
        assertThat(registrationResponse.status).isEqualTo(CREATED)
        return registrationDto
    }

    @Nested
    inner class LoginValidation {
        @Test
        fun `login user`() {
            val registrationDto = registerJuanPerez()

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
            val registrationDto = registerJuanPerez()

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
        fun `find all users`() {
            val registrationDto = registerJuanPerez()

            val usersResponse = backend(Request(GET, "/users"))
            assertThat(usersResponse.status).isEqualTo(OK)

            val userList = userListResponseLens.get(usersResponse)
            assertThat(userList.size).isEqualTo(1)

            val registeredUser = userList.first()
            assertThat(registeredUser.username).isEqualTo(registrationDto.username)
            assertThat(registeredUser.about).isEqualTo(registrationDto.about)
            assertThat(registeredUser.homePage).isEqualTo(registrationDto.homePage)
        }
    }
}