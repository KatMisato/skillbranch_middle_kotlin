package ru.skillbranch.skillarticles.data.models

data class ArticlePersonalInfo(
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val notSendedComment: String? = null
)