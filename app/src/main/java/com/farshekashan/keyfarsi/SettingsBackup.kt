package com.farshekashan.keyfarsi

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/** Independent backup for keyboard settings; learned typing data is intentionally excluded. */
internal object SettingsBackup {
    const val FILE_MIME = "application/json"
    const val FILE_PREFIX = "SMHKeyboard-Settings-"
    private const val FORMAT = "smhkeyboard-settings"
    private const val VERSION = 1
    private const val PREFS = "smhkeyboard_ia_v2"
    private val learningKeys = setOf(
        "trigrams", "bigrams", "correction_memory", "learned_words",
        "fa_neural_context_mlp_v1", "fa_neural_context_examples_v1", "clipboard"
    )

    fun write(context: Context, uri: Uri) {
        val all = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
        val values = JSONObject()
        all.forEach { (key, value) ->
            if (key in learningKeys) return@forEach
            val item = JSONObject()
            when (value) {
                is String -> item.put("type", "string").put("value", value)
                is Int -> item.put("type", "int").put("value", value)
                is Long -> item.put("type", "long").put("value", value)
                is Float -> item.put("type", "float").put("value", value.toDouble())
                is Boolean -> item.put("type", "boolean").put("value", value)
                is Set<*> -> {
                    item.put("type", "string_set")
                    val array = JSONArray()
                    value.filterIsInstance<String>().forEach { array.put(it) }
                    item.put("value", array)
                }
                else -> return@forEach
            }
            values.put(key, item)
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("created_at", System.currentTimeMillis())
            .put("settings", values)
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
            it.write(root.toString(2))
        } ?: error("امکان نوشتن فایل پشتیبان وجود ندارد")
    }

    fun read(context: Context, uri: Uri): Int {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("فایل انتخاب‌شده قابل خواندن نیست")
        require(raw.length <= 8 * 1024 * 1024) { "حجم فایل پشتیبان غیرعادی است" }
        val root = JSONObject(raw)
        require(root.optString("format") == FORMAT) { "این فایل، پشتیبان تنظیمات کیبورد نیست" }
        require(root.optInt("version", -1) in 1..VERSION) { "نسخه فایل پشتیبان پشتیبانی نمی‌شود" }
        val values = root.optJSONObject("settings") ?: error("تنظیماتی در فایل پیدا نشد")
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        var count = 0
        val keys = values.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key in learningKeys) continue
            val item = values.optJSONObject(key) ?: continue
            if (!item.has("value") || item.isNull("value")) continue
            when (item.optString("type")) {
                "string" -> editor.putString(key, item.getString("value"))
                "int" -> editor.putInt(key, item.getInt("value"))
                "long" -> editor.putLong(key, item.getLong("value"))
                "float" -> editor.putFloat(key, item.getDouble("value").toFloat())
                "boolean" -> editor.putBoolean(key, item.getBoolean("value"))
                "string_set" -> {
                    val array = item.getJSONArray("value")
                    val set = linkedSetOf<String>()
                    for (i in 0 until array.length()) set.add(array.getString(i))
                    editor.putStringSet(key, set)
                }
                else -> continue
            }
            count++
        }
        require(count > 0) { "هیچ تنظیم معتبر و قابل بازیابی در فایل پیدا نشد" }
        check(editor.commit()) { "ذخیره تنظیمات بازیابی‌شده ناموفق بود" }
        return count
    }
}
