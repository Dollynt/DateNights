package com.dollynt.datenights.ui.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class UserViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createInviteToken(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser ?: return

        // Generate a random token
        val token = UUID.randomUUID().toString()

        // Save the token to the user's document
        db.collection("users").document(user.uid)
            .update("secretInviteToken", token)
            .addOnSuccessListener {
                onSuccess(token)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun createInvitationLink(token: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://datenights.page.link/join?inviteToken=$token"))
            .setDomainUriPrefix("https://datenights.page.link")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildDynamicLink()

        val dynamicLinkUri = dynamicLink.uri
        onSuccess(dynamicLinkUri.toString())
    }

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
