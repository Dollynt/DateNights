package com.dollynt.datenights.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.repository.CoupleRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CoupleRepository(application)
    private val _isInCouple = MutableLiveData<Boolean>()
    val isInCouple: LiveData<Boolean> = _isInCouple

    fun checkCoupleStatus(userId: String) {
        viewModelScope.launch {
            val isUserInCouple = repository.isUserInCouple(userId)
            _isInCouple.value = isUserInCouple
        }
    }
}
