package com.almeydajuan.openchat.model

import java.util.Optional

class OpenChatSystem(private val clock: Clock) {
    val CANNOT_REGISTER_SAME_USER_TWICE = "Username already in use."
    val USER_NOT_REGISTERED = "User not registered"
    val INVALID_PUBLICATION = "Invalid post"

    private val userCards: MutableMap<String, UserCard> = mutableMapOf()
    private val likersByPublication: MutableMap<Publication, MutableSet<Publisher>> = mutableMapOf()

    fun hasUsers() = userCards.isNotEmpty()

    fun register(userName: String, password: String, about: String, homePage: String): User {
        assertIsNotDuplicated(userName)
        val newUser = User(userName, about, homePage)
        userCards[userName] = UserCard(newUser, password, Publisher(newUser))
        return newUser
    }

    //Uso userCardForUserName en vez de hacer userCards.get para que la búsqueda por nombre esté en un solo lugar
    private fun hasUserNamed(potentialUserName: String) =
        userCardForUserName(potentialUserName).isPresent

    fun numberOfUsers() = userCards.size

    fun publishForUserNamed(userName: String, message: String): Publication {
        val newPublication = publisherForUserNamed(userName).publish(message, clock.now())
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
        return if (userCard != null && userCard.isPassword(password)) { userCard.user } else { null }
    }

    private fun userCardForUserName(userName: String) = Optional.ofNullable(userCards[userName])

    private fun publisherForUserNamed(userName: String) =
        userCardForUserName(userName).map { it.publisher }.orElseThrow { ModelException(USER_NOT_REGISTERED) }

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