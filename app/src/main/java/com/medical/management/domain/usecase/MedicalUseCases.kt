package com.medical.management.domain.usecase

import com.medical.management.domain.repository.MedicalRepository
import javax.inject.Inject

class MedicalUseCases @Inject constructor(val repository: MedicalRepository)
