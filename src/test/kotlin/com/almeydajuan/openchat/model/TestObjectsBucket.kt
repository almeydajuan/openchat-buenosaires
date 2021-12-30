package com.almeydajuan.openchat.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import java.time.LocalDateTime

object TestObjectsBucket {
    const val PEPE_SANCHEZ_NAME = "Pepe Sanchez"
    const val PEPE_SANCHEZ_PASSWORD = "password"
    const val PEPE_SANCHEZ_ABOUT = "anotherAbout"
    const val PEPE_SANCHEZ_HOME_PAGE = "www.twitter.com/pepeSanchez"

    const val JUAN_PEREZ_NAME = "Juan Perez"
    const val JUAN_PEREZ_PASSWORD = "otherPassword"
    const val JUAN_PEREZ_ABOUT = "about"
    const val JUAN_PEREZ_HOME_PAGE = "www.twitter.com/juanPerez"

    var now: LocalDateTime = LocalDateTime.now()

    fun changeNowTo(newNow: LocalDateTime) {
        now = newNow
    }

    fun assertThrowsModelExceptionWithErrorMessage(errorMessage: String, closureToFail: Executable) {
        val error = assertThrows<ModelException> { closureToFail.execute() }

        assertThat(errorMessage).isEqualTo(error.message)
    }
}
