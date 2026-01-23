package com.example.sonicflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application pour SonicFlow
 * Initialise Hilt pour l'injection de dépendances
 */
@HiltAndroidApp
class SonicFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialiser les composants nécessaires au démarrage de l'app
        initializeComponents()
    }

    private fun initializeComponents() {
        // TODO: Initialiser les composants globaux si nécessaire
        // Ex: Timber pour le logging, WorkManager, etc.
    }
}