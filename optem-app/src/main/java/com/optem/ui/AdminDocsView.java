package com.optem.ui;

import java.util.prefs.Preferences;

import com.optem.controllers.DashboardController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminDocsView {

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
        Label titulo = new Label("📁 Documentos y Trayectorias");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Repositorio global de expedientes, reportes de sistema y respaldos.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnGenerar = new Button("📄 Generar Reporte PDF");
        btnGenerar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        header.getChildren().addAll(titleBox, spacer, btnGenerar);

        HBox foldersBox = new HBox(20);
        foldersBox.getChildren().addAll(
            crearCarpeta("🎓 Expedientes (Alumnos)", "420 Archivos"),
            crearCarpeta("👨‍🏫 Contratos (Docentes)", "45 Archivos"),
            crearCarpeta("💾 Respaldos (Backups)", "12 Archivos")
        );

        Label lblRecientes = new Label("Archivos Recientes");
        lblRecientes.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + colorTextMain + ";");

        GridPane gridArchivos = new GridPane();
        gridArchivos.setVgap(15); gridArchivos.setHgap(15);
        gridArchivos.add(crearArchivo("Kardex_JoseFonseca.pdf", "Expedientes", "PDF"), 0, 0);
        gridArchivos.add(crearArchivo("Backup_OptemDB_Oct.sql", "Respaldos", "SQL"), 1, 0);
        gridArchivos.add(crearArchivo("Reporte_Actividades.xlsx", "Sistema", "XLSX"), 2, 0);
        gridArchivos.add(crearArchivo("Contrato_RicardoG.pdf", "Contratos", "PDF"), 0, 1);

        layoutMain.getChildren().addAll(header, foldersBox, lblRecientes, gridArchivos);
        
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

    private VBox crearCarpeta(String nombre, String detalles) {
        VBox box = new VBox(5);
        box.setPrefWidth(220);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8; -fx-cursor: hand;");
        Label lblNom = new Label(nombre);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        Label lblDet = new Label(detalles);
        lblDet.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px;");
        box.getChildren().addAll(lblNom, lblDet);
        return box;
    }

    private HBox crearArchivo(String nombre, String categoria, String tipo) {
        HBox box = new HBox(10);
        box.setPrefWidth(300);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8;");

        Label icon = new Label(tipo.equals("PDF") ? "📕" : tipo.equals("SQL") ? "🛢️" : "📗");
        icon.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(2);
        Label lblNom = new Label(nombre);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 13px;");
        Label lblCat = new Label(categoria);
        lblCat.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 11px;");
        info.getChildren().addAll(lblNom, lblCat);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnDescargar = new Button("⬇");
        btnDescargar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorFuerte + "; -fx-cursor: hand; -fx-font-weight: bold;");

        box.getChildren().addAll(icon, info, spacer, btnDescargar);
        return box;
    }
}