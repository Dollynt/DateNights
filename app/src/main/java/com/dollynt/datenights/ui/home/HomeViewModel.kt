package com.dollynt.datenights.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData para armazenar as opções de randomização
    private val _randomizationOptions = MutableLiveData<List<String>>()
    val randomizationOptions: LiveData<List<String>> = _randomizationOptions

    // LiveData para armazenar a opção selecionada
    private val _selectedOption = MutableLiveData<String>()
    val selectedOption: LiveData<String> = _selectedOption

    init {
        // Inicializa as opções de randomização com valores padrão
        _randomizationOptions.value = listOf("What to Eat", "What to Do", "What to Play")
    }

    // Função para atualizar as opções de randomização
    fun updateRandomizationOptions(options: List<String>) {
        _randomizationOptions.value = options
    }

    // Função para definir a opção selecionada
    fun selectOption(option: String) {
        _selectedOption.value = option
    }

    // Função para randomizar uma opção da lista
    fun randomizeOption() {
        viewModelScope.launch {
            _randomizationOptions.value?.let { options ->
                if (options.isNotEmpty()) {
                    val randomOption = options.random()
                    _selectedOption.value = randomOption
                }
            }
        }
    }
}
