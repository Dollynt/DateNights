package com.dollynt.datenights.ui.couple

import android.app.Application
import android.widget.Toast
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
    val isInCouple: LiveData<Boolean> get() = _isInCouple
    private val _isCoupleComplete = MutableLiveData<Boolean>()
    val isCoupleComplete: LiveData<Boolean> get() = _isCoupleComplete
    private val _inviteLink = MutableLiveData<String>()
    val inviteLink: LiveData<String> get() = _inviteLink
    private val _inviteCode = MutableLiveData<String>()
    val inviteCode: LiveData<String> get() = _inviteCode
    private val _couple = MutableLiveData<Couple?>()
    val couple: LiveData<Couple?> get() = _couple

    init {
        fetchCouple()
    }

    fun fetchCouple() {
        val userId = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getCoupleByUserId(userId, { couple ->
                _couple.postValue(couple)
            }, { exception ->
                handleException(exception)
            })
        }
    }

    fun createCouple(userId: String) {
        viewModelScope.launch {
            repository.isUserInCouple(userId, { isInCouple ->
                if (!isInCouple) {
                    repository.createCouple(userId, { success ->
                        _isInCouple.postValue(success)
                        if (success) {
                            setupCouple()
                        }
                    }, { exception ->
                        handleException(exception)
                    })
                } else {
                    _isInCouple.postValue(true)
                }
            }, { exception ->
                handleException(exception)
            })
        }
    }

    fun joinCouple(userId: String, inviteCode: String) {
        viewModelScope.launch {
            repository.joinCouple(userId, inviteCode,
                onComplete = { success ->
                    if (success) {
                        setupCouple()
                    } else {
                        Toast.makeText(getApplication(), "Failed to join couple", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { exception ->
                    handleException(exception)
                }
            )
        }
    }

    fun deleteCouple(userId: String) {
        viewModelScope.launch {
            repository.deleteCouple(userId, {
                _isCoupleComplete.postValue(false)
                _isInCouple.postValue(false)
                _inviteLink.postValue("")
                _inviteCode.postValue("")
                _couple.postValue(null)
            }, { exception ->
                handleException(exception)
            })
        }
    }

    private fun setupCouple() {
        viewModelScope.launch {
            val userId = Firebase.auth.currentUser?.uid ?: return@launch
            repository.getCoupleByUserId(userId, { couple ->
                couple?.let {
                    _inviteLink.postValue(it.inviteLink)
                    _inviteCode.postValue(it.inviteCode)
                    _isInCouple.postValue(true)
                    _couple.postValue(it)
                    checkIfCoupleComplete(userId)
                }
            }, { exception ->
                handleException(exception)
            })
        }
    }

    fun checkCoupleStatus(userId: String? = Firebase.auth.currentUser?.uid) {
        userId?.let {
            viewModelScope.launch {
                repository.getCoupleByUserId(it, { couple ->
                    _isInCouple.postValue(couple != null)
                    couple?.let {
                        _inviteLink.postValue(it.inviteLink)
                        _inviteCode.postValue(it.inviteCode)
                        checkIfCoupleComplete(userId)
                    }
                }, { exception ->
                    handleException(exception)
                })
            }
        }
    }

    private fun checkIfCoupleComplete(userId: String) {
        viewModelScope.launch {
            repository.isCoupleComplete(userId, { isComplete ->
                _isCoupleComplete.postValue(isComplete)
            }, { exception ->
                handleException(exception)
            })
        }
    }

    private fun handleException(exception: Exception) {
        Toast.makeText(getApplication(), exception.message, Toast.LENGTH_SHORT).show()
    }
}
