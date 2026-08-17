package com.piieradication.agent.di

import javax.inject.Qualifier

/** Distinguishes the deletion-request Retrofit instance from the main [UserApi] one. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeletionEndpoint
