package com.dollynt.datenights.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dollynt.datenights.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Configurar as opções de randomização
        setupRandomizationOptions()

        return binding.root
    }

    private fun setupRandomizationOptions() {
        // Botão para as opções do aplicativo
//        binding.appOptionsButton.setOnClickListener {
//            navigateToRandomizationScreen("APP")
//        }
//
//        // Botão para as opções personalizadas do casal
//        binding.coupleOptionsButton.setOnClickListener {
//            navigateToRandomizationScreen("COUPLE")
//        }
    }

    private fun navigateToRandomizationScreen(optionType: String) {
//        val intent = Intent(activity, RandomizationActivity::class.java)
//        intent.putExtra("OPTION_TYPE", optionType)
//        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
