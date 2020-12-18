package ru.skillbranch.skillarticles.ui.articles

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(private val listener: (ArticleItemData) -> Unit, private val bookmarkListener: (String, Boolean) -> Unit) : PagedListAdapter<ArticleItemData, ArticleVH>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val containerView = ArticleItemView(parent.context)
        return ArticleVH(containerView)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), listener,  bookmarkListener)
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<ArticleItemData>() {
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean = oldItem == newItem
}

class ArticleVH(override val containerView: ArticleItemView) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(
            item: ArticleItemData?,
            listener: (ArticleItemData) -> Unit,
            bookmarkListener: (String, Boolean) -> Unit
    ) {
        (containerView as ArticleItemView).bind(item!!, bookmarkListener)
        itemView.setOnClickListener { listener(item) }
    }
}