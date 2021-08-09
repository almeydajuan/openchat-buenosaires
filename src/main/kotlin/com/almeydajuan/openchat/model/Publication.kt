package com.almeydajuan.openchat.model

import java.time.LocalDateTime
import java.util.Collections

class Publication(
    val publisher: Publisher,
    val message: String,
    val publicationTime: LocalDateTime
) {

    init {
        assertIsAppropriate(message)
    }

    private fun assertIsAppropriate(message: String) {
        if (isInappropriate(message)) throw ModelException(INAPPROPRIATE_WORD)
    }

    private fun isInappropriate(message: String) = inappropriateWords.any { message.lowercase().contains(it) }

    fun publisherRelatedUser() = publisher.relatedUser()

}

const val INAPPROPRIATE_WORD = "Post contains inappropriate language."
private val inappropriateWords = Collections.unmodifiableList(listOf("elephant", "ice cream", "orange"))