package com.example.bubtrack.utill

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

object Utility {

    fun formatDate(millis: Long) : String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
    }

    fun getCurrentDate() : String{
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return currentDate
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun toEpochMilli(date: LocalDate, zoneId: ZoneId = ZoneOffset.UTC): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
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

    fun createNonce() : String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold(""){str,it ->
            str + "%02x".format(it)
        }
    }
}