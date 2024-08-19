package com.dollynt.datenights.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.MainActivity
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.ActivityLoginBinding
import com.dollynt.datenights.ui.register.RegisterActivity
import com.dollynt.datenights.ui.user.UserViewModel
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private var inviteCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleDynamicLink(intent)

        if (Firebase.auth.currentUser != null) {
            navigateToMainActivity()
        } else {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
            coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            binding.googleSignInButton.setOnClickListener {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }

            binding.emailSignInButton.setOnClickListener {
                val email = binding.email.text.toString()
                val password = binding.password.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    userViewModel.signInWithEmailAndPassword(email, password)
                } else {
                    Snackbar.make(binding.root, R.string.error_empty_fields, Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.register.setOnClickListener {
                navigateToRegister()
            }

            userViewModel.user.observe(this) { user ->
                if (user != null) {
                    inviteCode?.let { code ->
                        coupleViewModel.joinCouple(user.uid, code)
                        // Espera pela conclusão do joinCouple
                        coupleViewModel.isInCouple.observe(this) { isInCouple ->
                            if (isInCouple) {
                                navigateToMainActivity()
                            }
                        }
                    } ?: run {
                        navigateToMainActivity()
                    }
                }
            }

            userViewModel.errorMessage.observe(this) { errorMessage ->
                if (errorMessage != null) {
                    Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleDynamicLink(intent: Intent) {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                inviteCode = deepLink?.getQueryParameter("inviteCode")
            }
            .addOnFailureListener(this) {
                Snackbar.make(binding.root, R.string.error_processing_link, Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun navigateToRegister() {
        val registerIntent = Intent(this, RegisterActivity::class.java).apply {
            putExtra("inviteCode", inviteCode)
        }
        startActivity(registerIntent)
        finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("inviteCode", inviteCode)
        })
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDynamicLink(intent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            userViewModel.firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Snackbar.make(binding.root, R.string.error_google_sign_in_failed, Snackbar.LENGTH_SHORT).show()
        }
    }
}
