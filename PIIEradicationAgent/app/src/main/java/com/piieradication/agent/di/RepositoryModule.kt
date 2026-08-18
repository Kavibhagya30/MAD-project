package com.piieradication.agent.di

import com.piieradication.agent.data.repository.DeletionRequestRepositoryImpl
import com.piieradication.agent.data.repository.EventLogRepositoryImpl
import com.piieradication.agent.data.repository.PiiRepositoryImpl
import com.piieradication.agent.data.repository.UserProfileRepositoryImpl
import com.piieradication.agent.domain.repository.DeletionRequestRepository
import com.piieradication.agent.domain.repository.EventLogRepository
import com.piieradication.agent.domain.repository.PiiRepository
import com.piieradication.agent.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPiiRepository(impl: PiiRepositoryImpl): PiiRepository

    @Binds
    @Singleton
    abstract fun bindDeletionRequestRepository(impl: DeletionRequestRepositoryImpl): DeletionRequestRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindEventLogRepository(impl: EventLogRepositoryImpl): EventLogRepository
}
