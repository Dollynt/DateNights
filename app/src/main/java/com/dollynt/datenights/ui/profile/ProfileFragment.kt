package com.dollynt.datenights.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentProfileBinding
import com.dollynt.datenights.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.buttonSettings.setOnClickListener {
            binding.fragmentProfile.removeAllViews()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_profile, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }

        return binding.root
    }

    private fun logoutUser() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            for (profile in user.providerData) {
                if (profile.providerId == GoogleAuthProvider.PROVIDER_ID) {
                    signOutGoogleUser()
                    return
                }
            }
        }
        signOutFirebaseUser()
    }

    private fun signOutGoogleUser() {
        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
            requireActivity(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
        googleSignInClient.signOut().addOnCompleteListener {
            signOutFirebaseUser()
        }
    }

    private fun signOutFirebaseUser() {
        Firebase.auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
