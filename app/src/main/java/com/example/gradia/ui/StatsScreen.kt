package com.example.gradia.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.presentation.viewmodel.StatsUiState
import com.example.gradia.presentation.viewmodel.StatsViewModel
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

@Composable
fun StatsScreen(
    statsViewModel: StatsViewModel,
    onNavigateToPerformance: () -> Unit = {}
) {
    val state by statsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (state.hasData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AverageCard(
                    average = state.generalAverage,
                    trend = state.generalTrend,
                    modifier = Modifier.weight(1.5f)
                )
                CreditsCard(
                    creditsApproved = state.totalCreditsApproved,
                    totalCredits = state.totalCredits,
                    modifier = Modifier.weight(1f)
                )
            }

            DistributionChartCard(
                excellentCount = state.excellentCount,
                approvedCount = state.approvedCount,
                regularCount = state.regularCount,
                failedCount = state.failedCount,
                totalItems = state.totalGradedItems
            )

            RecentMilestonesCard(
                bestSubjectName = state.bestSubjectName,
                bestSubjectAverage = state.bestSubjectAverage,
                bestGradeName = state.bestGradeName,
                bestGradeValue = state.bestGradeValue
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (state.totalSubjects == 0) stringResource(R.string.stats_no_subjects)
                    else stringResource(R.string.stats_add_grades_hint),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily
                    )
                )
            }
        }

        CTAButton(onNavigateToPerformance = onNavigateToPerformance)

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun AverageCard(
    average: Double,
    trend: Double,
    modifier: Modifier = Modifier
) {
    val trendText = if (trend >= 0) "+%.1f".format(trend) else "%.1f".format(trend)
    val trendColor = if (trend >= 0) Color(0xFF34C759) else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier
            .border(2.dp, PurpleGradia, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                stringResource(R.string.stats_general_average),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = PurpleGradia,
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "%.1f".format(average),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleGradia,
                        fontSize = 32.sp,
                        fontFamily = InterFontFamily
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        trendText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = trendColor,
                            fontFamily = InterFontFamily
                        ),
                        modifier = Modifier.offset(x = 25.dp, y = 12.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_average),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = trendColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditsCard(
    creditsApproved: Int,
    totalCredits: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                stringResource(R.string.stats_credits),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "$creditsApproved",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleGradia,
                        fontSize = 32.sp,
                        fontFamily = InterFontFamily
                    )
                )
                Text(
                    " / $totalCredits",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily
                    )
                )
            }
        }
    }
}

@Composable
private fun DistributionChartCard(
    excellentCount: Int,
    approvedCount: Int,
    regularCount: Int,
    failedCount: Int,
    totalItems: Int
) {
    val total = excellentCount + approvedCount + regularCount + failedCount
    val ePct = if (total > 0) excellentCount.toFloat() / total else 0f
    val aPct = if (total > 0) approvedCount.toFloat() / total else 0f
    val rPct = if (total > 0) regularCount.toFloat() / total else 0f
    val fPct = if (total > 0) failedCount.toFloat() / total else 0f

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.stats_grade_distribution),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    percentages = listOf(ePct, aPct, rPct, fPct),
                    totalItems = totalItems
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(
                        color = Color(0xFF4CAF50),
                        label = stringResource(R.string.stats_excellent, excellentCount)
                    )
                    LegendItem(
                        color = Color(0xFF81C784),
                        label = stringResource(R.string.stats_approved, approvedCount)
                    )
                    LegendItem(
                        color = Color(0xFFFFF176),
                        label = stringResource(R.string.stats_regular, regularCount)
                    )
                    LegendItem(
                        color = Color(0xFFEF9A9A),
                        label = stringResource(R.string.stats_failed, failedCount)
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    percentages: List<Float>,
    totalItems: Int
) {
    val animatedSweep by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "donut"
    )

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            val colors = listOf(
                Color(0xFF4CAF50),
                Color(0xFF81C784),
                Color(0xFFFFF176),
                Color(0xFFEF9A9A)
            )

            var startAngle = -90f

            percentages.forEachIndexed { index, percentage ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = 360f * percentage * animatedSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += 360f * percentage
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$totalItems",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontFamily = InterFontFamily
                )
            )
            Text(
                stringResource(R.string.stats_grades),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = InterFontFamily
                )
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = InterFontFamily
            )
        )
    }
}

@Composable
private fun RecentMilestonesCard(
    bestSubjectName: String,
    bestSubjectAverage: Double,
    bestGradeName: String,
    bestGradeValue: Double
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                stringResource(R.string.stats_milestones),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.statistics_icon),
                    contentDescription = "Estadísticas",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if (bestSubjectName.isNotEmpty()) {
                        Text(
                            stringResource(R.string.stats_best_average, bestSubjectName),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = InterFontFamily
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (bestGradeName.isNotEmpty()) {
                        Text(
                            stringResource(R.string.stats_best_grade, "%.1f".format(bestGradeValue), bestGradeName),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = InterFontFamily
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CTAButton(onNavigateToPerformance: () -> Unit = {}) {
    Button(
        onClick = onNavigateToPerformance,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia)
    ) {
        Text(
            stringResource(R.string.stats_view_performance),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = InterFontFamily
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}