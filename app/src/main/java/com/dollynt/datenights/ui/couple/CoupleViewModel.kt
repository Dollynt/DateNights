package com.dollynt.datenights.ui.couple

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.model.Couple
import com.dollynt.datenights.repository.CoupleRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch

class CoupleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CoupleRepository(application)
    private val _isInCouple = MutableLiveData<Boolean>()
    val isInCouple: LiveData<Boolean> = _isInCouple
    private val _isCoupleComplete = MutableLiveData<Boolean>()
    val isCoupleComplete: LiveData<Boolean> = _isCoupleComplete
    private val _inviteLink = MutableLiveData<String>()
    val inviteLink: LiveData<String> = _inviteLink
    private val _inviteCode = MutableLiveData<String>()
    val inviteCode: LiveData<String> = _inviteCode
    private val _couple = MutableLiveData<Couple?>()
    val couple: LiveData<Couple?> = _couple

    fun createCouple(userId: String) {
        viewModelScope.launch {
            if (!repository.isUserInCouple(userId)) {
                _isInCouple.value = repository.createCouple(userId)
                if (_isInCouple.value == true) {
                    setupCouple()
                }
            } else {
                _isInCouple.value = true
            }
        }
    }

    fun joinCouple(userId: String, inviteCode: String) {
        viewModelScope.launch {
            val success = repository.joinCouple(userId, inviteCode)
            _isInCouple.value = success
            if (success) {
                setupCouple()
            }
        }
    }

    fun deleteCouple(userId: String) {
        viewModelScope.launch {
            repository.deleteCouple(userId)
            _isCoupleComplete.value = false
            _isInCouple.value = false
            _inviteLink.value = ""
            _inviteCode.value = ""
            _couple.value = null
        }
    }

    private fun setupCouple() {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch
            val couple = repository.getCoupleByUserId(userId)
            if (couple != null) {
                _inviteLink.value = couple.inviteLink
                _inviteCode.value = couple.inviteCode
                _couple.value = couple
                checkIfCoupleComplete(userId)
            }
        }
    }

    fun checkCoupleStatus(userId: String? = Firebase.auth.currentUser?.uid) {
        userId?.let {
            viewModelScope.launch {
                val couple = repository.getCoupleByUserId(it)
                _isInCouple.value = couple != null
                if (couple != null) {
                    _inviteLink.value = "https://datenights.app/invite?code=${couple.inviteCode}"
                    _inviteCode.value = couple.inviteCode
                    checkIfCoupleComplete(it)
                }
            }
        }
    }

    private fun checkIfCoupleComplete(userId: String) {
        viewModelScope.launch {
            _isCoupleComplete.value = repository.isCoupleComplete(userId)
        }
    }
}
