package com.dollynt.datenights.ui.history

import RandomizationResultViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentHistoryBinding
import com.dollynt.datenights.model.RandomizationResult
import com.dollynt.datenights.ui.home.RandomizationResultAdapter
import com.dollynt.datenights.ui.couple.CoupleViewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var randomizationResultViewModel: RandomizationResultViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var adapter: RandomizationResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        randomizationResultViewModel = ViewModelProvider(this)[RandomizationResultViewModel::class.java]
        coupleViewModel = ViewModelProvider(requireActivity())[CoupleViewModel::class.java]

        setupRecyclerView()

        observeCouple()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = RandomizationResultAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun observeCouple() {
        coupleViewModel.couple.observe(viewLifecycleOwner) { couple ->
            couple?.let {
                randomizationResultViewModel.fetchRandomizationResults(it.id)
            }
        }

        observeHistory()
    }

    private fun observeHistory() {
        randomizationResultViewModel.randomizationResults.observe(viewLifecycleOwner) { history ->
            if (history.isNullOrEmpty()) {
                navigateToNoHistoryFragment()
            } else {
                showHistory(history)
            }
        }
    }

    private fun showHistory(history: List<RandomizationResult>) {
        adapter.submitList(history)
    }

    private fun navigateToNoHistoryFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_history, NoHistoryFragment())
            .addToBackStack(null)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
