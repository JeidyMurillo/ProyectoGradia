package com.example.gradia.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.util.Log
import kotlin.math.roundToInt
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.domain.model.Subject
import com.example.gradia.presentation.viewmodel.GradeSort
import com.example.gradia.presentation.viewmodel.NotesViewModel
import com.example.gradia.presentation.viewmodel.NotificationsViewModel
import com.example.gradia.presentation.viewmodel.SubjectSort
import com.example.gradia.presentation.viewmodel.TasksViewModel
import com.example.gradia.ui.theme.*
import java.util.Locale
import com.example.gradia.ui.AccountScreen
import com.example.gradia.ui.ProfileScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkMode: Boolean = false,
    userName: String = "",
    userEmail: String = ""
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var previousTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableStateOf("Home") }
    var isQuickAddOpen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<com.example.gradia.domain.model.Category?>(null) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedSubjectName by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val app = LocalContext.current.applicationContext as GradiaApplication
    val context = LocalContext.current
    val currentUserId = app.authRepository.getCurrentUserId() ?: ""
    val notesViewModel = remember(currentUserId) { app.provideNotesViewModel(currentUserId) }
    val notesState by notesViewModel.uiState.collectAsState()
    val tasksViewModel = remember { app.provideTasksViewModel() }
    val tasksState by tasksViewModel.uiState.collectAsState()
    val calendarViewModel = remember { app.provideCalendarViewModel() }
    val calendarState by calendarViewModel.uiState.collectAsState()
    val statsViewModel = remember { app.provideStatsViewModel() }
    val statsState by statsViewModel.uiState.collectAsState()
    val notificationsViewModel = remember { app.provideNotificationsViewModel() }
    val notificationsState by notificationsViewModel.uiState.collectAsState()
    val subjectsViewModel = remember { app.provideSubjectsViewModel() }
    val subjectsState by subjectsViewModel.uiState.collectAsState()
    // ViewModel del detalle de asignatura, izado para poder ordenar sus notas
    // desde el menú de la barra superior (tab 9).
    val subjectDetailViewModel = remember(selectedSubjectId) {
        selectedSubjectId?.let { app.provideSubjectDetailViewModel(it) }
    }

    var showOnboarding by remember { mutableStateOf(false) }
    var onboardingCareer by remember { mutableStateOf("") }
    var onboardingSemestre by remember { mutableStateOf("") }
    var onboardingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d("Onboarding", "Iniciando carga de usuario, currentUserId=$currentUserId")
        val user = app.userRepository.getUserById(currentUserId).first()
        Log.d("Onboarding", "Usuario obtenido: $user")
        Log.d("Onboarding", "semestre='${user?.semestre}', isBlank=${user?.semestre?.isBlank()}")
        if (user != null && user.semestre.isBlank()) {
            showOnboarding = true
            onboardingCareer = user.carrera
            Log.d("Onboarding", "Dialog mostrado correctamente")
        } else {
            Log.d("Onboarding", "NO se muestra dialog. user=null? ${user == null}")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color(0xCC453284), // #453284 with 80% visibility
        drawerContent = {
            GradiaDrawerContent(
                selectedItem = selectedDrawerItem,
                onClose = { scope.launch { drawerState.close() } },
                userName = userName,
                userEmail = userEmail,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                onItemClick = { item ->
                    if (item == "Log Out") {
                        onLogout()
                    } else if (item == "Ajustes") {
                        previousTab = selectedTab
                        selectedTab = 7 // New index for Settings
                        selectedDrawerItem = item
                        scope.launch { drawerState.close() }
                    } else if (item == "Perfil") {
                        previousTab = selectedTab
                        selectedTab = 8 // New index for Profile
                        selectedDrawerItem = item
                        scope.launch { drawerState.close() }
                    } else if (item == "Estadísticas") {
                        previousTab = selectedTab
                        selectedTab = 10 // New index for Stats
                        selectedDrawerItem = item
                        scope.launch { drawerState.close() }
                    } else {
                        selectedDrawerItem = item
                        scope.launch { drawerState.close() }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                 when (selectedTab) {
                                     0 -> stringResource(R.string.tab_home)
                                     1 -> stringResource(R.string.tab_subjects)
                                     2 -> stringResource(R.string.tab_add)
                                     3 -> stringResource(R.string.tab_final_grade)
                                     4 -> stringResource(R.string.tab_calendar)
                                    5 -> stringResource(R.string.tab_notes)
                                    6 -> stringResource(R.string.tab_tasks)
                                    7 -> stringResource(R.string.tab_settings)
                                    8 -> stringResource(R.string.tab_profile)
                                    9 -> selectedSubjectName.ifBlank { stringResource(R.string.tab_subject) }
                                    10 -> stringResource(R.string.tab_statistics)
                                    12 -> stringResource(R.string.tab_performance)
                                    13 -> stringResource(R.string.tab_notifications)
                                    else -> stringResource(R.string.app_name)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        navigationIcon = {
                            if (selectedTab in 5..7 || selectedTab == 9 || selectedTab == 12 || selectedTab == 13) {
                                IconButton(onClick = { selectedTab = previousTab }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = PurpleGradia,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = PurpleGradia,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            if (selectedTab == 6) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { showAchievementsDialog = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "\uD83C\uDFC6",
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${tasksState.tareasCompletadas.size}",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleGradia,
                                                fontFamily = InterFontFamily
                                            )
                                        )
                                    }
                                }
                            } else if (selectedTab in setOf(1, 5, 7, 9)) {
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More",
                                            tint = PurpleGradia,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        if (selectedTab == 6 && tasksState.selectedTaskIds.isNotEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.menu_delete_selected, tasksState.selectedTaskIds.size), color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showMenu = false
                                                    tasksViewModel.deleteSelectedTasks()
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                                }
                                            )
                                            HorizontalDivider()
                                        }
                                        if (selectedTab == 5) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.menu_manage_categories)) },
                                                onClick = {
                                                    showMenu = false
                                                    showCategoryManager = true
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Edit, contentDescription = null)
                                                }
                                            )
                                        }
                                        if (selectedTab == 1) {
                                            Text(
                                                text = stringResource(R.string.menu_sort_by),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontFamily = InterFontFamily,
                                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                            )
                                            SubjectSortMenuItem(
                                                label = stringResource(R.string.sort_name_az),
                                                value = SubjectSort.NOMBRE,
                                                selected = subjectsState.sort,
                                                onClick = {
                                                    subjectsViewModel.onSortChange(SubjectSort.NOMBRE)
                                                    showMenu = false
                                                }
                                            )
                                            SubjectSortMenuItem(
                                                label = stringResource(R.string.sort_semester),
                                                value = SubjectSort.SEMESTRE,
                                                selected = subjectsState.sort,
                                                onClick = {
                                                    subjectsViewModel.onSortChange(SubjectSort.SEMESTRE)
                                                    showMenu = false
                                                }
                                            )
                                            SubjectSortMenuItem(
                                                label = stringResource(R.string.sort_credits_desc),
                                                value = SubjectSort.CREDITOS,
                                                selected = subjectsState.sort,
                                                onClick = {
                                                    subjectsViewModel.onSortChange(SubjectSort.CREDITOS)
                                                    showMenu = false
                                                }
                                            )
                                            SubjectSortMenuItem(
                                                label = stringResource(R.string.sort_average_desc),
                                                value = SubjectSort.PROMEDIO,
                                                selected = subjectsState.sort,
                                                onClick = {
                                                    subjectsViewModel.onSortChange(SubjectSort.PROMEDIO)
                                                    showMenu = false
                                                }
                                            )
                                        }
                                        if (selectedTab == 9 && subjectDetailViewModel != null) {
                                            val detailState by subjectDetailViewModel.uiState.collectAsState()
                                            Text(
                                                text = stringResource(R.string.sort_grades_header),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontFamily = InterFontFamily,
                                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                            )
                                            SortCheckMenuItem(
                                                label = stringResource(R.string.sort_default),
                                                isSelected = detailState.sort == GradeSort.PREDETERMINADO,
                                                onClick = {
                                                    subjectDetailViewModel.onGradeSortChange(GradeSort.PREDETERMINADO)
                                                    showMenu = false
                                                }
                                            )
                                            SortCheckMenuItem(
                                                label = stringResource(R.string.sort_grade_desc),
                                                isSelected = detailState.sort == GradeSort.NOTA,
                                                onClick = {
                                                    subjectDetailViewModel.onGradeSortChange(GradeSort.NOTA)
                                                    showMenu = false
                                                }
                                            )
                                            SortCheckMenuItem(
                                                label = stringResource(R.string.sort_weight_desc),
                                                isSelected = detailState.sort == GradeSort.PESO,
                                                onClick = {
                                                    subjectDetailViewModel.onGradeSortChange(GradeSort.PESO)
                                                    showMenu = false
                                                }
                                            )
                                            SortCheckMenuItem(
                                                label = stringResource(R.string.sort_pending_first),
                                                isSelected = detailState.sort == GradeSort.PENDIENTES,
                                                onClick = {
                                                    subjectDetailViewModel.onGradeSortChange(GradeSort.PENDIENTES)
                                                    showMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.padding(end = 8.dp)) {
                                    IconButton(
                                        onClick = {
                                            previousTab = selectedTab
                                            selectedTab = 13
                                        },
                                        modifier = Modifier
.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = PurpleGradia,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    if (notificationsState.totalCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-2).dp, y = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    GradiaBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            if (it == 2) {
                                isQuickAddOpen = !isQuickAddOpen
                            } else {
                                if (it != selectedTab) {
                                    previousTab = selectedTab
                                }
                                selectedTab = it
                                isQuickAddOpen = false
                            }
                        },
                        isQuickAddOpen = isQuickAddOpen
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        0 -> HomeContent(
                            onSubjectClick = { subject ->
                                selectedSubjectId = subject.id
                                selectedSubjectName = subject.name
                                previousTab = selectedTab
                                selectedTab = 9
                            }
                        )
                        1 -> SubjectsScreen(
                            externalViewModel = subjectsViewModel,
                            onSubjectClick = { subject ->
                                selectedSubjectId = subject.id
                                selectedSubjectName = subject.name
                                previousTab = selectedTab
                                selectedTab = 9
                            }
                        )
                        3 -> FinalGradeScreen()
                        4 -> CalendarScreen(viewModel = calendarViewModel)
                        5 -> NotesScreen(viewModel = notesViewModel)
                        6 -> TasksScreen(viewModel = tasksViewModel)
                        7 -> SettingsScreen(
                            onNavigateToAccount = {
                                previousTab = selectedTab
                                selectedTab = 11
                            },
                            onNavigateToTerms = onNavigateToTerms
                        )
                        8 -> ProfileScreen(userId = currentUserId)
                        11 -> AccountScreen(
                            onNavigateToProfile = {
                                previousTab = selectedTab
                                selectedTab = 8
                            },
                            onDeleteAccount = onDeleteAccount,
                            onBack = {
                                selectedTab = previousTab
                            }
                        )
                        9 -> selectedSubjectId?.let {
                            SubjectDetailScreen(
                                subjectId = it,
                                onSubjectDeleted = { selectedTab = previousTab },
                                externalViewModel = subjectDetailViewModel
                            )
                        }
                        10 -> StatsScreen(
                            statsViewModel = statsViewModel,
                            onNavigateToPerformance = {
                                previousTab = selectedTab
                                selectedTab = 12
                            }
                        )
                        12 -> SubjectPerformanceScreen(
                            subjects = statsState.subjectsPerformance,
                            onBackClick = { selectedTab = previousTab }
                        )
                        13 -> NotificationsScreen(viewModel = notificationsViewModel)
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.coming_soon), style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }

            if (showOnboarding) {
                OnboardingDialog(
                    career = onboardingCareer,
                    semestre = onboardingSemestre,
                    error = onboardingError,
                    onCareerChange = { onboardingCareer = it },
                    onSemestreChange = { onboardingSemestre = it; onboardingError = null },
                    onSave = {
                        if (onboardingSemestre.isBlank()) {
                            onboardingError = context.getString(R.string.onboarding_semestre_required)
                        } else {
                            val sem = onboardingSemestre.toIntOrNull()
                            if (sem == null || sem !in 1..12) {
                                onboardingError = context.getString(R.string.profile_error_semester_invalid)
                            } else {
                                scope.launch {
                                    Log.d("Onboarding", "Guardando: career='$onboardingCareer', semestre='$onboardingSemestre'")
                                    val user = app.userRepository.getUserById(currentUserId).first()
                                    Log.d("Onboarding", "Usuario encontrado para update: $user")
                                    user?.let {
                                        app.userRepository.updateUser(
                                            it.copy(carrera = onboardingCareer, semestre = onboardingSemestre)
                                        )
                                        Log.d("Onboarding", "updateUser ejecutado correctamente")
                                    } ?: Log.d("Onboarding", "user es null, no se actualizó")
                                    showOnboarding = false
                                }
                            }
                        }
                    }
                )
            }

            if (showCategoryManager) {
                AlertDialog(
                    onDismissRequest = { showCategoryManager = false },
                    title = { Text(stringResource(R.string.dialog_manage_categories), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            if (notesState.allCategories.isEmpty()) {
                                Text(stringResource(R.string.dialog_no_categories), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                notesState.allCategories.forEach { cat ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(cat.color))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat.name,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium
                                        )
                                        IconButton(
                                            onClick = { editCategory = cat },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = stringResource(R.string.cd_edit_note),
                                                tint = PurpleGradia,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { notesViewModel.deleteCategory(cat.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.cd_delete_note),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryManager = false }) {
                            Text(stringResource(R.string.action_close))
                        }
                    }
                )
            }

            if (showAchievementsDialog) {
                val completedCount = tasksState.tareasCompletadas.size
                AlertDialog(
                    onDismissRequest = { showAchievementsDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "\uD83C\uDFC6",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.achievements_dialog_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleGradia,
                                    fontFamily = InterFontFamily
                                )
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.achievements_dialog_body, completedCount),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = InterFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.achievements_dialog_motivation),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PurpleGradia,
                                    fontFamily = InterFontFamily
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAchievementsDialog = false }) {
                            Text(
                                text = stringResource(R.string.action_close),
                                fontWeight = FontWeight.Bold,
                                color = PurpleGradia
                            )
                        }
                    }
                )
            }

            editCategory?.let { cat ->
                EditCategoryDialog(
                    category = cat,
                    onDismiss = { editCategory = null },
                    onSave = { name, color ->
                        notesViewModel.updateCategory(cat.id, name, color)
                        editCategory = null
                    },
                    onDelete = {
                        notesViewModel.deleteCategory(cat.id)
                        editCategory = null
                    }
                )
            }

            // Quick Add Menu Overlay with Animation
            AnimatedVisibility(
                visible = isQuickAddOpen,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC453284)) // Purple with 80% opacity
                        .clickable { isQuickAddOpen = false }
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp)
                            .animateEnterExit(
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(400)
                                ) + fadeIn(),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(400)
                                ) + fadeOut()
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickAddMenuItem(
                            label = stringResource(R.string.quick_add_note),
                            iconRes = R.drawable.sticky,
                            onClick = {
                                previousTab = selectedTab
                                selectedTab = 5
                                isQuickAddOpen = false
                            }
                        )
                        QuickAddMenuItem(
                            label = stringResource(R.string.quick_add_todo),
                            iconRes = R.drawable.todo,
                            onClick = {
                                previousTab = selectedTab
                                selectedTab = 6
                                isQuickAddOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectSortMenuItem(
    label: String,
    value: SubjectSort,
    selected: SubjectSort,
    onClick: () -> Unit
) {
    SortCheckMenuItem(label = label, isSelected = value == selected, onClick = onClick)
}

@Composable
private fun SortCheckMenuItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PurpleGradia else MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick,
        trailingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PurpleGradia,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
fun QuickAddMenuItem(label: String, iconRes: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PurpleGradia,
        modifier = Modifier
            .width(220.dp)
            .height(55.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = InterFontFamily
            )
        }
    }
}

