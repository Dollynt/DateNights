package com.dollynt.datenights.ui.couple

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.repository.CoupleRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch

class CoupleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CoupleRepository(application)
    private val _isCoupleCreated = MutableLiveData<Boolean>()
    val isCoupleCreated: LiveData<Boolean> = _isCoupleCreated
    private val _inviteLink = MutableLiveData<String>()
    val inviteLink: LiveData<String> = _inviteLink
    private val _inviteCode = MutableLiveData<String>()
    val inviteCode: LiveData<String> = _inviteCode

    init {
        checkCoupleStatus()
    }

    fun createCouple(userId: String) {
        viewModelScope.launch {
            if (!repository.isUserInCouple(userId)) {
                _isCoupleCreated.value = repository.createCouple(userId)
                if (_isCoupleCreated.value == true) {
                    generateInviteDetails()
                }
            } else {
                _isCoupleCreated.value = true
            }
        }
    }

    fun joinCouple(userId: String, inviteCode: String) {
        viewModelScope.launch {
            if (!repository.isUserInCouple(userId)) {
                _isCoupleCreated.value = repository.joinCouple(userId, inviteCode)
                if (_isCoupleCreated.value == true) {
                    generateInviteDetails()
                }
            } else {
                _isCoupleCreated.value = true
            }
        }
    }

    private fun generateInviteDetails() {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch
            val couple = repository.getCoupleByUserId(userId)
            if (couple != null) {
                _inviteLink.value = "https://datenights.app/invite?code=${couple.inviteCode}"
                _inviteCode.value = couple.inviteCode
            }
        }
    }

    fun checkCoupleStatus(userId: String? = Firebase.auth.currentUser?.uid) {
        userId?.let {
            viewModelScope.launch {
                val couple = repository.getCoupleByUserId(it)
                _isCoupleCreated.value = couple != null
                if (couple != null) {
                    _inviteLink.value = "https://datenights.app/invite?code=${couple.inviteCode}"
                    _inviteCode.value = couple.inviteCode
                }
            }
        }
    }
}
