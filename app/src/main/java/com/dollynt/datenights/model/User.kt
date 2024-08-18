package com.dollynt.datenights.model

data class User(
    val uid: String,
    val email: String?,
    val name: String?,
    val birthdate: String?,
    val gender: String?,
    val profilePictureUrl: String?
) {
    constructor() : this("", null, null, null, null, null)
}
