package com.example.gradia.data.repository

import android.util.Log
import com.example.gradia.data.local.dao.AsignaturaDao
import com.example.gradia.data.local.dao.NotaDao
import com.example.gradia.domain.mapper.toDomain
import com.example.gradia.domain.mapper.toEntity
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.util.ErrorHandler
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
        userRepository.getCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                asignaturaDao.getAsignaturaById(id, user.id).map { it?.toDomain() }
            }
        }

    // Las calificaciones (tabla `notas`) no tienen columna userId: cuelgan de una
    // asignatura, que sí está aislada por usuario. Solo se accede a ellas mediante
    // un subjectId proveniente de la lista de asignaturas del propio usuario, por lo
    // que su aislamiento queda garantizado de forma indirecta.
    override fun getGradeItemsBySubject(subjectId: Long): Flow<List<GradeItem>> =
        notaDao.getNotasByAsignatura(subjectId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSubject(subject: Subject): Long {
        val user = userRepository.getCurrentUser().first()
            ?: error("Debes iniciar sesión para crear una asignatura.")
        return try {
            asignaturaDao.insertAsignatura(subject.toEntity(user.id))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.insertSubject", e,
                "No se pudo guardar la asignatura."))
        }
    }

    override suspend fun updateSubject(subject: Subject) {
        val user = userRepository.getCurrentUser().first()
            ?: error("Debes iniciar sesión para editar una asignatura.")
        try {
            asignaturaDao.updateAsignatura(subject.toEntity(user.id))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.updateSubject", e,
                "No se pudo actualizar la asignatura."))
        }
    }

    override suspend fun deleteSubject(subjectId: Long) {
        val user = userRepository.getCurrentUser().first()
            ?: error("Debes iniciar sesión para eliminar una asignatura.")
        try {
            asignaturaDao.deleteAsignaturaById(subjectId, user.id)
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.deleteSubject", e,
                "No se pudo eliminar la asignatura."))
        }
    }

    override suspend fun insertGradeItem(gradeItem: GradeItem): Long {
        return try {
            notaDao.insertNota(gradeItem.toEntity())
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.insertGradeItem", e,
                "No se pudo guardar la calificación."))
        }
    }

    override suspend fun updateGradeItem(gradeItem: GradeItem) {
        try {
            val existing = notaDao.getNotaByIdSync(gradeItem.id)
            val entity = gradeItem.toEntity()
            notaDao.updateNota(
                if (existing != null) entity.copy(fechaCreacion = existing.fechaCreacion) else entity
            )
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.updateGradeItem", e,
                "No se pudo actualizar la calificación."))
        }
    }

    override suspend fun deleteGradeItem(gradeItem: GradeItem) {
        try {
            notaDao.deleteNota(gradeItem.toEntity())
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.deleteGradeItem", e,
                "No se pudo eliminar la calificación."))
        }
    }

    override suspend fun updateGrade(gradeItemId: Long, newGrade: Double) {
        try {
            val nota = notaDao.getNotaByIdSync(gradeItemId) ?: return
            notaDao.updateNota(nota.copy(valor = newGrade.toFloat()))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handle("RoomSubjectRepository.updateGrade", e,
                "No se pudo actualizar la calificación."))
        }
    }

}
