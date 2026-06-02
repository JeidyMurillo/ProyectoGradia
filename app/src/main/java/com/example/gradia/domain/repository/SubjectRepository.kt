package com.example.gradia.domain.repository

import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    fun getSubjects(): Flow<List<Subject>>

    fun getSubjectById(id: Long): Flow<Subject?>

    fun getGradeItemsBySubject(subjectId: Long): Flow<List<GradeItem>>

    suspend fun insertSubject(subject: Subject): Long

    suspend fun updateSubject(subject: Subject)

    suspend fun deleteSubject(subjectId: Long)

    suspend fun insertGradeItem(gradeItem: GradeItem): Long

    suspend fun updateGradeItem(gradeItem: GradeItem)

    suspend fun deleteGradeItem(gradeItem: GradeItem)

    suspend fun updateGrade(gradeItemId: Long, newGrade: Double)
}
