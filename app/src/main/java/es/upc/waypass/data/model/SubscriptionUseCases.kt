package es.upc.waypass.data.model

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