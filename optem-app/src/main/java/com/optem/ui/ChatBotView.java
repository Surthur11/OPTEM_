package com.optem.ui;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ChatBotView {

    private String colorFuerte, colorTextMain, colorTextMuted, colorBgPanel, colorBorder;

    // Recibe el Runnable para poder cerrarse desde el Dashboard
    public VBox getView(Runnable onClose) {
        leerPreferencias();

        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-width: 1; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");
        
        // --- HEADER DEL WIDGET ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: " + colorFuerte + "; -fx-background-radius: 11 11 0 0;");

        Label iconoIA = new Label("🤖");
        iconoIA.setStyle("-fx-font-size: 26px; -fx-text-fill: white;");

        VBox titleBox = new VBox(2);
        Label title = new Label("Asistente IA");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        Label subtitle = new Label("OptemBot en desarrollo");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCerrar.setOnAction(e -> {
            if (onClose != null) onClose.run(); // Cierra el chat en el Dashboard
        });

        header.getChildren().addAll(iconoIA, titleBox, spacer, btnCerrar);

        // --- BODY (CARTE DE CONSTRUCCIÓN) ---
        VBox body = new VBox(20);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(30));
        VBox.setVgrow(body, Priority.ALWAYS);

        Label cartelIcono = new Label("🔌");
        cartelIcono.setStyle("-fx-font-size: 60px; -fx-font-family: 'Segoe UI Emoji';");

        Label cartelTit = new Label("Esperando API...");
        cartelTit.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + colorTextMain + ";");

        Label cartelSub = new Label("El motor de Inteligencia Artificial se conectará en futuras versiones mediante una API externa (OpenAI / Gemini). La interfaz está lista para recibir respuestas.");
        cartelSub.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 13px; -fx-text-alignment: center; -fx-wrap-text: true; -fx-line-spacing: 4px;");

        body.getChildren().addAll(cartelIcono, cartelTit, cartelSub);

        root.getChildren().addAll(header, body);
        return root;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
}