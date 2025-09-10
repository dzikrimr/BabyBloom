package com.example.bubtrack.domain.activities

data class Activity(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val date: Long,
    val hour: Int,
    val minute : Int,
    val type: String
)

val dummyActivities = listOf(
    Activity(
        1, "user1", "Morning Feeding",
        "Feeding", System.currentTimeMillis(), hour = 12,
        minute = 30,
        ActivityType.CHECKUP.value,
    ),
    Activity(
        2,
        "user1",
        "Check-up",
        "Pediatric visit",
        System.currentTimeMillis() + 86400000,
        hour = 12,
        minute = 30,
        ActivityType.CHECKUP.value,
        )
)

enum class ActivityType(val value: String){
    FEEDING("Feeding"),
    CHECKUP("Check-up"),
    VACCINE("Vaccine"),
    PLAYTIME("Playtime")
}
