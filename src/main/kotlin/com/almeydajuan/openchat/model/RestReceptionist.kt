package com.almeydajuan.openchat.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class RestReceptionist(private val system: OpenChatSystem) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private val idsByUser: MutableMap<User, String> = mutableMapOf()
    private val idsByPublication: MutableMap<Publication, String> = mutableMapOf()

    fun registerUser(registrationDto: RegistrationDto): UserDto {
        val registeredUser: User = system.register(
                user = User(registrationDto.username, registrationDto.about, registrationDto.homePage),
                password = registrationDto.password
        )
        val registeredUserId = UUID.randomUUID().toString()
        idsByUser[registeredUser] = registeredUserId
        return registeredUser.toUserDto(registeredUserId)
    }

    fun login(loginDto: LoginDto): UserDto =
        system.authenticateUser(loginDto.username, loginDto.password)?.let { it.toUserDto(userIdFor(it)) }
            ?: throw RuntimeException(INVALID_CREDENTIALS)

    fun users(): List<UserDto> = system.users().map { it.toUserDto(userIdFor(it)) }

    fun followings(followingDto: FollowingDto) = system.followForUserNamed(
        userNameIdentifiedAs(followingDto.followeeId),
        userNameIdentifiedAs(followingDto.followerId)
    )

    fun followersOf(userId: String) =
        system.followersOfUserNamed(userNameIdentifiedAs(userId)).map { it.toUserDto(userIdFor(it)) }

    fun addPublication(userId: String, publicationTextDto: PublicationTextDto): PublicationDto {
        val publication: Publication = system.publishForUserNamed(userNameIdentifiedAs(userId), publicationTextDto.text)
        val publicationId = UUID.randomUUID().toString()
        idsByPublication[publication] = publicationId
        return mapToPublicationDto(publication)
    }

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

    fun likePublicationIdentifiedAs(publicationId: String, likerDto: LikerDto): LikesDto {
        val userName = userNameIdentifiedAs(likerDto.userId)
        val publication = idsByPublication.entries
            .firstOrNull { (_, value) -> value == publicationId }?.key ?: throw ModelException(INVALID_PUBLICATION)
        return LikesDto(system.likePublication(publication, userName))
    }

    private fun userIdentifiedAs(userId: String): User =
        idsByUser.entries.firstOrNull { (_, value) -> value == userId }?.key
            ?: throw ModelException(INVALID_CREDENTIALS)

    private fun userNameIdentifiedAs(userId: String) = userIdentifiedAs(userId).name

    private fun formatDateTime(dateTimeToFormat: LocalDateTime) = dateTimeFormatter.format(dateTimeToFormat)

    private fun publicationIdFor(publication: Publication) = idsByPublication.getValue(publication)

    private fun userIdFor(user: User) = idsByUser.getValue(user)
}

const val FOLLOWING_CREATED = "Following created."
const val INVALID_CREDENTIALS = "Invalid credentials."
const val INVALID_PUBLICATION = "Invalid post"