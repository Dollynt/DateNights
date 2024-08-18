package com.dollynt.datenights.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.model.User
import com.dollynt.datenights.repository.CoupleRepository
import com.dollynt.datenights.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val coupleRepository = CoupleRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun registerWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        try {
                            val user = userRepository.createUserInFirestore()
                            _user.value = user
                        } catch (e: Exception) {
                            _errorMessage.value = "Erro ao criar o usuário: ${e.message}"
                        }
                    }
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    fun joinCouple(userId: String, inviteCode: String) {
        viewModelScope.launch {
            coupleRepository.joinCouple(userId, inviteCode,
                onComplete = { success ->
                    if (!success) {
                        _errorMessage.postValue("Failed to join couple")
                    }
                },
                onError = { exception ->
                    _errorMessage.postValue(exception.message)
                }
            )
        }
    }
}
