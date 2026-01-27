package ru.dan.rag.model

data class ListBlock(
    val items: List<String>
) : RawArticleBlock()