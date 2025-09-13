package com.example.bubtrack.presentation.diary

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import com.example.bubtrack.utill.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.widget.Toast

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val growthRepo: BabyGrowthRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrowthUiState())
    val uiState = _uiState.asStateFlow()

    private val _diaryState = MutableStateFlow<List<Diary>>(emptyList())
    val diaryState = _diaryState.asStateFlow()

    private var isCloudinaryInitialized = false
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
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
                    } ?: emptyList()
                    _diaryState.value = diaries
                }
        }
    }

    private fun initializeCloudinary(context: Context) {
        if (!isCloudinaryInitialized) {
            try {
                MediaManager.init(context, mapOf(
                    "cloud_name" to "dvwbrl4el",
                    "api_key" to "132211812476492",
                    "api_secret" to "LJm1DiUIlp4E_6jSI90EaIVFAFU"
                ))
                isCloudinaryInitialized = true
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menginisialisasi Cloudinary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addDiaryEntry(title: String, description: String, imageUri: String?, context: Context) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(context, "Pengguna tidak terautentikasi!", Toast.LENGTH_SHORT).show()
                return@launch
            }
            initializeCloudinary(context)
            if (imageUri != null) {
                MediaManager.get().upload(Uri.parse(imageUri))
                    .option("babyyy", "babybloom_upload")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Toast.makeText(context, "Mengunggah gambar...", Toast.LENGTH_SHORT).show()
                        }
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as? String
                            saveDiaryToFirestore(title, description, imageUrl, userId, context)
                        }
                        override fun onError(requestId: String, error: ErrorInfo) {
                            Toast.makeText(context, "Gagal mengunggah gambar: ${error.description}", Toast.LENGTH_SHORT).show()
                        }
                        override fun onReschedule(requestId: String, error: ErrorInfo) {}
                    }).dispatch(context)
            } else {
                saveDiaryToFirestore(title, description, null, userId, context)
            }
        }
    }

    private fun saveDiaryToFirestore(title: String, description: String, imageUrl: String?, userId: String, context: Context) {
        val diary = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to System.currentTimeMillis(),
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
}