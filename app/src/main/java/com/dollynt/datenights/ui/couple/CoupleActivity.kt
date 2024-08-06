package com.dollynt.datenights.ui.couple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class CoupleActivity : AppCompatActivity() {

    private lateinit var viewModel: CoupleViewModel
    private lateinit var tokenInput: EditText
    private lateinit var joinCoupleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_couple)

        viewModel = ViewModelProvider(this).get(CoupleViewModel::class.java)

        tokenInput = findViewById(R.id.tokenInput)
        joinCoupleButton = findViewById(R.id.joinCoupleButton)

        joinCoupleButton.setOnClickListener {
            val token = tokenInput.text.toString().trim()
            if (token.isNotEmpty()) {
                viewModel.joinCoupleWithToken(
                    token,
                    onSuccess = {
                        Toast.makeText(this, "Joined couple successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Failed to join couple: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                deepLink?.getQueryParameter("inviteToken")?.let { token ->
                    viewModel.joinCoupleWithToken(
                        token,
                        onSuccess = {
                            Toast.makeText(this, "Joined couple successfully", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(this, "Failed to join couple: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            .addOnFailureListener(this) { e ->
                Toast.makeText(this, "Failed to retrieve link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
