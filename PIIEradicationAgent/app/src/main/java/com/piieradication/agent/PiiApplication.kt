package com.piieradication.agent

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.piieradication.agent.data.notification.NotificationHelper
import com.piieradication.agent.domain.usecase.SchedulePiiSyncUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PiiApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var schedulePiiSyncUseCase: SchedulePiiSyncUseCase

    @Inject lateinit var notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannel()
        // Kick off the real periodic background sync as soon as the
        // app process starts. WorkManager persists this across
        // process death and device reboot (ExistingPeriodicWorkPolicy.KEEP
        // avoids re-enqueuing duplicates on every cold start).
        schedulePiiSyncUseCase.schedulePeriodic()
    }
}
