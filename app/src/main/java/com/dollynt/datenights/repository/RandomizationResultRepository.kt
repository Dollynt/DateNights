package com.dollynt.datenights.repository

import com.dollynt.datenights.model.RandomizationResult
import com.google.firebase.firestore.FirebaseFirestore

class RandomizationResultRepository {

    private val db = FirebaseFirestore.getInstance()
    private val randomizationResultsCollection = db.collection("randomization_results")

    fun saveRandomizationResult(randomizationResult: RandomizationResult) {
        randomizationResultsCollection.add(randomizationResult)
            .addOnSuccessListener {
                // Sucesso ao salvar o resultado
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
