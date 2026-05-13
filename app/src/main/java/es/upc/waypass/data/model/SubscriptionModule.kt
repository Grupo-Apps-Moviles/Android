package es.upc.waypass.data.model

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
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