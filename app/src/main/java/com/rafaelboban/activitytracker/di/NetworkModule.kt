package com.rafaelboban.activitytracker.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.rafaelboban.activitytracker.data.session.AuthInfo
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.activitytracker.network.TokenRefreshService
import com.rafaelboban.activitytracker.network.model.TokenRefreshRequest
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_BASE_URL = "http://192.168.8.102:3000"

    @Provides
    @Singleton
    fun provideKotlinJsonAdapterFactory(): KotlinJsonAdapterFactory = KotlinJsonAdapterFactory()

    @Provides
    @Singleton
    fun provideMoshi(kotlinJsonAdapterFactory: KotlinJsonAdapterFactory): Moshi {
        return Moshi.Builder()
            .add(kotlinJsonAdapterFactory)
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { Timber.tag("OkHttp").d(it) }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authTokenInterceptor: Interceptor,
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRefreshService(
        loggingInterceptor: HttpLoggingInterceptor,
        moshi: Moshi
    ): TokenRefreshService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(API_BASE_URL)
            .build()

        return retrofit.create(TokenRefreshService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthTokenInterceptor(
        applicationScope: CoroutineScope,
        sessionStorage: EncryptedSessionStorage,
        refreshService: TokenRefreshService
    ) = Interceptor { chain ->
        val sessionData = runBlocking { sessionStorage.get() }
        val accessToken = sessionData?.accessToken
        val originalRequest = chain.request()

        val request = originalRequest.newBuilder().apply {
            header("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(request.build())

        if (response.code == 401 && sessionData?.refreshToken != null) {
            val refreshResponse = runBlocking { refreshService.refreshToken(TokenRefreshRequest(sessionData.refreshToken)) }
            val data = refreshResponse.getOrNull() ?: run {
                applicationScope.launch { sessionStorage.clear() }
                return@Interceptor response
            }

            val retryRequest = originalRequest.newBuilder().apply {
                header("Authorization", "Bearer ${data.accessToken}")
            }

            applicationScope.launch {
                sessionStorage.set(AuthInfo(data.user, data.accessToken, data.refreshToken))
            }

            response.close()

            return@Interceptor chain.proceed(retryRequest.build())
        }

        return@Interceptor response
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(API_BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
