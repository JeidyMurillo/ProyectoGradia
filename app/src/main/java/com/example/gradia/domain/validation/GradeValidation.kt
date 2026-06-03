package com.example.gradia.domain.validation

import com.example.gradia.domain.model.GradeItem

/**
 * Reglas de validación compartidas para crear y editar notas (calificaciones).
 * Lanza [IllegalArgumentException] con un mensaje en español apto para mostrarse
 * directamente al usuario (snackbar). No depende de Android, por lo que es
 * reutilizable desde cualquier ViewModel y testeable de forma aislada.
 */
object GradeValidation {
    const val MAX_NAME_LENGTH = 40
    const val MAX_TOTAL_PERCENTAGE = 100.0
    const val MIN_GRADE = 0.0
    const val MAX_GRADE = 5.0

    // Tolerancia para comparaciones de punto flotante (los porcentajes se guardan
    // como Float, así que sumas como 33.3 * 3 pueden desviarse ligeramente).
    private const val EPSILON = 0.001

    /**
     * @param grade la nota a validar.
     * @param existingGrades el resto de notas de la asignatura; se usa para
     *   comprobar que la suma total de porcentajes no supere el 100%. La propia
     *   nota se excluye por [GradeItem.id] (relevante al editar).
     */
    fun validate(grade: GradeItem, existingGrades: List<GradeItem>) {
        val name = grade.name.trim()
        when {
            name.isBlank() ->
                throw IllegalArgumentException("El nombre de la actividad es obligatorio")
            name.length > MAX_NAME_LENGTH ->
                throw IllegalArgumentException("El nombre no puede superar los $MAX_NAME_LENGTH caracteres")
            grade.percentage <= 0.0 ->
                throw IllegalArgumentException("El porcentaje debe ser mayor que 0")
            grade.percentage > MAX_TOTAL_PERCENTAGE + EPSILON ->
                throw IllegalArgumentException("El porcentaje no puede superar el 100%")
            grade.grade != null && grade.grade !in MIN_GRADE..MAX_GRADE ->
                throw IllegalArgumentException("La nota debe estar entre 0.0 y 5.0")
            else -> {
                val usedByOthers = existingGrades
                    .filter { it.id != grade.id }
                    .sumOf { it.percentage }
                if (usedByOthers + grade.percentage > MAX_TOTAL_PERCENTAGE + EPSILON) {
                    val available = (MAX_TOTAL_PERCENTAGE - usedByOthers).coerceAtLeast(0.0)
                    throw IllegalArgumentException(
                        "El porcentaje total superaría el 100%. Disponible: ${formatPercentage(available)}%"
                    )
                }
            }
        }
    }

    /** Formatea un porcentaje sin ceros decimales sobrantes: 12.0 → "12", 12.40 → "12.4". */
    fun formatPercentage(value: Double): String {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            var text = String.format(java.util.Locale.US, "%.2f", rounded)
            while (text.endsWith("0")) text = text.dropLast(1)
            text
        }
    }
}
