package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.local.entities.Tag
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.remote.res.ArticleRes
import ru.skillbranch.skillarticles.extensions.data.toArticle
import ru.skillbranch.skillarticles.extensions.data.toArticleCounts

interface IArticlesRepository {
    fun loadArticlesFromNetwork(start: Int = 0, size: Int): List<ArticleRes>
    fun insertArticlesToDb(articles: List<ArticleRes>)
    fun toggleBookmark(articleId: String)
    fun findTags(): LiveData<List<String>>
    fun findCategoriesData(): LiveData<List<CategoryData>>
    fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem>
    fun incrementTagUseCount(tag: String)
}

object ArticlesRepository : IArticlesRepository {
    private val network = NetworkDataHolder

    private var articlesDao = db.articlesDao()
    private var articleCountsDao = db.articleCountsDao()
    private var categoriesDao = db.categoriesDao()
    private var tagsDao = db.tagsDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setupTestDao(
            articlesDao: ArticlesDao,
            articleCountsDao: ArticleCountsDao,
            categoriesDao: CategoriesDao,
            tagsDao: TagsDao,
            articlePersonalDao: ArticlePersonalInfosDao
    ) {
        this.articlesDao = articlesDao
        this.articleCountsDao = articleCountsDao
        this.categoriesDao = categoriesDao
        this.tagsDao = tagsDao
        this.articlePersonalDao = articlePersonalDao
    }

    override fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleRes> =
            network.findArticlesItem(start, size)

    override fun insertArticlesToDb(articles: List<ArticleRes>) {
        articlesDao.upsert(articles.map { it.data.toArticle() })
        articleCountsDao.upsert(articles.map { it.counts.toArticleCounts() })

        val refs = articles.map { it.data }.fold(mutableListOf<Pair<String, String>>()) { acc, res ->
            acc.also { list -> list.addAll(res.tags.map { res.id to it }) }
        }

        val tags = refs.map { it.second }.distinct().map { Tag(it) }

        val xRefs = refs.map { ArticleTagXRef(it.first, it.second) }
        val categories = articles.map { it.data.category }

        categoriesDao.insert(categories)
        tagsDao.insert(tags)
        tagsDao.insertRefs(xRefs)
    }

    override fun toggleBookmark(articleId: String) = articlePersonalDao.toggleBookmarkOrInsert(articleId)

    override fun findTags(): LiveData<List<String>> = tagsDao.findTags()

    override fun findCategoriesData(): LiveData<List<CategoryData>> = categoriesDao.findAllCategoriesData()

    override fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem> = articlesDao.findArticlesByRaw(SimpleSQLiteQuery(filter.toQuery()))

    override fun incrementTagUseCount(tag: String) = tagsDao.incrementTagUseCount(tag)
}

class ArticleFilter(
        val search: String? = null,
        val isBookmark: Boolean = false,
        val categories: List<String> = listOf(),
        val isHashtag: Boolean = false
) {
    fun toQuery(): String {
        val qb = QueryBuilder()

        qb.table("ArticleItem")

        if (search != null && !isHashtag) qb.appendWhere("title LIKE %search%")
        if (search != null && isHashtag) {
            qb.innerJoin("article_tag_x_ref AS refs", "refs.a_id = id")
            qb.appendWhere("refs.t_id = '$search'")
        }
        if (isBookmark) qb.appendWhere("is_bookmark = 1")
        if (categories.isNotEmpty()) qb.appendWhere("category_id IN (${categories.joinToString(",")})")

        qb.orderBy("date")
        return qb.build()
    }
}

class QueryBuilder() {
    private var table: String? = null
    private var selectColumns: String = "*"
    private var joinTables: String? = null
    private var whereCondition: String? = null
    private var order: String? = null

    fun table(table: String): QueryBuilder {
        this.table = table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true): QueryBuilder {
        order = "ORDER BY $column ${if (isDesc) "DESC" else "ASC"}"
        return this
    }

    fun appendWhere(condition: String, logic: String = "AND"): QueryBuilder {
        if (whereCondition.isNullOrEmpty()) whereCondition = "WHERE $condition "
        else whereCondition += "$logic $condition "
        return this
    }

    fun innerJoin(table: String, on: String): QueryBuilder {
        if (joinTables.isNullOrEmpty()) joinTables = "INNER JOIN $table ON $on "
        else joinTables += "INNER JOIN $table ON $on "
        return this
    }

    fun build(): String {
        check(table != null) { "table must not be null" }

        val strBuilder = StringBuilder("SELECT ")
                .append("$selectColumns ")
                .append("FROM $table ")

        if (joinTables != null) strBuilder.append(joinTables)
        if (whereCondition != null) strBuilder.append(whereCondition)
        if (order != null) strBuilder.append(order)
        return strBuilder.toString()
    }

}

class ArticlesDataFactory(val strategy: ArticleStrategy) :
        DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)
}


class ArticleDataSource(private val strategy: ArticleStrategy) :
        PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
            params: LoadInitialParams,
            callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        callback.onResult(result)
    }
}

sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(
            private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
                itemProvider(start, size)
    }

    class SearchArticle(
            private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
            private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
                itemProvider(start, size, query)
    }

    class SearchBookmark(
            private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
            private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
                itemProvider(start, size, query)
    }

    class BookmarkArticles(
            private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
                itemProvider(start, size)
    }
}