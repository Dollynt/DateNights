package com.dollynt.datenights.repository

import android.content.Context
import android.net.Uri
import com.dollynt.datenights.model.Couple
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class CoupleRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun isUserInCouple(userId: String): Boolean {
        val snapshot = db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .await()
        return snapshot.documents.isNotEmpty()
    }

    suspend fun createCouple(userId: String): Boolean {
        val inviteCode = generateUniqueInviteCode()
        val couple = Couple(
            users = listOf(userId),
            inviteCode = inviteCode,
            inviteLink = generateInviteLink(inviteCode)
        )
        val result = db.collection("couples").add(couple).await()
        return result.id.isNotEmpty()
    }

    suspend fun joinCouple(userId: String, inviteCode: String): Boolean {
        val snapshot = db.collection("couples")
            .whereEqualTo("inviteCode", inviteCode)
            .get()
            .await()
        if (snapshot.documents.isNotEmpty()) {
            val coupleId = snapshot.documents[0].id
            db.collection("couples").document(coupleId)
                .update("users", FieldValue.arrayUnion(userId))
                .await()
            return true
        }
        return false
    }

    suspend fun deleteCouple(userId: String) {
        val snapshot = db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .await()
        if (snapshot.documents.isNotEmpty()) {
            val coupleId = snapshot.documents[0].id
            db.collection("couples").document(coupleId).delete().await()
        }
    }

    suspend fun getCoupleByUserId(userId: String): Couple? {
        val snapshot = db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .await()
        return if (snapshot.documents.isNotEmpty()) {
            snapshot.documents[0].toObject(Couple::class.java)
        } else {
            null
        }
    }

    suspend fun isCoupleComplete(userId: String): Boolean {
        val couple = getCoupleByUserId(userId)
        return couple?.users?.size == 2
    }

    private suspend fun generateUniqueInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        var inviteCode: String
        var isUnique = false

        while (!isUnique) {
            inviteCode = (1..8).map { chars.random() }.joinToString("")
            val snapshot = db.collection("couples")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
            if (snapshot.documents.isEmpty()) {
                isUnique = true
                return inviteCode
            }
        }
        throw Exception("Unable to generate a unique invite code")
    }

    private suspend fun generateInviteLink(inviteCode: String): String {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://datenights.page.link/invite?inviteCode=$inviteCode"))
            .setDomainUriPrefix("https://datenights.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.dollynt.datenights") // Especifique o nome do pacote aqui
                    .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.dollynt.datenights"))
                    .build()
            )
            .buildShortDynamicLink()
            .await()

        return dynamicLink.shortLink.toString()
    }

}
