package com.dollynt.datenights.ui.couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinCoupleFragment : Fragment() {

    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_join_couple, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)

        val joinCoupleButton = view.findViewById<Button>(R.id.joinCoupleButton)
        val joinCoupleCodeInput = view.findViewById<EditText>(R.id.joinCoupleCodeInput)
        val backIcon = view.findViewById<ImageView>(R.id.back_icon)

        joinCoupleButton.setOnClickListener {
            val code = joinCoupleCodeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.joinCouple(Firebase.auth.currentUser?.uid ?: "", code)
            } else {
                Toast.makeText(context, "Por favor, insira um código", Toast.LENGTH_SHORT).show()
            }
        }

        backIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
                .replace(R.id.coupleContentFrame, CreateOrJoinCoupleFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}

