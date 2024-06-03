package com.rafaelboban.activitytracker.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rafaelboban.activitytracker.data.session.AuthInfo
import com.rafaelboban.activitytracker.data.session.EncryptedSessionStorage
import com.rafaelboban.activitytracker.network.ApiService
import com.rafaelboban.activitytracker.network.TokenRefreshService
import com.rafaelboban.activitytracker.network.model.TokenRefreshRequest
import com.rafaelboban.activitytracker.network.ws.WebSocketClient
import com.skydoves.sandwich.getOrNull
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val API_BASE_URL = "http://192.168.8.102:3000"

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideJsonRetrofitAdapter(json: Json) = json.asConverterFactory("application/json".toMediaType())

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
            .pingInterval(30.seconds.toJavaDuration())
            .addInterceptor(ChuckerInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRefreshService(
        loggingInterceptor: HttpLoggingInterceptor,
        jsonConverterFactory: Converter.Factory
    ): TokenRefreshService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
            .addConverterFactory(jsonConverterFactory)
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
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
            .addConverterFactory(jsonConverterFactory)
            .baseUrl(API_BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWebSocketClient(okHttpClient: OkHttpClient) = WebSocketClient(okHttpClient)
}
