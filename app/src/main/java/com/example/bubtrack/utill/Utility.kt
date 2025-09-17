package com.example.bubtrack.utill


import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utility {

    fun formatDate(millis: Long) : String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
    }


    fun formatToDateMonth(millis: Long) : String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }


    fun formatPrettyDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale("id", "ID"))
        return formatter.format(Date(millis))
    }

    fun formatNotificationTimestamp(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("d MMMM • h:mm a", Locale("id", "ID"))
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

}