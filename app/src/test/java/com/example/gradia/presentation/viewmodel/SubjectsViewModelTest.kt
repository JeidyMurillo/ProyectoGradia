package com.example.gradia.presentation.viewmodel

import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSubjectRepository
    private lateinit var viewModel: SubjectsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSubjectRepository()
        viewModel = SubjectsViewModel(
            subjectRepository = repository,
            calculateCurrentAverage = CalculateCurrentAverageUseCase()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.subjects.isEmpty())
    }

    @Test
    fun `addSubject adds subject to list`() = runTest {
        val subject = Subject(id = 0, name = "Matematicas", creditHours = 3, semester = 1)

        var success = false
        viewModel.addSubject(subject) { success = true }
        advanceUntilIdle()

        assertTrue(success)
        val state = viewModel.uiState.value
        assertEquals(1, state.subjects.size)
        assertEquals("Matematicas", state.subjects.first().name)
    }

    @Test
    fun `deleteSubject removes subject from list`() = runTest {
        val subject = Subject(id = 0, name = "Matematicas", creditHours = 3, semester = 1)
        var addedId = 0L
        viewModel.addSubject(subject) { addedId = repository.lastInsertedId }
        advanceUntilIdle()

        var deleted = false
        viewModel.deleteSubject(addedId) { deleted = true }
        advanceUntilIdle()

        assertTrue(deleted)
        val state = viewModel.uiState.value
        assertTrue(state.subjects.isEmpty())
    }

    @Test
    fun `filter defaults to TODAS`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(SubjectFilter.TODAS, state.filter)
    }

    @Test
    fun `onFilterChange updates filter`() = runTest {
        viewModel.onFilterChange(SubjectFilter.ACTUAL)
        advanceUntilIdle()
        assertEquals(SubjectFilter.ACTUAL, viewModel.uiState.value.filter)
    }

    @Test
    fun `onSortChange updates sort`() = runTest {
        viewModel.onSortChange(SubjectSort.SEMESTRE)
        advanceUntilIdle()
        assertEquals(SubjectSort.SEMESTRE, viewModel.uiState.value.sort)
    }

    @Test
    fun `clearError resets error state`() = runTest {
        viewModel.clearError()
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `sort by name orders alphabetically`() = runTest {
        repository.addSubjectDirect(Subject(id = 1, name = "Zebra", creditHours = 3, semester = 1))
        repository.addSubjectDirect(Subject(id = 2, name = "Alpha", creditHours = 3, semester = 1))
        repository.emitSubjects()

        viewModel.onSortChange(SubjectSort.NOMBRE)
        advanceUntilIdle()

        val names = viewModel.uiState.value.subjects.map { it.name }
        assertEquals(listOf("Alpha", "Zebra"), names)
    }

    @Test
    fun `sort by credits descending`() = runTest {
        repository.addSubjectDirect(Subject(id = 1, name = "A", creditHours = 2, semester = 1))
        repository.addSubjectDirect(Subject(id = 2, name = "B", creditHours = 5, semester = 1))
        repository.emitSubjects()

        viewModel.onSortChange(SubjectSort.CREDITOS)
        advanceUntilIdle()

        val credits = viewModel.uiState.value.subjects.map { it.creditHours }
        assertEquals(listOf(5, 2), credits)
    }
}

class FakeSubjectRepository : SubjectRepository {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    private val _grades = mutableMapOf<Long, List<GradeItem>>()
    var lastInsertedId = 0L
    private var nextId = 1L

    override fun getSubjects() = _subjects

    override fun getSubjectById(id: Long) =
        flowOf(_subjects.value.find { it.id == id })

    override fun getGradeItemsBySubject(subjectId: Long) =
        flowOf(_grades[subjectId] ?: emptyList())

    override suspend fun insertSubject(subject: Subject): Long {
        val id = if (subject.id == 0L) nextId++ else subject.id
        lastInsertedId = id
        val list = _subjects.value.toMutableList()
        list.add(subject.copy(id = id))
        _subjects.value = list
        return id
    }

    override suspend fun updateSubject(subject: Subject) {
        val list = _subjects.value.toMutableList()
        val index = list.indexOfFirst { it.id == subject.id }
        if (index >= 0) list[index] = subject
        _subjects.value = list
    }

    override suspend fun deleteSubject(subjectId: Long) {
        _subjects.value = _subjects.value.filter { it.id != subjectId }
    }

    override suspend fun insertGradeItem(gradeItem: GradeItem): Long = 0L

    override suspend fun updateGradeItem(gradeItem: GradeItem) {}

    override suspend fun deleteGradeItem(gradeItem: GradeItem) {}

    override suspend fun updateGrade(gradeItemId: Long, newGrade: Double) {}

    fun addSubjectDirect(subject: Subject) {
        val list = _subjects.value.toMutableList()
        list.add(subject)
        _subjects.value = list
    }

    fun emitSubjects() {
        _subjects.value = _subjects.value.toList()
    }
}
