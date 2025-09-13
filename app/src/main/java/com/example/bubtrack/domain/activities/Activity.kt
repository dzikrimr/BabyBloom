package com.example.bubtrack.domain.activities

import kotlinx.serialization.Serializable

@Serializable
    data class Activity(
        val id: Int = 0,
        val userId: String = "",
        val title: String = "",
        val description: String = "",
        val date: Long = 0L,
        val hour: Int = 0,
        val minute: Int = 0,
        val type: String = ""
) {
    constructor() : this(0, "", "", "", 0L, 0, 0, "")
}