@Composable
fun GradiaDrawerContent(
    selectedItem: String,
    onClose: () -> Unit,
    onItemClick: (String) -> Unit,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    userName: String = "",
    userEmail: String = ""
) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val currentUserId = app.authRepository.getCurrentUserId()
    val user by if (currentUserId != null) {
        app.userRepository.getUserById(currentUserId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }
    val fotoUrl = user?.fotoUrl

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.width(330.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Close Button - More Visible
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.End)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = PurpleGradia,
                    modifier = Modifier.size(36.dp) // Increased size for better visibility
                )
            }

            // Profile Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SocialIconBg),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.splash_logo)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.splash_logo),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SocialIconBg),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName.ifBlank { stringResource(R.string.home_default_username) },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            DrawerMenuItem(
                label = stringResource(R.string.drawer_profile),
                outlineIconId = R.drawable.user_outline,
                boldIconId = R.drawable.user_bold,
                isSelected = selectedItem == "Perfil",
                onClick = { onItemClick("Perfil") }
            )
            DrawerMenuItem(
                label = stringResource(R.string.drawer_statistics),
                outlineIconId = R.drawable.stats_chart_outline,
                boldIconId = R.drawable.stats_chart,
                isSelected = selectedItem == "Estadísticas",
                onClick = { onItemClick("Estadísticas") }
            )
            DrawerMenuItem(
                label = stringResource(R.string.drawer_settings),
                outlineIconId = R.drawable.settings_linear,
                boldIconId = R.drawable.settings_rounded,
                isSelected = selectedItem == "Ajustes",
                onClick = { onItemClick("Ajustes") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Items
            DrawerMenuItem(
                label = if (isDarkMode) stringResource(R.string.drawer_light_theme) else stringResource(R.string.drawer_dark_theme),
                outlineIconId = if (isDarkMode) R.drawable.sun_light else R.drawable.moon,
                boldIconId = if (isDarkMode) R.drawable.sun_light else R.drawable.moon,
                isSelected = false,
                onClick = onToggleTheme
            )
            DrawerMenuItem(
                label = stringResource(R.string.drawer_logout),
                outlineIconId = R.drawable.log_out,
                boldIconId = R.drawable.logout_bold,
                isSelected = selectedItem == "Log Out",
                onClick = { onItemClick("Log Out") }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    outlineIconId: Int,
    boldIconId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) boldIconId else outlineIconId),
            contentDescription = null,
            tint = PurpleGradia,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = PurpleGradia,
                fontFamily = InterFontFamily,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
fun HomeContent(onSubjectClick: (Subject) -> Unit = {}) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val homeViewModel = remember { app.provideHomeViewModel() }
    val homeState by homeViewModel.uiState.collectAsState()
    val subjects = homeState.subjects
    var isExpanded by remember { mutableStateOf(false) }

    val visibleSubjects = if (isExpanded || subjects.size <= 3) subjects else subjects.take(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                stringResource(R.string.home_my_semester),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.home_overall_average),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val average = homeState.generalAverage
                    val hasGrades = homeState.hasGrades
                    val performance = performanceFor(average, hasGrades)

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressChart(
                            progress = if (hasGrades) (average / 5.0).toFloat().coerceIn(0f, 1f) else 0f,
                            score = if (hasGrades) String.format(Locale.US, "%.1f", average) else "—",
                            status = stringResource(performance.statusResId)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(performance.messageResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_subjects),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily
                    )
                )
                if (subjects.size > 3) {
                    Text(
                        text = if (isExpanded) stringResource(R.string.home_see_less) else stringResource(R.string.home_see_all),
                        color = PurpleGradia,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            }
        }

        if (subjects.isEmpty()) {
            item { EmptySubjectsCard() }
        } else {
            items(visibleSubjects, key = { it.id }) { subject ->
                SubjectHomeItem(
                    subject = subject,
                    average = homeState.averages[subject.id] ?: 0.0,
                    onClick = { onSubjectClick(subject) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para la bottom bar
    }
}

@Composable
fun EmptySubjectsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.graduation_cap),
                contentDescription = null,
                tint = PurpleGradia,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontFamily = InterFontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_empty_body),
                fontSize = 12.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                fontFamily = InterFontFamily
            )
        }
    }
}

