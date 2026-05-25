package com.jawla.jawla;

import javafx.scene.layout.VBox;
import javafx.application.Platform;

public abstract class BasePage<T> extends VBox {

    protected T pageData; 
    protected NavigationHandler navigationHandler;

    public BasePage() {
        this.setFillWidth(true);
    }

    public void setNavigationHandler(NavigationHandler handler) {
        this.navigationHandler = handler;
    }

    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    // --- التعديل الأساسي هنا عشان الـ App يشتغل ---
    
    // بدل ما كانت بترمي Exception، دلوقت بتخزن الداتا فعلاً
    @SuppressWarnings("unchecked")
    public void setData(Object data) {
        this.pageData = (T) data;
    }

    public T getData() {
        return pageData;
    }

    // الميثود الأساسية اللي بتملأ البيانات في الخانات
    public abstract void loadData();

    // ميثود مساعدة للـ Edit
    public void loadData(T data) {
        this.pageData = data;
        loadData();
    }

    // --- بقية الميثودز القديمة زي ما هي عشان مفيش حاجة تضرب ---
    protected void runInBackgroundTask(Runnable task) {
        Thread backgroundThread = new Thread(() -> {
            try {
                task.run(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        backgroundThread.setDaemon(true); 
        backgroundThread.start();
    }

    protected void updateUI(Runnable updateTask) {
        Platform.runLater(updateTask);
    }
}