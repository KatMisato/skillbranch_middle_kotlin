package ru.skillbranch.skillarticles.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.skillbranch.skillarticles.data.local.DateConverter
import java.util.*

@Entity(tableName = "article_personal_infos")
@TypeConverters(DateConverter::class)
data class ArticlePersonalInfo(
    @PrimaryKey
    @ColumnInfo(name = "article_id")
    val articleId: String,
    @ColumnInfo(name = "is_like")
    val isLike: Boolean = false,
    @ColumnInfo(name = "is_bookmark")
    val isBookmark: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)