package com.almeydajuan.openchat.model

import com.almeydajuan.openchat.TestObjectsBucket.PEPE_NAME
import com.almeydajuan.openchat.TestObjectsBucket.assertThrowsModelExceptionWithErrorMessage
import com.almeydajuan.openchat.TestObjectsBucket.createPepeSanchez
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    fun canNotCreateUserWithBlankName() {
        assertThrowsModelExceptionWithErrorMessage(NAME_CANNOT_BE_BLANK) { User.named(" ", "about", "www.10pines.com") }
    }

    @Test
    fun canCreateUserWithNoBlankName() {
        val createdUser: User = createPepeSanchez()
        assertTrue(createdUser.isNamed(PEPE_NAME))
    }

    @Test
    fun isNamedReturnsFalseWhenAskedWithOtherName() {
        val createdUser: User = createPepeSanchez()
        assertFalse(createdUser.isNamed(PEPE_NAME + "x"))
    }
}