package com.task.data.remote

import com.squareup.moshi.Moshi
import com.task.BASE_URL
import com.task.BuildConfig
import com.task.MOVIE_KEY
import com.task.data.remote.moshiFactories.MyKotlinJsonAdapterFactory
import com.task.data.remote.moshiFactories.MyStandardJsonAdapters
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


private const val timeoutRead = 60   //In seconds
private const val contentType = "Content-Type"
private const val contentTypeValue = "application/json"
private const val timeoutConnect = 60   //In seconds

@Singleton
class ServiceGenerator @Inject constructor() {
    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private val retrofit: Retrofit

    private var headerInterceptor = Interceptor { chain ->
        val original = chain.request()

        val url = original.url.newBuilder().addQueryParameter("api_key", MOVIE_KEY).build()
        val request = original.newBuilder()
            .header(contentType, contentTypeValue)
            .method(original.method, original.body)
            //.url(url)
            .build()

        chain.proceed(request)
    }

    private val logger: HttpLoggingInterceptor
        get() {
            val loggingInterceptor = HttpLoggingInterceptor()
            //if (BuildConfig.DEBUG) {
            loggingInterceptor.apply { level = HttpLoggingInterceptor.Level.BODY }
            //  }
            return loggingInterceptor
        }

    init {
        okHttpBuilder.addInterceptor(headerInterceptor)
        if (BuildConfig.DEBUG)
            okHttpBuilder.addInterceptor(logger)
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        val client = okHttpBuilder.build()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL).client(client)
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .build()
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }

    private fun getMoshi(): Moshi {
        return Moshi.Builder()
            .add(MyKotlinJsonAdapterFactory())
            .add(MyStandardJsonAdapters.FACTORY)
            .build()
    }
}


