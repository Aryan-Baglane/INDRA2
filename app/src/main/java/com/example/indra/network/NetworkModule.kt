package com.example.indra.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    // For Android emulator, use 10.0.2.2 to access host machine's localhost
    // For physical device, use your computer's IP address
    private const val BASE_URL_ASSESSMENT = "https://backend-hydra.onrender.com/"
    private const val BASE_URL_CHATBOT = "https://rag-1-7afi.onrender.com/"


    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofitAssessment: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_ASSESSMENT)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val retrofitChatbot: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CHATBOT)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    val assessmentApiService: AssessmentApiService by lazy {
        retrofitAssessment.create(AssessmentApiService::class.java)
    }

    val chatbotApiService: ChatbotApiService by lazy {
        retrofitChatbot.create(ChatbotApiService::class.java)
    }
}
