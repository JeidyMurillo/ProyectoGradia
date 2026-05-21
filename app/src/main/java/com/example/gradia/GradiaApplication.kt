package com.example.gradia

import android.app.Application
import android.content.Context
import com.example.gradia.data.firebase.FirebaseAuthService
import com.example.gradia.data.local.AppDatabase
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.AuthRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.NotaRepository
import com.example.gradia.data.repository.NoteRepositoryImpl
import com.example.gradia.data.repository.RoomSubjectRepository
import com.example.gradia.data.repository.UserRepository
import com.example.gradia.domain.repository.NoteRepository
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import com.example.gradia.domain.usecase.CalculateRemainingPercentageUseCase
import com.example.gradia.domain.usecase.CalculateRequiredGradeUseCase
import com.example.gradia.domain.usecase.notes.CreateCategoryUseCase
import com.example.gradia.domain.usecase.notes.DeleteCategoryUseCase
import com.example.gradia.domain.usecase.notes.DeleteNoteUseCase
import com.example.gradia.domain.usecase.notes.GetCategoriesUseCase
import com.example.gradia.domain.usecase.notes.GetNotesUseCase
import com.example.gradia.domain.usecase.notes.SaveNoteUseCase
import com.example.gradia.domain.usecase.notes.UpdateCategoryUseCase
import com.example.gradia.presentation.viewmodel.FinalGradeViewModel
import com.example.gradia.presentation.viewmodel.NotesViewModel
import com.example.gradia.presentation.viewmodel.SubjectDetailViewModel
import com.example.gradia.presentation.viewmodel.SubjectsViewModel
import com.example.gradia.presentation.viewmodel.TasksViewModel

class GradiaApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val asignaturaRepository by lazy { AsignaturaRepository(database.asignaturaDao()) }
    val notaRepository by lazy { NotaRepository(database.notaDao()) }
    val eventoRepository by lazy { EventoRepository(database.eventoDao()) }

    val subjectRepository: SubjectRepository by lazy {
        RoomSubjectRepository(
            asignaturaDao = database.asignaturaDao(),
            notaDao = database.notaDao(),
            userRepository = userRepository
        )
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(
            notaDao = database.notaContenidoDao(),
            categoriaDao = database.categoriaDao(),
            notaCategoriaDao = database.notaCategoriaDao()
        )
    }

    val firebaseAuthService by lazy { FirebaseAuthService() }
    val authRepository by lazy { AuthRepository(firebaseAuthService, userRepository) }

    val calculateCurrentAverageUseCase by lazy { CalculateCurrentAverageUseCase() }
    val calculateRemainingPercentageUseCase by lazy { CalculateRemainingPercentageUseCase() }

    val calculateRequiredGradeUseCase by lazy {
        CalculateRequiredGradeUseCase(
            calculateCurrentAverage = calculateCurrentAverageUseCase,
            calculateRemainingPercentage = calculateRemainingPercentageUseCase
        )
    }

    val getNotesUseCase by lazy { GetNotesUseCase(noteRepository) }
    val saveNoteUseCase by lazy { SaveNoteUseCase(noteRepository) }
    val deleteNoteUseCase by lazy { DeleteNoteUseCase(noteRepository) }
    val getCategoriesUseCase by lazy { GetCategoriesUseCase(noteRepository) }
    val createCategoryUseCase by lazy { CreateCategoryUseCase(noteRepository) }
    val updateCategoryUseCase by lazy { UpdateCategoryUseCase(noteRepository) }
    val deleteCategoryUseCase by lazy { DeleteCategoryUseCase(noteRepository) }

    private val prefs by lazy { getSharedPreferences("gradia_prefs", Context.MODE_PRIVATE) }

    var isRememberMeEnabled: Boolean
        get() = prefs.getBoolean("remember_me", false)
        set(value) = prefs.edit().putBoolean("remember_me", value).apply()

    fun provideFinalGradeViewModel(): FinalGradeViewModel {
        return FinalGradeViewModel(
            subjectRepository = subjectRepository,
            calculateCurrentAverage = calculateCurrentAverageUseCase,
            calculateRemainingPercentage = calculateRemainingPercentageUseCase,
            calculateRequiredGrade = calculateRequiredGradeUseCase
        )
    }

    fun provideNotesViewModel(userId: String): NotesViewModel {
        return NotesViewModel(
            userId = userId,
            getNotesUseCase = getNotesUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            createCategoryUseCase = createCategoryUseCase,
            updateCategoryUseCase = updateCategoryUseCase,
            deleteCategoryUseCase = deleteCategoryUseCase
        )
    }

    fun provideTasksViewModel(): TasksViewModel {
        return TasksViewModel(
            userRepository = userRepository,
            eventoRepository = eventoRepository,
            asignaturaRepository = asignaturaRepository
        )
    }

    fun provideSubjectsViewModel(): SubjectsViewModel {
        return SubjectsViewModel(subjectRepository = subjectRepository)
    }

    fun provideSubjectDetailViewModel(subjectId: Long): SubjectDetailViewModel {
        return SubjectDetailViewModel(
            subjectId = subjectId,
            subjectRepository = subjectRepository
        )
    }
}