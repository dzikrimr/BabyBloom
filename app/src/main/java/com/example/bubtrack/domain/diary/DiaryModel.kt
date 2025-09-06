package com.example.bubtrack.domain.diary

data class DiaryModel(
    val id: String,
    val title: String,
    val desc: String,
    val date: Long,
    val imgUrl : String?
)
