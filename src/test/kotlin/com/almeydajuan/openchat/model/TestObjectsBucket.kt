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
    private const val JUAN_PEREZ_ABOUT = "about"
    private const val JUAN_PEREZ_HOME_PAGE = "www.twitter.com/juanPerez"

    var now: LocalDateTime = LocalDateTime.now()

    fun changeNowTo(newNow: LocalDateTime) {
        now = newNow
    }

    fun createUserJuanPerez() = User.named(JUAN_PEREZ_NAME, JUAN_PEREZ_ABOUT, JUAN_PEREZ_HOME_PAGE)

    fun createPepeSanchez() = User.named(PEPE_SANCHEZ_NAME, PEPE_SANCHEZ_ABOUT, PEPE_SANCHEZ_HOME_PAGE)

    fun assertThrowsModelExceptionWithErrorMessage(errorMessage: String, closureToFail: Executable) {
        val error = assertThrows<ModelException> { closureToFail.execute() }

        assertThat(errorMessage).isEqualTo(error.message)
    }
}
