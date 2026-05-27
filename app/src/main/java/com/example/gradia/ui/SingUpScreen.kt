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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingUpScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onTermsClick: () -> Unit = {},
    onRegister: (String, String, String) -> Unit = { _, _, _ -> },
    onGoogleSignUp: () -> Unit = {},
    onFacebookSignUp: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    googleMessage: String? = null
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }
    var hasVisitedTerms by rememberSaveable { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val displayError = localError ?: errorMessage

    fun calculatePasswordStrength(pwd: String): Int {
        var score = 0
        if (pwd.length >= 8) score++
        if (pwd.any { it.isUpperCase() }) score++
        if (pwd.any { it.isLowerCase() }) score++
        if (pwd.any { it.isDigit() }) score++
        if (pwd.any { !it.isLetterOrDigit() }) score++
        return score
    }

    fun isStrongPassword(pwd: String): Boolean {
        return pwd.length >= 8 &&
               pwd.any { it.isUpperCase() } &&
               pwd.any { it.isLowerCase() } &&
               pwd.any { it.isDigit() } &&
               pwd.any { !it.isLetterOrDigit() }
    }

    val passwordStrength = calculatePasswordStrength(password)

    fun validate(): String? {
        if (nombre.isBlank()) return "Ingresa tu nombre"
        if (email.isBlank()) return "Ingresa tu correo electrónico"
        if (!email.contains("@") || !email.contains(".")) return "El correo electrónico no es válido"
        if (password.isBlank()) return "Ingresa tu contraseña"
        if (!isStrongPassword(password)) return "La contraseña debe tener mayúsculas, minúsculas, número, carácter especial y al menos 8 caracteres"
        if (!acceptTerms) return "Debes aceptar los términos y condiciones"
        return null
    }

    fun clearError() {
        localError = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                text = "Registrarse",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                color = PurpleGradia,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (googleMessage != null) {
                Text(
                    text = googleMessage,
                    color = PurpleGradia,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PurpleGradia.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SingUpTextField(
                value = nombre,
                onValueChange = { nombre = it; clearError() },
                placeholder = "Nombre"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SingUpTextField(
                value = email,
                onValueChange = { email = it; clearError() },
                placeholder = "Correo Electrónico"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SingUpTextField(
                value = password,
                onValueChange = { password = it; clearError() },
                placeholder = "Contraseña",
                isPassword = true
            )

            if (password.isNotEmpty()) {
                val strengthColor = when (passwordStrength) {
                    5, 4 -> Color(0xFF4CAF50)
                    3 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
                val strengthLabel = when (passwordStrength) {
                    5, 4 -> "Fuerte"
                    3 -> "Media"
                    else -> "Débil"
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = passwordStrength / 5f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(strengthColor)
                    )
                }
                Text(
                    text = "Seguridad: $strengthLabel",
                    color = strengthColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            val annotatedTermsText = remember {
                buildAnnotatedString {
                    withStyle(SpanStyle(
                        color = Color.Gray,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp
                    )) {
                        append("He leído y acepto los ")
                    }
                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                    withStyle(SpanStyle(
                        color = PurpleGradia,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append("Términos y Condiciones.")
                    }
                    pop()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it; clearError() },
                        enabled = hasVisitedTerms,
                        colors = CheckboxDefaults.colors(checkedColor = PurpleGradia)
                    )
                    if (!hasVisitedTerms) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Primero lee los Términos y Condiciones"
                                        )
                                    }
                                }
                        )
                    }
                }
                ClickableText(
                    text = annotatedTermsText,
                    modifier = Modifier.weight(1f),
                    onClick = { offset ->
                        annotatedTermsText
                            .getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                            .firstOrNull()?.let {
                                hasVisitedTerms = true
                                onTermsClick()
                            }
                    }
                )
            }

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

            SingUpPrimaryButton(
                text = "Registrarse",
                onClick = {
                    val error = validate()
                    if (error != null) {
                        localError = error
                    } else {
                        onRegister(email, password, nombre)
                    }
                },
                enabled = acceptTerms && !isLoading
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
                text = "Registrarse con:",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialIconSingUp(
                    painter = painterResource(R.drawable.ic_google),
                    onClick = onGoogleSignUp
                )
                SocialIconSingUp(
                    painter = painterResource(R.drawable.ic_facebook),
                    onClick = onFacebookSignUp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¿Ya estás registrado?",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val loginInteractionSource = remember { MutableInteractionSource() }
            val isLoginPressed by loginInteractionSource.collectIsPressedAsState()
            val loginScale by animateFloatAsState(
                targetValue = if (isLoginPressed) 0.95f else 1f,
                label = "LoginButtonScale"
            )

            Button(
                onClick = onLoginClick,
                interactionSource = loginInteractionSource,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .scale(loginScale)
                    .border(1.dp, GrayBorder, RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SocialIconBg,
                    contentColor = PurpleGradia
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SingUpTextField(
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
fun SingUpPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
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
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = PurpleGradia,
            disabledContainerColor = PurpleGradia.copy(alpha = 0.4f)
        ),
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
fun SocialIconSingUp(
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
fun SingUpScreenPreview() {
    GradiaTheme {
        SingUpScreen(onBackClick = {}, onLoginClick = {})
    }
}
