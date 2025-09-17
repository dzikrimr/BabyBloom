package com.example.bubtrack.presentation.diary

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.cloudinary.CloudinaryManager
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import android.widget.Toast
import com.example.bubtrack.presentation.profile.UserProfile
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val growthRepo: BabyGrowthRepo,
    private val cloudinaryManager: CloudinaryManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(GrowthUiState())
    val uiState = _uiState.asStateFlow()

    private val _diaryState = MutableStateFlow<List<Diary>>(emptyList())
    val diaryState = _diaryState.asStateFlow()

    private val _babyStats = MutableStateFlow<GrowthStats?>(null)
    val babyStats = _babyStats.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        getGrowth()
        getDiaryEntries()
        getBabyStats()
        getUserProfile()
    }

    fun getBabyStats() {
        viewModelScope.launch {
            growthRepo.getStats().collect { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Error<*> -> Unit
                    is Resource.Idle -> Unit
                    is Resource.Success<*> -> {
                        _babyStats.value = result.data
                    }
                }
            }
        }
    }

    fun setSelectedDate(date: LocalDate?) {
        _selectedDate.value = date
        getGrowth()
        getDiaryEntries()
    }

    private fun getGrowth() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                _uiState.value = GrowthUiState(isEmpty = true)
                return@launch
            }
            firestore.collection("users").document(userId)
                .collection("growthRecords")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = GrowthUiState(isError = error.message)
                        return@addSnapshotListener
                    }
                    val growthList = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            BabyGrowth(
                                id = doc.id,
                                date = doc.getLong("date") ?: 0L,
                                weight = doc.getDouble("weight"),
                                height = doc.getDouble("height"),
                                headCircumference = doc.getDouble("headCircumference"),
                                armLength = doc.getDouble("armLength"),
                                ageInMonths = doc.getLong("ageInMonths")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.filter { growth ->
                        _selectedDate.value?.let { selected ->
                            val growthDate = Instant.ofEpochMilli(growth.date)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            growthDate == selected
                        } ?: true
                    } ?: emptyList()
                    _uiState.value = GrowthUiState(
                        isSuccess = growthList,
                        isEmpty = growthList.isEmpty(),
                        isLoading = false
                    )
                }
        }
    }

    fun addGrowthRecord(
        date: Long,
        weight: Double,
        height: Double,
        headCircumference: Double,
        armLength: Double,
        ageInMonths: Int
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val growth = hashMapOf(
                "date" to date,
                "weight" to weight,
                "height" to height,
                "headCircumference" to headCircumference,
                "armLength" to armLength,
                "ageInMonths" to ageInMonths
            )
            firestore.collection("users").document(userId)
                .collection("growthRecords")
                .add(growth)
                .addOnSuccessListener {
                    // Success handled in UI with Toast
                }
                .addOnFailureListener { e ->
                    // Failure handled in UI with Toast
                }

            val update = hashMapOf(
                "date" to date,
                "weight" to weight,
                "height" to height,
                "headCircumference" to headCircumference,
                "armCircumference" to armLength,
                "ageInMonths" to ageInMonths
            )
            firestore.collection("users").document(userId)
                .collection("babyProfiles")
                .document("primary")
                .update(update as Map<String, Any>)
            getBabyStats()
        }
    }

    private fun getDiaryEntries() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                _diaryState.value = emptyList()
                return@launch
            }
            firestore.collection("users").document(userId)
                .collection("diaries")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _diaryState.value = emptyList()
                        return@addSnapshotListener
                    }
                    val diaries = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Diary(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                desc = doc.getString("description") ?: "",
                                date = doc.getLong("date") ?: System.currentTimeMillis(),
                                imgUrl = doc.getString("imgUrl")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.filter { diary ->
                        _selectedDate.value?.let { selected ->
                            val diaryDate = Instant.ofEpochMilli(diary.date)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            diaryDate == selected
                        } ?: true
                    } ?: emptyList()
                    _diaryState.value = diaries
                }
        }
    }

    fun addDiaryEntry(title: String, description: String, imageUri: String?, context: Context, date: Long) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(context, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val imageUrl = if (imageUri != null) {
                cloudinaryManager.uploadImage(imageUri).getOrNull()
            } else {
                null
            }
            saveDiaryToFirestore(title, description, imageUrl, userId, context, date)
        }
    }

    private fun saveDiaryToFirestore(title: String, description: String, imageUrl: String?, userId: String, context: Context, date: Long) {
        val diary = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "imgUrl" to imageUrl
        )
        firestore.collection("users").document(userId)
            .collection("diaries")
            .add(diary)
            .addOnSuccessListener {
                Toast.makeText(context, "Diary berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal menyimpan diary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserProfile(){
        viewModelScope.launch {
            growthRepo.getBabyProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userProfile.value = resource.data ?: UserProfile()
                    }
                    is Resource.Error -> {
                        _userProfile.value = UserProfile()
                    }
                    is Resource.Loading -> {
                    }
                    is Resource.Idle -> {
                        // No action needed for Idle state
                    }
                }
            }
        }
    }
}