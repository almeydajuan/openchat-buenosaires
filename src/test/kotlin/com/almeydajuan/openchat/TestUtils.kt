package com.almeydajuan.openchat

import com.almeydajuan.openchat.model.ModelException
import com.almeydajuan.openchat.model.RegistrationDto
import com.almeydajuan.openchat.model.User
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import java.time.LocalDateTime

object TestUtilities {
    var now: LocalDateTime = LocalDateTime.now()

    fun delayOneSecond() {
        now = now.plusSeconds(1)
    }

    fun assertThrowsModelExceptionWithErrorMessage(errorMessage: String, closureToFail: Executable) {
        val error = assertThrows<ModelException> { closureToFail.execute() }

        Assertions.assertThat(errorMessage).isEqualTo(error.message)
    }
}

object TestFactory {
    const val USER_NAME = "username"
    const val ANOTHER_USER_NAME = "anotherUser"
    const val USER_PASSWORD = "userPassword"
    const val USER_ABOUT = "userAbout"
    const val USER_HOME_PAGE = "www.twitter.com/user"

    fun createUserNamed(name: String) = User.named(name, USER_ABOUT, USER_HOME_PAGE)

    fun createRegistrationDto(name: String) = RegistrationDto(name, USER_PASSWORD, USER_ABOUT, USER_HOME_PAGE)
}