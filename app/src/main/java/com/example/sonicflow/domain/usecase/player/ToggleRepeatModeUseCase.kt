package com.example.sonicflow.domain.usecase.player

import com.example.sonicflow.domain.model.RepeatMode
import javax.inject.Inject

class ToggleRepeatModeUseCase @Inject constructor() {
    /**
     * Cycle vers le mode de répétition suivant
     * OFF -> ONE -> ALL -> OFF
     */
    operator fun invoke(currentMode: RepeatMode): RepeatMode {
        return currentMode.next()
    }
}
