package com.optem;
import com.optem.ui.SplashScreen;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        SplashScreen splash = new SplashScreen();
        splash.show(primaryStage);
    }
    public static void main(String[] args) {
        launch(args);
    }
}