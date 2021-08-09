package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.ClockImpl
import com.almeydajuan.openchat.model.FOLLOWING_CREATED
import com.almeydajuan.openchat.model.FollowingDto
import com.almeydajuan.openchat.model.INVALID_CREDENTIALS
import com.almeydajuan.openchat.model.LikerDto
import com.almeydajuan.openchat.model.LikesDto
import com.almeydajuan.openchat.model.LoginDto
import com.almeydajuan.openchat.model.ModelException
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
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
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
val followingBodyLens = Body.auto<FollowingDto>().toLens()
val likerBodyLens = Body.auto<LikerDto>().toLens()

val userResponseLens = autoBody<UserDto>().toLens()
val userListResponseLens = autoBody<List<UserDto>>().toLens()
val publicationResponseLens = autoBody<PublicationDto>().toLens()
val publicationListResponseLens = autoBody<List<PublicationDto>>().toLens()
val likesResponseLens = autoBody<LikesDto>().toLens()

val userIdPathLens = Path.string().of("userId")
val followerIdPathLens = Path.string().of("followerId")
val publicationIdPathLens = Path.string().of("publicationId")

fun newBackend(restReceptionist: RestReceptionist) = routes(
    "/status" bind GET to {
        Response(OK).body("OpenChat: OK!")
    },
    "/login" bind POST to {
        val loginDto = loginBodyLens.extract(it)
        runCatching {
            userResponseLens.inject(restReceptionist.login(loginDto), Response(OK))
        }.onFailure { Response(NOT_FOUND).body(INVALID_CREDENTIALS) }.getOrThrow()
    },
    "/users" bind GET to {
        userListResponseLens.inject(restReceptionist.users(), Response(OK))
    },
    "/users" bind POST to { request ->
        val registrationDto = registrationBodyLens.extract(request)
        runCatching {
            val user = restReceptionist.registerUser(registrationDto)
            userResponseLens.inject(user, Response(CREATED))
        }.onFailure { mapFailure(it) }.getOrThrow()
    },
    "/users/:userId/timeline" bind GET to {
        val userId = userIdPathLens.extract(it)

        publicationListResponseLens.inject(restReceptionist.timelineOf(userId), Response(OK))
    },
    "/users/:userId/timeline" bind POST to { request ->
        val userId = userIdPathLens.extract(request)
        val publication = publicationBodyLens.extract(request)
        runCatching {
            val publicationAdded = restReceptionist.addPublication(userId, publication)
            publicationResponseLens.inject(publicationAdded, Response(CREATED))
        }.onFailure { mapFailure(it) }.getOrThrow()
    },
    "/followings/:followerId/followees" bind GET to {
        val userId = followerIdPathLens.extract(it)
        val followers = restReceptionist.followersOf(userId)

        userListResponseLens.inject(followers, Response(OK))
    },
    "/followings" bind POST to {
        val followingDto = followingBodyLens.extract(it)
        runCatching {
            restReceptionist.followings(followingDto)
            Response(CREATED).body(FOLLOWING_CREATED)
        }.onFailure { mapFailure(it) }.getOrThrow()
    },
    "/users/:userId/wall" bind GET to {
        val userId = userIdPathLens.extract(it)
        val wall = restReceptionist.wallOf(userId)

        publicationListResponseLens.inject(wall, Response(OK))
    },
    "/publications/:publicationId/like" bind POST to {
        val publicationId = publicationIdPathLens.extract(it)
        val likerDto = likerBodyLens.extract(it)
        runCatching {
            val likesDto = restReceptionist.likePublicationIdentifiedAs(publicationId, likerDto)
            likesResponseLens.inject(likesDto, Response(OK))
        }.onFailure { mapFailure(it) }.getOrThrow()
    }
).withFilter(PrintRequestAndResponse().then(CatchAll()))

private fun mapFailure(exception: Throwable) {
    if (exception is ModelException) {
        Response(BAD_REQUEST).body(exception.reason)
    } else {
        Response(INTERNAL_SERVER_ERROR)
    }
}