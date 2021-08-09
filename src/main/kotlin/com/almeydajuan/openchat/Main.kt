package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.ClockImpl
import com.almeydajuan.openchat.model.LoginDto
import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.PublicationDto
import com.almeydajuan.openchat.model.PublicationTextDto
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.model.UserDto
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequestAndResponse
import org.http4k.filter.ServerFilters.CatchAll
import org.http4k.format.Jackson.auto
import org.http4k.format.Jackson.autoBody
import org.http4k.lens.Path
import org.http4k.lens.string
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer

fun main() {
    val restReceptionist = RestReceptionist(OpenChatSystem(ClockImpl()))
    newBackend(restReceptionist).asServer(ApacheServer(port = 8080)).start()
}

val registrationBodyLens = Body.auto<RegistrationDto>().toLens()
val loginBodyLens = Body.auto<LoginDto>().toLens()
val publicationBodyLens = Body.auto<PublicationTextDto>().toLens()

val userResponseLens = autoBody<UserDto>().toLens()
val userListResponseLens = autoBody<List<UserDto>>().toLens()
val publicationResponseLens = autoBody<PublicationDto>().toLens()
val publicationListResponseLens = autoBody<List<PublicationDto>>().toLens()

val userIdPathLens = Path.string().of("userId")

fun newBackend(restReceptionist: RestReceptionist) = routes(
    "/status" bind GET to {
        Response(OK).body("OpenChat: OK!")
    },
    "/users" bind GET to {
        userListResponseLens.inject(restReceptionist.users(), Response(OK))
    },
    "/users" bind POST to {
        val registrationDto = registrationBodyLens.extract(it)
        val user = restReceptionist.registerUser(registrationDto)
        userResponseLens.inject(user, Response(CREATED))
    },
    "/users/:userId/timeline" bind GET to {
        val userId = userIdPathLens.extract(it)
        publicationListResponseLens.inject(restReceptionist.timelineOf(userId), Response(OK))
    },
    "/users/:userId/timeline" bind POST to {
        val userId = userIdPathLens.extract(it)
        val publication = publicationBodyLens.extract(it)

        publicationResponseLens.inject(restReceptionist.addPublication(userId, publication), Response(CREATED))
    },
    "/login" bind POST to {
        val loginDto = loginBodyLens.extract(it)
        val user = restReceptionist.login(loginDto)
        user?.let { userDto ->
            userResponseLens.inject(userDto, Response(OK))
        } ?: Response(NOT_FOUND)
    }
).withFilter(PrintRequestAndResponse().then(CatchAll()))