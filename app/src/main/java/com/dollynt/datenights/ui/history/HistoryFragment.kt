package com.dollynt.datenights.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.dollynt.datenights.R
import com.dollynt.datenights.adapter.RandomizationResultAdapter
import com.dollynt.datenights.model.RandomizationResult
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.dollynt.datenights.viewmodel.RandomizationResultViewModel

class HistoryFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!

    private lateinit var randomizationResultViewModel: RandomizationResultViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var adapter: RandomizationResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = inflater.inflate(R.layout.fragment_history_placeholder, container, false)

        coupleViewModel = ViewModelProvider(requireActivity())[CoupleViewModel::class.java]
        randomizationResultViewModel = ViewModelProvider(this)[RandomizationResultViewModel::class.java]

        coupleViewModel.couple.value?.id?.let {
            randomizationResultViewModel.fetchRandomizationResults(
                it
            )
        }

        observeHistory()

        return binding
    }

    override fun onResume() {
        super.onResume()
        coupleViewModel.couple.value?.id?.let {
            randomizationResultViewModel.fetchRandomizationResults(
                it
            )
        }

        observeHistory()
    }

    private fun observeHistory() {
        randomizationResultViewModel.randomizationResults.observe(viewLifecycleOwner) { history ->
            updateLayout(history)
        }
    }

    private fun updateLayout(history: List<RandomizationResult>?) {
        val contentFrame = binding.findViewById<ViewGroup>(R.id.historyContentFrame)
        val layoutInflater = LayoutInflater.from(context)

        contentFrame.removeAllViews()

        if (history.isNullOrEmpty()) {
            showNoHistoryLayout(layoutInflater, contentFrame)
        } else {
            showHistoryLayout(layoutInflater, contentFrame, history)
        }
    }

    private fun showHistoryLayout(layoutInflater: LayoutInflater, contentFrame: ViewGroup, history: List<RandomizationResult>) {
        val historyView = layoutInflater.inflate(R.layout.fragment_history, contentFrame, false)
        contentFrame.addView(historyView)

        val recyclerView = historyView.findViewById<RecyclerView>(R.id.recycler_view_history)
        adapter = RandomizationResultAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Preencher o RecyclerView com os dados de histórico
        adapter.submitList(history)
    }

    private fun showNoHistoryLayout(layoutInflater: LayoutInflater, contentFrame: ViewGroup) {
        val noHistoryView = layoutInflater.inflate(R.layout.fragment_no_history, contentFrame, false)
        TransitionManager.beginDelayedTransition(contentFrame)
        contentFrame.addView(noHistoryView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
