package com.example.gradia.data.repository

import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeSubjectRepository : SubjectRepository {

    private val subjects = MutableStateFlow(
        listOf(
            Subject(id = 1, name = "Calculo IV", icon = "\uD83D\uDCCA", passingGrade = 3.0, creditHours = 4),
            Subject(id = 2, name = "Fisica II", icon = "\uD83E\uDD16", passingGrade = 3.0, creditHours = 4),
            Subject(id = 3, name = "Programacion", icon = "\uD83D\uDCBB", passingGrade = 3.0, creditHours = 3)
        )
    )

    private val gradeItems = MutableStateFlow(
        listOf(
            GradeItem(id = 1, subjectId = 1, name = "Parcial 1", percentage = 25.0, grade = 3.5),
            GradeItem(id = 2, subjectId = 1, name = "Parcial 2", percentage = 25.0, grade = 2.8),
            GradeItem(id = 3, subjectId = 1, name = "Tareas", percentage = 15.0, grade = 4.0),
            GradeItem(id = 4, subjectId = 1, name = "Examen Final", percentage = 35.0, grade = null),
            GradeItem(id = 5, subjectId = 2, name = "Parcial 1", percentage = 30.0, grade = 4.2),
            GradeItem(id = 6, subjectId = 2, name = "Parcial 2", percentage = 30.0, grade = 3.8),
            GradeItem(id = 7, subjectId = 2, name = "Laboratorios", percentage = 20.0, grade = 4.5),
            GradeItem(id = 8, subjectId = 2, name = "Examen Final", percentage = 20.0, grade = null),
            GradeItem(id = 9, subjectId = 3, name = "Proyecto 1", percentage = 20.0, grade = 4.8),
            GradeItem(id = 10, subjectId = 3, name = "Proyecto 2", percentage = 20.0, grade = 4.5),
            GradeItem(id = 11, subjectId = 3, name = "Quices", percentage = 15.0, grade = 4.0),
            GradeItem(id = 12, subjectId = 3, name = "Examen Final", percentage = 45.0, grade = null)
        )
    )

    private var nextItemId = 100L
    private var nextSubjectId = 10L

    override fun getSubjects(): Flow<List<Subject>> = subjects.asStateFlow()

    override fun getSubjectById(id: Long): Flow<Subject?> =
        subjects.map { list -> list.find { it.id == id } }

    override fun getGradeItemsBySubject(subjectId: Long): Flow<List<GradeItem>> =
        gradeItems.map { list -> list.filter { it.subjectId == subjectId } }

    override suspend fun insertSubject(subject: Subject): Long {
        val newSubject = subject.copy(id = nextSubjectId++)
        subjects.value += newSubject
        return newSubject.id
    }

    override suspend fun insertGradeItem(gradeItem: GradeItem): Long {
        val newItem = gradeItem.copy(id = nextItemId++)
        gradeItems.value += newItem
        return newItem.id
    }

    override suspend fun updateGradeItem(gradeItem: GradeItem) {
        val current = gradeItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == gradeItem.id }
        if (index != -1) current[index] = gradeItem
        gradeItems.value = current
    }

    override suspend fun deleteGradeItem(gradeItem: GradeItem) {
        gradeItems.value = gradeItems.value.filter { it.id != gradeItem.id }
    }

    override suspend fun updateGrade(gradeItemId: Long, newGrade: Double) {
        val current = gradeItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == gradeItemId }
        if (index != -1) {
            current[index] = current[index].copy(grade = newGrade)
            gradeItems.value = current
        }
    }
}
