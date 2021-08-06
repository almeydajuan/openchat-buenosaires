package com.almeydajuan.openchat.model

import java.time.LocalDateTime
import java.util.Collections

class Publisher(private val user: User) {

    private val followers: MutableList<Publisher> = mutableListOf()
    private val publications: MutableList<Publication> = mutableListOf()

    fun hasFollowers() = followers.isNotEmpty()

    fun followedBy(potentialFollower: Publisher) {
        assertCanNotFollowSelf(potentialFollower)
        assertCanNotFollowTwice(potentialFollower)
        followers.add(potentialFollower)
    }

    private fun isFollowedBy(potentialFollower: Publisher) = followers.contains(potentialFollower)

    fun numberOfFollowers() = followers.size

    fun hasPublications() = publications.isNotEmpty()

    fun publish(message: String, publicationTime: LocalDateTime): Publication {
        val newPublication = Publication(this, message, publicationTime)
        publications.add(newPublication)
        return newPublication
    }

    fun timeLine(): List<Publication> = sortedPublications(publications)

    fun wall(): List<Publication> {
        val wall = publications.toMutableList()
        followers.forEach { it.addPublicationTo(wall) }
        return sortedPublications(wall)
    }

    fun followers(): List<Publisher> = Collections.unmodifiableList(followers)

    fun relatedUser(): User {
        return user
    }

    private fun assertCanNotFollowTwice(potentialFollower: Publisher) {
        if (isFollowedBy(potentialFollower)) throw ModelException(CANNOT_FOLLOW_TWICE)
    }

    private fun assertCanNotFollowSelf(potentialFollower: Publisher) {
        if (this == potentialFollower) throw ModelException(CANNOT_FOLLOW_SELF)
    }

    private fun sortedPublications(publications: List<Publication>) = publications.sortedBy { it.publicationTime }

    private fun addPublicationTo(publicationCollector: MutableList<Publication>) {
        publicationCollector.addAll(publications)
    }
}

const val CANNOT_FOLLOW_SELF = "Can not follow self"
const val CANNOT_FOLLOW_TWICE = "Can not follow publisher twice"