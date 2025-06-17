package pl.edu.pja.s27599.rssnews.ui.auth

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task, authViewModel, context)
        } else {
            Toast.makeText(context, "Google Sign-In cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                Toast.makeText(context, "Authentication successful!", Toast.LENGTH_SHORT).show()
                onAuthSuccess()
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isRegisterMode) "Register" else "Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isRegisterMode) {
                    authViewModel.signUpWithEmail(email, password)
                } else {
                    authViewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            Text(text = if (isRegisterMode) "Register" else "Login")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register")
        }
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val signInIntent = authViewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            Text("Sign in with Google")
        }
    }
}

private fun handleSignInResult(task: Task<GoogleSignInAccount>, authViewModel: AuthViewModel, context: Context) {
    try {
        val account = task.getResult(ApiException::class.java)
        account.idToken?.let { idToken ->
            authViewModel.signInWithGoogle(idToken)
        } ?: run {
            Toast.makeText(context, "Google ID Token is null", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ApiException) {
        Toast.makeText(context, "Google sign in failed: ${e.statusCode} ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}