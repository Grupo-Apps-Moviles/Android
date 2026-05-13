package es.upc.waypass.domain.repository

import es.upc.waypass.data.dto.SubscriptionStatus

interface SubscriptionRepository {
    suspend fun createSubscription(): Result<String>

    suspend fun getSubscriptionStatus(): Result<SubscriptionStatus>
}