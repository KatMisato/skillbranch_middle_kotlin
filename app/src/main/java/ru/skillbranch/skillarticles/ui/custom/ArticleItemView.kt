package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.ArticleItemData
import ru.skillbranch.skillarticles.extensions.*
import java.lang.Math.max

class ArticleItemView constructor(context: Context) : ViewGroup(context), LayoutContainer {
    override val containerView = this

    val tv_date: TextView
    val tv_author: TextView
    val tv_title: TextView
    val iv_poster: ImageView
    val iv_category: ImageView
    val tv_description: TextView
    val iv_likes: ImageView
    val tv_likes_count: TextView
    val iv_comments: ImageView
    val tv_comments_count: TextView
    val tv_read_duration: TextView
    val iv_bookmark: ImageView

    val sizeText12sp = 12f
    val sizeText14sp = 14f
    val sizeText18sp = 18f

    val padding = context.dpToIntPx(16)

    val authorMarginStart = context.dpToIntPx(8)

    val barrierHeight = context.dpToIntPx(8)

    val titleMarginEnd = context.dpToIntPx(24)
    val titleMarginTopBottom = context.dpToIntPx(8)

    val descriptionMarginTop = context.dpToIntPx(8)

    val posterSize = containerView.context.dpToIntPx(64)
    val cornerRadius = containerView.context.dpToIntPx(8)
    val categorySize = containerView.context.dpToIntPx(40)
    val iconSize = context.dpToIntPx(16)

    @ColorInt
    private val textColorGray: Int = context.getColor(R.color.color_gray)

    @ColorInt
    private val textColorPrimary: Int = context.attrValue(R.attr.colorPrimary)

    init {
        this.setPadding(padding, padding, padding, padding)

        tv_date = addTextView(R.id.tv_date, sizeText12sp, textColorGray)
        tv_author = addTextView(R.id.tv_author, sizeText12sp, textColorPrimary)

        tv_title =  addTextView(R.id.tv_title, sizeText18sp, textColorPrimary, true)
        iv_poster = addImageViewWithId(R.id.iv_poster, posterSize)
        iv_category = addImageViewWithId(R.id.iv_category, categorySize)

        tv_description = addTextView(R.id.tv_description, sizeText14sp, textColorGray)

        iv_likes = addImageViewWithIcon(R.drawable.ic_favorite_black_24dp)
        tv_likes_count = addTextView(R.id.tv_likes_count, sizeText12sp, textColorGray)
        iv_comments = addImageViewWithIcon(R.drawable.ic_insert_comment_black_24dp)
        tv_comments_count = addTextView(R.id.tv_comments_count, sizeText12sp, textColorGray)
        tv_read_duration = addTextView(R.id.tv_read_duration, sizeText12sp, textColorGray)
        iv_bookmark = addImageViewWithIcon(R.drawable.bookmark_states)
    }

    private fun addTextView(@IdRes idView: Int, viewTextSize: Float, @ColorInt textColor: Int, isBold: Boolean = false): TextView {
        val tv = TextView(context).apply {
            id = idView
            textSize = viewTextSize
            setTextColor(textColor)
            if (isBold) {
                setTypeface(typeface, Typeface.BOLD)
            }
        }
        addView(tv)
        return tv
    }

    private fun addImageViewWithId(@IdRes idView: Int, size: Int): ImageView {
        val iv = ImageView(context).apply {
            id = idView
            layoutParams = LayoutParams(size, size)
        }
        addView(iv)
        return iv
    }

