package com.almeydajuan.openchat.model

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonValue

data class ReceptionistResponse(
    val status: Int = 0,
    val responseBody: String = ""
) {

    /* Lamentablemente no se puede usar solo un Json como responseBody
     * porque hay API que devuelven un String que no es un Json string
     * como cuando se produce un error o followings, etc - Hernan
     */
    constructor(status: Int, bodyAsJson: JsonValue): this(status, bodyAsJson.toString())

    fun isStatus(potentialStatus: Int) = status == potentialStatus

    fun responseBody() = responseBody

    fun responseBodyAsJson() = Json.parse(responseBody).asObject()

    fun responseBodyAsJsonArray(): JsonArray = Json.parse(responseBody).asArray()
}