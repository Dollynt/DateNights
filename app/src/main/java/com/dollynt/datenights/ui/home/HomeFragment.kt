package com.dollynt.datenights.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dollynt.datenights.R
import com.dollynt.datenights.adapter.ExpandableListAdapter
import com.dollynt.datenights.databinding.FragmentHomeBinding
import com.dollynt.datenights.databinding.FragmentHomeNoCoupleBinding
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.ui.couple.CoupleViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var _bindingNoCouple: FragmentHomeNoCoupleBinding? = null
    private val binding get() = _binding!!
    private val bindingNoCouple get() = _bindingNoCouple!!

    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var homeViewModel: HomeViewModel
    private var isInCouple: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        coupleViewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

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
            setupListeners()
            binding.root
        } else {
            _binding = null
            _bindingNoCouple = FragmentHomeNoCoupleBinding.inflate(inflater, container, false)
            bindingNoCouple.root
        }

        fragmentContainer?.removeAllViews()
        fragmentContainer?.addView(newView)
    }

    private fun setupListeners() {
        binding.appOptionsButton.setOnClickListener {
            lifecycleScope.launch {
                homeViewModel.fetchOptions(1.toString())
                homeViewModel.options.observe(viewLifecycleOwner) { options ->
                    showSelectOptionsScreen(options)
                }
            }
        }

        binding.coupleOptionsButton.setOnClickListener {
            lifecycleScope.launch {
                val coupleId = coupleViewModel.couple.value?.id.toString()
                homeViewModel.fetchOptions(coupleId)
                homeViewModel.options.observe(viewLifecycleOwner) { options ->
                    showSelectOptionsScreen(options)
                }
            }
        }
    }

    private fun showSelectOptionsScreen(options: List<Option>) {
        val inflater = LayoutInflater.from(context)
        val selectOptionsView = inflater.inflate(R.layout.fragment_select_options, binding.root, false)

        val dynamicOptionsContainer = selectOptionsView.findViewById<LinearLayout>(R.id.dynamic_options_container)
        val btnRandomize = selectOptionsView.findViewById<Button>(R.id.btn_randomize)

        options.forEach { option ->
            val optionView = inflater.inflate(R.layout.layout_option_item, dynamicOptionsContainer, false)
            val checkBox = optionView.findViewById<CheckBox>(R.id.cb_option).apply {
                text = option.name
                textSize = 20.0F
            }
            val expandIcon = optionView.findViewById<ImageView>(R.id.expand_icon)
            val subOptionsContainer = optionView.findViewById<LinearLayout>(R.id.sub_options_container)

            // Add suboptions dynamically
            option.subOptions.forEach { subOption ->
                val subOptionView = inflater.inflate(android.R.layout.simple_list_item_1, subOptionsContainer, false)
                val subOptionTextView = subOptionView.findViewById<TextView>(android.R.id.text1)
                subOptionTextView.text = subOption
                subOptionsContainer.addView(subOptionView)
            }

            // Toggle visibility of subOptionsContainer
            expandIcon.setOnClickListener {
                if (subOptionsContainer.visibility == View.GONE) {
                    subOptionsContainer.visibility = View.VISIBLE
                    expandIcon.setImageResource(R.drawable.ic_expand_less)  // Update icon to indicate it's expanded
                } else {
                    subOptionsContainer.visibility = View.GONE
                    expandIcon.setImageResource(R.drawable.ic_expand_more)  // Update icon to indicate it's collapsed
                }
            }

            dynamicOptionsContainer.addView(optionView)
        }

        btnRandomize.setOnClickListener {
            val selectedOptions = getSelectedOptions(dynamicOptionsContainer, options)
            if (selectedOptions.isNotEmpty()) {
                randomizeOptions(selectedOptions)
            } else {
                Toast.makeText(context, "Selecione pelo menos uma opção.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.removeAllViews()
        binding.root.addView(selectOptionsView)
    }


    private fun getSelectedOptions(container: LinearLayout, options: List<Option>): List<Option> {
        val selectedOptions = mutableListOf<Option>()
        for (i in 0 until container.childCount) {
            val optionView = container.getChildAt(i)
            val checkBox = optionView.findViewById<CheckBox>(R.id.cb_option)
            if (checkBox.isChecked) {
                selectedOptions.add(options[i])
            }
        }
        return selectedOptions
    }

    private fun setupExpandableListView(listView: ExpandableListView, title: String, subOptions: List<String>) {
        val listDataHeader = listOf(title)
        val listDataChild = hashMapOf(title to subOptions)
        val listAdapter = ExpandableListAdapter(requireContext(), listDataHeader, listDataChild)
        listView.setAdapter(listAdapter)
    }

    private fun randomizeOptions(selectedOptions: List<Option>) {
        binding.root.postDelayed({
            val randomOption = selectedOptions.randomOrNull()?.name ?: "Nenhuma opção selecionada"
            Toast.makeText(context, "Resultado: $randomOption", Toast.LENGTH_LONG).show()
        }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bindingNoCouple = null
    }
}
