package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element

class BlockCodeSpan(
        @ColorInt
        private val textColor: Int,
        @ColorInt
        private val bgColor: Int,
        @Px
        private val cornerRadius: Float,
        @Px
        private val padding: Float,
        private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var measureWidth: Int = 0

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    override fun getSize(
            paint: Paint,
            text: CharSequence,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            when (type) {
                Element.BlockCode.Type.START -> {
                    fm.ascent = (paint.ascent() - 2 * padding).toInt()
                    fm.descent = paint.descent().toInt()
                }
                Element.BlockCode.Type.END -> {
                    fm.ascent = paint.ascent().toInt()
                    fm.descent = (paint.descent() + 2 * padding).toInt()
                }
                Element.BlockCode.Type.MIDDLE -> {
                    fm.ascent = paint.ascent().toInt()
                    fm.descent = paint.descent().toInt()
                }
                Element.BlockCode.Type.SINGLE -> {
                    fm.ascent = (paint.ascent() - 2 * padding).toInt()
                    fm.descent = (paint.descent() + 2 * padding).toInt()
                }
            }

            fm.top = fm.ascent
            fm.bottom = fm.descent
        }
        return 0
    }

    override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
    ) {
        when (type) {
            Element.BlockCode.Type.START -> {
                paint.forBackground {
                    val corners = floatArrayOf(
                            cornerRadius, cornerRadius,   // top-left radius in px
                            cornerRadius, cornerRadius,   // top-right radius in px
                            0f, 0f,   // bottom-right radius in px
                            0f, 0f    // bottom-left radius in px
                    )
                    rect.set(0f, top + padding, canvas.width.toFloat(), bottom.toFloat())
                    path.reset()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }
            }
            Element.BlockCode.Type.END -> {
                paint.forBackground {
                    val corners = floatArrayOf(
                            0f, 0f,     // top-left radius in px
                            0f, 0f,     // top-right radius in px
                            cornerRadius, cornerRadius,   // bottom-right radius in px
                            cornerRadius, cornerRadius    // bottom-left radius in px
                    )
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom - padding)
                    path.reset()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }
            }
            Element.BlockCode.Type.MIDDLE -> {
                paint.forBackground {
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom.toFloat())
                    canvas.drawRect(rect, paint)
                }
            }
            Element.BlockCode.Type.SINGLE -> {
                paint.forBackground {
                    rect.set(0f, top + padding, canvas.width.toFloat(), bottom - padding)
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                }
            }
        }
        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldColor = color
        val oldFont = typeface
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0

        color = textColor
        textSize *= 0.85f
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)

        block()

        typeface = oldFont
        textSize = oldSize
        color = oldColor
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style

        color = bgColor
        style = Paint.Style.FILL

        block()

        style = oldStyle
        color = oldColor
    }
}