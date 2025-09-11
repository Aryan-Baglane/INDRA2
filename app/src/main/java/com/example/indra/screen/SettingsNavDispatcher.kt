package com.example.indra.screen

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SettingsNavDispatcher {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun navigateToOnboarding() {
        _events.tryEmit(Unit)
    }
}


