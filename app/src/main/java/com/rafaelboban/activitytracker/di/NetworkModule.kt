package com.rafaelboban.activitytracker.di

import android.content.Context
import android.content.SharedPreferences
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.activitytracker.util.Constants.AUTH_TOKEN
import com.rafaelboban.activitytracker.worker.TokenRefreshWorker
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    fun provideAuthTokenInterceptor(
        @ApplicationContext application: Context,
        @PreferencesEncrypted preferences: SharedPreferences
    ) = Interceptor { chain ->
        val token = preferences.getString(AUTH_TOKEN, null)
        val request = chain.request().newBuilder().apply {
            header("Authorization", "Bearer ${preferences.getString(AUTH_TOKEN, null)}")
        }

        val response = chain.proceed(request.build())

        token?.let {
            try {
                val jwt = JWT(token)
                val shouldRefreshToken = jwt.expiresAt?.toInstant()?.isBefore(Instant.now().plus(1, ChronoUnit.DAYS)) == true

                if (shouldRefreshToken) {
                    TokenRefreshWorker.enqueue(application)
                }
            } catch (e: DecodeException) {
                Timber.e(e)
            }
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
