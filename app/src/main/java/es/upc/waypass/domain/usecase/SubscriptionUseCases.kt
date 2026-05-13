package es.upc.waypass.domain.usecase

import es.upc.waypass.data.dto.SubscriptionStatus
import es.upc.waypass.domain.repository.SubscriptionRepository
import javax.inject.Inject

class CreateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<String> =
        repository.createSubscription()
}

class GetSubscriptionStatusUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<SubscriptionStatus> =
        repository.getSubscriptionStatus()
}