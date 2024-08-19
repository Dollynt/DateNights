package com.dollynt.datenights.ui.home

import com.dollynt.datenights.viewmodel.RandomizationResultViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.model.RandomizationResult
import com.dollynt.datenights.ui.couple.CoupleViewModel

class ResultFragment : Fragment() {

    companion object {
        private const val ARG_SELECTED_OPTIONS = "selected_options"

        fun newInstance(selectedOptions: List<Option>): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putSerializable(ARG_SELECTED_OPTIONS, ArrayList(selectedOptions))
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var randomizationResultViewModel: RandomizationResultViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private var currentResult: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        val backIcon = view.findViewById<ImageView>(R.id.back_icon)

        backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        randomizationResultViewModel = ViewModelProvider(requireActivity())[RandomizationResultViewModel::class.java]
        coupleViewModel = ViewModelProvider(requireActivity())[CoupleViewModel::class.java]

        val selectedOptions = arguments?.getSerializable(ARG_SELECTED_OPTIONS) as? List<Option>

        randomizeOptions(selectedOptions, view)

        val btnSatisfied = view.findViewById<Button>(R.id.btn_confirm)
        val btnNotSatisfied = view.findViewById<Button>(R.id.btn_randomize_again)

        btnSatisfied.setOnClickListener {
            saveRandomizationResult()
            inflateHomeFragment()
        }

        btnNotSatisfied.setOnClickListener {
            val diceImage = view.findViewById<ImageView>(R.id.dice_image)
            diceImage.visibility = View.VISIBLE

            val rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation)
            diceImage.startAnimation(rotateAnimation)

            view.postDelayed({
                diceImage.clearAnimation()
                diceImage.visibility = View.GONE

                randomizeOptions(selectedOptions, view)
            }, 1500)
        }

        return view
    }

    private fun randomizeOptions(selectedOptions: List<Option>?, view: View) {
        currentResult = selectedOptions?.map { option ->
            val randomSubOption = option.subOptions.randomOrNull() ?: "Nenhuma opção selecionada"
            "${option.name}: $randomSubOption"
        } ?: listOf("Nenhuma opção selecionada")

        val resultMessage = currentResult!!.joinToString(separator = "\n")
        val resultText = view.findViewById<TextView>(R.id.result_text)
        resultText.text = resultMessage
    }

    private fun saveRandomizationResult() {
        val coupleId = coupleViewModel.couple.value?.id ?: return
        val randomizationResult = RandomizationResult(coupleId, currentResult!!)
        randomizationResultViewModel.saveRandomizationResult(randomizationResult)
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
}
