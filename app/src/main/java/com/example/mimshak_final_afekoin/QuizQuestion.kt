package com.example.mimshak_final_afekoin

/** A single question entry parsed from assets/questions.json. */
data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctAnswer: String
)