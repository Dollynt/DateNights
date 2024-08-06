package com.dollynt.datenights.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R

class UserActivity : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel
    private lateinit var createTokenButton: Button
    private lateinit var createLinkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        createTokenButton = findViewById(R.id.createTokenButton)
        createLinkButton = findViewById(R.id.createLinkButton)

        createTokenButton.setOnClickListener {
            viewModel.createInviteToken(
                onSuccess = { token ->
                    Toast.makeText(this, "Invite token created: $token", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(this, "Failed to create invite token: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        createLinkButton.setOnClickListener {
            viewModel.createInviteToken(
                onSuccess = { token ->
                    viewModel.createInvitationLink(
                        token,
                        onSuccess = { link ->
                            Toast.makeText(this, "Invitation link created: $link", Toast.LENGTH_SHORT).show()
                            // You can share the link using an intent
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, link)
                                type = "text/plain"
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share link via"))
                        },
                        onFailure = { e ->
                            Toast.makeText(this, "Failed to create invitation link: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFailure = { e ->
                    Toast.makeText(this, "Failed to create invite token: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
