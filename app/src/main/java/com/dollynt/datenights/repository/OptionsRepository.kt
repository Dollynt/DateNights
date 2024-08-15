package com.dollynt.datenights.repository

import android.content.Context
import com.dollynt.datenights.model.Option
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OptionsRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getOptionsFromTable(coupleId: String): List<Option> {
        return try {
            val optionsList = mutableListOf<Option>()
            val querySnapshot = db.collection("options")
                .whereEqualTo("ownerCouple", coupleId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val option = document.toObject(Option::class.java)
                if (option != null) {
                    optionsList.add(option)
                }
            }
            return optionsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Em caso de erro, retorna uma lista vazia
        }
    }
}
