package com.dollynt.datenights.ui.couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateOrJoinCoupleFragment : Fragment() {

    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_or_join_couple, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)

        val createCoupleButton = view.findViewById<Button>(R.id.createCoupleButton)
        val showJoinCoupleLayoutButton = view.findViewById<Button>(R.id.showJoinCoupleLayoutButton)

        createCoupleButton.setOnClickListener {
            viewModel.createCouple(Firebase.auth.currentUser?.uid ?: "")
        }

        showJoinCoupleLayoutButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
                .replace(R.id.coupleContentFrame, JoinCoupleFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
