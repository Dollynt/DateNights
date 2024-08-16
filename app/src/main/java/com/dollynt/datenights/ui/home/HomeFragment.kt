package com.dollynt.datenights.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentHomeBinding
import com.dollynt.datenights.databinding.FragmentHomeNoCoupleBinding
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.ui.couple.CoupleViewModel
import com.dollynt.datenights.ui.selectOptions.SelectOptionsFragment
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var _bindingNoCouple: FragmentHomeNoCoupleBinding? = null
    private val binding get() = _binding
    private val bindingNoCouple get() = _bindingNoCouple

    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var homeViewModel: HomeViewModel
    private var isInCouple: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        coupleViewModel = ViewModelProvider(requireActivity())[CoupleViewModel::class.java]
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        coupleViewModel.isInCouple.observe(viewLifecycleOwner) { isCouple ->
            isInCouple = isCouple
            updateLayout(inflater, container)
        }

        return binding?.root
    }

    private fun updateLayout(inflater: LayoutInflater, container: ViewGroup?) {
        val fragmentContainer = view?.findViewById<FrameLayout>(R.id.fragment_container)

        val newView = if (isInCouple == true) {
            _bindingNoCouple = null
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            setupListeners()
            binding?.root
        } else {
            _binding = null
            _bindingNoCouple = FragmentHomeNoCoupleBinding.inflate(inflater, container, false)
            bindingNoCouple?.root
        }

        fragmentContainer?.removeAllViews()
        fragmentContainer?.addView(newView)
    }

    private fun setupListeners() {
        binding?.appOptionsButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                homeViewModel.fetchOptions("1")
                homeViewModel.options.observe(viewLifecycleOwner) { options ->
                    showSelectOptionsScreen(options)
                }
            }
        }

        binding?.coupleOptionsButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val coupleId = coupleViewModel.couple.value?.id.toString()
                homeViewModel.fetchOptions(coupleId)
                homeViewModel.options.observe(viewLifecycleOwner) { options ->
                    showSelectOptionsScreen(options)
                }
            }
        }
    }

    private fun showSelectOptionsScreen(options: List<Option>) {
        val fragmentContainer = view?.findViewById<FrameLayout>(R.id.fragment_container)

        fragmentContainer?.removeAllViews()

        val selectOptionsFragment = SelectOptionsFragment.newInstance(options)
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, selectOptionsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bindingNoCouple = null
    }
}
