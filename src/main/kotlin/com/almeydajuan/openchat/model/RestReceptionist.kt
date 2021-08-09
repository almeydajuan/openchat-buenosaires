package com.almeydajuan.openchat.model

import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RestReceptionist(private val system: OpenChatSystem) {
    private val USERNAME_KEY = "username"
    private val PASSWORD_KEY = "password"
    private val ABOUT_KEY = "about"
    private val HOME_PAGE_KEY = "homePage"
    private val ID_KEY = "id"
    private val FOLLOWED_ID_KEY = "followerId"
    private val FOLLOWER_ID_KEY = "followeeId"
    private val POST_ID_KEY = "postId"
    private val USER_ID_KEY = "userId"
    private val TEXT_KEY = "text"
    private val DATE_TIME_KEY = "dateTime"
    private val LIKES_KEY = "likes"
    private val PUBLICATION_ID_KEY = "publicationId"
    private val INVALID_CREDENTIALS = "Invalid credentials."
    private val INVALID_PUBLICATION = "Invalid post"
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private val idsByUser: MutableMap<User, String> = mutableMapOf()
    private val idsByPublication: MutableMap<Publication, String> = mutableMapOf()

    fun registerUser(registrationDto: RegistrationDto) = runCatching {
        val registeredUser: User = system.register(
            userName = registrationDto.username,
            password = registrationDto.password,
            about = registrationDto.about,
            homePage = registrationDto.homePage
        )
        val registeredUserId = UUID.randomUUID().toString()
        idsByUser[registeredUser] = registeredUserId
        registeredUser.toUserDto(registeredUserId)
    }.onFailure { transformModelException(it) }.getOrThrow()

    fun login(loginDto: LoginDto): UserDto? =
        system.authenticateUser(loginDto.username, loginDto.password)?.let { it.toUserDto(userIdFor(it)) }

    fun users(): List<UserDto> = system.users().map { it.toUserDto(userIdFor(it)) }

    fun followings(followingDto: FollowingDto) = runCatching {
        system.followForUserNamed(
            userNameIdentifiedAs(followingDto.followeeId),
            userNameIdentifiedAs(followingDto.followerId)
        )
    }.onFailure {
        transformModelException(it)
    }.getOrThrow()

    private fun transformModelException(exception: Throwable) {
        if (exception is ModelException) {
            ReceptionistResponse(400, exception.reason)
        }
    }

    fun followersOf(userId: String) =
        system.followersOfUserNamed(userNameIdentifiedAs(userId)).map { it.toUserDto(userIdFor(it)) }

    fun addPublication(userId: String, publicationTextDto: PublicationTextDto) = runCatching {
        val publication: Publication = system.publishForUserNamed(userNameIdentifiedAs(userId), publicationTextDto.text)
        val publicationId = UUID.randomUUID().toString()
        idsByPublication[publication] = publicationId
        mapToPublicationDto(publication)
    }.onFailure { transformModelException(it) }.getOrThrow()

    fun timelineOf(userId: String) =
        system.timeLineForUserNamed(userNameIdentifiedAs(userId)).map { mapToPublicationDto(it) }

    private fun mapToPublicationDto(publication: Publication) = PublicationDto(
        postId = publicationIdFor(publication),
        userId = userIdFor(publication.publisherRelatedUser()),
        text = publication.message,
        dateTime = formatDateTime(publication.publicationTime),
        likes = system.likesOf(publication)
    )

    fun wallOf(userId: String) = system.wallForUserNamed(userNameIdentifiedAs(userId)).map { mapToPublicationDto(it) }

    fun likePublicationIdentifiedAs(publicationId: String, likerDto: LikerDto) = runCatching {
        val userName = userNameIdentifiedAs(likerDto.userId)
        val publication = idsByPublication.entries
            .firstOrNull { (_, value) -> value == publicationId }?.key ?: throw ModelException(INVALID_PUBLICATION)
        LikesDto(system.likePublication(publication, userName))
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

const val FOLLOWING_CREATED = "Following created."