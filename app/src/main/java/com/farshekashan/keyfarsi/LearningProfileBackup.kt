package com.farshekashan.keyfarsi

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/** Portable, versioned backup of the user's learned Persian typing profile. */
internal object LearningProfileBackup {
    const val FILE_MIME = "application/json"
    const val FILE_PREFIX = "KeyFarsi-Learning-"
    private const val FORMAT = "keyfarsi-learning-profile"
    private const val SCHEMA_VERSION = 1
    private const val PREFS = "smhkeyboard_ia_v2"

    // Deliberately excludes clipboard contents and visual preferences.
    private val learningKeys = setOf(
        "trigrams", "bigrams", "correction_memory", "learned_words",
        "fa_neural_context_mlp_v1", "fa_neural_context_examples_v1"
    )

    data class ImportResult(val importedKeys: Int, val learnedExamples: Int)

    fun write(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val values = JSONObject()
        val all = prefs.all
        learningKeys.forEach { key ->
            val value = all[key] ?: return@forEach
            when (value) {
                is String -> values.put(key, value)
                is Int -> values.put(key, value)
                is Long -> values.put(key, value)
                is Float -> values.put(key, value.toDouble())
                is Boolean -> values.put(key, value)
            }
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("schema_version", SCHEMA_VERSION)
            .put("app", "KeyFarsi")
            .put("created_at_utc", System.currentTimeMillis())
            .put("learning_data", values)
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
            it.write(root.toString(2))
        } ?: error("امکان نوشتن فایل پشتیبان وجود ندارد")
    }

    fun read(context: Context, uri: Uri): ImportResult {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("فایل انتخاب‌شده قابل خواندن نیست")
        require(raw.length <= 8 * 1024 * 1024) { "حجم فایل پشتیبان غیرعادی است" }
        val root = JSONObject(raw)
        require(root.optString("format") == FORMAT) { "این فایل، پشتیبان یادگیری KeyFarsi نیست" }
        require(root.optInt("schema_version", -1) in 1..SCHEMA_VERSION) { "نسخه فایل پشتیبان پشتیبانی نمی‌شود" }
        val data = root.optJSONObject("learning_data") ?: error("اطلاعات یادگیری در فایل پیدا نشد")
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        var count = 0
        learningKeys.forEach { key ->
            if (!data.has(key) || data.isNull(key)) return@forEach
            val value = data.get(key)
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
                is Boolean -> editor.putBoolean(key, value)
                else -> return@forEach
            }
            count++
        }
        require(count > 0) { "این فایل هیچ داده یادگیری معتبری ندارد" }
        check(editor.commit()) { "ذخیره اطلاعات بازیابی‌شده ناموفق بود" }
        val examples = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt("fa_neural_context_examples_v1", 0)
        return ImportResult(count, examples)
    }
}
