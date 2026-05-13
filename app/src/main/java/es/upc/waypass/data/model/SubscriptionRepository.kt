package es.upc.waypass.data.model

interface SubscriptionRepository {
    suspend fun createSubscription(): Result<String>

    suspend fun getSubscriptionStatus(): Result<SubscriptionStatus>
}