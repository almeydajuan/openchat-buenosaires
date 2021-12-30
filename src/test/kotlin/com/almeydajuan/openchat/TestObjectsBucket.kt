package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.ModelException
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import java.time.LocalDateTime

object TestObjectsBucket {
    const val PEPE_NAME = "Pepe Sanchez"
    const val PEPE_PASSWORD = "password"
    const val PEPE_ABOUT = "anotherAbout"
    const val PEPE_HOME_PAGE = "www.twitter.com/pepeSanchez"

    const val JUAN_NAME = "Juan Perez"
    const val JUAN_PASSWORD = "otherPassword"
    private const val JUAN_ABOUT = "about"
    private const val JUAN_HOME_PAGE = "www.twitter.com/juanPerez"

    var now: LocalDateTime = LocalDateTime.now()

    fun changeNowTo(newNow: LocalDateTime) {
        now = newNow
    }

    fun createUserJuanPerez() = User.named(JUAN_NAME, JUAN_ABOUT, JUAN_HOME_PAGE)

    fun createPepeSanchez() = User.named(PEPE_NAME, PEPE_ABOUT, PEPE_HOME_PAGE)

    fun createJuanPerezRegistrationDto() = RegistrationDto(JUAN_NAME, JUAN_PASSWORD, JUAN_ABOUT, JUAN_HOME_PAGE)

    fun createPepeSanchezRegistrationDto() = RegistrationDto(PEPE_NAME, PEPE_PASSWORD, PEPE_ABOUT, PEPE_HOME_PAGE)

    fun assertThrowsModelExceptionWithErrorMessage(errorMessage: String, closureToFail: Executable) {
        val error = assertThrows<ModelException> { closureToFail.execute() }

        assertThat(errorMessage).isEqualTo(error.message)
    }
}
