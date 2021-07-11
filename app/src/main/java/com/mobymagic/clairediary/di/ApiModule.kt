package com.mobymagic.clairediary.di

import android.content.Context
import com.mobymagic.clairediary.BuildConfig
import com.mobymagic.clairediary.Constants
import com.mobymagic.clairediary.api.ApiService
import com.mobymagic.clairediary.util.LiveDataCallAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

val apiModule = applicationContext {

    bean {
        createOkHttpClient(androidApplication(), BuildConfig.DEBUG)
    }

    bean {
        createWebService(get(), Constants.BASE_API_URL)
    }
}

private fun createOkHttpClient(context: Context, isDebugMode: Boolean): OkHttpClient {
    val diskCacheSize = 10L * 1024L * 1024L // 10MB
    val okHttpBuilder = OkHttpClient.Builder().cache(createOkHttpCache(context, diskCacheSize))

    if (isDebugMode) {
        okHttpBuilder.addInterceptor(createHttpLoggingInterceptor())
    }

    return okHttpBuilder.build()
}

private fun createWebService(okHttpClient: OkHttpClient, baseUrl: String): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(LiveDataCallAdapterFactory()).build()
    return retrofit.create(ApiService::class.java)
}

private fun createOkHttpCache(context: Context, diskCacheSize: Long): Cache {
    val cacheDir = File(context.cacheDir, "okHttp")
    return Cache(cacheDir, diskCacheSize)
}

private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return httpLoggingInterceptor
}