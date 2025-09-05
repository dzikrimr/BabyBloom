package com.example.bubtrack.utill

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
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