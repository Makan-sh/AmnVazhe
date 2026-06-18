package com.example.utils

object Localization {
    
    enum class Lang {
        FA, EN
    }

    private val farsiStrings = mapOf(
        "app_title" to "AmnVazeh",
        "nav_passwords" to "رمزها",
        "nav_security" to "امنیت",
        "nav_trash" to "سطل زباله",
        "nav_settings" to "تنظیمات",
        "nav_about" to "درباره",
        
        "dashboard_title" to "AmnVazeh",
        "search_hint" to "جستجو در میان رمزها...",
        "stat_total" to "کل گذرواژه‌ها",
        "stat_weak" to "رمزهای ضعیف",
        "stat_favorites" to "علاقه‌مندی‌ها",
        "recent_used" to "اخیراً استفاده شده",
        "no_passwords" to "هیچ رمزی یافت نشد. برای افزودن دکمه + را بزنید.",
        "copied_clipboard" to "کپی شد اما تا ۲۰ ثانیه ماندگاره در کلیپبورد",
        "clipboard_secured" to "حافظه موقت جهت افزایش امنیت پاک شد.",
        
        "cat_social" to "شبکه‌های اجتماعی",
        "cat_banking" to "بانکی",
        "cat_personal" to "شخصی",
        "cat_public" to "عمومی",
        "cat_other" to "دیگر",
        
        "security_title" to "بررسی امنیت",
        "security_ok" to "امنیت تایید شده",
        "security_ok_desc" to "هیچ رمز ضعیف یا تکراری یافت نشد!",
        "security_all_clear" to "امنیت گذرواژه‌ها در وضعیت ایده آل است.",
        "security_warning" to "⚠️ ضرورت اصلاح رمزهای ضعیف",
        "security_total_pass" to "کل رمزها",
        
        "trash_title" to "سطل زباله",
        "trash_empty" to "سطل زباله خالی است",
        "trash_info" to "گذرواژه‌های موجود در این صفحه بعد از ۳۰ روز به طور خودکار حذف می‌شوند.",
        "restore_btn" to "بازیابی رمز",
        "delete_forever" to "حذف دائمی",
        
        "settings_title" to "تنظیمات برنامه",
        "set_account_security" to "امنیت و حساب کاربری",
        "set_pin" to "رمز پین (۴ رقمی)",
        "set_pin_desc_on" to "پین کد ۴ رقمی فعال است",
        "set_pin_desc_off" to "پین کد تعریف نشده است",
        "set_biometric" to "ورود با اثر انگشت",
        "set_biometric_desc" to "فعال‌سازی اسکنر بیومتریک برای باز شدن سریع",
        "set_autolock" to "قفل خودکار ۵ دقیقه‌ای",
        "set_autolock_desc" to "در صورت ۵ دقیقه عدم فعالیت، برای ایمنی قفل می‌شود",
        "set_darktheme" to "تم تاریک برنامه",
        "set_darktheme_desc" to "سوئیچ به حالت شب و روز محیط کاربری",
        "set_theme_color" to "تم رنگی برنامه",
        "set_theme_color_desc" to "انتخاب پالت رنگی مورد علاقه شما",
        "set_font_size" to "اندازه فونت",
        "set_font_size_desc" to "تغییر سایز متون برنامه برای خوانایی بهتر سالمندان",
        "set_language" to "زبان برنامه (Language)",
        "set_language_desc" to "سوئیچ سریع بین فارسی و انگلیسی",
        "set_backup" to "پشتیبان‌گیری رمزنگاری‌شده",
        "set_backup_desc" to "تهیه نسخه خروجی JSON رمزگذاری شده با PBKDF2 و رمز اصلی",
        "set_restore" to "بازیابی اطلاعات پشتیبان",
        "set_restore_desc" to "وارد کردن فایل یا کد پشتیبان به همراه تایپ رمز اصلی",
        "set_reset" to "حذف کل اطلاعات فرعی (ریست کامل)",
        "set_reset_desc" to "تمام رمزها و تنظیمات پاک شده و برنامه به حالت اول برمی‌گردد",
        
        "add_edit_new" to "افزودن رمز عبور جدید",
        "add_edit_edit" to "ویرایش رمز عبور",
        "lbl_site" to "نام سایت / برنامه",
        "lbl_username" to "نام کاربری / ایمیل",
        "lbl_password" to "رمز عبور",
        "lbl_description" to "توضیحات اختیاری",
        "lbl_category" to "دسته‌بندی رمز",
        "btn_generate" to "تولید رمز تصادفی",
        "btn_save" to "ذخیره تغییرات",
        "btn_cancel" to "انصراف",
        "strength_weak" to "ضعیف",
        "strength_medium" to "متوسط",
        "strength_strong" to "بسیار قوی",
        
        "font_normal" to "استاندارد (یک برابر)",
        "font_medium" to "متوسط (۱.۲ برابر)",
        "font_large" to "بزرگ (۱.۴ برابر)",
        "font_senior" to "بسیار بزرگ (۱.۶ برابر) مخصوص سالمندان",
        
        "theme_bento" to "سرمه‌ای و تیکابی (Slate Teal)",
        "theme_amethyst" to "آمیتیست رویال (Royal Purple)",
        "theme_emerald" to "جنگل زمرد (Emerald Forest)",
        "theme_solar" to "آفتابی و کربن (Solar Amber)",
        
        "about_title" to "درباره AmnVazeh",
        "about_desc" to "امن‌واژه یک گاوصندوق کاملاً آفلاین، کدگذاری شده و پیشرفته برای نگهداری گذرواژه‌های شما بر روی موبایل می‌باشد.",
        "about_security_subtitle" to "امنیت اطلاعات",
        "about_security_desc" to "اطلاعات در این اپلیکیشن با الگوریتم‌های درجه نظامی رمزگذاری شده و هرگز به سروری ارسال نمی‌شوند. امنیت شما اولویت ماست.",
        "about_tech" to "مشخصات فنی و توسعه",
        "about_tech_desc" to "ساخته شده به صورت کاملاً بومی با فریم‌ورک مدرن Jetpack Compose، دیتابیس Room و استانداردهای رمزنگاری ایمن."
    )

