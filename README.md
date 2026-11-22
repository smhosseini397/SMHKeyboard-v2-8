# SmhKeyboard IA | کیبورد فارسی

کیبورد مستقل اندروید با چیدمان فارسی و انگلیسی، صفحه اعداد بزرگ، و کلیپ‌بورد قابل پین.

## امکانات نسخه اولیه
- سرویس واقعی Android Input Method (IME)
- چیدمان فارسی و انگلیسی و جابه‌جایی سریع زبان
- صفحه اعداد بزرگ با دکمه اختصاصی و ارقام فارسی/انگلیسی
- کلیپ‌بورد محلی، تاریخچه، پین و حذف موردی
- تنظیمات اندازه ارتفاع کلیدها، ارتفاع کیبورد، عرض پیشنهادی، فاصله کلیدها و تم تیره
- آیکون و نشان تجاری اولیه SmhKeyboard IA
- ساخت APK با GitHub Actions

## ساخت APK با GitHub
1. فایل ZIP را از حالت فشرده خارج کنید.
2. تمام محتویات پوشه `SmhKeyboard IA_Android` را در ریشه یک Repository جدید GitHub بارگذاری کنید.
3. از زبانه **Actions**، اجرای workflow با نام **Build Android APK** را باز کنید.
4. پس از پایان موفق، از بخش Artifacts فایل `SmhKeyboard IA-debug-apk` را دانلود کنید.
5. APK را روی گوشی نصب کنید.

## ساخت محلی
- Android Studio با JDK 17
- Android Gradle Plugin 8.7.3 و Gradle 8.9 یا نسخه سازگار
- پروژه را باز کنید و Build > Build APK(s) را بزنید.

## فعال‌سازی روی گوشی
پس از نصب، برنامه «SmhKeyboard IA» را باز کنید:
1. فعال‌سازی کیبورد
2. انتخاب SmhKeyboard IA از فهرست کیبوردها

## نکات فنی و محدودیت‌های اندروید
- اندروید برای حفظ حریم خصوصی، دسترسی برنامه‌ها به کلیپ‌بورد را محدود می‌کند. این پروژه موارد قابل‌مشاهده در زمانی که کیبورد فعال است را ثبت می‌کند؛ تضمین ثبت تمام کپی‌ها در پس‌زمینه ممکن نیست.
- عرض و ارتفاع واقعی کیبورد به فضای قابل‌استفاده صفحه و سیاست پنجره IME اندروید وابسته است. اعداد تنظیمات نمی‌توانند از محدوده فیزیکی صفحه عبور کنند.
- ورودی «عرض کیبورد» در نسخه اولیه ذخیره می‌شود؛ اعمال عرض دقیق پنجره در همه دستگاه‌ها و حالت‌های چرخش نیازمند تست روی دستگاه‌های مختلف است.
- پیشنهاد کلمات و اصلاح خودکار در این نسخه هنوز موتور زبانی کامل ندارند و باید در نسخه بعدی اضافه و آزمایش شوند.
- برای انتشار عمومی در Google Play، سیاست‌های حریم خصوصی و افشای پردازش متن باید تکمیل شوند.

## لوگو
لوگوی اولیه برداری در `app/src/main/res/drawable/ic_keyfarsi.xml` و آیکون لانچر در مسیر `mipmap` قرار دارد. می‌توانید طرح و نام تجاری را بعداً سفارشی کنید.


## Persian spell-check development

- Offline Persian lexicon: 1,383 entries (`app/src/main/assets/fa_words.txt`).
- Weighted edit distance with Persian keyboard adjacency and transposition.
- Learned vocabulary and previous-word context affect ranking.
- Conservative one-edit auto-correction on space; ambiguous terms are not changed.
- See `PERSIAN_SPELLCHECKER.md` and `SPELLCHECK_TESTS.md`.


## Context model update (v6 development)

- Added a two-word context (trigram) model alongside the existing one-word bigram model.
- Uses recent completed words to score typo candidates with local sentence context.
- Learns the committed sequence on-device and keeps the personal trigram store bounded.
- This is a compact statistical n-gram model, not a neural LLM; see `PERSIAN_CONTEXT_LEARNING.md`.


## Version 7 — offline neural contextual correction

Version 7 adds `PersianNeuralContextModel`, a compact trainable one-hidden-layer MLP that runs locally in the keyboard process. It uses 32 hashed/normalized features and 12 hidden units. Features include candidate character trigrams, the two previous words, candidate-in-context hashes, and typo-shape signals.

