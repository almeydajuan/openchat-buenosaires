package com.almeydajuan.openchat.model

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RestReceptionist(private val system: OpenChatSystem) {
    val USERNAME_KEY = "username"
    val PASSWORD_KEY = "password"
    val ABOUT_KEY = "about"
    val HOME_PAGE_KEY = "homePage"
    val ID_KEY = "id"
    val FOLLOWED_ID_KEY = "followerId"
    val FOLLOWER_ID_KEY = "followeeId"
    val POST_ID_KEY = "postId"
    val USER_ID_KEY = "userId"
    val TEXT_KEY = "text"
    val DATE_TIME_KEY = "dateTime"
    val LIKES_KEY = "likes"
    val PUBLICATION_ID_KEY = "publicationId"
    val INVALID_CREDENTIALS = "Invalid credentials."
    val FOLLOWING_CREATED = "Following created."
    val INVALID_PUBLICATION = "Invalid post"
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private val idsByUser: MutableMap<User, String> = mutableMapOf()
    private val idsByPublication: MutableMap<Publication, String> = mutableMapOf()

    fun registerUser(registrationBodyAsJson: JsonObject) = runCatching {
        val registeredUser: User = system.register(
            userNameFrom(registrationBodyAsJson),
            passwordFrom(registrationBodyAsJson),
            aboutFrom(registrationBodyAsJson),
            homePageFrom(registrationBodyAsJson))
        val registeredUserId = UUID.randomUUID().toString()
        idsByUser[registeredUser] = registeredUserId
        ReceptionistResponse(201, userResponseAsJson(registeredUser, registeredUserId))
    }.onFailure { transformModelException(it) }.getOrThrow()

    fun login(loginBodyAsJson: JsonObject) = system.withAuthenticatedUserDo(
        userNameFrom(loginBodyAsJson),
        passwordFrom(loginBodyAsJson),
        { authenticatedUser -> authenticatedUserResponse(authenticatedUser) }
    ) {
        ReceptionistResponse(404, INVALID_CREDENTIALS)
    }

    fun users() = okResponseWithUserArrayFrom(system.users())

    fun followings(followingsBodyAsJson: JsonObject) = kotlin.runCatching {
        val followedId: String = followingsBodyAsJson.getString(FOLLOWED_ID_KEY, "")
        val followerId: String = followingsBodyAsJson.getString(FOLLOWER_ID_KEY, "")

        system.followForUserNamed(
            userNameIdentifiedAs(followedId),
            userNameIdentifiedAs(followerId))
        ReceptionistResponse(201, FOLLOWING_CREATED)
    }.onFailure {
        transformModelException(it)
    }.getOrThrow()

    private fun transformModelException(exception: Throwable) {
        if (exception is ModelException) {
            ReceptionistResponse(400, exception.reason)
        }
    }

    fun followersOf(userId: String): ReceptionistResponse {
        val followers: List<User> = system.followersOfUserNamed(userNameIdentifiedAs(userId))
        return okResponseWithUserArrayFrom(followers)
    }

    fun addPublication(userId: String, messageBodyAsJson: JsonObject) = runCatching {
        val publication: Publication = system.publishForUserNamed(userNameIdentifiedAs(userId), messageBodyAsJson.getString("text", ""))
        val publicationId = UUID.randomUUID().toString()
        idsByPublication[publication] = publicationId
        ReceptionistResponse(201, publicationAsJson(userId, publication, publicationId))
    }.onFailure { transformModelException(it) }.getOrThrow()

    fun timelineOf(userId: String) =
        publicationsAsJson(system.timeLineForUserNamed(userNameIdentifiedAs(userId)))

    fun wallOf(userId: String) = publicationsAsJson(system.wallForUserNamed(userNameIdentifiedAs(userId)))

    fun likePublicationIdentifiedAs(publicationId: String, likerAsJson: JsonObject) = runCatching {
        val userName = userNameIdentifiedAs(likerAsJson.getString(USER_ID_KEY, ""))
        val publication = idsByPublication.entries
            .firstOrNull { (_, value) -> value == publicationId }?.key ?: throw ModelException(INVALID_PUBLICATION)
        val likes: Int = system.likePublication(publication, userName)
        val likesAsJsonObject: JsonObject = JsonObject()
            .add(LIKES_KEY, likes)
        ReceptionistResponse(200, likesAsJsonObject)
    }.onFailure { transformModelException(it) }.getOrThrow()

    private fun passwordFrom(registrationAsJson: JsonObject) = registrationAsJson.getString(PASSWORD_KEY, "")

    private fun userNameFrom(registrationAsJson: JsonObject) = registrationAsJson.getString(USERNAME_KEY, "")

    private fun aboutFrom(registrationAsJson: JsonObject) = registrationAsJson.getString(ABOUT_KEY, "")

    private fun homePageFrom(registrationAsJson: JsonObject) = registrationAsJson.getString(HOME_PAGE_KEY, "")

    private fun userResponseAsJson(registeredUser: User, registeredUserId: String) = JsonObject()
        .add(ID_KEY, registeredUserId)
        .add(USERNAME_KEY, registeredUser.name)
        .add(ABOUT_KEY, registeredUser.about)
        .add(HOME_PAGE_KEY, registeredUser.homePage)

    private fun authenticatedUserResponse(authenticatedUser: User) =
        ReceptionistResponse(200, userResponseAsJson(
            authenticatedUser,
            userIdFor(authenticatedUser)).toString())

    private fun userIdentifiedAs(userId: String): User =
        idsByUser.entries.firstOrNull { (_, value) -> value == userId }?.key
            ?: throw ModelException(INVALID_CREDENTIALS)

    private fun okResponseWithUserArrayFrom(users: List<User>): ReceptionistResponse {
        val usersAsJsonArray = JsonArray()
        users.map { userResponseAsJson(it, userIdFor(it)) }.forEach { usersAsJsonArray.add(it) }
        return ReceptionistResponse(200, usersAsJsonArray)
    }

    private fun userNameIdentifiedAs(userId: String) = userIdentifiedAs(userId).name

    private fun publicationAsJson(userId: String, publication: Publication, publicationId: String): JsonObject {
        return JsonObject()
            .add(POST_ID_KEY, publicationId)
            .add(USER_ID_KEY, userId)
            .add(TEXT_KEY, publication.message)
            .add(DATE_TIME_KEY, formatDateTime(publication.publicationTime))
            .add(LIKES_KEY, system.likesOf(publication))
    }

    private fun formatDateTime(dateTimeToFormat: LocalDateTime) = DATE_TIME_FORMATTER.format(dateTimeToFormat)

    private fun publicationsAsJson(timeLine: List<Publication>): ReceptionistResponse {
        val publicationsAsJsonObject = JsonArray()
        timeLine.map {
            publicationAsJson(
                userId = userIdFor(user = it.publisherRelatedUser()),
                publication = it,
                publicationId = publicationIdFor(it))
        }.forEach {
            publicationsAsJsonObject.add(it)
        }
        return ReceptionistResponse(200, publicationsAsJsonObject)
    }

    private fun publicationIdFor(publication: Publication) = idsByPublication.getValue(publication)

    private fun userIdFor(user: User) = idsByUser.getValue(user)
}