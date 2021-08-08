package com.almeydajuan.openchat.model

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