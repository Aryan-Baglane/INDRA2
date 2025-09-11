package com.example.indra.data

data class ChatbotRequest(
    val question: String
)

data class ChatbotAnswer(
    val query: String,
    val result: String
)

data class ChatbotResponse(
    val answer: ChatbotAnswer
)


