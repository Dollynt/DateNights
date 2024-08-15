package com.dollynt.datenights.model

data class Couple(
    val id: String,
    val users: List<String>,
    val inviteCode: String,
    val inviteLink: String
){
    constructor() : this("", emptyList(), "", "")
}
