package com.almeydajuan.openchat.integration

import com.almeydajuan.openchat.TestFactory.createRegistrationDto
import com.almeydajuan.openchat.TestUtilities
import com.almeydajuan.openchat.followingBodyLens
import com.almeydajuan.openchat.likerBodyLens
import com.almeydajuan.openchat.loginBodyLens
import com.almeydajuan.openchat.model.CANNOT_REGISTER_SAME_USER_TWICE
import com.almeydajuan.openchat.model.FollowingDto
import com.almeydajuan.openchat.model.INAPPROPRIATE_WORD
import com.almeydajuan.openchat.model.INVALID_CREDENTIALS
import com.almeydajuan.openchat.model.LikerDto
import com.almeydajuan.openchat.model.LoginDto
import com.almeydajuan.openchat.model.OpenChatSystem
import com.almeydajuan.openchat.model.PublicationDto
import com.almeydajuan.openchat.model.PublicationTextDto
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.RestReceptionist
import com.almeydajuan.openchat.model.UserDto
import com.almeydajuan.openchat.newBackend
import com.almeydajuan.openchat.publicationBodyLens
import com.almeydajuan.openchat.publicationListResponseLens
import com.almeydajuan.openchat.publicationResponseLens
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
            val registrationDto = createRegistrationDto("user")
            registerUser(registrationDto)

            val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
            assertThat(registrationResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(registrationResponse.bodyString()).isEqualTo(CANNOT_REGISTER_SAME_USER_TWICE)
        }
    }

    private fun registerUser(registrationDto: RegistrationDto = createRegistrationDto("user")): UserDto {
        val registrationResponse = backend(registrationBodyLens.set(Request(POST, "/users"), registrationDto))
        assertThat(registrationResponse.status).isEqualTo(CREATED)
        return userResponseLens(registrationResponse)
    }

    private fun addFollowing(follower: UserDto, followee: UserDto) {
        val followingResponse = backend(followingBodyLens.set(
                target = Request(POST, "/followings"),
                value = FollowingDto(follower.userId, followee.userId))
        )
        assertThat(followingResponse.status).isEqualTo(CREATED)
    }

    private fun addPublication(user: UserDto, publication: PublicationTextDto): PublicationDto {
        val publicationResponse = backend(publicationBodyLens.set(Request(POST, timelineUrlForUser(user)), publication))
        assertThat(publicationResponse.status).isEqualTo(CREATED)
        return publicationResponseLens(publicationResponse)
    }

    private fun timelineUrlForUser(user: UserDto) = "/users/${user.userId}/timeline"

    @Nested
    inner class LoginValidation {

        @Test
        fun `login user`() {
            val registrationDto = createRegistrationDto("user")
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
            val registrationDto = createRegistrationDto("user")
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
            assertThat(usersResponse.bodyString()).isEqualTo("[]")

            val userList = userListResponseLens(usersResponse)
            assertThat(userList).isEmpty()
        }

        @Test
        fun `find all users`() {
            val juan = registerUser(createRegistrationDto("juan"))
            val carlos = registerUser(createRegistrationDto("carlos"))

            val usersResponse = backend(Request(GET, "/users"))
            assertThat(usersResponse.status).isEqualTo(OK)

            val userList = userListResponseLens(usersResponse)
            assertThat(userList.size).isEqualTo(2)
            assertThat(userList).isEqualTo(listOf(juan, carlos))
        }
    }

    @Nested
    inner class FolloweesValidation {

        @Test
        fun `new user has no followees`() {
            val juan = registerUser(createRegistrationDto("juan"))

            val followeesResponse = backend(Request(GET, "/followings/${juan.userId}/followees"))
            assertThat(followeesResponse.status).isEqualTo(OK)
            assertThat(followeesResponse.bodyString()).isEqualTo("[]")

            val followees = userListResponseLens(followeesResponse)
            assertThat(followees).isEmpty()
        }

        @Test
        fun `find all followees for user`() {
            val juan = registerUser(createRegistrationDto("juan"))
            val maria = registerUser(createRegistrationDto("maria"))
            val diego = registerUser(createRegistrationDto("diego"))

            addFollowing(maria, juan)
            addFollowing(diego, juan)

            val followeesResponse = backend(Request(GET, "/followings/${juan.userId}/followees"))
            assertThat(followeesResponse.status).isEqualTo(OK)

            val followees = userListResponseLens(followeesResponse)
            assertThat(followees).hasSize(2)
            assertThat(followees).isEqualTo(listOf(maria, diego))
        }
    }

    @Nested
    inner class TimelineValidation {

        @Test
        fun `empty timeline for new user`() {
            val user = registerUser()

            val timelineResponse = backend(Request(GET, timelineUrlForUser(user)))
            assertThat(timelineResponse.status).isEqualTo(OK)
            assertThat(timelineResponse.bodyString()).isEqualTo("[]")

            val timeline = publicationListResponseLens(timelineResponse)
            assertThat(timeline).isEmpty()
        }

        @Test
        fun `user can add a post`() {
            val user = registerUser()
            val publicationDto = PublicationTextDto("some text")
            val publication = addPublication(user, publicationDto)

            val timelineResponse = backend(Request(GET, timelineUrlForUser(user)))
            assertThat(timelineResponse.status).isEqualTo(OK)
            assertThat(timelineResponse.bodyString()).isEqualTo(
                    "[{\"postId\":\"${publication.postId}\",\"userId\":\"${user.userId}\",\"text\":\"${publicationDto.text}\",\"dateTime\":\"${publication.dateTime}\",\"likes\":0}]"
            )

            val timeline = publicationListResponseLens(timelineResponse)
            assertThat(timeline).hasSize(1)

            val post = timeline.first()
            assertThat(post).isEqualTo(publication)
            assertThat(post.userId).isEqualTo(user.userId)
            assertThat(post.likes).isEqualTo(0)
        }

        @Test
        fun `timeline for user`() {
            val user = registerUser()

            val rangeOfPublications = (1..5).map { it.toString() }

            rangeOfPublications.forEach {
                addPublication(user, PublicationTextDto(it))
                TestUtilities.delayOneSecond()
            }
            val timelineResponse = backend(Request(GET, timelineUrlForUser(user)))
            assertThat(timelineResponse.status).isEqualTo(OK)

            val timeline = publicationListResponseLens(timelineResponse)
            assertThat(timeline).hasSize(rangeOfPublications.count())
            assertThat(timeline.map { it.text }).isEqualTo(rangeOfPublications.reversed())
        }

        @Test
        fun `user can like a post`() {
            val user = registerUser()
            addPublication(user, PublicationTextDto("some text"))

            val post = publicationListResponseLens(backend(Request(GET, timelineUrlForUser(user)))).first()

            val likeResponse = backend(likerBodyLens.set(Request(POST, "/publications/${post.postId}/like"), LikerDto(user.userId)))
            assertThat(likeResponse.status).isEqualTo(OK)
            assertThat(likeResponse.bodyString()).isEqualTo("{\"likes\":1}")

            val updatedPost = publicationListResponseLens(backend(Request(GET, timelineUrlForUser(user)))).first()
            assertThat(updatedPost.likes).isEqualTo(1)
        }

        @Test
        fun `user cannot add inappropriate post`() {
            val user = registerUser()
            val inappropriatePost = PublicationTextDto("orange")

            val publicationResponse = backend(publicationBodyLens.set(Request(POST, timelineUrlForUser(user)), inappropriatePost))
            assertThat(publicationResponse.status).isEqualTo(BAD_REQUEST)
            assertThat(publicationResponse.bodyString()).isEqualTo(INAPPROPRIATE_WORD)
        }
    }

    @Nested
    inner class WallValidation {

        @Test
        fun `retrieve user's wall`() {
            val juan = registerUser(createRegistrationDto("juan"))
            val diego = registerUser(createRegistrationDto("diego"))
            val carla = registerUser(createRegistrationDto("carla"))

            val users = listOf(juan, diego, carla)

            addFollowing(diego, juan)
            addFollowing(carla, juan)
            val postsRange = (1..10).map { it.toString() }

            postsRange.forEach {
                addPublication(users.random(), PublicationTextDto(it))
                TestUtilities.delayOneSecond()
            }

            val wallResponse = backend(Request(GET, "/users/${juan.userId}/wall"))
            assertThat(wallResponse.status).isEqualTo(OK)

            val publications = publicationListResponseLens(wallResponse)
            assertThat(publications).hasSize(postsRange.count())
            assertThat(publications.map { it.text }).isEqualTo(postsRange.reversed())
        }
    }
}