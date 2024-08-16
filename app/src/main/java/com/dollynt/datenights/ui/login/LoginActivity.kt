package com.dollynt.datenights.ui.login

import LoginViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.MainActivity
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.ActivityLoginBinding
import com.dollynt.datenights.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var inviteCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        inviteCode = intent.getStringExtra("inviteCode")

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
                viewModel.signInWithEmailAndPassword(email, password)
            } else {
                Snackbar.make(binding.root, R.string.error_empty_fields, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java).apply {
                putExtra("inviteCode", inviteCode)
            }
            startActivity(intent)
        }

        viewModel.user.observe(this) { user ->
            if (user != null) {
                inviteCode?.let {
                    viewModel.joinCouple(user.uid, it)
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        inviteCode = intent.getStringExtra("inviteCode")
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Snackbar.make(binding.root, R.string.error_google_sign_in_failed, Snackbar.LENGTH_SHORT).show()
        }
    }
}
