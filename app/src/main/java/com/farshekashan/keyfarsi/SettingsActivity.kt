package com.farshekashan.keyfarsi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : Activity() {
    companion object {
        private const val REQUEST_EXPORT_LEARNING = 7101
        private const val REQUEST_IMPORT_LEARNING = 7102
        private const val REQUEST_EXPORT_SETTINGS = 7103
        private const val REQUEST_IMPORT_SETTINGS = 7104
    }
    private val prefs by lazy { getSharedPreferences("smhkeyboard_ia_v2", MODE_PRIVATE) }

    private data class MergeOption(
        val enableKey: String,
        val deleteKey: String,
        val label: String
    )

    private val mergeOptions = listOf(
        MergeOption("merge_key_kaf",    "merge_key_kaf_delete",    "ادغام ک و گ (نگه‌داشتن ک → گ)"),
        MergeOption("merge_b_pe", "merge_b_pe_delete", "ادغام ب و پ (نگه‌داشتن ب → پ)"),
        MergeOption("merge_key_re",     "merge_key_re_delete",     "ادغام ر و ز (نگه‌داشتن ر → ز)"),
        MergeOption("merge_z_zh", "merge_z_zh_delete", "ادغام ز و ژ (نگه‌داشتن ز → ژ)"),
        MergeOption("merge_ta_za",      "merge_ta_za_delete",      "ادغام ط و ظ (نگه‌داشتن ط → ظ)"),
        MergeOption("merge_sad_zad",    "merge_sad_zad_delete",    "ادغام ص و ض (نگه‌داشتن ص → ض)"),
        MergeOption("merge_dal_zal",    "merge_dal_zal_delete",    "ادغام د و ذ (نگه‌داشتن د → ذ)"),
        MergeOption("merge_seen_the",   "merge_seen_the_delete",   "ادغام س و ش (نگه‌داشتن س → ش)"),
        MergeOption("merge_ta_se", "merge_ta_se_delete", "ادغام ت و ث (نگه‌داشتن ت → ث)"),
        MergeOption("merge_f_qaf", "merge_f_qaf_delete", "ادغام ف و ق (نگه‌داشتن ف → ق)"),
        MergeOption("merge_ain_ghain",  "merge_ain_ghain_delete",  "ادغام ع و غ (نگه‌داشتن ع → غ)"),
        MergeOption("merge_he_khe",     "merge_he_khe_delete",     "ادغام ح و ج (نگه‌داشتن ح → ج)"),
        MergeOption("merge_ch_kh", "merge_ch_kh_delete", "ادغام چ و خ (نگه‌داشتن چ → خ)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F2FA.toInt())
        }

        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(8), dp(4), 0)
        }

        val tabContent = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val generalScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(createGeneralTab())
        }
        val standardScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(createStandardTab())
            visibility = View.GONE
        }
        val englishScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(createEnglishTab())
            visibility = View.GONE
        }
        val themeScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(createThemeTab())
            visibility = View.GONE
        }

        tabContent.addView(generalScroll)
        tabContent.addView(standardScroll)
        tabContent.addView(englishScroll)
        tabContent.addView(themeScroll)

        val btnGeneral = Button(this).apply {
            text = "عمومی"
            setOnClickListener {
                generalScroll.visibility = View.VISIBLE
                standardScroll.visibility = View.GONE
                englishScroll.visibility = View.GONE
                themeScroll.visibility = View.GONE
            }
        }
        val btnStandard = Button(this).apply {
            text = "استاندارد"
            setOnClickListener {
                generalScroll.visibility = View.GONE
                standardScroll.visibility = View.VISIBLE
                englishScroll.visibility = View.GONE
                themeScroll.visibility = View.GONE
            }
        }
        val btnEnglish = Button(this).apply {
            text = "انگلیسی"
            setOnClickListener {
                generalScroll.visibility = View.GONE
                standardScroll.visibility = View.GONE
                englishScroll.visibility = View.VISIBLE
                themeScroll.visibility = View.GONE
            }
        }
        val btnTheme = Button(this).apply {
            text = "تم و ظاهر"
            setOnClickListener {
                generalScroll.visibility = View.GONE
                standardScroll.visibility = View.GONE
                englishScroll.visibility = View.GONE
                themeScroll.visibility = View.VISIBLE
            }
        }

        listOf(btnGeneral, btnStandard, btnEnglish, btnTheme).forEach { button ->
            button.isAllCaps = false
            button.textSize = 12f
            button.minWidth = 0
            button.setPadding(dp(2), 0, dp(2), 0)
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE5DDF2.toInt())
            button.setTextColor(0xFF332B45.toInt())
        }
        fun selectTab(selected: Button) {
            listOf(btnGeneral, btnStandard, btnEnglish, btnTheme).forEach { button ->
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (button === selected) 0xFF6750A4.toInt() else 0xFFE5DDF2.toInt()
                )
                button.setTextColor(if (button === selected) android.graphics.Color.WHITE else 0xFF332B45.toInt())
            }
        }
        selectTab(btnGeneral)
        btnGeneral.setOnClickListener { generalScroll.visibility = View.VISIBLE; standardScroll.visibility = View.GONE; englishScroll.visibility = View.GONE; themeScroll.visibility = View.GONE; selectTab(btnGeneral) }
        btnStandard.setOnClickListener { generalScroll.visibility = View.GONE; standardScroll.visibility = View.VISIBLE; englishScroll.visibility = View.GONE; themeScroll.visibility = View.GONE; selectTab(btnStandard) }
        btnEnglish.setOnClickListener { generalScroll.visibility = View.GONE; standardScroll.visibility = View.GONE; englishScroll.visibility = View.VISIBLE; themeScroll.visibility = View.GONE; selectTab(btnEnglish) }
        btnTheme.setOnClickListener { generalScroll.visibility = View.GONE; standardScroll.visibility = View.GONE; englishScroll.visibility = View.GONE; themeScroll.visibility = View.VISIBLE; selectTab(btnTheme) }

        tabBar.addView(btnGeneral, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        tabBar.addView(btnStandard, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        tabBar.addView(btnEnglish, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        tabBar.addView(btnTheme, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })

        mainLayout.addView(tabBar)
        mainLayout.addView(tabContent)
        setContentView(mainLayout)

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    // ---------------------------------------------------------------------
    // تب عمومی
    // ---------------------------------------------------------------------
    private fun createGeneralTab(): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(24))
        }

        val title = TextView(this).apply {
            text = "⌨️  کلید فارسی"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(8), 0, dp(20))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        outer.addView(title)

        val enable = Button(this).apply {
            text = "۱. فعال‌سازی کیبورد"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        outer.addView(enable, match())

        val choose = Button(this).apply {
            text = "۲. انتخاب کلید فارسی"
            setOnClickListener {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showInputMethodPicker()
            }
        }
        outer.addView(choose, match())

        outer.addView(sectionTitle("پشتیبان مستقل تنظیمات کیبورد"))
        outer.addView(TextView(this).apply {
            text = "اندازه‌ها، ارتفاع و عرض کلیدها، چیدمان، ادغام حروف، پوسته، رنگ‌ها و سایر تنظیمات در یک فایل جداگانه ذخیره می‌شود. این پشتیبان مستقل از اطلاعات یادگیری است و آن‌ها را تغییر نمی‌دهد."
            textSize = 14f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        outer.addView(Button(this).apply {
            text = "تهیه پشتیبان تنظیمات کیبورد"
            setOnClickListener { createSettingsBackup() }
        }, match())
        outer.addView(Button(this).apply {
            text = "بازیابی تنظیمات از فایل پشتیبان"
            setOnClickListener { openSettingsBackup() }
        }, match())
        outer.addView(TextView(this).apply {
            text = "فایل با نام SMHKeyboard-Settings-تاریخ.json ذخیره می‌شود."
            textSize = 12f
            setTextColor(0xFF6B6475.toInt())
            setPadding(0, dp(4), 0, dp(16))
        })

        outer.addView(sectionTitle("پشتیبان یادگیری و انتقال به گوشی دیگر"))
        outer.addView(TextView(this).apply {
            text = "اطلاعات یادگرفته‌شده، اصلاحات شخصی، واژه‌های شما و وزن‌های مدل در یک فایل JSON قابل‌انتقال ذخیره می‌شود. این فایل را می‌توانی در پوشه KeyFarsi-Learning نگه داری یا روی هاست خودت بارگذاری کنی و در گوشی دیگر بازیابی کنی. محتوای کلیپ‌بورد صادر نمی‌شود."
            textSize = 14f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        outer.addView(Button(this).apply {
            text = "تهیه فایل پشتیبان یادگیری"
            setOnClickListener { createLearningBackup() }
        }, match())
        outer.addView(Button(this).apply {
            text = "بازیابی یادگیری از فایل پشتیبان"
            setOnClickListener { openLearningBackup() }
        }, match())
        outer.addView(TextView(this).apply {
            text = "مسیر پیشنهادی: حافظه داخلی / Download / KeyFarsi-Learning /\nنام فایل: KeyFarsi-Learning-تاریخ.json"
            textSize = 12f
            setTextColor(0xFF6B6475.toInt())
            setPadding(0, dp(8), 0, dp(16))
        })

        outer.addView(sectionTitle("تصحیح خودکار"))
        val autoCorrect = CheckBox(this).apply {
            text = "فعال‌سازی جایگزینی خودکار کلمات"
            isChecked = prefs.getBoolean("auto_correct", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_correct", checked).apply()
            }
        }
        outer.addView(autoCorrect)
        outer.addView(TextView(this).apply {
            text = "اگر خاموش باشد، کلمات شما به‌صورت خودکار تغییر نمی‌کنند، اما پیشنهادها نمایش داده می‌شوند و یادگیری کیبورد ادامه پیدا می‌کند."
            textSize = 12f
            setTextColor(0xFF8A8399.toInt())
            setPadding(0, dp(4), 0, dp(8))
        })

        // ✅ توضیح اصلاح‌شده: این تنظیم مدت زمان تایپ کل کلمه را می‌سنجد
        addNumberField(
            outer,
            "آستانه تایپ آرام (میلی‌ثانیه)\nاگر کل کلمه را در بیش از این مدت تایپ کنی، تصحیح خودکار انجام نمی‌شود. بیشتر = تصحیح کمتر\nپیشنهاد: ۱۵۰۰ms برای تایپ عادی",
            "slow_typing_threshold",
            1500,
            300,
            5000
        )

        outer.addView(sectionTitle("ادغام کلیدها (قابل تنظیم در هر دو حالت)"))
        outer.addView(TextView(this).apply {
            text = "برای هر ادغام، دو گزینه داری:\n• «فعال‌سازی ادغام»: حرف دوم کم‌رنگ پایین حرف اول نمایش داده می‌شود و با نگه‌داشتن دکمه، حرف دوم تایپ می‌شود.\n• «حذف کلید دوم»: کلید حرف دوم به‌طور کامل از کیبورد حذف می‌شود."
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        addMergeSection(outer)

        return outer
    }

    private fun addMergeSection(parent: LinearLayout) {
        mergeOptions.forEach { option ->
            val enableCb = CheckBox(this).apply {
                text = option.label
                isChecked = prefs.getBoolean(option.enableKey, false)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean(option.enableKey, checked).apply()
                    Toast.makeText(
                        this@SettingsActivity,
                        "برای اعمال تغییرات، کیبورد را ببندید و دوباره باز کنید",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            parent.addView(enableCb)

            val deleteCb = CheckBox(this).apply {
                text = "   ↳ حذف کلید دوم از کیبورد"
                textSize = 13f
                isChecked = prefs.getBoolean(option.deleteKey, false)
                setPadding(dp(24), 0, 0, dp(6))
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean(option.deleteKey, checked).apply()
                    Toast.makeText(
                        this@SettingsActivity,
                        "برای اعمال تغییرات، کیبورد را ببندید و دوباره باز کنید",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            parent.addView(deleteCb)
        }
    }

    private fun createSettingsBackup() {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = SettingsBackup.FILE_MIME
            putExtra(Intent.EXTRA_TITLE, "${SettingsBackup.FILE_PREFIX}$stamp.json")
        }
        startActivityForResult(intent, REQUEST_EXPORT_SETTINGS)
    }

    private fun openSettingsBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT_SETTINGS)
    }

    private fun createLearningBackup() {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = LearningProfileBackup.FILE_MIME
            putExtra(Intent.EXTRA_TITLE, "${LearningProfileBackup.FILE_PREFIX}$stamp.json")
        }
        startActivityForResult(intent, REQUEST_EXPORT_LEARNING)
    }

    private fun openLearningBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT_LEARNING)
    }

    @Deprecated("Uses document picker result for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        try {
            when (requestCode) {
                REQUEST_EXPORT_SETTINGS -> {
                    SettingsBackup.write(this, uri)
                    Toast.makeText(this, "پشتیبان تنظیمات ذخیره شد", Toast.LENGTH_LONG).show()
                }
                REQUEST_IMPORT_SETTINGS -> {
                    val count = SettingsBackup.read(this, uri)
                    Toast.makeText(this, "$count تنظیم بازیابی شد؛ کیبورد را یک‌بار ببند و دوباره باز کن.", Toast.LENGTH_LONG).show()
                    recreate()
                }
                REQUEST_EXPORT_LEARNING -> {
                    LearningProfileBackup.write(this, uri)
                    Toast.makeText(this, "فایل پشتیبان یادگیری ذخیره شد", Toast.LENGTH_LONG).show()
                }
                REQUEST_IMPORT_LEARNING -> {
                    val result = LearningProfileBackup.read(this, uri)
                    Toast.makeText(this, "بازیابی انجام شد؛ ${result.importedKeys} بخش و ${result.learnedExamples} نمونه یادگیری منتقل شد. کیبورد را یک‌بار ببند و دوباره باز کن.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "خطا: ${e.message ?: "عملیات انجام نشد"}", Toast.LENGTH_LONG).show()
        }
    }

    // ---------------------------------------------------------------------
    // تب استاندارد
    // ---------------------------------------------------------------------
    private fun createStandardTab(): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(24))
        }

        val title = TextView(this).apply {
            text = "کیبورد استاندارد"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(8), 0, dp(20))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        outer.addView(title)

        val standardBig = CheckBox(this).apply {
            text = "فعال‌سازی کیبورد استاندارد"
            isChecked = prefs.getBoolean("standard_big_keyboard", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("standard_big_keyboard", checked).apply()
                Toast.makeText(
                    this@SettingsActivity,
                    "برای اعمال تغییرات، کیبورد را ببندید و دوباره باز کنید",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        outer.addView(standardBig)

        // ✅ دکمه‌های نقطه و علامت سوال بین فاصله و اینتر (در هر دو حالت)
        val punctuation = CheckBox(this).apply {
            text = "نمایش دکمه‌های «.» و «؟» بین دکمه فاصله و اینتر"
            isChecked = prefs.getBoolean("punctuation_near_enter", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("punctuation_near_enter", checked).apply()
            }
        }
        outer.addView(punctuation)

        outer.addView(sectionTitle("ادغام کلیدهای هم‌خانواده"))
        outer.addView(TextView(this).apply {
            text = "این تنظیمات با تب عمومی مشترک است."
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(12))
        })
        addMergeSection(outer)

        outer.addView(sectionTitle("تنظیمات اندازه کیبورد استاندارد"))
        addNumberField(outer, "عرض کیبورد استاندارد (dp) - ۰ برای خودکار", "standard_keyboard_width", 0, 0, 1200)
        addNumberField(outer, "ارتفاع کیبورد استاندارد (dp) - ۰ برای خودکار", "standard_keyboard_height", 0, 0, 800)
        addNumberField(outer, "عرض دکمه‌های استاندارد (dp) - ۰ برای خودکار", "standard_key_width", 0, 0, 200)
        addNumberField(outer, "ارتفاع دکمه‌های استاندارد (dp) - ۰ برای خودکار", "standard_key_height", 0, 0, 120)
        addNumberField(outer, "فاصله بین دکمه‌ها (dp) - ۰ برای پیش‌فرض", "standard_key_gap", -1, -1, 20)
        addNumberField(outer, "مقیاس کیبورد استاندارد (٪)", "standard_scale", 100, 50, 150)

        return outer
    }

    // ---------------------------------------------------------------------
    // تب انگلیسی
    // ---------------------------------------------------------------------
    private fun createEnglishTab(): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(24))
        }

        val title = TextView(this).apply {
            text = "کیبورد انگلیسی"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(8), 0, dp(20))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        outer.addView(title)

        outer.addView(sectionTitle("تنظیمات انگلیسی (عمودی)"))
        addNumberField(outer, "عرض کیبورد انگلیسی (dp)", "en_keyboard_width", 360, 0, 1200)
        addNumberField(outer, "ارتفاع کل کیبورد انگلیسی (dp)", "en_keyboard_height", 270, 150, 800)
        addNumberField(outer, "عرض دکمه‌ها (dp) - ۰ برای خودکار", "en_key_width", 0, 0, 200)
        addNumberField(outer, "ارتفاع دکمه‌ها (dp)", "en_key_height", 48, 30, 120)
        addNumberField(outer, "فاصله بین دکمه‌ها (dp)", "en_key_gap", 4, 0, 20)
        outer.addView(sectionTitle("ردیف اعداد بالای حروف انگلیسی"))
        val enNumbers = CheckBox(this).apply {
            text = "نمایش ردیف اعداد بالای حروف انگلیسی"
            isChecked = prefs.getBoolean("en_top_numbers", false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("en_top_numbers", checked).apply() }
        }
        outer.addView(enNumbers)
        addNumberField(outer, "عرض مستقل هر کلید عدد انگلیسی (dp) - ۰ برای کششی", "en_top_number_width", 0, 0, 200)

        return outer
    }

    // ---------------------------------------------------------------------
    // تب تم و ظاهر
    // ---------------------------------------------------------------------
    private fun createThemeTab(): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(24))
        }

        val title = TextView(this).apply {
            text = "ظاهر و تم"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(8), 0, dp(20))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        outer.addView(title)

        val dark = CheckBox(this).apply {
            text = "تم تیره (برای تم پیش‌فرض)"
            isChecked = prefs.getBoolean("dark", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("dark", checked).apply()
            }
        }
        outer.addView(dark)

        val pressAnim = CheckBox(this).apply {
            text = "انیمیشن فشردن کلیدها (اگر کندی حس می‌کنی خاموش کن)"
            isChecked = prefs.getBoolean("key_press_animation", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("key_press_animation", checked).apply()
            }
        }
        outer.addView(pressAnim)
        val animationStyleLabel = TextView(this).apply {
            text = "مدل انیمیشن فشردن کلید:"
            textSize = 14f
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(8), 0, dp(4))
        }
        outer.addView(animationStyleLabel)
        val animationStyles = listOf("بدون حرکت (تغییر شفافیت)", "فشرده شدن", "حرکت رو به پایین", "چرخش ظریف", "بزرگ‌نمایی و درخشش", "نمایش سریع نام کلید")
        val animationSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@SettingsActivity, android.R.layout.simple_spinner_dropdown_item, animationStyles)
            setSelection(prefs.getInt("key_press_animation_style", 1).coerceIn(0, animationStyles.lastIndex))
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    prefs.edit().putInt("key_press_animation_style", position).apply()
                }
            }
        }
        outer.addView(animationSpinner)

        val intensityLabel = TextView(this).apply {
            text = "شدت حرکت افکت: ${prefs.getInt("key_press_animation_intensity", 7).coerceIn(1, 10)} از ۱۰"
            textSize = 14f
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(10), 0, dp(2))
        }
        outer.addView(intensityLabel)
        val intensitySeek = SeekBar(this).apply {
            max = 9
            progress = prefs.getInt("key_press_animation_intensity", 7).coerceIn(1, 10) - 1
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = progress + 1
                    intensityLabel.text = "شدت حرکت افکت: $value از ۱۰"
                    prefs.edit().putInt("key_press_animation_intensity", value).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        outer.addView(intensitySeek)

        outer.addView(TextView(this).apply {
            text = "با خاموش کردن این گزینه، رنگ کلید هنگام فشردن تغییر نمی‌کند و پردازش کمتری مصرف می‌شود."
            textSize = 12f
            setTextColor(0xFF8A8399.toInt())
            setPadding(0, dp(4), 0, dp(8))
        })

        val themeLabel = TextView(this).apply {
            text = "انتخاب تم کیبورد:"
            textSize = 14f
            setTextColor(0xFF332B45.toInt())
            setPadding(0, dp(12), 0, dp(4))
        }
        outer.addView(themeLabel)

        val themes = arrayOf("default" to "پیش‌فرض", "retro" to "رترو", "retro_white" to "اپتیکال سفید کلاسیک", "retro_black" to "اپتیکال مشکی کلاسیک", "neon" to "نئون", "glass" to "شیشه‌ای")
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@SettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                themes.map { it.second }
            )
            val currentTheme = prefs.getString("theme", "default")
            setSelection(themes.indexOfFirst { it.first == currentTheme }.coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    prefs.edit().putString("theme", themes[position].first).apply()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        outer.addView(spinner, match())

        outer.addView(sectionTitle("اندازه و چیدمان کیبورد حروف"))
        addNumberField(outer, "عرض دکمه‌ها (dp) - ۰ برای خودکار", "fa_key_width", 0, 0, 200)
        addNumberField(outer, "ارتفاع دکمه‌ها (dp)", "fa_key_height", 40, 28, 120)
        addNumberField(outer, "فاصله بین دکمه‌ها (dp)", "fa_key_gap", 4, 0, 20)
        outer.addView(sectionTitle("ردیف اعداد بالای حروف فارسی"))
        val faNumbers = CheckBox(this).apply {
            text = "نمایش ردیف اعداد بالای حروف فارسی"
            isChecked = prefs.getBoolean("fa_top_numbers", false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("fa_top_numbers", checked).apply() }
        }
        outer.addView(faNumbers)
        addNumberField(outer, "عرض مستقل هر کلید عدد فارسی (dp) - ۰ برای کششی", "fa_top_number_width", 0, 0, 200)
        addNumberField(outer, "ارتفاع ردیف اعداد بالای حروف (dp) - ۰ = ارتفاع حروف", "fa_top_number_height", 0, 0, 100)
        addNumberField(outer, "حاشیه امن (پیکسل)", "keyboard_padding", 2, 0, 5)
        addNumberField(
            outer,
            "حاشیه طرفین ردیف حروف (dp)\nمنفی = چسبیده به دیواره، مثبت = فاصله از دیواره",
            "letter_side_margin",
            4,
            -200,
            100
        )
        addNumberField(outer, "عرض کیبورد در حالت افقی (dp) - ۰ برای خودکار", "keyboard_width_landscape", 0, 0, 1200)
        addNumberField(outer, "ارتفاع کیبورد در حالت افقی (dp) - ۰ برای خودکار", "keyboard_height_landscape", 0, 0, 800)

        outer.addView(sectionTitle("اندازه صفحه کلید عددی (مستقل از حروف)"))
        outer.addView(TextView(this).apply {
            text = "این مقادیر فقط روی صفحه اعداد اعمال می‌شوند و با تغییر اندازه دکمه‌های حروف، اعداد تغییر نمی‌کنند.\n۰ = خودکار (کششی)"
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        addNumberField(outer, "عرض دکمه‌های عدد (dp) - ۰ برای کششی", "num_key_width", 0, 0, 200)
        addNumberField(outer, "ارتفاع دکمه‌های عدد (dp)", "num_key_height", 46, 30, 80)
        addNumberField(outer, "فاصله بین دکمه‌های عدد (dp) - ۰ برای پیش‌فرض", "num_key_gap", 4, 0, 20)

        outer.addView(sectionTitle("اندازه دکمه‌های فاصله و اینتر"))
        outer.addView(TextView(this).apply {
            text = "مقدار بزرگ‌تر = دکمه پهن‌تر. در هر دو حالت عادی و استاندارد اعمال می‌شود."
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        addNumberField(outer, "ارتفاع ردیف دکمه فاصله (dp) - ۰ برای خودکار", "fa_space_row_height", 0, 0, 140)
        addNumberField(outer, "وزن دکمه فاصله (۱ تا ۸)", "space_weight", 3, 1, 8)
        addNumberField(outer, "وزن دکمه اینتر (۱۰ تا ۲۵ - تقسیم بر ۱۰)", "enter_weight", 15, 10, 25)

        outer.addView(sectionTitle("دکمه‌های حروف و اعداد"))
        outer.addView(TextView(this).apply {
            text = "سرعت پاسخ دکمه‌ها را تنظیم کن. مقدار کمتر = پاسخ سریع‌تر هنگام نگه‌داشتن دکمه."
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        addNumberField(
            outer,
            "تأخیر نگه‌داشتن دکمه حروف برای تغییر (میلی‌ثانیه)\nکمتر = سریع‌تر، بیشتر = فرصت بیشتر برای تایپ عادی",
            "long_press_delay",
            400,
            150,
            1200
        )

        outer.addView(sectionTitle("بک‌اسپیس"))
        val bsCheck = CheckBox(this).apply {
            text = "انتقال بک‌اسپیس به ردیف مستقل پایین"
            isChecked = prefs.getBoolean("backspace_separate_row", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("backspace_separate_row", checked).apply()
                Toast.makeText(this@SettingsActivity, "برای اعمال تغییرات، کیبورد را ببندید و دوباره باز کنید", Toast.LENGTH_SHORT).show()
            }
        }
        outer.addView(bsCheck)
        addNumberField(outer, "وزن بک‌اسپیس در ردیف مستقل (۱ تا ۵)", "backspace_weight", 2, 1, 5)
        addNumberField(outer, "ارتفاع بک‌اسپیس در ردیف مستقل (dp) - ۰ برای خودکار", "backspace_separate_height", 0, 0, 120)
        addNumberField(
            outer,
            "فاصله عمودی ردیف بک‌اسپیس با ردیف اسپیس (dp) - بیشتر = دورتر",
            "backspace_separate_gap",
            8,
            0,
            40
        )
        addNumberField(outer, "عرض بک‌اسپیس در حالت عادی (۵ تا ۳۰)", "backspace_inline_width", 15, 5, 30)

        outer.addView(sectionTitle("جابه‌جایی عمودی کیبورد"))
        outer.addView(TextView(this).apply {
            text = "مقدار مثبت = بالا، منفی = پایین"
            textSize = 13f
            setTextColor(0xFF514B5D.toInt())
            setPadding(0, dp(4), 0, dp(10))
        })
        addNumberField(outer, "جابه‌جایی عمودی (dp) - ۱۰۰- تا ۲۰۰", "keyboard_vertical_offset", 0, -100, 200)

        val reset = Button(this).apply {
            text = "بازنشانی همه تنظیمات به پیش‌فرض"
            setOnClickListener {
                prefs.edit()
                    .putInt("en_keyboard_width", 360)
                    .putInt("en_keyboard_height", 270)
                    .putInt("keyboard_width_landscape", 0)
                    .putInt("keyboard_height_landscape", 0)
                    .putInt("fa_key_width", 0)
                    .putInt("fa_key_height", 40)
                    .putInt("fa_key_gap", 4)
                    .putInt("en_key_width", 0)
                    .putInt("en_key_height", 48)
                    .putInt("en_key_gap", 4)
                    .putInt("standard_keyboard_width", 0)
                    .putInt("standard_keyboard_height", 0)
                    .putInt("standard_key_width", 0)
                    .putInt("standard_key_height", 0)
                    .putInt("standard_key_gap", -1)
                    .putInt("standard_scale", 100)
                    .putInt("keyboard_padding", 2)
                    .putInt("letter_side_margin", 4)
                    .putBoolean("merge_key_kaf", false)
                    .putBoolean("merge_key_kaf_delete", false)
                    .putBoolean("merge_key_re", false)
                    .putBoolean("merge_key_re_delete", false)
                    .putBoolean("merge_ta_za", false)
                    .putBoolean("merge_ta_za_delete", false)
                    .putBoolean("merge_sad_zad", false)
                    .putBoolean("merge_sad_zad_delete", false)
                    .putBoolean("merge_dal_zal", false)
                    .putBoolean("merge_dal_zal_delete", false)
                    .putBoolean("merge_seen_the", false)
                    .putBoolean("merge_seen_the_delete", false)
                    .putBoolean("merge_ain_ghain", false)
                    .putBoolean("merge_ain_ghain_delete", false)
                    .putBoolean("merge_che_je", false)
                    .putBoolean("merge_che_je_delete", false)
                    // ✅ ادغام ح و خ
                    .putBoolean("merge_he_khe", false)
                    .putBoolean("merge_he_khe_delete", false)
                    .putBoolean("merge_ta_se", false).putBoolean("merge_ta_se_delete", false)
                    .putBoolean("merge_f_qaf", false).putBoolean("merge_f_qaf_delete", false)
                    .putBoolean("merge_ch_kh", false).putBoolean("merge_ch_kh_delete", false)
                    .putBoolean("merge_z_zh", false).putBoolean("merge_z_zh_delete", false)
                    .putBoolean("fa_top_numbers", false).putInt("fa_top_number_width", 0)
                    .putBoolean("en_top_numbers", false).putInt("en_top_number_width", 0)
                    .putBoolean("backspace_separate_row", false)
                    .putInt("backspace_weight", 2)
                    .putInt("backspace_separate_height", 0)
                    .putInt("backspace_separate_gap", 8)
                    .putInt("backspace_inline_width", 15)
                    .putInt("keyboard_vertical_offset", 0)
                    .putInt("fa_space_row_height", 0)
                    .putInt("space_weight", 3)
                    .putInt("enter_weight", 15)
                    .putInt("slow_typing_threshold", 1500)
                    .putBoolean("auto_correct", true)
                    .putBoolean("dark", false)
                    .putString("theme", "default")
                    .putBoolean("standard_big_keyboard", false)
                    .putInt("long_press_delay", 400)
                    .putInt("number_key_height", 46)
                    .putInt("num_key_width", 0)
                    .putInt("num_key_height", 46)
                    .putInt("num_key_gap", 4)
                    .putBoolean("key_press_animation", true)
                    // ✅ دکمه‌های نقطه و علامت سوال بین فاصله و اینتر
                    .putBoolean("punctuation_near_enter", true)
                    .apply()
                Toast.makeText(this@SettingsActivity, "تنظیمات بازنشانی شد", Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
        outer.addView(reset, match())

        val note = TextView(this).apply {
            text = "مقدار ۰ در هر فیلد به معنای «خودکار» است."
            textSize = 12f
            setPadding(0, dp(12), 0, dp(8))
            setTextColor(0xFF8A8399.toInt())
        }
        outer.addView(note)

        return outer
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun match() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(8) }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTypeface(null, android.graphics.Typeface.BOLD)
        setTextColor(0xFF332B45.toInt())
        setPadding(0, dp(20), 0, dp(8))
    }

    private fun addNumberField(
        parent: LinearLayout,
        label: String,
        key: String,
        default: Int,
        minValue: Int,
        maxValue: Int
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }

        val labelView = TextView(this).apply {
            text = "$label  (${minValue} تا ${maxValue})"
            textSize = 14f
            setTextColor(0xFF332B45.toInt())
        }
        row.addView(labelView)

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            setSingleLine(true)
            setText(prefs.getInt(key, default).toString())
            hint = "عدد دلخواه"
        }
        row.addView(input)
        input.setOnFocusChangeListener { focusedView, hasFocus ->
            if (hasFocus) {
                focusedView.postDelayed({
                    var ancestor: View? = row
                    while (ancestor != null && ancestor !is ScrollView) ancestor = ancestor.parent as? View
                    (ancestor as? ScrollView)?.smoothScrollTo(0, row.top.coerceAtLeast(0))
                }, 250)
            }
        }

        val seek = SeekBar(this).apply {
            this.max = maxValue - minValue
            progress = (prefs.getInt(key, default) - minValue).coerceIn(0, this.max)
        }
        row.addView(seek)

        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val v = s?.toString()?.toIntOrNull()
                if (v != null && v in minValue..maxValue) {
                    seek.progress = v - minValue
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    input.setText((progress + minValue).toString())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val save = Button(this).apply {
            text = "ذخیره"
            setOnClickListener {
                val value = input.text.toString().toIntOrNull()
                when {
                    value == null -> Toast.makeText(
                        this@SettingsActivity, "یک عدد معتبر وارد کنید", Toast.LENGTH_SHORT
                    ).show()
                    value < minValue -> Toast.makeText(
                        this@SettingsActivity, "حداقل مقدار $minValue است", Toast.LENGTH_SHORT
                    ).show()
                    value > maxValue -> Toast.makeText(
                        this@SettingsActivity, "حداکثر مقدار $maxValue است", Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        prefs.edit().putInt(key, value).apply()
                        Toast.makeText(this@SettingsActivity, "ذخیره شد", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        row.addView(save)

        parent.addView(row)
    }
}
