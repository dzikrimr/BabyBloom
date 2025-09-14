package com.example.bubtrack.utill

sealed class MatchState {
    data object NewState : MatchState()
    data object Idle : MatchState()
    data object LookingForMatchState : MatchState()
    data class OfferedMatchState(
        val participant : String
    ) : MatchState()
    data class ReceivedMatchState(
        val participant: String
    ) : MatchState()
    data object Connected : MatchState()
}