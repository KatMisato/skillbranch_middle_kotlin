package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.Log
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
import java.lang.Math.abs
import java.lang.Math.max
import kotlin.math.round

class ArticleItemView constructor(context: Context) : ViewGroup(context), LayoutContainer {
    override val containerView = this

    private val tv_date: TextView
    private val tv_author: TextView
    private val tv_title: TextView
    private val iv_poster: ImageView
    private val iv_category: ImageView
    private val tv_description: TextView
    private val iv_likes: ImageView
    private val tv_likes_count: TextView
    private val iv_comments: ImageView
    private val tv_comments_count: TextView
    private val tv_read_duration: TextView
    private val iv_bookmark: ImageView

    private val sizeText12sp = 12f
    private val sizeText14sp = 14f
    private val sizeText18sp = 18f

    private val dpInPx8 = context.dpToIntPx(8)
    private val dpInPx16 = context.dpToIntPx(16)
    private val dpInPx24 = context.dpToIntPx(24)

    private val posterSize = containerView.context.dpToIntPx(64)
    private val cornerRadius = containerView.context.dpToIntPx(8)
    private val categorySize = containerView.context.dpToIntPx(40)
    private val titleHeight = containerView.context.dpToIntPx(84)

    private val iconSize = context.dpToIntPx(16)

    @ColorInt
    private val textColorGray: Int = context.getColor(R.color.color_gray)

    @ColorInt
    private val textColorPrimary: Int = context.attrValue(R.attr.colorPrimary)

    init {
        this.setPadding(dpInPx16, dpInPx16, dpInPx16, dpInPx16)

        tv_date = addTextView(R.id.tv_date, sizeText12sp, textColorGray)
        tv_author = addTextView(R.id.tv_author, sizeText12sp, textColorPrimary)

        tv_title = addTextView(R.id.tv_title, sizeText18sp, textColorPrimary, true)
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
        var usedHeight = dpInPx16
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        // date + author
        measureChild(tv_date, widthMeasureSpec, heightMeasureSpec)
        tv_author.maxWidth = width - (tv_date.measuredWidth + dpInPx16)
        measureChild(tv_author, widthMeasureSpec, heightMeasureSpec)
        usedHeight += tv_author.measuredHeight

        // title block
        tv_title.maxWidth = width - (titleHeight + dpInPx24)
        measureChild(tv_title, widthMeasureSpec, heightMeasureSpec)
        usedHeight += max(tv_title.measuredHeight, titleHeight) + dpInPx16

        // description block
        measureChild(tv_description, widthMeasureSpec, heightMeasureSpec)
        usedHeight += tv_description.measuredHeight + dpInPx8

        // icons block
        measureChild(tv_likes_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_comments_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_read_duration, widthMeasureSpec, heightMeasureSpec)

        val fontDiff = iconSize - tv_likes_count.measuredHeight
        usedHeight += iconSize + dpInPx16 - fontDiff + 1
        setMeasuredDimension(width, usedHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = r - l - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        //Log.e("ArticleItemView", "bodyWidth = $bodyWidth")

        // первая строчка - автор + дата
        val firstLineBottom = usedHeight + tv_date.measuredHeight
        tv_date.layout(left, usedHeight, left + tv_date.measuredWidth, firstLineBottom)
        tv_author.layout(left + tv_date.measuredWidth + dpInPx16, usedHeight, right, firstLineBottom)
        usedHeight += tv_author.measuredHeight + dpInPx8

        // заголовок + картинки
        val titleMeasuredHeight = tv_title.measuredHeight
        Log.e("ArticleItemView", "titleHeight = $titleHeight, titleMeasuredHeight = $titleMeasuredHeight")
        val topTitle = if (titleHeight > titleMeasuredHeight) {
            usedHeight + (titleHeight - titleMeasuredHeight) / 2
        } else {
            usedHeight
        }
        tv_title.layout(left, topTitle, left + tv_title.measuredWidth, topTitle + titleMeasuredHeight)
        iv_poster.layout(right - posterSize, usedHeight, right, usedHeight + posterSize)
        iv_category.layout(iv_poster.left - categorySize / 2, iv_poster.bottom - categorySize / 2, iv_poster.left + categorySize / 2, iv_poster.bottom + categorySize / 2)
        usedHeight += kotlin.math.max(titleHeight, titleMeasuredHeight) + dpInPx8

        // description
        tv_description.layout(left, usedHeight, left + bodyWidth, usedHeight + tv_description.measuredHeight)
        usedHeight += tv_description.measuredHeight + dpInPx8

        // icons block
        val fontDiff = iconSize - tv_likes_count.measuredHeight

        val topIcon = usedHeight - fontDiff
        val bottomIcon = topIcon + iconSize
        val bottomText = usedHeight + tv_likes_count.measuredHeight

        iv_likes.layout(left, topIcon, left + iconSize, bottomIcon)
        tv_likes_count.layout(iv_likes.right + dpInPx8, usedHeight, iv_likes.right + dpInPx8 + tv_likes_count.measuredWidth, bottomText)

        iv_comments.layout(tv_likes_count.right + dpInPx16, topIcon, tv_likes_count.right + dpInPx16 + iconSize, bottomIcon)
        tv_comments_count.layout(iv_comments.right + dpInPx8, usedHeight, iv_comments.right + dpInPx8 + tv_comments_count.measuredWidth, bottomText)

        tv_read_duration.layout(tv_comments_count.right + dpInPx16, usedHeight, tv_comments_count.right + dpInPx16 + tv_read_duration.measuredWidth, bottomText)

        iv_bookmark.layout(right - iconSize, topIcon, right, bottomIcon)
    }

    fun bind(item: ArticleItemData) {
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