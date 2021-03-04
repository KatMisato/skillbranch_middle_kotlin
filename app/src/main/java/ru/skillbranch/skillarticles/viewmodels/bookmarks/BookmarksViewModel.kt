package ru.skillbranch.skillarticles.viewmodels.bookmarks

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleFilter
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import java.util.concurrent.Executors

class BookmarksViewModel(handle: SavedStateHandle) : BaseViewModel<BookmarksState>(handle, BookmarksState()) {
    val repository = ArticlesRepository
    val listConfig by lazy {
        PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .setPrefetchDistance(30)
                .setInitialLoadSizeHint(50)
                .build()
    }

    private val listData = Transformations.switchMap(state) {
        val filter = it.toArticleFilter()
        return@switchMap buildPagedList(repository.rawQueryArticles(filter))
    }

    fun observeList(owner: LifecycleOwner,
                    onChange: (list: PagedList<ArticleItem>) -> Unit) {
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(
            dataFactory: DataSource.Factory<Int, ArticleItem>
    ): LiveData<PagedList<ArticleItem>> {
        val builder = LivePagedListBuilder<Int, ArticleItem>(
                dataFactory,
                listConfig
        )
        return builder.setFetchExecutor(Executors.newSingleThreadExecutor()).build()
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy(searchQuery = query) }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch) }
    }

    fun handleToggleBookmark(id: String) {
        repository.toggleBookmark(id)
        listData.value?.dataSource?.invalidate()
    }
}

data class BookmarksState(val isSearch: Boolean = false,
                         val searchQuery: String? = null,
                         val isLoading: Boolean = true
) : IViewModelState

private fun BookmarksState.toArticleFilter(): ArticleFilter = ArticleFilter(
        search = searchQuery,
        isBookmark = true
)
