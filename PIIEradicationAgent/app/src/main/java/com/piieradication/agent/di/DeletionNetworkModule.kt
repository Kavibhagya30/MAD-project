package com.piieradication.agent.di

import com.piieradication.agent.data.remote.DeletionRequestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeletionNetworkModule {

    private const val BASE_URL = "https://httpbin.org/"

    @DeletionEndpoint
    @Provides
    @Singleton
    fun provideDeletionRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideDeletionRequestApi(@DeletionEndpoint retrofit: Retrofit): DeletionRequestApi =
        retrofit.create(DeletionRequestApi::class.java)
}
