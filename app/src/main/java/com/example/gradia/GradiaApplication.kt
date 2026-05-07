package com.example.gradia

import android.app.Application
import com.example.gradia.data.local.AppDatabase
import com.example.gradia.data.repository.AsignaturaRepository
import com.example.gradia.data.repository.EventoRepository
import com.example.gradia.data.repository.FakeSubjectRepository
import com.example.gradia.data.repository.NotaRepository
import com.example.gradia.data.repository.UserRepository
import com.example.gradia.domain.repository.SubjectRepository
import com.example.gradia.domain.usecase.CalculateCurrentAverageUseCase
import com.example.gradia.domain.usecase.CalculateRemainingPercentageUseCase
import com.example.gradia.domain.usecase.CalculateRequiredGradeUseCase
import com.example.gradia.presentation.viewmodel.FinalGradeViewModel

class GradiaApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val asignaturaRepository by lazy { AsignaturaRepository(database.asignaturaDao()) }
    val notaRepository by lazy { NotaRepository(database.notaDao()) }
    val eventoRepository by lazy { EventoRepository(database.eventoDao()) }

    val subjectRepository: SubjectRepository by lazy { FakeSubjectRepository() }

    val calculateCurrentAverageUseCase by lazy { CalculateCurrentAverageUseCase() }

    val calculateRemainingPercentageUseCase by lazy { CalculateRemainingPercentageUseCase() }

    val calculateRequiredGradeUseCase by lazy {
        CalculateRequiredGradeUseCase(
            calculateCurrentAverage = calculateCurrentAverageUseCase,
            calculateRemainingPercentage = calculateRemainingPercentageUseCase
        )
    }

    fun provideFinalGradeViewModel(): FinalGradeViewModel {
        return FinalGradeViewModel(
            subjectRepository = subjectRepository,
            calculateCurrentAverage = calculateCurrentAverageUseCase,
            calculateRemainingPercentage = calculateRemainingPercentageUseCase,
            calculateRequiredGrade = calculateRequiredGradeUseCase
        )
    }
}