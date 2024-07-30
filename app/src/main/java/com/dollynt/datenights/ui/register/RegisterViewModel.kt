package com.dollynt.datenights.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    val user = MutableLiveData<FirebaseUser?>()
    val errorMessage = MutableLiveData<String?>()

    fun registerWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            user.postValue(Firebase.auth.currentUser)
                        } else {
                            user.postValue(null)
                            errorMessage.postValue(authResult.exception?.message)
                        }
                    }
            } catch (e: Exception) {
                user.postValue(null)
                errorMessage.postValue(e.message)
            }
        }
    }
}
