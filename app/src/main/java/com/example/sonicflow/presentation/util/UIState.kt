package com.example.sonicflow.presentation.util

/**
 * Classe scellée représentant les différents états de l'UI
 * Utilisée pour gérer les états de chargement, succès et erreur
 */
sealed class UIState<out T> {
    /**
     * État initial ou idle
     */
    data object Idle : UIState<Nothing>()

    /**
     * État de chargement
     */
    data object Loading : UIState<Nothing>()

    /**
     * État de succès avec données
     */
    data class Success<T>(val data: T) : UIState<T>()

    /**
     * État d'erreur avec message
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UIState<Nothing>()

    /**
     * Vérifie si l'état est Loading
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Vérifie si l'état est Success
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Vérifie si l'état est Error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Récupère les données si Success, sinon null
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Récupère le message d'erreur si Error, sinon null
     */
    fun getErrorOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }
}

/**
 * Extension pour transformer UIState avec une fonction
 */
inline fun <T, R> UIState<T>.map(transform: (T) -> R): UIState<R> {
    return when (this) {
        is UIState.Success -> UIState.Success(transform(data))
        is UIState.Error -> UIState.Error(message, throwable)
        is UIState.Loading -> UIState.Loading
        is UIState.Idle -> UIState.Idle
    }
}

/**
 * Extension pour exécuter du code sur Success
 */
inline fun <T> UIState<T>.onSuccess(action: (T) -> Unit): UIState<T> {
    if (this is UIState.Success) {
        action(data)
    }
    return this
}

/**
 * Extension pour exécuter du code sur Error
 */
inline fun <T> UIState<T>.onError(action: (String) -> Unit): UIState<T> {
    if (this is UIState.Error) {
        action(message)
    }
    return this
}

/**
 * Extension pour exécuter du code sur Loading
 */
inline fun <T> UIState<T>.onLoading(action: () -> Unit): UIState<T> {
    if (this is UIState.Loading) {
        action()
    }
    return this
}
