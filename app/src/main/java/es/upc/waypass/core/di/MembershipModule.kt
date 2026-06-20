package es.upc.waypass.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.upc.waypass.data.remote.WayPassApiService
import es.upc.waypass.data.repository.MembershipRepositoryImpl
import es.upc.waypass.domain.repository.MembershipRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MembershipModule {
    @Provides
    @Singleton
    fun provideMembershipRepository(
        api: WayPassApiService
    ): MembershipRepository =
        MembershipRepositoryImpl(api)
}
