package es.upc.waypass.presentation.subscription

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.upc.waypass.data.remote.WayPassApiService
import es.upc.waypass.data.repository.SubscriptionRepositoryImpl
import es.upc.waypass.domain.repository.SubscriptionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionModule {
    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        api: WayPassApiService
    ): SubscriptionRepository =
        SubscriptionRepositoryImpl(api)
}