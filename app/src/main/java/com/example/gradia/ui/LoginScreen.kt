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
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Box para el botón de volver respetando el Safe Area (Status Bar)
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
            // Logo (Usamos splash_logo que es el morado)
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Logo Gradia",
                modifier = Modifier.size(140.dp)
            )

            // Título con Martini (Definida en Type.kt)
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = PurpleGradia,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Inputs con Inter
            LoginTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo Electrónico"
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                isPassword = true
            )

            // Fila de Recuérdame y Olvidaste contraseña
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Principal con animación
            LoginPrimaryButton(text = "Inicia Sesión", onClick = onLoginSuccess)

            Spacer(modifier = Modifier.height(32.dp))

            // Divisor "o"
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

            // Social Buttons (Placeholders)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialIconLogin(painterResource(R.drawable.ic_github)) // Reemplazar por GitHub
                SocialIconLogin(painterResource(R.drawable.ic_google)) // Reemplazar por Google
                SocialIconLogin(painterResource(R.drawable.ic_facebook)) // Reemplazar por Facebook
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Aún no estas registrado?",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Registro secundario
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
fun LoginPrimaryButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "btnScale")

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(50.dp)
            .scale(scale),
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
fun SocialIconLogin(painter: androidx.compose.ui.graphics.painter.Painter) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(SocialIconBg, CircleShape)
            .clickable { },
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
        LoginScreen(onBackClick = {}, onRegisterClick = {}, onLoginSuccess = {})
    }
}