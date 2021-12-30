package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.TestFactory.createUserNamed
import com.almeydajuan.openchat.TestUtilities.assertThrowsModelExceptionWithErrorMessage
import com.almeydajuan.openchat.TestUtilities.randomString
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    fun canNotCreateUserWithBlankName() {
        assertThrowsModelExceptionWithErrorMessage(NAME_CANNOT_BE_BLANK) {
            User.named(" ", randomString(), randomString())
        }
    }

    @Test
    fun canCreateUserWithNoBlankName() {
        val userName = randomString()
        val createdUser = createUserNamed(userName)

        assertTrue(createdUser.name.isNotBlank())
        assertTrue(createdUser.isNamed(userName))
    }

    @Test
    fun isNamedReturnsFalseWhenAskedWithOtherName() {
        val userName = randomString()
        val createdUser = createUserNamed(userName)

        assertFalse(createdUser.isNamed(userName + randomString()))
    }
}