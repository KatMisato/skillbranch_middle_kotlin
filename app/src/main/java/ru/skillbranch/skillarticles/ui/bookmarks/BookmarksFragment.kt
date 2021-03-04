package ru.skillbranch.skillarticles.ui.bookmarks

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.articles.ArticlesAdapter
import ru.skillbranch.skillarticles.ui.articles.ArticlesFragmentDirections
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.base.MenuItemHolder
import ru.skillbranch.skillarticles.ui.base.ToolbarBuilder
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.bookmarks.BookmarksState
import ru.skillbranch.skillarticles.viewmodels.bookmarks.BookmarksViewModel

class BookmarksFragment : BaseFragment<BookmarksViewModel>() {
    override val viewModel: BookmarksViewModel by viewModels()
    override val layout: Int = R.layout.fragment_articles
    override val binding: BookmarksFragment.BookmarksBinding by lazy { BookmarksBinding() }

    override val prepareToolbar: (ToolbarBuilder.() -> Unit)? = {
        addMenuItem(
                MenuItemHolder("Search",
                        R.id.action_search,
                        R.drawable.ic_search_black_24dp,
                        R.layout.search_view_layout)
        )
    }

    private val bookmarksAdapter = ArticlesAdapter({ item, isChecked ->
        val action = ArticlesFragmentDirections.actionToPageArticle(
                item.id,
                item.author,
                item.authorAvatar ?: "",
                item.category,
                item.categoryIcon,
                item.poster,
                item.title,
                item.date
        )

        viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))
        viewModel.handleToggleBookmark(item.id)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = (menuItem?.actionView as SearchView)
        searchView.queryHint = getString(R.string.article_search_placeholder)

        if (binding.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(binding.searchQuery, false)
            if (binding.isFocusedSearch) searchView.requestFocus() else searchView.clearFocus()
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
    }

    override fun setupViews() {
        with(rv_bookmarks) {
            layoutManager = LinearLayoutManager(context)
            adapter = bookmarksAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        viewModel.observeList(viewLifecycleOwner) {
            bookmarksAdapter.submitList(it)
        }
    }

    inner class BookmarksBinding : Binding() {
        var isFocusedSearch = false
        var searchQuery: String? = null
        var isSearch: Boolean = false
        var isLoading: Boolean by RenderProp(true) {
            //TODO show shimmer on rv_list
        }

        override fun bind(data: IViewModelState) {
            data as BookmarksState
            isSearch = data.isSearch
            searchQuery = data.searchQuery
            isLoading = data.isLoading
        }

        override fun saveUi(outState: Bundle) {
            outState.putBoolean(::isLoading.name, isLoading)
            outState.putBoolean(::isSearch.name, isSearch)
            outState.putString(::searchQuery.name, searchQuery ?: "")
            outState.putBoolean(::isFocusedSearch.name, search_view?.hasFocus() ?: false)
        }

        override fun restoreUi(savedState: Bundle?) {
            isFocusedSearch = savedState?.getBoolean(::isFocusedSearch.name) ?: false
            searchQuery = savedState?.getString(::searchQuery.name)
            isSearch = savedState?.getBoolean(::isSearch.name) ?: false
            isLoading = savedState?.getBoolean(::isLoading.name) ?: false
        }
    }
}