private data class PerformanceInfo(val statusResId: Int, val messageResId: Int)

/**
 * Devuelve la etiqueta y el mensaje del Promedio General según cómo le va al
 * estudiante. Las franjas coinciden con las usadas en el detalle de cada nota.
 */
private fun performanceFor(average: Double, hasGrades: Boolean): PerformanceInfo = when {
    !hasGrades -> PerformanceInfo(
        statusResId = R.string.status_no_grades,
        messageResId = R.string.message_no_grades
    )
    average < 3.0 -> PerformanceInfo(
        statusResId = R.string.status_low,
        messageResId = R.string.message_low
    )
    average < 4.0 -> PerformanceInfo(
        statusResId = R.string.status_average,
        messageResId = R.string.message_average
    )
    average < 4.5 -> PerformanceInfo(
        statusResId = R.string.status_good,
        messageResId = R.string.message_good
    )
    else -> PerformanceInfo(
        statusResId = R.string.status_excellent,
        messageResId = R.string.message_excellent
    )
}

@Composable
fun CircularProgressChart(progress: Float, score: String, status: String) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawArc(
                color = surfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = PurpleGradia,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = status,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubjectHomeItem(subject: Subject, average: Double = 0.0, onClick: () -> Unit = {}) {
    val subtitle = subject.professor.ifBlank {
        subject.classroom.ifBlank { "Semestre ${subject.semester}" }
    }
    val iconType = resolveSubjectIcon(subject.icon, subject.name)
    val hasGrades = average > 0.0
    val progress = (average / 5.0).toFloat().coerceIn(0f, 1f)
    val percent = (progress * 100).roundToInt()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(PurpleGradia, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SubjectIconRender(type = iconType, tint = Color.White, size = 28.dp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = InterFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (hasGrades) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PurpleGradia,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$percent%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = InterFontFamily
                        )
                    }
                } else {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun GradiaBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit, isQuickAddOpen: Boolean) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(20.dp),
        color = PurpleGradia,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                isSelected = selectedTab == 0 && !isQuickAddOpen,
                onClick = { onTabSelected(0) },
                whiteIconId = R.drawable.home_white,
                purpleIconId = R.drawable.home_purple,
                contentDescription = "Home"
            )
            BottomBarItem(
                isSelected = selectedTab == 1 && !isQuickAddOpen,
                onClick = { onTabSelected(1) },
                whiteIconId = R.drawable.books_add_white,
                purpleIconId = R.drawable.books_add_purple,
                contentDescription = "Books"
            )

            // Special Plus Button (The "Add" icon selected when in Notes or Tasks from Quick Add)
            val isAddIconSelected = isQuickAddOpen || selectedTab == 5 || selectedTab == 6
            IconButton(
                onClick = { onTabSelected(2) },
                modifier = if (isAddIconSelected) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .size(35.dp)
                } else {
                    Modifier.size(40.dp)
                }
            ) {
                Icon(
                    painter = if (isAddIconSelected) {
                        painterResource(id = R.drawable.plus_purple)
                    } else {
                        painterResource(id = R.drawable.plus_white)
                    },
                    contentDescription = "Quick Add",
                    tint = if (isAddIconSelected) PurpleGradia else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            BottomBarItem(
                isSelected = selectedTab == 3 && !isQuickAddOpen,
                onClick = { onTabSelected(3) },
                whiteIconId = R.drawable.calculator_white,
                purpleIconId = R.drawable.calculator_purple,
                contentDescription = "Calculator"
            )
            BottomBarItem(
                isSelected = selectedTab == 4 && !isQuickAddOpen,
                onClick = { onTabSelected(4) },
                whiteIconId = R.drawable.calendar_white,
                purpleIconId = R.drawable.calendar_purple,
                contentDescription = "Calendar"
            )
        }
    }
}

@Composable
fun BottomBarItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    whiteIconId: Int,
    purpleIconId: Int,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = if (isSelected) {
            Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .size(35.dp)
        } else {
            Modifier.size(40.dp)
        }
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) purpleIconId else whiteIconId),
            contentDescription = contentDescription,
            tint = if (isSelected) PurpleGradia else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun OnboardingDialog(
    career: String,
    semestre: String,
    error: String?,
    onCareerChange: (String) -> Unit,
    onSemestreChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.onboarding_title),
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.onboarding_message),
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.profile_career),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .border(1.dp, PurpleGradia, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (career.isEmpty()) {
                            Text(
                                stringResource(R.string.onboarding_career_hint),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = career,
                            onValueChange = onCareerChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(PurpleGradia)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.profile_current_semester),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .border(1.dp, PurpleGradia, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (semestre.isEmpty()) {
                            Text(
                                stringResource(R.string.onboarding_semestre_hint),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp
                            )
                        }
                        BasicTextField(
                            value = semestre,
                            onValueChange = onSemestreChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(PurpleGradia)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .width(180.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia)
                ) {
                    Text(
                        stringResource(R.string.profile_save),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GradiaTheme {
        HomeScreen()
    }
}
