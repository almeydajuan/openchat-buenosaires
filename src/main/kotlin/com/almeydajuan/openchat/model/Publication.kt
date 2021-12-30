package com.almeydajuan.openchat.model

import java.time.LocalDateTime
import java.util.Collections

class Publication(
        private val publisher: Publisher,
        val message: String,
        val publicationTime: LocalDateTime
) {

    init {
        assertIsAppropriate(message)
    }

    fun publisherRelatedUser() = publisher.relatedUser()

    fun hasMessage(message: String) = this.message == message

    fun wasPublishedAt(publicationTime: LocalDateTime) = this.publicationTime == publicationTime

    companion object {
        fun madeBy(publisher: Publisher, message: String, publicationTime: LocalDateTime): Publication {
            assertIsAppropriate(message)
            return Publication(publisher, message, publicationTime)
        }

        private fun assertIsAppropriate(message: String) {
            if (isInappropriate(message)) throw ModelException(INAPPROPRIATE_WORD)
        }

        private fun isInappropriate(message: String) = inappropriateWords.any { message.lowercase().contains(it) }
    }
}

const val INAPPROPRIATE_WORD = "Post contains inappropriate language."
private val inappropriateWords = Collections.unmodifiableList(listOf("elephant", "ice cream", "orange"))