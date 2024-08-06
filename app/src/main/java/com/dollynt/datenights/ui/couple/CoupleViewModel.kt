package com.dollynt.datenights.ui.couple

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CoupleViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun joinCoupleWithToken(token: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser ?: return

        // Find the user with the given token
        db.collection("users").whereEqualTo("secretInviteToken", token).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onFailure(Exception("Invalid token"))
                    return@addOnSuccessListener
                }

                val user1 = documents.documents[0].id
                val couple = hashMapOf(
                    "user1" to user1,
                    "user2" to user.uid,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("couples").add(couple)
                    .addOnSuccessListener { documentReference ->
                        // Update the coupleId in both users' documents
                        val coupleId = documentReference.id
                        db.collection("users").document(user1).update("coupleId", coupleId)
                        db.collection("users").document(user.uid).update("coupleId", coupleId)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
