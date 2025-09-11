package com.example.indra.data

import com.example.indra.network.ChatbotApiService
import com.example.indra.network.NetworkModule

interface ChatbotRepository {
    suspend fun ask(question: String): Result<String>
}

class NetworkChatbotRepository(
    private val api: ChatbotApiService
) : ChatbotRepository {
    override suspend fun ask(question: String): Result<String> {
        return try {
            val response = api.askQuestion(ChatbotRequest(question))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.answer.result)
                } else {
                    Result.failure(IllegalStateException("Empty response body"))
                }
            } else {
                Result.failure(IllegalStateException("HTTP ${'$'}{response.code()}: ${'$'}{response.message()}"))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}

object ChatbotRepositoryProvider {
    fun repository(): ChatbotRepository = NetworkChatbotRepository(NetworkModule.chatbotApiService)
}