    private fun addImageViewWithIcon(@DrawableRes idDrawble: Int): ImageView {
        val iv = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize, iconSize)
            imageTintList = ColorStateList.valueOf(textColorGray)
            setImageResource(idDrawble)
        }
        addView(iv)
        return iv
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var currentHeight = paddingTop
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        // date + author
        measureChild(tv_date, widthMeasureSpec, heightMeasureSpec)
        tv_author.maxWidth = width - (tv_date.measuredWidth + authorMarginStart)
        measureChild(tv_author, widthMeasureSpec, heightMeasureSpec)
        currentHeight += tv_author.measuredHeight

        // title block
        val titleHeight = posterSize + categorySize / 2
        tv_title.maxWidth = width - (titleHeight + titleMarginEnd)
        measureChild(tv_title, widthMeasureSpec, heightMeasureSpec)
        currentHeight += max(tv_title.measuredHeight, titleHeight) + 2 * titleMarginTopBottom

        // description block
        measureChild(tv_description, widthMeasureSpec, heightMeasureSpec)
        currentHeight += tv_description.measuredHeight + descriptionMarginTop

        // icons block
        measureChild(tv_likes_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_comments_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_read_duration, widthMeasureSpec, heightMeasureSpec)

        currentHeight += iconSize + paddingBottom
        setMeasuredDimension(width, currentHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentHeight = paddingTop
        var paddingLeft = getPaddingLeft()
        val bodyWidth = right - left - paddingLeft - paddingRight

        //Log.e("ArticleItemView", "bodyWidth = $bodyWidth")

        // author + date
        tv_date.layout(paddingLeft, currentHeight, paddingLeft + tv_date.measuredWidth, currentHeight + tv_date.measuredHeight)
        paddingLeft += tv_date.right + authorMarginStart
        tv_author.layout(paddingLeft, currentHeight, paddingLeft + tv_author.measuredWidth, currentHeight + tv_author.measuredHeight)
        currentHeight += tv_author.measuredHeight

        paddingLeft = getPaddingLeft()

        // title block
        val titleHeight = posterSize + categorySize / 2
        if (titleHeight > tv_title.measuredHeight) {
            val diffH = (titleHeight - tv_title.measuredHeight) / 2 + 9
            tv_title.layout(paddingLeft, currentHeight + diffH, paddingLeft + tv_title.measuredWidth, currentHeight + diffH + tv_title.measuredHeight)
            paddingLeft = padding
            iv_poster.layout(paddingLeft + bodyWidth - posterSize, currentHeight, paddingLeft + bodyWidth, currentHeight + posterSize)
            iv_category.layout(iv_poster.left - categorySize / 2, iv_poster.bottom - categorySize / 2, iv_poster.left + categorySize / 2, iv_poster.bottom + categorySize / 2)
            currentHeight += titleHeight
        } else {
            val diffH = (tv_title.measuredHeight - titleHeight) / 2 + 9
            tv_title.layout(left, currentHeight, left + tv_title.measuredWidth, currentHeight + tv_title.measuredHeight)
            iv_poster.layout(left + bodyWidth - posterSize, currentHeight + diffH, left + bodyWidth, currentHeight + diffH + posterSize)
            iv_category.layout(iv_poster.left - categorySize / 2, iv_poster.bottom - categorySize / 2, iv_poster.left + categorySize / 2, iv_poster.bottom + categorySize / 2)
            currentHeight += tv_title.measuredHeight
        }
        paddingLeft = padding

        currentHeight += titleMarginTopBottom + barrierHeight

        // description
        tv_description.layout(paddingLeft, currentHeight, paddingLeft + bodyWidth, currentHeight + tv_description.measuredHeight)
        currentHeight += tv_description.measuredHeight + titleMarginTopBottom

        // icons block
        val fontDiff = iconSize - tv_likes_count.measuredHeight
        iv_likes.layout(paddingLeft, currentHeight - fontDiff, paddingLeft + iconSize, currentHeight + iconSize - fontDiff)
        paddingLeft = iv_likes.right + titleMarginTopBottom

        tv_likes_count.layout(paddingLeft, currentHeight, paddingLeft + tv_likes_count.measuredWidth, currentHeight + tv_likes_count.measuredHeight)
        paddingLeft = tv_likes_count.right + padding

        iv_comments.layout(paddingLeft, currentHeight - fontDiff, paddingLeft + iconSize, currentHeight + iconSize - fontDiff)
        paddingLeft = iv_comments.right + titleMarginTopBottom

        tv_comments_count.layout(paddingLeft, currentHeight, paddingLeft + tv_comments_count.measuredWidth, currentHeight + tv_comments_count.measuredHeight)
        paddingLeft = tv_comments_count.right + padding

        tv_read_duration.layout(paddingLeft, currentHeight, paddingLeft + tv_read_duration.measuredWidth, currentHeight + tv_read_duration.measuredHeight)
        paddingLeft = padding

        iv_bookmark.layout(paddingLeft + bodyWidth - iconSize, currentHeight - fontDiff, paddingLeft + bodyWidth, currentHeight + iconSize - fontDiff)
    }

    fun bind(item: ArticleItemData) {
        val cornerRadius = context.dpToIntPx(8)

        Glide.with(context)
                .load(item.poster)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .override(posterSize)
                .into(iv_poster)

        Glide.with(containerView.context)
                .load(item.poster)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .override(categorySize)
                .into(iv_category)

        tv_date.text = item.date.format()
        tv_author.text = item.author
        tv_title.text = item.title
        tv_description.text = item.description
        tv_likes_count.text = "${item.likeCount}"
        tv_comments_count.text = "${item.commentCount}"
        tv_read_duration.text = "${item.readDuration} min read"
    }
}