package pl.edu.pja.s27599.rssnews.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.edu.pja.s27599.rssnews.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Unauthenticated
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Google Sign-In failed")
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun getGoogleSignInIntent() = authRepository.getGoogleSignInIntent()
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}