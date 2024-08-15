package com.dollynt.datenights.model

data class Option(
    val id: String,
    val name: String,
    val subOptions: List<String>,
    val ownerCouple: String
){
    constructor() : this("", "", emptyList(), "")
}
