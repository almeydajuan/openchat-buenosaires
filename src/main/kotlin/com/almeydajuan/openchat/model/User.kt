package com.almeydajuan.openchat.model

data class User(
    val name: String,
    val about: String,
    val homePage: String
) {
    init {
        assertNameIsNotBlank(name)
    }

    private fun assertNameIsNotBlank(name: String) {
        if (name.isBlank()) throw ModelException(NAME_CANNOT_BE_BLANK);
    }

    fun isNamed(potentialName: String): Boolean {
        return name == potentialName;
    }
}

const val NAME_CANNOT_BE_BLANK = "Name can not be blank";