package com.optem.controllers;

import java.util.prefs.Preferences;

import com.optem.ui.DashboardView;

import javafx.application.Platform;

public class SplashController {

    public static class SplashTheme {
        public String bgRoot;
        public String textTitle;
        public String textSlogan;
        public String textStatus;

        public SplashTheme(String bgRoot, String textTitle, String textSlogan, String textStatus) {
            this.bgRoot = bgRoot;
            this.textTitle = textTitle;
            this.textSlogan = textSlogan;
            this.textStatus = textStatus;
        }
    }

    public SplashTheme getTheme() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String lastActive = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + lastActive, false);

        String bgRoot = isDark ? "#121212" : "white";
        String textTitle = isDark ? "#f8f9fa" : "#1B263B";
        String textSlogan = isDark ? "#A0A0A0" : "#52796F"; 
        String textStatus = isDark ? "#84A98C" : "#2D463E"; 

        return new SplashTheme(bgRoot, textTitle, textSlogan, textStatus);
    }

    public void simulateLoading(Runnable onSuccess) {
        Thread loadingThread = new Thread(() -> {
            try {
                Thread.sleep(2000); 
                Platform.runLater(onSuccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loadingThread.setDaemon(true);
        loadingThread.start();
    }
}