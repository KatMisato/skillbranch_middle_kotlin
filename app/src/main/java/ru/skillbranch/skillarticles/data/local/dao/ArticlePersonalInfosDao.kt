package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo

@Dao
interface ArticlePersonalInfosDao : BaseDao<ArticlePersonalInfo> {
    @Transaction
    fun upsert(list: List<ArticlePersonalInfo>) {
        insert(list)
                .mapIndexed { index, recordResult -> if (recordResult == -1L) list[index] else null }
                .filterNotNull()
                .also { if (it.isNotEmpty()) update(it) }

    }

    @Query(
            """
                UPDATE article_personal_infos SET is_like = NOT is_like, update_at = CURRENT_TIMESTAMP
                WHERE article_id = :articeId
            """
    )
    fun toggleLike(articeId: String): Int


    @Query(
            """
                UPDATE article_personal_infos SET is_bookmark = NOT is_bookmark, update_at = CURRENT_TIMESTAMP
                WHERE article_id = :articeId
            """
    )
    fun toggleBookmark(articeId: String): Int

    @Query(
            """
                UPDATE article_personal_infos SET is_bookmark = NOT is_bookmark, update_at = CURRENT_TIMESTAMP
                WHERE article_id = :articeId
            """
    )

    fun toggleLikeOrInsert(articeId: String) {
        if (toggleLike(articeId) == 0) insert(ArticlePersonalInfo(articleId = articeId, isBookmark = true))
    }

    fun toggleBookmarkOrInsert(articeId: String) {
        if (toggleBookmark(articeId) == 0) insert(ArticlePersonalInfo(articleId = articeId, isBookmark = true))
    }
}