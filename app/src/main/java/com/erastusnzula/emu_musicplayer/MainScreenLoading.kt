package com.erastusnzula.emu_musicplayer
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenLoading:ViewModel() {
    private val loading = MutableStateFlow(true)
    var isLoading = loading.asStateFlow()
    init {
        viewModelScope.launch{
            delay(500)
            loading.value=false
        }
    }

}