package com.example.gradia.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import org.json.JSONArray
import org.json.JSONObject

data class RichTextData(
    val text: String,
    val boldRanges: List<IntRange>,
    val italicRanges: List<IntRange>,
    val imagePaths: List<String> = emptyList()
)

object RichTextUtil {

    fun serialize(text: String, boldRanges: List<IntRange>, italicRanges: List<IntRange>, imagePaths: List<String> = emptyList()): String {
        val obj = JSONObject()
        obj.put("t", text)
        obj.put("b", toJsonArray(boldRanges))
        obj.put("i", toJsonArray(italicRanges))
        obj.put("imgs", JSONArray(imagePaths))
        return obj.toString()
    }

    fun deserialize(json: String): RichTextData {
        return try {
            val obj = JSONObject(json)
            val text = obj.optString("t", "")
            val boldRanges = parseRanges(obj.optJSONArray("b"))
            val italicRanges = parseRanges(obj.optJSONArray("i"))
            val imagePaths = obj.optJSONArray("imgs")?.let { arr ->
                (0 until arr.length()).map { arr.optString(it, "") }.filter { it.isNotEmpty() }
            } ?: emptyList()
            RichTextData(text, boldRanges, italicRanges, imagePaths)
        } catch (e: Exception) {
            RichTextData(json, emptyList(), emptyList())
        }
    }

    fun extractPlainText(json: String): String {
        return try {
            JSONObject(json).optString("t", json)
        } catch (e: Exception) {
            json
        }
    }

    fun applyBold(ranges: List<IntRange>, target: IntRange): List<IntRange> {
        val result = ranges.toMutableList()
        val existing = result.indexOfFirst { it == target }
        if (existing >= 0) {
            result.removeAt(existing)
        } else {
            result.add(target)
        }
        return mergeRanges(result.sortedBy { it.first })
    }

    fun applyItalic(ranges: List<IntRange>, target: IntRange): List<IntRange> {
        return applyBold(ranges, target)
    }

    fun adjustRangesOnTextChange(
        ranges: List<IntRange>,
        oldLen: Int,
        newLen: Int
    ): List<IntRange> {
        val diff = newLen - oldLen
        if (diff == 0) return ranges
        return ranges.mapNotNull { range ->
            when {
                range.last < oldLen -> range
                range.first >= oldLen -> null
                else -> range.first until (range.last.coerceAtMost(newLen - 1) + 1)
            }
        }.filter { it.first < it.last }
    }

    fun visualTransformation(boldRanges: List<IntRange>, italicRanges: List<IntRange>): VisualTransformation {
        return VisualTransformation { text ->
            val annotated = buildString(text.text, boldRanges, italicRanges)
            TransformedText(annotated, OffsetMapping.Identity)
        }
    }

    private fun buildString(
        text: String,
        boldRanges: List<IntRange>,
        italicRanges: List<IntRange>
    ): AnnotatedString {
        return AnnotatedString.Builder(text).apply {
            for (range in boldRanges) {
                val start = range.first.coerceIn(0, text.length)
                val end = range.last.coerceIn(start, text.length - 1) + 1
                if (start < end) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                }
            }
            for (range in italicRanges) {
                val start = range.first.coerceIn(0, text.length)
                val end = range.last.coerceIn(start, text.length - 1) + 1
                if (start < end) {
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                }
            }
        }.toAnnotatedString()
    }

    private fun toJsonArray(ranges: List<IntRange>): JSONArray {
        val arr = JSONArray()
        for (range in ranges) {
            val pair = JSONArray()
            pair.put(range.first)
            pair.put(range.last)
            arr.put(pair)
        }
        return arr
    }

    private fun parseRanges(arr: JSONArray?): List<IntRange> {
        if (arr == null) return emptyList()
        val result = mutableListOf<IntRange>()
        for (i in 0 until arr.length()) {
            val pair = arr.optJSONArray(i) ?: continue
            if (pair.length() >= 2) {
                result.add(pair.getInt(0)..pair.getInt(1))
            }
        }
        return result
    }

    private fun mergeRanges(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return ranges
        val result = mutableListOf<IntRange>()
        var current = ranges.first()
        for (next in ranges.drop(1)) {
            if (next.first <= current.last + 1) {
                current = current.first..maxOf(current.last, next.last)
            } else {
                result.add(current)
                current = next
            }
        }
        result.add(current)
        return result
    }
}
