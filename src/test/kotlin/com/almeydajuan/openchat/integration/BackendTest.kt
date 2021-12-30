package com.almeydajuan.openchat.integration

import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.newBackend
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.jupiter.api.Test

internal class BackendTest {

    private val backend = newBackend(RestReceptionist(OpenChatSystem()))

    @Test fun `backend is up`() {
        val response = backend(Request(GET, "/status"))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.body.toString()).isEqualTo("OpenChat: OK!")
    }
}