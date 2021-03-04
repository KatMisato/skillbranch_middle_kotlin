package ru.skillbranch.skillarticles.data.local.entities

import androidx.room.*
import ru.skillbranch.skillarticles.data.local.DateConverter
import java.util.*

@Entity(
    tableName = "article_counts",
    foreignKeys = [ForeignKey(
        entity = Article::class,
        parentColumns = ["id"],
        childColumns = ["article_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArticleCounts(
    @PrimaryKey
    @ColumnInfo(name = "article_id")
    val articleId: String,
    val likes: Int = 0,
    val comments: Int = 0,
    @ColumnInfo(name = "read_duration")
    val readDuration: Int = 0,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)