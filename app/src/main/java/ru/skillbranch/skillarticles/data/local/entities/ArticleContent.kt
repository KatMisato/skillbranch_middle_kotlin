package ru.skillbranch.skillarticles.data.local.entities

import androidx.room.*
import ru.skillbranch.skillarticles.data.local.DateConverter
import java.util.*

@Entity(tableName = "article_contents",
        foreignKeys = [ForeignKey(
                entity = Article::class,
                parentColumns = ["id"],
                childColumns = ["article_id"],
                onDelete = ForeignKey.CASCADE
        )])
data class ArticleContent(
        @PrimaryKey
        @ColumnInfo(name = "article_id")
        val articleId: String,
        val content: String,
        val source: String? = null,
        @ColumnInfo(name = "share_link")
        val shareLink: String,
        @ColumnInfo(name = "updated_at")
        val updatedAt: Date = Date()
)