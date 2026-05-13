package es.upc.waypass.data.repository

import es.upc.waypass.domain.repository.SubscriptionRepository
import es.upc.waypass.data.dto.SubscriptionStatus
import es.upc.waypass.data.remote.WayPassApiService
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val api: WayPassApiService
) : SubscriptionRepository {

    override suspend fun createSubscription(): Result<String> =
        runCatching {
            api.createSubscription().approvalUrl
        }

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> =
        runCatching {
            val response = api.getSubscriptionStatus()
            SubscriptionStatus(
                isActive = response.isActive,
                status = response.status,
                expiresAt = response.expiresAt
            )
        }
}