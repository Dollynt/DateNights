package com.dollynt.datenights.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentHomeBinding
import com.dollynt.datenights.databinding.FragmentHomeNoCoupleBinding
import com.dollynt.datenights.ui.couple.CoupleViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var _bindingNoCouple: FragmentHomeNoCoupleBinding? = null
    private val binding get() = _binding!!
    private val bindingNoCouple get() = _bindingNoCouple!!

    private lateinit var coupleViewModel: CoupleViewModel
    private var isInCouple: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        coupleViewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)
        coupleViewModel.isInCouple.observe(viewLifecycleOwner) { isCouple ->
            isInCouple = isCouple
            updateLayout(inflater, container)
        }

        return inflater.inflate(R.layout.fragment_home_placeholder, container, false)
    }

    private fun updateLayout(inflater: LayoutInflater, container: ViewGroup?) {
        val fragmentContainer = view?.findViewById<FrameLayout>(R.id.fragment_container)

        val newView = if (isInCouple == true) {
            _bindingNoCouple = null
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            binding.root
        } else {
            _binding = null
            _bindingNoCouple = FragmentHomeNoCoupleBinding.inflate(inflater, container, false)
            bindingNoCouple.root
        }

        fragmentContainer?.removeAllViews()
        fragmentContainer?.addView(newView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bindingNoCouple = null
    }
}
