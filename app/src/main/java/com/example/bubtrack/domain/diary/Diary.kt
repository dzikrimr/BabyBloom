package com.example.bubtrack.domain.diary

data class Diary(
    val id: String,
    val title: String,
    val desc: String,
    val date: Long,
    val imgUrl : String?
)
