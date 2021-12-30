package com.almeydajuan.openchat.model

import java.time.LocalDateTime

class OpenChatSystem {
    private val userCards: MutableMap<String, UserCard> = mutableMapOf()
    private val likersByPublication: MutableMap<Publication, MutableSet<Publisher>> = mutableMapOf()

    fun register(userName: String, password: String, about: String, homePage: String): User {
        assertIsNotDuplicated(userName)
        val newUser = User(userName, about, homePage)
        userCards[userName] = UserCard(newUser, password, Publisher(newUser))
        return newUser
    }

    fun hasUsers() = userCards.isNotEmpty()

    fun numberOfUsers() = userCards.size

    //Uso userCardForUserName en vez de hacer userCards.get para que la búsqueda por nombre esté en un solo lugar
    fun hasUserNamed(potentialUserName: String) = userCardForUserName(potentialUserName) != null

    fun publishForUserNamed(userName: String, message: String): Publication {
        val newPublication = publisherForUserNamed(userName).publish(message, LocalDateTime.now())
        likersByPublication[newPublication] = HashSet()
        return newPublication
    }

    fun timeLineForUserNamed(userName: String) = publisherForUserNamed(userName).timeLine()

    fun followForUserNamed(followedUserName: String, followerUserName: String) {
        val followed = publisherForUserNamed(followedUserName)
        val follower = publisherForUserNamed(followerUserName)
        followed.followedBy(follower)
    }

    fun followersOfUserNamed(userName: String) = publisherForUserNamed(userName).followers().map { it.relatedUser() }

    fun wallForUserNamed(userName: String) = publisherForUserNamed(userName).wall()

    fun users() = userCards.values.map { it.user }

    private fun assertIsNotDuplicated(userName: String) {
        if (hasUserNamed(userName)) throw ModelException(CANNOT_REGISTER_SAME_USER_TWICE)
    }

    fun authenticateUser(userName: String, password: String): User? {
        val userCard = userCards[userName]
        return if (userCard != null && userCard.isPassword(password)) {
            userCard.user
        } else {
            null
        }
    }

    fun <T> withAuthenticatedUserDo(
            userName: String,
            password: String,
            authenticatedClosure: (User) -> T,
            failedClosure: () -> T
    ): T = authenticateUser(userName, password)
                ?.let(authenticatedClosure)
                ?: failedClosure.invoke()

    private fun userCardForUserName(userName: String): UserCard? = userCards[userName]

    private fun publisherForUserNamed(userName: String) =
            userCardForUserName(userName)?.publisher ?: throw ModelException(USER_NOT_REGISTERED)

    fun likesOf(publication: Publication) = likersOf(publication).size

    fun likePublication(publication: Publication, userName: String): Int {
        val likers = likersOf(publication)
        likers.add(publisherForUserNamed(userName))
        return likers.size
    }

    private fun likersOf(publication: Publication) =
            likersByPublication[publication] ?: throw ModelException(INVALID_PUBLICATION)

    private data class UserCard(val user: User, private val password: String, val publisher: Publisher) {
        fun isPassword(potentialPassword: String): Boolean {
            return password == potentialPassword
        }
    }
}

const val CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use."
const val USER_NOT_REGISTERED = "User not registered"