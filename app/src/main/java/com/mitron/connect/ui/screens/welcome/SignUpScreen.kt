package com.mitron.connect.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import kotlinx.coroutines.launch
import com.mitron.connect.data.VercelRepository

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val colors = ConnectTheme.colors

    Scaffold(
        topBar = { ConnectTopBar(title = "", onBack = onBack) },
        containerColor = colors.surface1
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.xl)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Text(
                text = "Join Mitron and expand your network",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.xxl)
            )

            if (errorMessage != null) {
                Surface(
                    color = colors.danger.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.lg)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = colors.danger,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Profession / Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = linkedin,
                onValueChange = { linkedin = it },
                label = { Text("LinkedIn URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                label = { Text("Personal/Company Website") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.fillPrimary)
                }
            } else {
                ConnectPrimaryButton(
                    text = "Sign Up",
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val repo = VercelRepository()
                                // Using email as username for now since Vercel API expects username
                                val id = repo.register(email, password, name, email)
                                if (id != null) {
                                    onSignUpSuccess()
                                } else {
                                    errorMessage = "Registration failed on server"
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Registration failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun SignUpScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        SignUpScreen(onSignUpSuccess = {}, onBack = {})
    }
}