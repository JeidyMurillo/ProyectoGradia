package com.example.gradia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.GradiaTheme
import com.example.gradia.ui.theme.PurpleGradia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.titleLarge,
                        color = PurpleGradia
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PurpleGradia
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFFBF8FF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.terms_last_updated),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            TermsSection(
                title = stringResource(R.string.terms_section1_title),
                content = stringResource(R.string.terms_section1_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section2_title),
                content = stringResource(R.string.terms_section2_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section3_title),
                content = stringResource(R.string.terms_section3_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section4_title),
                content = stringResource(R.string.terms_section4_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section5_title),
                content = stringResource(R.string.terms_section5_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section6_title),
                content = stringResource(R.string.terms_section6_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section7_title),
                content = stringResource(R.string.terms_section7_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section8_title),
                content = stringResource(R.string.terms_section8_body)
            )
            TermsSection(
                title = stringResource(R.string.terms_section9_title),
                content = stringResource(R.string.terms_section9_body)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        ),
        color = PurpleGradia,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF444444)
    )
}

@Preview(showBackground = true)
@Composable
fun TermsAndConditionsScreenPreview() {
    GradiaTheme {
        TermsAndConditionsScreen(onBackClick = {})
    }
}
