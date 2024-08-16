package com.dollynt.datenights.ui.selectOptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentSelectOptionsBinding
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.ui.home.HomeFragment
import com.dollynt.datenights.ui.result.ResultFragment

class SelectOptionsFragment : Fragment() {

    private var options: List<Option>? = null
    private var _binding: FragmentSelectOptionsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_OPTIONS = "selected_options"

        fun newInstance(options: List<Option>): SelectOptionsFragment {
            val fragment = SelectOptionsFragment()
            val args = Bundle()
            args.putSerializable(ARG_OPTIONS, ArrayList(options))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectOptionsBinding.inflate(inflater, container, false)

        options = arguments?.getSerializable(ARG_OPTIONS) as? List<Option>

        val dynamicOptionsContainer = binding.dynamicOptionsContainer
        val btnRandomize = binding.btnRandomize
        val backIcon = binding.backIcon

        backIcon.setOnClickListener {
            inflateHomeFragment()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            inflateHomeFragment()
        }

        options?.forEach { option ->
            val optionView = inflater.inflate(R.layout.layout_option_item, dynamicOptionsContainer, false)
            val checkBox = optionView.findViewById<CheckBox>(R.id.cb_option).apply {
                text = option.name
            }
            val expandIcon = optionView.findViewById<ImageView>(R.id.expand_icon)
            val subOptionsContainer = optionView.findViewById<LinearLayout>(R.id.sub_options_container)

            option.subOptions.forEach { subOption ->
                val subOptionView = inflater.inflate(android.R.layout.simple_list_item_1, subOptionsContainer, false)
                val subOptionTextView = subOptionView.findViewById<TextView>(android.R.id.text1)
                subOptionTextView.text = subOption
                subOptionsContainer.addView(subOptionView)
            }

            expandIcon.setOnClickListener {
                if (subOptionsContainer.visibility == View.GONE) {
                    subOptionsContainer.visibility = View.VISIBLE
                    expandIcon.setImageResource(R.drawable.ic_expand_less)
                } else {
                    subOptionsContainer.visibility = View.GONE
                    expandIcon.setImageResource(R.drawable.ic_expand_more)
                }
            }

            dynamicOptionsContainer.addView(optionView)
        }

        btnRandomize.setOnClickListener {
            val selectedOptions = getSelectedOptions(dynamicOptionsContainer, options ?: emptyList())
            if (selectedOptions.isNotEmpty()) {
                navigateToResultScreen(selectedOptions)
            } else {
                Toast.makeText(context, "Selecione pelo menos uma opção.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
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

    private fun navigateToResultScreen(selectedOptions: List<Option>) {
        val diceImage = binding.root.findViewById<ImageView>(R.id.dice_image)
        diceImage.visibility = View.VISIBLE

        val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation)
        diceImage.startAnimation(rotateAnimation)

        binding.root.postDelayed({
            diceImage.clearAnimation()
            diceImage.visibility = View.GONE

            val resultFragment = ResultFragment.newInstance(selectedOptions)
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .addToBackStack(null)
                .replace(R.id.fragment_container, resultFragment)
                .commit()

        }, 1500)

    }
    private fun inflateHomeFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
