package com.dollynt.datenights.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dollynt.datenights.R
import com.dollynt.datenights.model.Option

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        val selectedOptions = arguments?.getSerializable(ARG_SELECTED_OPTIONS) as? List<Option>

        val resultMessage = selectedOptions?.joinToString(separator = "\n") { option ->
            val randomSubOption = option.subOptions.randomOrNull() ?: "Nenhuma opção selecionada"
            "${option.name}: $randomSubOption"
        } ?: "Nenhuma opção selecionada"

        val resultText = view.findViewById<TextView>(R.id.result_text)
        resultText.text = resultMessage

        val btnSatisfied = view.findViewById<Button>(R.id.btn_confirm)
        val btnNotSatisfied = view.findViewById<Button>(R.id.btn_randomize_again)

        btnSatisfied.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        btnNotSatisfied.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
