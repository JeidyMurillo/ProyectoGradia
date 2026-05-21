package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.AsignaturaDao
import com.example.gradia.data.local.dao.NotaDao
import com.example.gradia.domain.mapper.toDomain
import com.example.gradia.domain.mapper.toEntity
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class RoomSubjectRepository(
    private val asignaturaDao: AsignaturaDao,
    private val notaDao: NotaDao,
    private val userRepository: UserRepository
) : SubjectRepository {

    override fun getSubjects(): Flow<List<Subject>> =
        userRepository.getCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                asignaturaDao.getAsignaturasByUser(user.id)
                    .map { list -> list.map { it.toDomain() } }
            }
        }

    override fun getSubjectById(id: Long): Flow<Subject?> =
        asignaturaDao.getAsignaturaById(id).map { it?.toDomain() }

    override fun getGradeItemsBySubject(subjectId: Long): Flow<List<GradeItem>> =
        notaDao.getNotasByAsignatura(subjectId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSubject(subject: Subject): Long {
        val user = userRepository.getCurrentUser().first()
            ?: error("No hay usuario activo para asociar la asignatura")
        return asignaturaDao.insertAsignatura(subject.toEntity(user.id))
    }

    override suspend fun insertGradeItem(gradeItem: GradeItem): Long =
        notaDao.insertNota(gradeItem.toEntity())

    override suspend fun updateGradeItem(gradeItem: GradeItem) {
        notaDao.updateNota(gradeItem.toEntity())
    }

    override suspend fun deleteGradeItem(gradeItem: GradeItem) {
        notaDao.deleteNota(gradeItem.toEntity())
    }

    override suspend fun updateGrade(gradeItemId: Long, newGrade: Double) {
        val nota = notaDao.getNotaByIdSync(gradeItemId) ?: return
        notaDao.updateNota(nota.copy(valor = newGrade.toFloat()))
    }
}
