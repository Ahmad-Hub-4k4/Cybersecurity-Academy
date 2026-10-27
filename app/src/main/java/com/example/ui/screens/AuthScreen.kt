package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.AuthenticatedUser
import com.example.ui.components.CyberCard
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.CyberCyanSecondary
import com.example.ui.theme.CyberNavyBackground
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyElevated
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondarySlate

@Composable
fun AuthScreen(
    activeUser: AuthenticatedUser?,
    registeredUsersCount: Int,
    authErrorMessage: String?,
    onSignUp: (email: String, password: String, displayName: String) -> Unit,
    onLogIn: (email: String, password: String) -> Unit,
    onLogOut: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by rememberSaveable { mutableStateOf(registeredUsersCount == 0) }
    var displayName by rememberSaveable { mutableStateOf("Waqas Ahmad") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyberCyanPrimary,
        unfocusedBorderColor = CyberNavyBorder,
        focusedLabelColor = CyberCyanPrimary,
        unfocusedLabelColor = TextSecondarySlate,
        focusedTextColor = TextPrimaryWhite,
        unfocusedTextColor = TextPrimaryWhite
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyBackground)
            .testTag("auth_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberCard(borderColor = CyberCyanPrimary.copy(alpha = 0.55f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCyanPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Authentication",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Waqas Cyber Academy 2.0",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryWhite
                            )
                            Text(
                                text = "Secure Local User Authentication",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyanPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_auth_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Return to Dashboard",
                            tint = TextSecondarySlate
                        )
                    }
                }

                if (activeUser != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusPresentGreen.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, StatusPresentGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Signed in as ${activeUser.displayName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = StatusPresentGreen,
                                    modifier = Modifier.testTag("auth_active_user_text")
                                )
                                Text(
                                    text = activeUser.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryWhite
                                )
                            }
                            OutlinedButton(
                                onClick = onLogOut,
                                border = BorderStroke(1.dp, StatusAbsentRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("auth_logout_button")
                            ) {
                                Text("Log Out", color = StatusAbsentRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Switcher: Sign Up vs Log In
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = { isSignUpMode = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSignUpMode) CyberCyanPrimary.copy(alpha = 0.2f) else CyberNavySurface,
                        border = BorderStroke(
                            width = if (isSignUpMode) 2.dp else 1.dp,
                            color = if (isSignUpMode) CyberCyanPrimary else CyberNavyBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("auth_mode_signup")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSignUpMode) CyberCyanPrimary else TextSecondarySlate
                            )
                        }
                    }

                    Surface(
                        onClick = { isSignUpMode = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isSignUpMode) CyberCyanPrimary.copy(alpha = 0.2f) else CyberNavySurface,
                        border = BorderStroke(
                            width = if (!isSignUpMode) 2.dp else 1.dp,
                            color = if (!isSignUpMode) CyberCyanPrimary else CyberNavyBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("auth_mode_login")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Log In",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (!isSignUpMode) CyberCyanPrimary else TextSecondarySlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!authErrorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StatusAbsentRed.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, StatusAbsentRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authErrorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusAbsentRed,
                            modifier = Modifier
                                .padding(12.dp)
                                .testTag("auth_error_text")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("waqas@cyberacademy.local") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 characters)") },
                    singleLine = true,
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = CyberCyanSecondary
                            )
                        }
                    },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            onSignUp(email, password, displayName)
                        } else {
                            onLogIn(email, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyanPrimary,
                        contentColor = CyberNavyBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag(if (isSignUpMode) "auth_signup_submit_button" else "auth_login_submit_button")
                ) {
                    Icon(
                        imageVector = if (isSignUpMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSignUpMode) "Sign Up & Create Secure Account" else "Log In to Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberNavyElevated.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Cryptographic Storage Specification",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passwords are never stored in plaintext. Credentials are salted with 128-bit SecureRandom entropy and derived via PBKDF2WithHmacSHA256 (65,536 iterations, 256-bit output) in Room SQLite ('user_accounts').",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }
        }
    }
}
