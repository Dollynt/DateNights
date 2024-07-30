package com.dollynt.datenights.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val user = MutableLiveData<FirebaseUser?>()
    val errorMessage = MutableLiveData<String?>()

    init {
        val currentUser = Firebase.auth.currentUser
        user.postValue(currentUser)
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                Firebase.auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            user.postValue(Firebase.auth.currentUser)
                        } else {
                            Log.w("LoginViewModel", "signInWithCredential:failure", authResult.exception)
                            user.postValue(null)
                        }
                    }
            } catch (e: Exception) {
                Log.w("LoginViewModel", "Google sign in failed", e)
                user.postValue(null)
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            user.postValue(Firebase.auth.currentUser)
                        } else {
                            Log.w("LoginViewModel", "signInWithEmail:failure", authResult.exception)
                            user.postValue(null)
                            errorMessage.postValue(authResult.exception?.message)
                        }
                    }
            } catch (e: Exception) {
                Log.w("LoginViewModel", "signInWithEmailAndPassword failed", e)
                user.postValue(null)
                errorMessage.postValue(e.message)
            }
        }
    }
}
