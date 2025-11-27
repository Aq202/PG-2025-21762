package com.example.public_transport_app.di

import android.content.Context
import com.example.public_transport_app.data.local.DataStoreManager
import com.example.public_transport_app.data.local.EncryptedPreferencesManager
import com.example.public_transport_app.data.remote.API
import com.example.public_transport_app.data.remote.AuthAPI
import com.example.public_transport_app.data.remote.auth.AuthInterceptor
import com.example.public_transport_app.data.remote.auth.TokenAuthenticator
import com.example.public_transport_app.data.repository.AgencyRepository
import com.example.public_transport_app.data.repository.AgencyRepositoryImp
import com.example.public_transport_app.data.repository.SessionRepository
import com.example.public_transport_app.data.repository.SessionRepositoryImp
import com.example.public_transport_app.data.remote.auth.TokenProvider
import com.example.public_transport_app.data.remote.auth.TokenProviderImp
import com.example.public_transport_app.data.repository.RouteRepository
import com.example.public_transport_app.data.repository.RouteRepositoryImp
import com.example.public_transport_app.data.repository.StopRepository
import com.example.public_transport_app.data.repository.StopRepositoryImp
import com.example.public_transport_app.utils.apiUrl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Interceptar petición para agregar auth token
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: okhttp3.OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(
            retrofit: Retrofit
    ): API {
        return retrofit
                .create(API::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(): AuthAPI {
        return Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(
            context,
        )
    }

    @Provides
    @Singleton
    fun provideEncryptedReferencesManager(
        @ApplicationContext context: Context
    ): EncryptedPreferencesManager {
        return EncryptedPreferencesManager(
            context,
        )
    }

    @Provides
    @Singleton
    fun provideSessionRepository(impl: SessionRepositoryImp): SessionRepository = impl

    @Provides
    @Singleton
    fun provideTokenProvider(
        impl: TokenProviderImp,
    ): TokenProvider = impl

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): AuthInterceptor =
        AuthInterceptor(tokenProvider)

    @Provides @Singleton
    fun provideTokenAuthenticator(tokenProvider: TokenProvider): TokenAuthenticator =
        TokenAuthenticator(tokenProvider)

    @Provides
    @Singleton
    fun provideAgencyRepository(api: API): AgencyRepository =
        AgencyRepositoryImp(api)

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient() // Para ser más flexible con JSON malformado
            .create()
    }

    @Provides
    @Singleton
    fun provideRouteRepository(api: API, gson: Gson): RouteRepository =
        RouteRepositoryImp(api, gson)@Provides

    @Singleton
    fun provideStopRepository(api: API): StopRepository =
        StopRepositoryImp(api)

}
