package com.example.vantink.domain.usecase

import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import javax.inject.Inject

class GetWebtoonDetailsUseCase @Inject constructor(
    private val repository: WebtoonRepository
) {
    suspend operator fun invoke(id: String): Result<Webtoon> {
        return repository.getWebtoonDetails(id)
    }
}
