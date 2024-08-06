package com.dollynt.datenights.ui.couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.databinding.FragmentCoupleBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CoupleFragment : Fragment() {

    private var _binding: FragmentCoupleBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoupleBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CoupleViewModel::class.java)

        val user = Firebase.auth.currentUser

        viewModel.isCoupleCreated.observe(viewLifecycleOwner) { isCoupleCreated ->
            if (isCoupleCreated) {
                binding.createCoupleLayout.visibility = View.GONE
                binding.enterJoinCodeLayout.visibility = View.GONE
                binding.inviteOptionsLayout.visibility = View.VISIBLE
            } else {
                binding.createCoupleLayout.visibility = View.VISIBLE
                binding.enterJoinCodeLayout.visibility = View.GONE
                binding.inviteOptionsLayout.visibility = View.GONE
            }
        }

        viewModel.inviteLink.observe(viewLifecycleOwner) { inviteLink ->
            binding.inviteLinkTextView.text = inviteLink
        }

        viewModel.inviteCode.observe(viewLifecycleOwner) { inviteCode ->
            binding.inviteCodeTextView.text = inviteCode
        }

        binding.createCoupleButton.setOnClickListener {
            viewModel.createCouple(user?.uid ?: "")
        }

        binding.showJoinCoupleLayoutButton.setOnClickListener {
            binding.createCoupleLayout.visibility = View.GONE
            binding.showJoinCoupleLayoutButton.visibility = View.GONE
            binding.enterJoinCodeLayout.visibility = View.VISIBLE
        }

        binding.joinCoupleButton.setOnClickListener {
            val code = binding.joinCoupleCodeInput.text.toString()
            viewModel.joinCouple(user?.uid ?: "", code)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
