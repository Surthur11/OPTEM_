package com.optem.ui;

import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ReunionesMaestroView {
    private String colorFuerte, colorTextMain, colorTextMuted, colorBgPanel;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(20);
        layoutMain.setAlignment(Pos.CENTER);
        layoutMain.setPadding(new Insets(50));
        layoutMain.setStyle("-fx-background-color: transparent;");

        VBox cartel = new VBox(15);
        cartel.setAlignment(Pos.CENTER);
        cartel.setMaxWidth(500);
        cartel.setPadding(new Insets(40));
        cartel.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");

        Label icono = new Label("🛠️");
        icono.setStyle("-fx-font-size: 80px; -fx-font-family: 'Segoe UI Emoji';");

        Label titulo = new Label("Aulas Virtuales en Mantenimiento");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: " + colorTextMain + ";");

        Label subtitulo = new Label("La infraestructura de streaming docente se encuentra en fase de desarrollo. Pronto podrás iniciar clases en vivo con tus alumnos directamente desde aquí.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px; -fx-text-alignment: center; -fx-wrap-text: true;");

        cartel.getChildren().addAll(icono, titulo, subtitulo);
        layoutMain.getChildren().add(cartel);

        return layoutMain;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
    }
}