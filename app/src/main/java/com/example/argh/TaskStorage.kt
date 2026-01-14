package com.example.argh

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Task(
    val id: Int,
    val title: String,
    var isDone: Boolean
)

object TaskStorage {
    private const val PREF = "tasks_pref"
    private const val KEY_TASKS = "tasks_json"

    fun load(context: Context): MutableList<Task> {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val json = sp.getString(KEY_TASKS, "[]") ?: "[]"

        val arr = JSONArray(json)
        val list = mutableListOf<Task>()

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Task(
                    id = obj.getInt("id"),
                    title = obj.getString("title"),
                    isDone = obj.getBoolean("isDone")
                )
            )
        }
        return list
    }

    fun save(context: Context, tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("isDone", t.isDone)
            arr.put(obj)
        }

        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_TASKS, arr.toString()).apply()
    }

    fun completedCount(context: Context): Int = load(context).count { it.isDone }

    fun nextId(context: Context): Int {
        val tasks = load(context)
        return (tasks.maxOfOrNull { it.id } ?: 0) + 1
    }
}
