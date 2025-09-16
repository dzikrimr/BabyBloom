package com.example.bubtrack.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bubtrack.data.notification.FcmRepo
import com.example.bubtrack.presentation.notification.comps.NotificationItem
import com.example.bubtrack.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val fcmRepo: FcmRepo
) : ViewModel() {


    private var _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    init {
        getNotifications()
    }


    private fun getNotifications(){
        viewModelScope.launch {
           val data = fcmRepo.getNotifications()
            if (data is Resource.Success){
                _notifications.value = data.data!!
            }
        }
    }z
}