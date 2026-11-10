package com.optem.ui;

import java.util.prefs.Preferences;

import com.optem.controllers.LogrosController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class LogrosMaestroView {

    private LogrosController controller;
    private String colorFuerte, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, fontSizeActual;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        VBox titleBox = new VBox(5);
        Label titulo = new Label("🏆 Desempeño y Reconocimientos Docentes");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        
        // --- LÓGICA DINÁMICA DEL MAESTRO ---
        int nivelM = controller.getNivelMaestro();
        int xpAdicional = controller.getXPAdicionalMaestro();
        
        Label subtitulo = new Label("Nivel actual: Maestro Titular Nivel " + nivelM + ". Has ganado " + xpAdicional + " XP cerrando tareas.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        VBox nivelBox = new VBox(10);
        nivelBox.setPadding(new Insets(20));
        nivelBox.setStyle("-fx-background-color: " + colorFuerte + "15; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");
        
        Label lblNivel = new Label(nivelM < 5 ? "Progreso hacia Nivel " + (nivelM + 1) + " (Cierra más tareas para avanzar)" : "¡Has alcanzado el Nivel Máximo (5)!");
        lblNivel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");
        
        ProgressBar pb = new ProgressBar(controller.getProgresoBonoMaestro());
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent: " + colorFuerte + ";");
        nivelBox.getChildren().addAll(lblNivel, pb);

        FlowPane gridMedallas = new FlowPane(15, 15);
        
        // Las medallas del maestro podrían conectarse a lógica más compleja en un futuro,
        // por ahora reflejamos que están ligadas a su gestión.
        boolean nivel2Alcanzado = nivelM >= 2;
        boolean nivel3Alcanzado = nivelM >= 3;
        boolean nivel4Alcanzado = nivelM >= 4;
        boolean nivel5Alcanzado = nivelM == 5;
        
        gridMedallas.getChildren().addAll(
            crearMedalla("📝", "Calificador Inicial", "Cerraste tus primeras tareas.", nivel2Alcanzado),
            crearMedalla("🌟", "Docente Dedicado", "Has cerrado tareas constantemente.", nivel3Alcanzado),
            crearMedalla("⏱", "Rey de la Gestión", "Excelente manejo de los tiempos de entrega.", nivel4Alcanzado),
            crearMedalla("🎯", "Maestro Titular Diamante", "Has alcanzado el nivel máximo de gestión.", nivel5Alcanzado)
        );

        ScrollPane scroll = new ScrollPane(gridMedallas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        layoutMain.getChildren().addAll(titleBox, nivelBox, scroll);
        return layoutMain;
    }

    private VBox crearMedalla(String icono, String titulo, String desc, boolean desbloqueada) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 2, 2);");

        Label icon = new Label(desbloqueada ? icono : "🔒");
        String colorIcono = desbloqueada ? "-color-text-main" : "-color-text-muted";
        icon.setStyle("-fx-font-size: 40px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: " + colorIcono + "; -fx-opacity: " + (desbloqueada ? "1.0" : "0.4") + ";");
        
        Label tit = new Label(titulo);
        tit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 14px;");
        
        Label d = new Label(desc);
        d.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px; -fx-wrap-text: true; -fx-text-alignment: center;");

        card.getChildren().addAll(icon, tit, d);
        return card;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        controller = new LogrosController(correoActual);
        
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";

        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b"; 
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
}