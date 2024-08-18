package com.dollynt.datenights.model

import java.text.SimpleDateFormat
import java.util.*

data class RandomizationResult(
    val coupleId: String = "",
    val results: List<String> = emptyList(),
    val createdAt: String = getCurrentTimestamp()
) {
    companion object {
        private fun getCurrentTimestamp(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
