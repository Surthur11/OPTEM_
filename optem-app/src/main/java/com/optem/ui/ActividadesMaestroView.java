package com.optem.ui;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ActividadesMaestroView {

    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;
    private VBox listaPendientes;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(5);
        Label titulo = new Label("✅ Gestión de Actividades Docentes");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Lleva el control de tus revisiones, planeaciones y reportes.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);
        header.getChildren().add(titleBox);

        HBox cajaNuevaActividad = new HBox(15);
        cajaNuevaActividad.setAlignment(Pos.CENTER_LEFT);
        cajaNuevaActividad.setPadding(new Insets(20));
        cajaNuevaActividad.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12;");

        TextField txtNueva = new TextField();
        txtNueva.setPromptText("Ej: Calificar Práctica 3 de Redes...");
        txtNueva.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10; -fx-background-radius: 6;");
        HBox.setHgrow(txtNueva, Priority.ALWAYS);

        Button btnAgregar = new Button("➕ Agregar Tarea");
        btnAgregar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnAgregar.setOnAction(e -> {
            if (!txtNueva.getText().trim().isEmpty()) {
                agregarTarea(txtNueva.getText());
                txtNueva.clear();
            }
        });
        cajaNuevaActividad.getChildren().addAll(txtNueva, btnAgregar);

        listaPendientes = new VBox(10);
        agregarTarea("Planeación de clase: Ingeniería de Software");
        agregarTarea("Revisión de proyectos parciales");
        agregarTarea("Subir calificaciones al sistema oficial");

        ScrollPane scroll = new ScrollPane(listaPendientes);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        layoutMain.getChildren().addAll(header, cajaNuevaActividad, scroll);
        return layoutMain;
    }

    private void agregarTarea(String texto) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        CheckBox chk = new CheckBox();
        chk.setStyle("-fx-cursor: hand;");
        
        Label lblTexto = new Label(texto);
        lblTexto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorTextMain + ";");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBorrar = new Button("🗑");
        btnBorrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
        btnBorrar.setOnAction(e -> listaPendientes.getChildren().remove(item));

        chk.setOnAction(e -> {
            if (chk.isSelected()) {
                lblTexto.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-strikethrough: true; -fx-font-size: 14px;");
                item.setOpacity(0.6);
            } else {
                lblTexto.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorTextMain + ";");
                item.setOpacity(1.0);
            }
        });

        item.getChildren().addAll(chk, lblTexto, spacer, btnBorrar);
        listaPendientes.getChildren().add(0, item);
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