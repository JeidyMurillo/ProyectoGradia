package com.example.gradia.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogin: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onGoogleSignIn: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayError = localError ?: errorMessage

    fun validate(): String? {
        if (email.isBlank()) return "Ingresa tu correo electrónico"
        if (!email.contains("@") || !email.contains(".")) return "El correo electrónico no es válido"
        if (password.isBlank()) return "Ingresa tu contraseña"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    fun clearError() {
        localError = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .background(PurpleGradia, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Logo Gradia",
                modifier = Modifier.size(140.dp)
            )

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = PurpleGradia,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LoginTextField(
                value = email,
                onValueChange = { email = it; clearError() },
                placeholder = "Correo Electrónico"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginTextField(
                value = password,
                onValueChange = { password = it; clearError() },
                placeholder = "Contraseña",
                isPassword = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = PurpleGradia)
                )
                Text(
                    text = "Recuérdame",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                )
            }

            Text(
                text = "Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium
                ),
                color = PurpleGradia,
                modifier = Modifier.clickable { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (displayError != null) {
                Text(
                    text = displayError,
                    color = Color(0xFFB00020),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF0F0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(color = PurpleGradia, modifier = Modifier.padding(16.dp))
            }

            LoginPrimaryButton(
                text = "Inicia Sesión",
                onClick = {
                    val error = validate()
                    if (error != null) {
                        localError = error
                    } else {
                        onLogin(email, password, rememberMe)
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrayBorder)
                Text(
                    text = " o ",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrayBorder)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Inicia sesión con:",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialIconLogin(painterResource(R.drawable.ic_github))
                SocialIconLogin(
                    painter = painterResource(R.drawable.ic_google),
                    onClick = onGoogleSignIn
                )
                SocialIconLogin(painterResource(R.drawable.ic_facebook))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Aún no estas registrado?",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val registerInteractionSource = remember { MutableInteractionSource() }
            val isRegisterPressed by registerInteractionSource.collectIsPressedAsState()
            val registerScale by animateFloatAsState(
                targetValue = if (isRegisterPressed) 0.95f else 1f,
                label = "RegisterButtonScale"
            )

            Button(
                onClick = onRegisterClick,
                interactionSource = registerInteractionSource,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .scale(registerScale)
                    .border(1.dp, GrayBorder, RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SocialIconBg,
                    contentColor = PurpleGradia
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Registrate",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = GrayText) },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorder, RoundedCornerShape(25.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = GrayTextField,
            unfocusedContainerColor = GrayTextField,
            disabledContainerColor = GrayTextField,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(25.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        singleLine = true
    )
}

@Composable
fun LoginPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "btnScale")

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(50.dp)
            .scale(if (enabled) scale else 1f),
        colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
        shape = RoundedCornerShape(25.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SocialIconLogin(
    painter: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(SocialIconBg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    GradiaTheme {
        LoginScreen(onBackClick = {}, onRegisterClick = {}, onLogin = { _, _, _ -> })
    }
}
