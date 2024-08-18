package com.dollynt.datenights.repository

import com.dollynt.datenights.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserData(uid: String): User? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(user: User) {
        try {
            db.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createUserInFirestore(): User? {
        val currentUser = auth.currentUser ?: return null

        val user = User(
            currentUser.uid,
            currentUser.email,
            null,
            null,
            null,
            null
        )

        saveUser(user)
        return user
    }
}
