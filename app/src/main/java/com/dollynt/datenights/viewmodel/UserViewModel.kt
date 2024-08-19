package com.dollynt.datenights.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.model.User
import com.dollynt.datenights.repository.CoupleRepository
import com.dollynt.datenights.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        loadUserData(it.uid)
                    }
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        checkAndCreateUser(it.uid, it.email, it.displayName, it.photoUrl?.toString())
                    }
                } else {
                    _errorMessage.value = task.exception?.message
                }
            }
    }

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

    private fun checkAndCreateUser(uid: String, email: String?, name: String?, profilePictureUrl: String?) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserData(uid)
                if (user == null) {
                    // Usuário não existe, criar novo documento no Firestore
                    val newUser = User(
                        uid,
                        email,
                        name,
                        null,
                        null,
                        profilePictureUrl
                    )
                    userRepository.saveUser(newUser)
                    _user.value = newUser
                } else {
                    // Usuário já existe, carregar os dados
                    _user.value = user
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar ou criar os dados do usuário: ${e.message}"
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserData(uid)
                _user.value = user
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar os dados do usuário: ${e.message}"
            }
        }
    }
}
