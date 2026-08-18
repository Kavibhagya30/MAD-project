package com.piieradication.agent.di

import android.content.Context
import androidx.room.Room
import com.piieradication.agent.data.local.AppEventDao
import com.piieradication.agent.data.local.DeletionRequestDao
import com.piieradication.agent.data.local.PiiDatabase
import com.piieradication.agent.data.local.PiiRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PiiDatabase =
        Room.databaseBuilder(context, PiiDatabase::class.java, "pii_eradication.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(database: PiiDatabase): PiiRecordDao = database.piiRecordDao()

    @Provides
    fun provideDeletionRequestDao(database: PiiDatabase): DeletionRequestDao =
        database.deletionRequestDao()

    @Provides
    fun provideAppEventDao(database: PiiDatabase): AppEventDao = database.appEventDao()
}
