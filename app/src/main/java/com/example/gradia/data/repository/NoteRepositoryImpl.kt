package com.example.gradia.data.repository

import com.example.gradia.data.local.dao.CategoriaDao
import com.example.gradia.data.local.dao.NotaCategoriaDao
import com.example.gradia.data.local.dao.NotaContenidoDao
import com.example.gradia.data.local.entity.CategoriaEntity
import com.example.gradia.data.local.entity.NotaCategoriaCrossRef
import com.example.gradia.data.local.entity.NotaConCategorias
import com.example.gradia.data.local.entity.NotaContenidoEntity
import com.example.gradia.domain.model.Category
import com.example.gradia.domain.model.Note
import com.example.gradia.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val notaDao: NotaContenidoDao,
    private val categoriaDao: CategoriaDao,
    private val notaCategoriaDao: NotaCategoriaDao
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return notaDao.getAllNotasConCategorias().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNoteById(id: Long): Flow<Note?> {
        return notaDao.getNotaConCategoriasById(id).map { it?.toDomain() }
    }

    override suspend fun saveNote(note: Note): Long {
        val entity = note.toEntity()
        val notaId = if (note.id == 0L) {
            notaDao.insertNota(entity)
        } else {
            notaDao.updateNota(entity.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }

        notaCategoriaDao.deleteByNotaId(notaId)
        note.categories.forEach { category ->
            if (category.id != 0L) {
                notaCategoriaDao.insert(NotaCategoriaCrossRef(notaId, category.id))
            }
        }

        return notaId
    }

    override suspend fun deleteNote(id: Long) {
        notaDao.deleteNotaById(id)
    }

    override fun getCategories(): Flow<List<Category>> {
        return categoriaDao.getAllCategorias().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createCategory(category: Category): Long {
        return categoriaDao.insertCategoria(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoriaDao.updateCategoria(category.id, category.name, category.color)
    }

    override suspend fun deleteCategory(id: Long) {
        categoriaDao.deleteCategoriaById(id)
    }

    override fun getNotesByCategories(categoryIds: List<Long>): Flow<List<Note>> {
        return notaDao.getNotasByCategorias(categoryIds).map { entities ->
            entities.map { it.toDomain(emptyList()) }
        }
    }
}

private fun NotaConCategorias.toDomain(): Note = Note(
    id = nota.id,
    title = nota.title,
    content = nota.content,
    color = nota.color,
    categories = categorias.map { it.toDomain() },
    createdAt = nota.createdAt,
    updatedAt = nota.updatedAt
)

private fun NotaContenidoEntity.toDomain(categorias: List<Category>): Note = Note(
    id = id,
    title = title,
    content = content,
    color = color,
    categories = categorias,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun Note.toEntity(): NotaContenidoEntity = NotaContenidoEntity(
    id = id,
    title = title,
    content = content,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun CategoriaEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    color = color
)

private fun Category.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    name = name,
    color = color
)
