package com.dollynt.datenights.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.MainActivity
import com.dollynt.datenights.databinding.ActivityRegisterBinding
import com.dollynt.datenights.ui.login.LoginActivity
import com.dollynt.datenights.ui.user.UserViewModel
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private var inviteCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]

        inviteCode = intent.getStringExtra("inviteCode")

        binding.registerButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                userViewModel.registerWithEmailAndPassword(email, password)
            } else {
                Snackbar.make(binding.root, "Por favor, preencha todos os campos.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.loginTextViewContainer.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                inviteCode?.let { code ->
                    coupleViewModel.joinCouple(user.uid, code)
                    // Espera pela conclusão do joinCouple
                    coupleViewModel.isInCouple.observe(this) { isInCouple ->
                        if (isInCouple) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                } ?: run {
                    // Caso não haja inviteCode, vá direto para MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
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