    private val englishStrings = mapOf(
        "app_title" to "AmnVazeh",
        "nav_passwords" to "Passwords",
        "nav_security" to "Security",
        "nav_trash" to "Trash Pin",
        "nav_settings" to "Settings",
        "nav_about" to "About",
        
        "dashboard_title" to "AmnVazeh",
        "search_hint" to "Search passwords...",
        "stat_total" to "Total Passwords",
        "stat_weak" to "Weak Passwords",
        "stat_favorites" to "Favorites",
        "recent_used" to "Recently Used",
        "no_passwords" to "No passwords found. Tap '+' to add a password.",
        "copied_clipboard" to "Copied! Valid in clipboard for only 20 seconds",
        "clipboard_secured" to "Clipboard cleared automatically for enhanced security.",
        
        "cat_social" to "Social Networks",
        "cat_banking" to "Banking",
        "cat_personal" to "Personal",
        "cat_public" to "Public Space",
        "cat_other" to "Other Accounts",
        
        "security_title" to "Security Checker",
        "security_ok" to "Fully Secured",
        "security_ok_desc" to "No weak or duplicate passwords found!",
        "security_all_clear" to "All password security is in ideal standing.",
        "security_warning" to "⚠️ High Priority: Fix Weak Passwords",
        "security_total_pass" to "Total Passwords",
        
        "trash_title" to "Trash Drawer",
        "trash_empty" to "Trash Bin is Empty",
        "trash_info" to "Passwords in here will be permanently deleted automatically after 30 days.",
        "restore_btn" to "Restore Code",
        "delete_forever" to "Delete Permanently",
        
        "settings_title" to "Settings Dashboard",
        "set_account_security" to "Privacy & Access Security",
        "set_pin" to "Screen PIN Lock (4 Digits)",
        "set_pin_desc_on" to "4-digit quick PIN is active",
        "set_pin_desc_off" to "Quick PIN code is not set",
        "set_biometric" to "In-App Biometrics",
        "set_biometric_desc" to "Authenticate quickly using fingerprint scan",
        "set_autolock" to "5-Minute Idle Lock",
        "set_autolock_desc" to "Automatically locks the vault after 5 mins of inactivity",
        "set_darktheme" to "Palette Night Mode",
        "set_darktheme_desc" to "Toggle between bright day and soft night interfaces",
        "set_theme_color" to "Aesthetics Palette Theme",
        "set_theme_color_desc" to "Choose your favorite bento grid color palette",
        "set_font_size" to "Typography Font Size",
        "set_font_size_desc" to "Scale typography sizes dynamically for low-vision and seniors",
        "set_language" to "Application Language",
        "set_language_desc" to "Switch quickly between Persian and English layouts",
        "set_backup" to "Cryptographic Backup",
        "set_backup_desc" to "Export AES-GCM encrypted backup of active passwords.",
        "set_restore" to "Restore Cryptographic Backup",
        "set_restore_desc" to "Import serialized backup codes and authenticate with master password",
        "set_reset" to "Emergency Master Reset",
        "set_reset_desc" to "Purge everything! Deletes config, passwords, database and lock settings.",
        
        "add_edit_new" to "Add New Password",
        "add_edit_edit" to "Edit Secure Code",
        "lbl_site" to "Service / Site Name",
        "lbl_username" to "Username or Email",
        "lbl_password" to "Vault Password",
        "lbl_description" to "Optional Notes / Metadata",
        "lbl_category" to "Secured Category",
        "btn_generate" to "Generate Safe Code",
        "btn_save" to "Commit Changes",
        "btn_cancel" to "Cancel",
        "strength_weak" to "Weak",
        "strength_medium" to "Medium",
        "strength_strong" to "Excellent Strong",
        
        "font_normal" to "Standard scale (1.0x)",
        "font_medium" to "Medium scale (1.2x)",
        "font_large" to "Large scale (1.4x)",
        "font_senior" to "Accessibility scale (1.6x) for Seniors",
        
        "theme_bento" to "Slate Navy & Teal (Bento Slate)",
        "theme_amethyst" to "Royal Purple (Amethyst)",
        "theme_emerald" to "Emerald Forest (Emerald Green)",
        "theme_solar" to "Amber Carbon (Solar Active)",
        
        "about_title" to "About AmnVazeh",
        "about_desc" to "AmnVazeh is a private, completely offline, zero-trust and military-grade dashboard for secure credentials management.",
        "about_security_subtitle" to "Isolated Local Security",
        "about_security_desc" to "Every item is encrypted locally. Zero analytics trackers and zero network uploads. Absolute visual privacy.",
        "about_tech" to "Technology Stack",
        "about_tech_desc" to "Built with Kotlin, Jetpack Compose, Room SQLite database, and advanced pbkdf2 keys."
    )

    fun getString(key: String, lang: String): String {
        return if (lang == "en") {
            englishStrings[key] ?: key
        } else {
            farsiStrings[key] ?: key
        }
    }
}
