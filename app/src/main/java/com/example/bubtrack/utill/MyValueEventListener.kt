package com.example.bubtrack.utill

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

open class MyValueEventListener : ValueEventListener {

    override fun onDataChange(snapshot: DataSnapshot) {

    }

    override fun onCancelled(error: DatabaseError) {

    }
}