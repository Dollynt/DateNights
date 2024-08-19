package com.dollynt.datenights.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dollynt.datenights.databinding.ItemHistoryRecordBinding
import com.dollynt.datenights.model.RandomizationResult

class RandomizationResultAdapter :
    ListAdapter<RandomizationResult, RandomizationResultAdapter.RandomizationResultViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomizationResultViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RandomizationResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RandomizationResultViewHolder, position: Int) {
        val randomizationResult = getItem(position)
        holder.bind(randomizationResult)
    }

    class RandomizationResultViewHolder(private val binding: ItemHistoryRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(randomizationResult: RandomizationResult) {
            binding.textDate.text = randomizationResult.createdAt
            binding.textResults.text = randomizationResult.results.joinToString(separator = "\n") {
                "• $it"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RandomizationResult>() {
        override fun areItemsTheSame(oldItem: RandomizationResult, newItem: RandomizationResult): Boolean {
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(oldItem: RandomizationResult, newItem: RandomizationResult): Boolean {
            return oldItem == newItem
        }
    }
}
