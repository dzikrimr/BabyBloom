package com.example.bubtrack.data.webrtc

import com.example.bubtrack.utill.MyValueEventListener
import com.example.bubtrack.utill.SharedPrefHelper
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val database: DatabaseReference,
    private val prefHelper: SharedPrefHelper,
    private val gson: Gson
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())



    fun clear() {
        coroutineScope.cancel()
    }

    // create empty room node (parent)
    suspend fun createRoom(roomId: String) {
        database.child("rooms").child(roomId).setValue(mapOf<String, Any>()).await()
    }

    fun generateRoomId(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    // baby posts offer SDP
    suspend fun postOffer(roomId: String, offerSdp: String) {
        database.child("rooms").child(roomId).child("offer").setValue(offerSdp).await()
    }

    // parent posts answer SDP
    suspend fun postAnswer(roomId: String, answerSdp: String) {
        database.child("rooms").child(roomId).child("answer").setValue(answerSdp).await()
    }

    // listen for offer (parent)
    fun observeOffer(roomId: String, onOffer: (String) -> Unit) {
        database.child("rooms").child(roomId).child("offer")
            .addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let { onOffer(it) }
                }
            })
    }

    // listen for answer (baby)
    fun observeAnswer(roomId: String, onAnswer: (String) -> Unit) {
        database.child("rooms").child(roomId).child("answer")
            .addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(String::class.java)?.let { onAnswer(it) }
                }
            })
    }

    // Post ICE candidate under role ("baby" or "parent")
    suspend fun postIceCandidate(roomId: String, role: String, candidateJson: String) {
        database.child("rooms").child(roomId).child("candidates").child(role).push().setValue(candidateJson).await()
    }

    // Observe ICE candidates for a roleToListen (e.g., parent listens baby candidates)
    fun observeIceCandidates(roomId: String, roleToListen: String, onCandidateJson: (String) -> Unit) {
        database.child("rooms").child(roomId).child("candidates").child(roleToListen)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(String::class.java)?.let { onCandidateJson(it) }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // remove room node (cleanup)
    suspend fun removeRoom(roomId: String) {
        database.child("rooms").child(roomId).removeValue().await()
    }

    suspend fun notifyParentLeft(roomId: String) {
        database.child("rooms").child(roomId).child("parentLeft")
            .setValue(true).await()
    }

    /**
     * Dipanggil bayi untuk listen kapan parent left
     */
    fun observeParentLeft(roomId: String, onParentLeft: () -> Unit) {
        database.child("rooms").child(roomId).child("parentLeft")
            .addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val left = snapshot.getValue(Boolean::class.java) ?: false
                    if (left) {
                        onParentLeft()
                    }
                }
            })
    }
}