package ru.dan.rag.model

import java.util.*

/**
 * Модель для обновления чанка вектором.
 */
data class ChunkForProcessing(
    val id: UUID,
    val text: String
)