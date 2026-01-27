package ru.dan.rag.model

data class ParagraphBlock(
    val content: String
) : RawArticleBlock()