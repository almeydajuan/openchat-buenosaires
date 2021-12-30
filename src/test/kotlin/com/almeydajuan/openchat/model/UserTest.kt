package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.TestFactory.createUserNamed
import com.almeydajuan.openchat.TestUtilities.assertThrowsModelExceptionWithErrorMessage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    fun canNotCreateUserWithBlankName() {
        assertThrowsModelExceptionWithErrorMessage(NAME_CANNOT_BE_BLANK) {
            User.named(" ", "about", "www.10pines.com")
        }
    }

    @Test
    fun canCreateUserWithNoBlankName() {
        val userName = "user"
        val createdUser = createUserNamed(userName)

        assertTrue(createdUser.name.isNotBlank())
        assertTrue(createdUser.isNamed(userName))
    }

    @Test
    fun isNamedReturnsFalseWhenAskedWithOtherName() {
        val userName = "user"
        val createdUser = createUserNamed(userName)

        assertFalse(createdUser.isNamed(userName + "x"))
    }
}