package com.almeydajuan.openchat.model

data class User(
    val name: String,
    val about: String,
    val homePage: String
) {
    init {
        assertNameIsNotBlank(name)
    }

    fun isNamed(potentialName: String) = name == potentialName

    fun toUserDto(registeredUserId: String) = UserDto(
        userId = registeredUserId,
        username = name,
        about = about,
        homePage = homePage
    )

    companion object {
        fun named(name: String, about: String, homePage: String): User {
            assertNameIsNotBlank(name)
            return User(name, about, homePage)
        }

        private fun assertNameIsNotBlank(name: String) {
            if (name.isBlank()) throw ModelException(NAME_CANNOT_BE_BLANK)
        }
    }
}

const val NAME_CANNOT_BE_BLANK = "Name can not be blank"