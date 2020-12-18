package ru.skillbranch.skillarticles.ui.bookmarks

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

internal class BookmarksAdapter : PagedListAdapter<ArticleItemData, BookmarkVH>(BookmarkDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkVH {
        return BookmarkVH(ArticleItemView(parent.context))
    }

    override fun onBindViewHolder(holder: BookmarkVH, position: Int) = holder.bind(getItem(position))
}

internal class BookmarkDiffCallback : DiffUtil.ItemCallback<ArticleItemData>() {
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem == newItem
}

internal class BookmarkVH(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(
            item: ArticleItemData?
    ) {
        item?.let { (containerView as ArticleItemView).bind(it) }
    }
}