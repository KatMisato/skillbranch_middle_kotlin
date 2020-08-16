package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState

class ArticleViewModel(private val articleId: String) :
        BaseViewModel<ArticleState>(ArticleState()), IArticleViewModel {
    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()) { article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                    shareLink = article.shareLink,
                    title = article.title,
                    author = article.author,
                    category = article.category,
                    categoryIcon = article.categoryIcon,
                    date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                    isLoadingContent = false,
                    content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) { info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                    isBookmark = info.isBookmark,
                    isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, state ->
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }
    }

    override fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()
        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                    "Don`t like it anymore", "No, still like it",
                    toggleLike
            )
        }
        notify(msg)
    }

    override fun handleBookmark() {
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    override fun handleShare() {
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    override fun handleToggleMenu() {
        updateState { state ->
            state.copy(isShowMenu = !state.isShowMenu)
        }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { state ->
            state.copy(isSearch = isSearch)
        }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        val result = (currentState.content.firstOrNull() as? String)
                .indexesOf(query)
                .map { it to it + query.length }
        updateState { state ->
            state.copy(searchQuery = query, searchResults = result, searchPosition = 0)
        }
    }

    fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }
}

data class ArticleState(
        val isAuth: Boolean = false, // пользователь авторизован
        val isLoadingContent: Boolean = true, // контент загружается
        val isLoadingReviews: Boolean = true, // отзывы загружается
        val isLike: Boolean = false, // отмечено как Like
        val isBookmark: Boolean = false, // в закладках
        val isShowMenu: Boolean = false, // отображается меню
        val isBigText: Boolean = false, // шрифт увеличен
        val isDarkMode: Boolean = false, // темный режим
        val isSearch: Boolean = false, // режим поиска
        val searchQuery: String? = null, // поисковый запрос
        val searchResults: List<Pair<Int, Int>> = emptyList(), // результаты поиска (стартовая и конечная позиции)
        val searchPosition: Int = 0, // текущая позиция найденного результата
        val shareLink: String? = null, // ссылка Share
        val title: String? = null, // заголовок статьи
        val category: String? = null, // категория
        val categoryIcon: Any? = null, // иконка категории
        val date: String? = null, // дата публикации
        val author: Any? = null,
        val poster: String? = null,
        val content: List<Any> = emptyList(),
        val reviews: List<Any> = emptyList()
) : IViewModelState {
    override fun save(outState: Bundle) {
        outState.putAll(
                bundleOf("isSearch" to isSearch,
                        "searchQuery" to searchQuery,
                        "searchResults" to searchResults,
                        "searchPosition" to searchPosition)
        )
    }

    override fun restore(savedState: Bundle): IViewModelState {
        return copy(isSearch = savedState["isSearch"] as Boolean,
                searchQuery = savedState["searchQuery"] as? String,
                searchResults = savedState["searchResults"] as List<Pair<Int, Int>>,
                searchPosition = savedState["searchPosition"] as Int
        )
    }
}
