package com.jawla.jawla;

public interface NavigationHandler {
    // 1. الميثود القديمة (زي ما هي)
    void navigateTo(String pageName);

    // 2. الميثود القديمة الخاصة بالبيانات (زي ما هي)
    void navigateTo(String pageName, Object data);

    // --- التعديل الجديد ---
    // 3. ميثود عشان نفتح صفحات بتحتاج (اسم المستخدم والرتبة) زي الـ Settings
    default void navigateTo(String pageName, String userName, String role) {
        // ميثود افتراضية عشان الكلاسات القديمة ما تضربش (Error)
    }
}