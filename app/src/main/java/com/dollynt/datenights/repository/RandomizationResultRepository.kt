package com.dollynt.datenights.repository

import com.dollynt.datenights.model.RandomizationResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

    fun getRandomizationResultsForCouple(coupleId: String, onSuccess: (List<RandomizationResult>) -> Unit, onFailure: (Exception) -> Unit) {
        randomizationResultsCollection
            .whereEqualTo("coupleId", coupleId)
            .get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.toObjects(RandomizationResult::class.java)
                onSuccess(results)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
