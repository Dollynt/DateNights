// CoupleRepository.kt

package com.dollynt.datenights.repository

import android.content.Context
import android.net.Uri
import com.dollynt.datenights.model.Couple
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class CoupleRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()

    fun isUserInCouple(userId: String, onComplete: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                onComplete(snapshot.documents.isNotEmpty())
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun createCouple(userId: String, onComplete: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        isUserInCouple(userId, { isInCouple ->
            if (isInCouple) {
                onError(Exception("User is already in a couple"))
            } else {
                val inviteCode = generateUniqueInviteCode()
                generateInviteLink(inviteCode) { link ->
                    val couple = Couple(
                        id = "",
                        users = listOf(userId),
                        inviteCode = inviteCode,
                        inviteLink = link
                    )
                    db.collection("couples").add(couple)
                        .addOnSuccessListener { result ->
                            val coupleId = result.id
                            val updatedCouple = couple.copy(id = coupleId)
                            db.collection("couples").document(coupleId).set(updatedCouple)
                                .addOnSuccessListener {
                                    onComplete(result.id.isNotEmpty())
                                }
                                .addOnFailureListener { exception ->
                                    onError(exception)
                                }
                        }
                        .addOnFailureListener { exception ->
                            onError(exception)
                        }
                }
            }
        }, onError)
    }

    fun joinCouple(userId: String, inviteCode: String, onComplete: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        isUserInCouple(userId, { isInCouple ->
            if (isInCouple) {
                onError(Exception("User is already in a couple"))
            } else {
                isCoupleComplete(userId, { isComplete ->
                    if (isComplete) {
                        onError(Exception("Couple is complete"))
                    } else {
                        db.collection("couples")
                            .whereEqualTo("inviteCode", inviteCode)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.documents.isNotEmpty()) {
                                    val coupleId = snapshot.documents[0].id
                                    db.collection("couples").document(coupleId)
                                        .update("users", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener {
                                            onComplete(true)
                                        }
                                        .addOnFailureListener { exception ->
                                            onError(exception)
                                        }
                                } else {
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { exception ->
                                onError(exception)
                            }
                    }
                }, onError)
            }
        }, onError)
    }

    fun deleteCouple(userId: String, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    val coupleId = snapshot.documents[0].id
                    db.collection("couples").document(coupleId).delete()
                        .addOnSuccessListener {
                            onComplete()
                        }
                        .addOnFailureListener { exception ->
                            onError(exception)
                        }
                } else {
                    onComplete()
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getCoupleByUserId(userId: String, onComplete: (Couple?) -> Unit, onError: (Exception) -> Unit) {
        db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val couple = if (snapshot.documents.isNotEmpty()) {
                    snapshot.documents[0].toObject(Couple::class.java)
                } else {
                    null
                }
                onComplete(couple)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun isCoupleComplete(userId: String, onComplete: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        db.collection("couples")
            .whereArrayContains("users", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    val document = snapshot.documents[0]
                    val users = document.get("users")
                    if (users is List<*>) {
                        val isComplete = users.size == 2
                        onComplete(isComplete)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private fun generateUniqueInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private fun generateInviteLink(inviteCode: String, onComplete: (String) -> Unit) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://datenights.page.link/invite?inviteCode=$inviteCode"))
            .setDomainUriPrefix("https://datenights.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.dollynt.datenights")
                    .setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.dollynt.datenights"))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { dynamicLink ->
                onComplete(dynamicLink.shortLink.toString())
            }
            .addOnFailureListener { exception ->
                onComplete("")
            }
    }
}
