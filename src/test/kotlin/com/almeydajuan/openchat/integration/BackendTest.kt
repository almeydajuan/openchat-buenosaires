package com.almeydajuan.openchat.integration

import com.almeydajuan.openchat.TestObjectsBucket.createJuanPerezRegistrationDto
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
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Test

internal class BackendTest {

    private val backend = newBackend(RestReceptionist(OpenChatSystem()))

    @Test fun `backend is up`() {
        val response = backend(Request(GET, "/status"))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.bodyString()).isEqualTo("OpenChat: OK!")
    }

    @Test fun `find all users`() {
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

    private fun registerJuanPerez(): RegistrationDto {
        val registrationDto = createJuanPerezRegistrationDto()
        val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
        assertThat(registrationResponse.status).isEqualTo(CREATED)
        return registrationDto
    }
}