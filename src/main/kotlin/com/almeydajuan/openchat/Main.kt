package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.FOLLOWING_CREATED
import com.almeydajuan.openchat.model.FollowingDto
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
import org.http4k.core.Filter
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
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
    val restReceptionist = RestReceptionist(OpenChatSystem())
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
        val loginDto = loginBodyLens(it)
        userResponseLens.inject(restReceptionist.login(loginDto), Response(OK))
    },
    "/users" bind GET to {
        userListResponseLens(restReceptionist.users(), Response(OK))
    },
    "/users" bind POST to {
        val registrationDto = registrationBodyLens(it)
        val user = restReceptionist.registerUser(registrationDto)

        userResponseLens.inject(user, Response(CREATED))
    },
    "/users/{userId}/timeline" bind GET to {
        val userId = userIdPathLens(it)
        val timeline = restReceptionist.timelineOf(userId)

        publicationListResponseLens.inject(timeline, Response(OK))
    },
    "/users/{userId}/timeline" bind POST to { request ->
        val userId = userIdPathLens(request)
        val publication = publicationBodyLens(request)
        val publicationAdded = restReceptionist.addPublication(userId, publication)

        Response(CREATED).with(publicationResponseLens of publicationAdded)
    },
    "/followings/{followerId}/followees" bind GET to {
        val userId = followerIdPathLens(it)
        val followers = restReceptionist.followersOf(userId)

        Response(OK).with(userListResponseLens of followers)
    },
    "/followings" bind POST to {
        val followingDto = followingBodyLens(it)
        restReceptionist.followings(followingDto)

        Response(CREATED).body(FOLLOWING_CREATED)
    },
    "/users/{userId}/wall" bind GET to {
        val userId = userIdPathLens(it)
        val wall = restReceptionist.wallOf(userId)

        Response(OK).with(publicationListResponseLens of wall)
    },
    "/publications/{publicationId}/like" bind POST to { request ->
        val publicationId = publicationIdPathLens(request)
        val likerDto = likerBodyLens(request)
        val likesDto = restReceptionist.likePublicationIdentifiedAs(publicationId, likerDto)

        Response(OK).with(likesResponseLens of likesDto)
    }
).withFilter(PrintRequestAndResponse().then(CatchAll()).then(mapFailures()))

fun mapFailures() = Filter { next ->
    {
        try {
            next(it)
        } catch (modelException: ModelException) {
            Response(BAD_REQUEST).body(modelException.reason)
        } catch (exception: Exception) {
            Response(INTERNAL_SERVER_ERROR)
        }
    }
}