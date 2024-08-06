package com.dollynt.datenights.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.dollynt.datenights.model.Couple
import kotlinx.coroutines.tasks.await

class CoupleRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("CouplePrefs", Context.MODE_PRIVATE)

    suspend fun createCouple(userId: String): Boolean {
        val couple = Couple(user1 = userId, inviteCode = generateUniqueInviteCode())
        return try {
            db.collection("couples")
                .add(couple)
                .await()
            saveCoupleCreatedState(true)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun joinCouple(userId: String, inviteCode: String): Boolean {
        return try {
            val snapshot = db.collection("couples")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val couple = snapshot.documents[0].toObject(Couple::class.java)
                couple?.let {
                    it.user2 = userId
                    db.collection("couples").document(snapshot.documents[0].id)
                        .set(it)
                        .await()
                    saveCoupleCreatedState(true)
                    return@let true
                } ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }

    private suspend fun generateUniqueInviteCode(): String {
        var inviteCode: String
        var isUnique = false
        do {
            inviteCode = generateInviteCode()
            val snapshot = db.collection("couples")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
            if (snapshot.isEmpty) {
                isUnique = true
            }
        } while (!isUnique)
        return inviteCode
    }

    suspend fun generateInviteLink(userId: String): String {
        val inviteCode = generateUniqueInviteCode()
        return "https://yourapp.com/invite?code=$inviteCode"
    }

    suspend fun generateInviteCode(userId: String): String {
        return generateUniqueInviteCode()
    }

    suspend fun isUserInCouple(userId: String): Boolean {
        val snapshot = db.collection("couples")
            .whereEqualTo("user1", userId)
            .get()
            .await()
        return !snapshot.isEmpty
    }

    private fun saveCoupleCreatedState(isCreated: Boolean) {
        sharedPreferences.edit().putBoolean("isCoupleCreated", isCreated).apply()
    }

    fun getCoupleCreatedState(): Boolean {
        return sharedPreferences.getBoolean("isCoupleCreated", false)
    }
}
