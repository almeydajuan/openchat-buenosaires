package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.ClockImpl
import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.model.User
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequestAndResponse
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.format.Jackson.auto
import org.http4k.format.Moshi.autoBody
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer

fun main() {
    val restReceptionist = RestReceptionist(OpenChatSystem(ClockImpl()))
    newBackend(restReceptionist).asServer(ApacheServer(port = 8080)).start()
}

val registrationLens = Body.auto<RegistrationDto>().toLens()
val userLens = autoBody<User>().toLens()

fun newBackend(restReceptionist: RestReceptionist) = routes(
    "/status" bind GET to {
        Response(OK).body("OpenChat: OK!")
    },
    "/users" bind POST to {
        val registrationDto = registrationLens.extract(it)
        val user = restReceptionist.registerUser(registrationDto)
        userLens.inject(user, Response(CREATED))
    }
).withFilter(PrintRequestAndResponse().then(CatchAll()))