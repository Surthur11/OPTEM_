package com.optem.ui;

import java.util.prefs.Preferences;

import com.optem.controllers.DashboardController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminIAView {

    private final DashboardController controller = new DashboardController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(5);
        Label titulo = new Label("🤖 Configuración de Inteligencia Artificial");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Gestiona las credenciales de API, el modelo de IA y el prompt del sistema.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        VBox configBox = new VBox(15);
        configBox.setPadding(new Insets(25));
        configBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12;");
        
        Label lblTitApi = new Label("Conexión con el Modelo Lingüístico (LLM)");
        lblTitApi.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 16px;");

        HBox row1 = new HBox(15);
        VBox col1 = new VBox(5);
        Label lblModelo = new Label("Modelo de IA"); lblModelo.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        ComboBox<String> cbModelo = new ComboBox<>();
        cbModelo.getItems().addAll("Gemini 1.5 Pro", "Gemini 1.5 Flash");
        cbModelo.setValue("Gemini 1.5 Pro");
        cbModelo.setPrefWidth(250);
        cbModelo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-text-fill: " + colorTextMain + ";");
        col1.getChildren().addAll(lblModelo, cbModelo);

        VBox col2 = new VBox(5);
        Label lblApi = new Label("Clave API (API Key)"); lblApi.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        PasswordField txtApi = new PasswordField();
        txtApi.setText("AIzaSyB-EjemploDeApiKeyOculta");
        txtApi.setPrefWidth(350);
        txtApi.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-text-fill: " + colorTextMain + ";");
        col2.getChildren().addAll(lblApi, txtApi);
        row1.getChildren().addAll(col1, col2);

        VBox promptBox = new VBox(5);
        Label lblPrompt = new Label("Instrucciones del Sistema (System Prompt)");
        lblPrompt.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        TextArea txtPrompt = new TextArea();
        txtPrompt.setText("Eres el asistente virtual del ERP Optem de la Universidad Autónoma del Estado de México. Tu objetivo es ayudar a estudiantes y maestros a navegar por el sistema, recordarles tareas y ofrecer respuestas amables, formales y concisas.");
        txtPrompt.setPrefRowCount(4);
        txtPrompt.setWrapText(true);
        txtPrompt.setStyle("-fx-control-inner-background: " + colorBgBase + "; -fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-border-color: " + colorBorder + ";");
        promptBox.getChildren().addAll(lblPrompt, txtPrompt);

        HBox botonesBox = new HBox(15);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnGuardar = new Button("Guardar Configuración");
        btnGuardar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");
        botonesBox.getChildren().add(btnGuardar);

        configBox.getChildren().addAll(lblTitApi, row1, promptBox, botonesBox);

        layoutMain.getChildren().addAll(header, configBox);
        
        ScrollPane scroll = new ScrollPane(layoutMain);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox layoutFinal = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return layoutFinal;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9"; colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b"; colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
}