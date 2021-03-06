package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.TestFactory
import com.almeydajuan.openchat.TestUtilities.assertThrowsModelExceptionWithErrorMessage
import com.almeydajuan.openchat.TestUtilities.randomString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PublisherTest {

    @Test
    fun createdPublisherHasNoFollowers() {
        val createdPublisher = createSomeUser()
        assertFalse(createdPublisher.hasFollowers())
    }

    @Test
    fun publisherCanFollowOtherPublisher() {
        val followed = createSomeUser()
        val follower = createSomeUser(randomString())

        followed.followedBy(follower)

        assertThat(followed.hasFollowers()).isTrue
        assertThat(followed.isFollowedBy(follower)).isTrue
        assertEquals(1, followed.numberOfFollowers())
    }

    @Test
    fun publisherCanNotFollowSelf() {
        val follower = createSomeUser()

        assertThrowsModelExceptionWithErrorMessage(CANNOT_FOLLOW_SELF) { follower.followedBy(follower) }
        assertThat(follower.hasFollowers()).isFalse
    }

    @Test
    fun publisherCanNotFollowSamePublisherTwice() {
        val followed = createSomeUser()
        val follower = createSomeUser(randomString())

        followed.followedBy(follower)

        assertThrowsModelExceptionWithErrorMessage(CANNOT_FOLLOW_TWICE) { followed.followedBy(follower) }

        assertTrue(followed.hasFollowers())
        assertTrue(followed.isFollowedBy(follower))
        assertEquals(1, followed.numberOfFollowers())
    }

    @Test
    fun createdPublisherHasNoPublications() {
        val createdPublisher = createSomeUser()
        assertFalse(createdPublisher.hasPublications())
    }

    @Test
    fun publisherCanPublishMessages() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = "a message"

        val publication = createdPublisher.publish(message, publicationTime)

        assertTrue(createdPublisher.hasPublications())
        assertTrue(publication.hasMessage(message))
        assertTrue(publication.wasPublishedAt(publicationTime))
        assertFalse(publication.hasMessage(message + randomString()))
        assertFalse(publication.wasPublishedAt(publicationTime.plusSeconds(1)))
    }

    @Test
    fun timelineHasPublisherPublicationsSortedWithLatestPublicationsFirst() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = randomString()

        val secondPublication = createdPublisher.publish(message, publicationTime.plusSeconds(1))
        val firstPublication = createdPublisher.publish(message, publicationTime)

        val timeLine = createdPublisher.timeLine()
        assertThat(timeLine).isEqualTo(listOf(secondPublication, firstPublication))
    }

    @Test
    fun wallContainsPublisherPublications() {
        val follower = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = randomString()

        val firstPublication = follower.publish(message, publicationTime)
        val wall = follower.wall()

        assertThat(wall).isEqualTo(listOf(firstPublication))
    }

    @Test
    fun wallContainsFollowersPublications() {
        val followed = createSomeUser()
        val follower = createSomeUser(randomString())
        followed.followedBy(follower)

        val publicationTime = LocalDateTime.now()
        val message = randomString()
        val firstPublication = follower.publish(message, publicationTime.plusSeconds(1))

        val wall = followed.wall()
        assertThat(wall).isEqualTo(listOf(firstPublication))
    }

    @Test
    fun wallContainsFollowersPublicationsWithLatestPublicationsFirst() {
        val followed = createSomeUser()
        val follower = createSomeUser(randomString())
        followed.followedBy(follower)

        val publicationTime = LocalDateTime.now()
        val message = randomString()
        val firstPublication = followed.publish(message, publicationTime)
        val secondPublication = follower.publish(message, publicationTime.plusSeconds(1))
        val thirdPublication = followed.publish(message, publicationTime.plusSeconds(2))

        val wall = followed.wall()

        assertThat(wall).isEqualTo(listOf(thirdPublication, secondPublication, firstPublication))
    }

    @Test
    fun canNotPublishWithInappropriateWord() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = "elephant"

        assertThrowsModelExceptionWithErrorMessage(INAPPROPRIATE_WORD) { createdPublisher.publish(message, publicationTime) }
        assertFalse(createdPublisher.hasPublications())
    }

    @Test
    fun canNotPublishWithInappropriateWordInUpperCase() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = "ELEPHANT"

        assertThrowsModelExceptionWithErrorMessage(INAPPROPRIATE_WORD) { createdPublisher.publish(message, publicationTime) }
        assertFalse(createdPublisher.hasPublications())
    }

    @Test
    fun canNotPublishAMessageContainingInappropriateWord() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()
        val message = "abc ELEPHANT xx"

        assertThrowsModelExceptionWithErrorMessage(INAPPROPRIATE_WORD) { createdPublisher.publish(message, publicationTime) }
        assertFalse(createdPublisher.hasPublications())
    }

    @Test
    fun canNotPublishAnyInappropriateWord() {
        val createdPublisher = createSomeUser()
        val publicationTime = LocalDateTime.now()

        listOf("elephant", "ice cream", "orange").forEach {
            assertThrowsModelExceptionWithErrorMessage(INAPPROPRIATE_WORD) {
                createdPublisher.publish(it, publicationTime)
            }
        }
    }

    private fun createSomeUser(user: String = randomString()): Publisher {
        return Publisher.relatedTo(TestFactory.createUserNamed(user))
    }
}