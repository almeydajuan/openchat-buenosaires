package com.almeydajuan.openchat.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

class Publication(
    val publisher: Publisher,
    val message: String,
    val publicationTime: LocalDateTime
) {

    init {
        assertIsAppropriate(message)
    }

    private val inappropriateWords = Collections.unmodifiableList(listOf("elephant", "ice cream", "orange"))

    private fun assertIsAppropriate(message: String) {
        if (isInappropriate(message)) throw ModelException(INAPPROPRIATE_WORD)
    }

    private fun isInappropriate(message: String) = inappropriateWords.any { message.lowercase().contains(it) }

    fun hasMessage(potentialMessage: String) = message == potentialMessage

    fun wasPublishedAt(potentialTime: LocalDateTime) = publicationTime == potentialTime

    fun comparePublicationTimeWith(publicationToCompare: Publication) =
        publicationTime.compareTo(publicationToCompare.publicationTime)

    fun publisherRelatedUser() = publisher.relatedUser()

    fun toPublicationDto(publicationId: String, userId: String, likes: Int) = PublicationDto(
        postId = publicationId,
        userId = userId,
        text = message,
        dateTime = formatDateTime(publicationTime),
        likes = likes
        )

    private fun formatDateTime(dateTimeToFormat: LocalDateTime) = DATE_TIME_FORMATTER.format(dateTimeToFormat)

}

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
const val INAPPROPRIATE_WORD = "Post contains inappropriate language."