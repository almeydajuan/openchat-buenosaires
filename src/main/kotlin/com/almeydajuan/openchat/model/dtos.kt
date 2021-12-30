package com.almeydajuan.openchat.model

import java.util.UUID

data class RegistrationDto(
    val username: String,
    val password: String,
    val about: String,
    val homePage: String
)

data class LoginDto(
    val username: String,
    val password: String
)

data class UserDto(
    val userId: String,
    val username: String,
    val about: String,
    val homePage: String
)

data class PublicationTextDto(
    val text: String
)

data class PublicationDto(
    val postId: String = UUID.randomUUID().toString(),
    val userId: String,
    val text: String,
    val dateTime: String,
    val likes: Int
)

data class FollowingDto(
    val followerId: String,
    val followeeId: String
)

data class LikerDto(
    val userId: String
)

data class LikesDto(
    val likes: Int
)