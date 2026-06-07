package com.example.vantink.domain.usecase

import com.example.vantink.domain.model.SearchFilter
import com.example.vantink.domain.model.Webtoon
import com.example.vantink.domain.repository.WebtoonRepository
import javax.inject.Inject

class GetWebtoonsUseCase @Inject constructor(
    private val repository: WebtoonRepository
) {
    suspend operator fun invoke(filter: SearchFilter): Result<List<Webtoon>> {
        return repository.getWebtoons(filter)
    }
}
