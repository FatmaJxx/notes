package com.example.notes.core.agent.chat.domain

import com.example.notes.core.model.AppColor
import com.example.notes.core.model.ChecklistItem
import com.example.notes.core.model.Reminder
import com.example.notes.core.model.Tag
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlin.collections.get

data class ToolArguments(val values: Map<String, Any?>) {
    fun getString(name: String): String? {
        val value = values[name]?.toString()?.trim()
        return value?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    }

    fun getReminder(name: String): Reminder? {
        val value = values[name]?.toString()?.trim() ?: return null
        return try {
            val json = JSONObject(value)
            val month = json.getInt("month")
            val day = json.getInt("day")

            val hour = json.getInt("hour")

            val minute = json.getInt("minute")

            val calender = Calendar.getInstance()

            calender.apply {
                set(Calendar.YEAR, calender.get(Calendar.YEAR))
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val timestamp = calender.timeInMillis

            val reminder = Reminder(noteId = 0, timestamp = timestamp)

            return reminder
        } catch (_: Exception) {
            null
        }
    }

    fun getColor(name: String): AppColor? {
        val text = getString(name) ?: return null
        return AppColor.entries.firstOrNull { it.name.equals(text, ignoreCase = true) }
    }

    fun getTags(name: String): List<Tag> {
        val raw = values[name] ?: return emptyList()

        val list = when (raw) {
            is List<*> -> raw
            is JSONArray -> (0 until raw.length()).map { raw.opt(it) }
            is String -> runCatching {
                val array = JSONArray(raw)
                (0 until array.length()).map { array.opt(it) }
            }.getOrNull()

            else -> null
        } ?: return emptyList()

        val tags = mutableListOf<Tag>()

        for (item in list) {
            val tagName = when (item) {
                is Map<*, *> -> item["name"]?.toString()
                is JSONObject -> item.optString("name")
                else -> null
            }?.trim()?.trimStart('#')?.trim() ?: continue

            if (tagName.isNotEmpty()) {
                tags.add(Tag(name = tagName))
            }
        }

        return tags
    }

    fun getCheckListItems(name: String): List<ChecklistItem> {
        val raw = values[name] ?: return emptyList()

        val list = when (raw) {
            is List<*> -> raw
            is JSONArray -> (0 until raw.length()).map { raw.opt(it) }
            is String -> runCatching {
                val array = JSONArray(raw)
                (0 until array.length()).map { array.opt(it) }
            }.getOrNull()

            else -> null
        } ?: return emptyList()

        val items = mutableListOf<ChecklistItem>()

        for (item in list) {
            val text = when (item) {
                is Map<*, *> -> item["text"]?.toString()
                is JSONObject -> item.optString("text")
                else -> null
            }?.trim()
                ?.trimStart('-', '*', '•', ' ')
                ?.replace(Regex("^\\d+[.)]\\s*"), "")
                ?.trim() ?: continue

            val isDone = when (item) {
                is Map<*, *> -> item["isDone"] as? Boolean
                is JSONObject -> item.optBoolean("isDone")
                else -> false
            } ?: false

            items.add(ChecklistItem(text = text, isDone = isDone))
        }

        return items
    }

    fun getBoolean(name: String): Boolean? = when (val value = values[name]) {
        is Boolean -> value
        is String -> value.trim().toBooleanStrictOrNull()
        else -> null
    }
}