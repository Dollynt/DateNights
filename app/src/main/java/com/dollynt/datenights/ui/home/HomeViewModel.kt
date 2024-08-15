package com.dollynt.datenights.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dollynt.datenights.model.Option
import com.dollynt.datenights.repository.OptionsRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OptionsRepository(application)

    private val _options = MutableLiveData<List<Option>>()
    val options: LiveData<List<Option>> get() = _options

    suspend fun fetchOptions(coupleId: String) {
        _options.value = repository.getOptionsFromTable(coupleId)
    }
}