The model is personalized: when the user explicitly selects a correction, the chosen word is trained as positive and nearby alternatives as negatives. Model weights and example count are stored in the app's private SharedPreferences; no text is sent to a server. After at least 3 explicit corrections it can rerank suggestions. Neural-only automatic replacement is more conservative and is enabled only after at least 10 learned corrections plus a clear score margin. Existing lexical and personal typo correction remain as fallbacks.

**Important limitation:** this is a small online-trained neural reranker, not a pretrained Persian semantic language model. It learns patterns from this user's corrections and local context; it does not yet understand arbitrary unseen sentences like ChatGPT. A general semantic model would require a licensed/validated Persian training corpus, offline model weights, quantization, and real-device latency/accuracy testing.

## Version 8 — portable learning profile

Settings now include export/import actions for a versioned JSON learning profile. It transfers personal corrections, learned vocabulary, bigram/trigram statistics, and the locally trained neural reranker weights to another installation. The suggested user-managed folder is `Download/SmhKeyboard IA-Learning`. A user may upload the exported JSON to their own host and later download/import it on another phone; no automatic server sync is performed. Clipboard contents are excluded. See `SmhKeyboard IA-Learning/README.md` and `LEARNING_BACKUP_V8_TESTS.md`.

**Pretrained semantic model status:** no pretrained model binary is bundled in this build. See `semantic-model/README.md` for the compact Persian ALBERT candidate and the remaining conversion, integration, and device-validation work. Do not describe this build as having general semantic sentence understanding.

## نسخه ۹: مدل زبانی فارسی آفلاین

نسخه ۹ موتور pretrained فارسی ALBERT (۱۱.۵ میلیون پارامتر) را از طریق ONNX Runtime برای رتبه‌بندی معنایی کاندیداهای غلط‌یابی اضافه می‌کند. مدل از متن قبل از واژه استفاده می‌کند و تنها با فاصله امتیاز مطمئن، اصلاح خودکار انجام می‌دهد. فایل‌های بزرگ مدل در زمان اجرای GitHub Actions از مخزن رسمی دریافت و به ONNX تبدیل می‌شوند؛ در زمان تایپ هیچ درخواست اینترنتی انجام نمی‌شود. جزئیات و محدودیت‌ها در `semantic-model/README.md` و `PERSIAN_ALBERT_V9_TESTS.md` آمده است.


## Persian contextual model (v10)

SmhKeyboard IA selects `shekar-ai/albert-base-v2-persian-zwnj-naab-mlm`. It is a compact Persian ALBERT masked-language encoder with an explicit ZWNJ SentencePiece token. SmhKeyboard IA uses it to rerank spelling candidates using the preceding text; it is not a generative model or a complete semantic typo-correction model. See `PERSIAN_ALBERT_MODEL_V10.md` and `PERSIAN_ALBERT_V10_TESTS.md` for upstream specifications and outstanding validation.

## شناسه و برند نسخه SmhKeyboard IA
- نام نمایشی برنامه و سرویس ورودی: `SmhKeyboard IA`
- شناسه نصب اندروید (applicationId): `com.smhkeyboard.ia`
- این شناسه با بسته قدیمی `com.farshekashan.keyfarsi` متفاوت است؛ بنابراین اندروید آن را برنامه‌ای جداگانه می‌شناسد و به‌جای نسخه قبلی نصب/به‌روزرسانی نمی‌کند.
- لوگوی برداری جدید: `branding/smhkeyboard-ia-logo.svg`


## SMH Keyboard IA V2 — نسخه مستقل
- شناسه نصب مستقل: `com.smhkeyboard.ia.v2`؛ نسخه قبلی حذف یا بازنویسی نمی‌شود.
- تنظیمات و داده‌های یادگیری در فضای جداگانه ذخیره می‌شوند.
- شروع هر ورودی جدید با صفحه حروف فارسی است.
- ردیف فاصله ارتفاع مستقل دارد (`fa_space_row_height`).
- بک‌اسپیس صفحه عددی در ردیف نمادها قرار گرفته و کنار صفر نیست؛ کلید `=` از آن ردیف حذف شده است.
- پیشنهادهای اصلاح املایی برای واژه کامل نیز نمایش داده می‌شوند.


## نسخه V2.8
بهبود ثبت لمس کلید فاصله و کلیدهای عمومی با محدوده رهاسازی منعطف‌تر؛ جزئیات در `SMH_V2_8_TOUCH_RELIABILITY_FA.md`.
