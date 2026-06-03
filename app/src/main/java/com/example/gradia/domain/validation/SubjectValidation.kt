package com.example.gradia.domain.validation

import com.example.gradia.domain.model.Subject

/**
 * Reglas de validación compartidas para crear y editar asignaturas.
 * Lanza [IllegalArgumentException] con un mensaje en español apto para
 * mostrarse directamente al usuario (snackbar). No depende de Android, por lo
 * que es reutilizable desde cualquier ViewModel y testeable de forma aislada.
 */
object SubjectValidation {
    const val MAX_NAME_LENGTH = 40
    const val MIN_CREDITS = 1
    const val MAX_CREDITS = 6

    /**
     * @param subject la asignatura a validar (se valida sobre el nombre ya recortado).
     * @param existing asignaturas ya guardadas; se usa para detectar nombres
     *   duplicados ignorando mayúsculas/minúsculas y excluyendo la propia
     *   asignatura por [Subject.id] (relevante al editar).
     */
    fun validate(subject: Subject, existing: List<Subject>) {
        val name = subject.name.trim()
        when {
            name.isBlank() ->
                throw IllegalArgumentException("El nombre de la asignatura es obligatorio")
            name.length > MAX_NAME_LENGTH ->
                throw IllegalArgumentException("El nombre no puede superar los $MAX_NAME_LENGTH caracteres")
            subject.creditHours !in MIN_CREDITS..MAX_CREDITS ->
                throw IllegalArgumentException("Los créditos deben estar entre $MIN_CREDITS y $MAX_CREDITS")
            subject.semester < 1 ->
                throw IllegalArgumentException("El semestre seleccionado no es válido")
            existing.any { it.id != subject.id && it.name.trim().equals(name, ignoreCase = true) } ->
                throw IllegalArgumentException("Ya existe una asignatura con ese nombre")
        }
    }
}
