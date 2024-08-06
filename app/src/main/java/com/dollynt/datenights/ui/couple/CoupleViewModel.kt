package com.dollynt.datenights.ui.couple

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.repository.CoupleRepository
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
        _isCoupleCreated.value = repository.getCoupleCreatedState()
    }

    fun createCouple(userId: String) {
        viewModelScope.launch {
            if (!repository.isUserInCouple(userId)) {
                _isCoupleCreated.value = repository.createCouple(userId)
                generateInviteDetails(userId)
            } else {
                _isCoupleCreated.value = true
            }
        }
    }

    fun joinCouple(userId: String, inviteCode: String) {
        viewModelScope.launch {
            if (!repository.isUserInCouple(userId)) {
                _isCoupleCreated.value = repository.joinCouple(userId, inviteCode)
                generateInviteDetails(userId)
            } else {
                _isCoupleCreated.value = true
            }
        }
    }

    private fun generateInviteDetails(userId: String) {
        viewModelScope.launch {
            _inviteLink.value = repository.generateInviteLink(userId)
            _inviteCode.value = repository.generateInviteCode(userId)
        }
    }
}
