package ru.skillbranch.skillarticles.markdown

import android.util.Log
import java.util.regex.Pattern

object MarkdownParser {

    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP = "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d{1,2}\\.\\s.+?$)"
    private const val BLOCK_CODE_GROUP = "(^```[\\s\\S]+?```$)"


    const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$ORDERED_LIST_ITEM_GROUP|$BLOCK_CODE_GROUP"

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))

        return MarkdownText(elements)
    }

    fun clear(string: String?): String? {
        if (string == null) return null
        val markdown = parse(string)
        return StringBuilder().apply {
            markdown.elements.forEach {
                if (it.elements.isNotEmpty())
                    clear(it.text.toString(), this)
                else
                    apply { append(it.text) }
            }
        }.toString()
    }

    private fun clear(string: String, builder: StringBuilder) {
        val markdown = parse(string)
        builder.apply {
            markdown.elements.forEach {
                if (it.elements.isNotEmpty())
                    clear(it.text.toString(), this)
                else
                    apply { append(it.text) }
            }
        }
    }

    private fun getInnerText(element: Element, accum: String = ""): String {
        if (element.elements.isEmpty()) {
            return accum + element.text
        }
        for (innerElement in element.elements) {
            return getInnerText(innerElement, accum)
        }
        return accum
    }

    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            var text: CharSequence

            val groups = 1..11
            var group = -1

            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> break@loop

                // UNORDERED LIST
                1 -> {
                    // text without "* "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val sub = findElements(text)
                    val element = Element.UnorderedListItem(text, sub)

                    parents.add(element)

                    lastStartIndex = endIndex
                }
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // QUOTE
                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    val sub = findElements(text)
                    val element = Element.Quote(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // italic
                4 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())

                    val sub = findElements(text)
                    val element = Element.Italic(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // bold
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))

                    val sub = findElements(text)
                    val element = Element.Bold(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // strike
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))

                    val sub = findElements(text)
                    val element = Element.Strike(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // rule
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex

                }
                // inline `
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())

                    val sub = findElements(text)
                    val element = Element.InlineCode(text, sub)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // link
                9 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)

                    parents.add(element)
                    lastStartIndex = endIndex
                }
                // ORDERED
                10 -> {
                    // text without "1. "
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.subSequence(startIndex, endIndex))
                    val order = reg!!.value

                    text = string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()

                    // find inner elements
                    val subs = findElements(text)
                    val element = Element.OrderedListItem(order, text.toString(), subs)
                    parents.add(element)

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }

                // BLOCK CODE
                11 -> {
                    // text without "```{}```"
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3)).toString()
                    if (text.contains(LINE_SEPARATOR)) {
                        for ((index, line) in text.lines().withIndex()) {
                            when (index) {
                                text.lines().lastIndex -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.END, line)
                                )
                                0 -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.START, line + LINE_SEPARATOR)
                                )
                                else -> parents.add(
                                        Element.BlockCode(Element.BlockCode.Type.MIDDLE, line + LINE_SEPARATOR)
                                )
                            }
                        }
                    } else {
                        parents.add(Element.BlockCode(Element.BlockCode.Type.SINGLE, text))
                    }
                    lastStartIndex = endIndex
                }
            }
        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
            val level: Int = 1,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {
    }

    data class Quote(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {
    }

    data class Italic(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {
    }

    data class Bold(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {

    }

    data class Rule(
            override val text: CharSequence = " ",
            override val elements: List<Element> = emptyList()
    ) : Element() {

    }

    data class InlineCode(
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {

    }

    data class Link(
            val link: String,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {

    }

    data class OrderedListItem(
            val order: String,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
            val type: Type = Type.MIDDLE,
            override val text: CharSequence,
            override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }
}