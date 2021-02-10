package ru.skillbranch.skillarticles.data.local.dao

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef

@Dao
interface TagsDao : BaseDao<Tag> {
    @Query("""
                SELECT tag FROM article_tags
                ORDER_BY use_count DESC
            """)
    fun findTags(): LiveData<List<String>>

    @Query(
            """
                UPDATE article_tags SET use_count = use_count + 1
                WHERE tag = :tag
            """
    )
    fun incrementTagUseCount(tag: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRefs(refs: List<ArticleTagXRef>): List<Long>
}