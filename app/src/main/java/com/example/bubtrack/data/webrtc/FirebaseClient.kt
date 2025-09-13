package com.example.bubtrack.data.webrtc

import com.example.bubtrack.utill.MatchState
import com.example.bubtrack.utill.MyValueEventListener
import com.example.bubtrack.utill.SharedPrefHelper
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val database: DatabaseReference,
    private val prefHelper: SharedPrefHelper,
    private val gson: Gson
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeUserStatus(callback: (MatchState) -> Unit) {
        coroutineScope.launch {
            removeSelfData()
            updateSelfStatus(StatusDataModel(type = StatusDataModelType.LookingForMatch))

            val userId = prefHelper.getUserId()
            val statusRef = database.child("users").child(userId)
                .child("status")

            statusRef.addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(StatusDataModel::class.java)?.let { status ->
                        val newState = when (status.type) {
                            StatusDataModelType.LookingForMatch -> MatchState.LookingForMatchState
                            StatusDataModelType.OfferedMatch -> MatchState.OfferedMatchState(status.participant!!)
                            StatusDataModelType.ReceivedMatch -> MatchState.ReceivedMatchState(
                                status.participant!!
                            )

                            StatusDataModelType.IDLE -> MatchState.Idle
                            StatusDataModelType.Connected -> MatchState.Connected
                            else -> null
                        }

                        newState?.let { callback(it) } ?: coroutineScope.launch {
                            updateSelfStatus(StatusDataModel(type = StatusDataModelType.LookingForMatch))
                            callback(MatchState.LookingForMatchState)
                        }
                    } ?: coroutineScope.launch {
                        updateSelfStatus(StatusDataModel(type = StatusDataModelType.LookingForMatch))
                        callback(MatchState.LookingForMatchState)
                    }
                }
            })
        }
    }

    suspend fun findNextMatch() {
        removeSelfData()
        findAvailableParticipant { foundTarget ->
            foundTarget?.let { target ->
                database.child("users").child(target)
                    .child("status").setValue(
                        StatusDataModel(
                            participant = prefHelper.getUserId(),
                            type = StatusDataModelType.ReceivedMatch
                        )
                    )

                coroutineScope.launch {
                    updateSelfStatus(
                        StatusDataModel(
                            type = StatusDataModelType.OfferedMatch,
                            participant = target
                        )
                    )
                }
            }
        }
    }

    private fun findAvailableParticipant(callback: (String?) -> Unit) {
        database.child("users").orderByChild("status/type")
            .equalTo(StatusDataModelType.LookingForMatch.name)
            .addListenerForSingleValueEvent(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundTarget: String? = null
                    snapshot.children.forEach { childSnapshot ->
                        if (childSnapshot.key != prefHelper.getUserId()) {
                            foundTarget = childSnapshot.key
                            return@forEach
                        }
                    }
                    callback(foundTarget)
                }

                override fun onCancelled(error: DatabaseError) {
                    super.onCancelled(error)
                    callback(null)
                }
            })
    }

    suspend fun updateParticipantDataModel(
        participant: String,
        data: SignalDataModel
    ) {
        database.child("users").child(participant)
            .child("data").setValue(gson.toJson(data)).await()
    }

    fun observeIncomingSignals(callback: (SignalDataModel) -> Unit) {
        database.child("users").child(prefHelper.getUserId())
            .child("data").addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    super.onDataChange(snapshot)
                    runCatching {
                        gson.fromJson(snapshot.value.toString(), SignalDataModel::class.java)
                    }.onSuccess {
                        if (it != null) callback(it)
                    }.onFailure {

                    }
                }
            })
    }

    suspend fun updateSelfStatus(status: StatusDataModel) {
        database.child("users").child(prefHelper.getUserId())
            .child("status")
            .setValue(status)
            .await()
    }

    suspend fun updateParticipantStatus(participantId: String, status: StatusDataModel) {
        database.child("users").child(participantId)
            .child("status").setValue(status).await()
    }


    suspend fun removeSelfData() {
        database.child("users").child(prefHelper.getUserId())
            .child("data").removeValue().await()
    }

    fun clear() {
        coroutineScope.cancel()
    }
}