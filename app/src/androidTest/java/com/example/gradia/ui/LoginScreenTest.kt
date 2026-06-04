package com.example.gradia.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.gradia.ui.theme.GradiaTheme
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysTitle() {
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysEmailAndPasswordFields() {
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Correo Electrónico").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Inicia Sesión").assertIsDisplayed()
    }

    @Test
    fun loginScreen_displaysRegisterLink() {
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Registrate").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsErrorOnEmptyFields_whenLoginClicked() {
        var loginCalled = false
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {},
                    onLogin = { _, _, _ -> loginCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Inicia Sesión").performClick()

        composeTestRule.onNodeWithText("Ingresa tu correo electrónico").assertIsDisplayed()
    }

    @Test
    fun loginScreen_callsOnLogin_withCredentials() {
        var capturedEmail = ""
        var capturedPassword = ""
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {},
                    onLogin = { email, password, _ ->
                        capturedEmail = email
                        capturedPassword = password
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Correo Electrónico").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("password123")
        composeTestRule.onNodeWithText("Inicia Sesión").performClick()

        assert(capturedEmail == "test@example.com") { "Email should be test@example.com but was $capturedEmail" }
        assert(capturedPassword == "password123") { "Password should be password123 but was $capturedPassword" }
    }

    @Test
    fun loginScreen_callsOnRegisterClick() {
        var registerClicked = false
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = { registerClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Registrate").performClick()
        assert(registerClicked) { "Register click should be called" }
    }

    @Test
    fun loginScreen_showsErrorMessage_whenProvided() {
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {},
                    errorMessage = "Correo o contraseña incorrectos"
                )
            }
        }

        composeTestRule.onNodeWithText("Correo o contraseña incorrectos").assertIsDisplayed()
    }

    @Test
    fun loginScreen_callsOnForgotPassword() {
        var forgotClicked = false
        composeTestRule.setContent {
            GradiaTheme {
                LoginScreen(
                    onBackClick = {},
                    onRegisterClick = {},
                    onForgotPassword = { forgotClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Olvidaste tu contraseña?").performClick()
        assert(forgotClicked) { "Forgot password click should be called" }
    }
}
