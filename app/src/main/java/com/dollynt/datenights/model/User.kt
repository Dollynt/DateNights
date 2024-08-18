package com.dollynt.datenights.model

data class User(
    var uid: String,
    var email: String?,
    var name: String?,
    var birthdate: String?,
    var gender: String?,
    var profilePictureUrl: String?
) {
    constructor() : this("", null, null, null, null, null)
}
