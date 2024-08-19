package com.dollynt.datenights.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.repository.OptionsRepository
import com.dollynt.datenights.repository.RandomizationResultRepository


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val optionsRepository = OptionsRepository(application)
    private val resultsRepository = RandomizationResultRepository()

    private val _options = MutableLiveData<List<Option>>()
    val options: LiveData<List<Option>> get() = _options

    suspend fun fetchOptions(coupleId: String) {
        _options.value = optionsRepository.getOptionsFromTable(coupleId)
    }
}
