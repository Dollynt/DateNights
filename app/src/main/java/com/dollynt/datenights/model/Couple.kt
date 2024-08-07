package com.dollynt.datenights.model

data class Couple(
    val users: List<String> = emptyList(),
    val inviteCode: String = "",
    val inviteLink: String = ""
)
