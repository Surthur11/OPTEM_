package com.optem.ui;

import java.util.List;
import java.util.prefs.Preferences;

import com.optem.controllers.NotasMaestroController;
import com.optem.logic.NotificacionManager;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotasMaestroView {
    
    private final NotasMaestroController controller = new NotasMaestroController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, fontSizeActual;
    
    private FlowPane gridNotas;
    private Stage stagePrincipal; 

    public VBox getView() {
        leerPreferencias();
        controller.getCategoriasDisponibles(); 

        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label titulo = new Label("Pizarrón Docente");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Organiza tus rúbricas, planeaciones y notas de academia en un solo lugar.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("✍ Nueva Planificación");
        btnNuevo.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        btnNuevo.setOnAction(e -> mostrarEditorAvanzado());

        header.getChildren().addAll(titleBox, spacer, btnNuevo);

        gridNotas = new FlowPane();
        gridNotas.setHgap(20);
        gridNotas.setVgap(20);
        gridNotas.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(gridNotas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        actualizarTableroNotas();

        layoutMain.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) newS.windowProperty().addListener((o, oldW, newW) -> { if(newW != null) stagePrincipal = (Stage) newW; });
        });

        layoutMain.getChildren().addAll(header, scroll);
        return layoutMain;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");

        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        // Colores de texto dinámicos para modo oscuro y claro
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private void actualizarTableroNotas() {
        gridNotas.getChildren().clear();
        List<NotasMaestroController.ApunteDocenteDTO> apuntes = controller.cargarApuntes();

        if (apuntes.isEmpty()) {
            VBox vacioBox = new VBox(15);
            vacioBox.setAlignment(Pos.CENTER);
            vacioBox.setPadding(new Insets(100, 0, 0, 0));
            Label iconoVacio = new Label("📋");
            iconoVacio.setStyle("-fx-font-size: 60px; -fx-font-family: 'Segoe UI Emoji';");
            Label msjVacio = new Label("No hay planeaciones registradas. El pizarrón está limpio.");
            msjVacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 16px;");
            vacioBox.getChildren().addAll(iconoVacio, msjVacio);
            gridNotas.getChildren().add(vacioBox);
            return;
        }

        for (NotasMaestroController.ApunteDocenteDTO apunte : apuntes) {
            gridNotas.getChildren().add(crearTarjetaKeep(apunte));
        }
    }

    private VBox crearTarjetaKeep(NotasMaestroController.ApunteDocenteDTO apunte) {
        VBox card = new VBox(10);
        card.setPrefWidth(260); 
        card.setMaxWidth(260);
        card.setPadding(new Insets(20));
        
        card.setStyle("-fx-background-color: " + apunte.colorHex + "; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;");

        // El texto sobre los post-its pastel debe ser oscuro sin importar el modo de la app
        String colorTextoNota = "#2D3436"; 
        String colorSubTextoNota = "#636E72";

        Label lblCat = new Label(apunte.categoria);
        lblCat.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: " + colorTextoNota + "; -fx-background-color: rgba(255,255,255,0.4); -fx-padding: 4 10; -fx-background-radius: 12;");
        
        Label lblTit = new Label(apunte.titulo);
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + colorTextoNota + "; -fx-wrap-text: true;");
        
        Label lblGrupoInfo = new Label("Para: " + apunte.grupo);
        lblGrupoInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: " + colorSubTextoNota + "; -fx-font-weight: bold;");

        String previewLimpio = apunte.contenido.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
        Label lblTexto = new Label(previewLimpio);
        lblTexto.setStyle("-fx-text-fill: " + colorTextoNota + "; -fx-wrap-text: true; -fx-line-spacing: 5px; -fx-font-size: 13px;");
        lblTexto.setMaxHeight(90); 
        
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label lblFecha = new Label(apunte.fecha);
        lblFecha.setStyle("-fx-font-size: 11px; -fx-text-fill: " + colorSubTextoNota + "; -fx-font-weight: bold;");
        Region spF = new Region(); HBox.setHgrow(spF, Priority.ALWAYS);
        
        Button btnEliminar = new Button("🗑");
        btnEliminar.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: #D32F2F; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;");
        btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;"));
        btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: #D32F2F; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;"));
        
        btnEliminar.setOnAction(e -> {
            e.consume();
            controller.eliminarApunte(apunte.id);
            actualizarTableroNotas();
            NotificacionManager.mostrarNotificacion("Eliminado", "La nota de academia fue eliminada.", "INFO");
        });
        footer.getChildren().addAll(lblFecha, spF, btnEliminar);

        card.getChildren().addAll(lblCat, lblTit, lblGrupoInfo, lblTexto, footer);
        card.setOnMouseClicked(e -> mostrarVisorDocumento(apunte));

        ScaleTransition stIn = new ScaleTransition(Duration.millis(150), card); stIn.setToX(1.04); stIn.setToY(1.04);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(150), card); stOut.setToX(1.0); stOut.setToY(1.0);
        card.setOnMouseEntered(e -> stIn.playFromStart());
        card.setOnMouseExited(e -> stOut.playFromStart());

        return card;
    }

    private void mostrarEditorAvanzado() {
        Stage popup = new Stage();
        if (stagePrincipal != null) popup.initOwner(stagePrincipal);
        popup.initStyle(StageStyle.UNDECORATED);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        // --- FIX COMBOBOX CATEGORÍA ---
        ComboBox<String> cmbCat = new ComboBox<>();
        cmbCat.setPromptText("Tipo de nota...");
        cmbCat.getItems().addAll(controller.getCategoriasDisponibles());
        cmbCat.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorFuerte + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: " + colorTextMain + ";");
        aplicarEstiloDesplegable(cmbCat);
        
        // --- FIX COMBOBOX GRUPO ---
        ComboBox<String> cmbGrupo = new ComboBox<>();
        cmbGrupo.setPromptText("Vincular a grupo...");
        cmbGrupo.getItems().addAll(controller.getGruposDisponibles());
        cmbGrupo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-text-fill: " + colorTextMain + ";");
        aplicarEstiloDesplegable(cmbGrupo);

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCancelar.setOnAction(e -> popup.close());
        
        Button btnGuardar = new Button("💾 Guardar Documento");
        btnGuardar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        topBar.getChildren().addAll(cmbCat, cmbGrupo, sp1, btnCancelar, btnGuardar);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 20, 0));

        VBox editorBox = new VBox(15);
        
        // --- FIX TEXTFIELD TÍTULO ---
        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Escribe el Título de la Planeación o Nota...");
        txtTitulo.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 24px; -fx-font-weight: bold; -fx-prompt-text-fill: " + colorTextMuted + ";");
        
        HTMLEditor editorHtml = new HTMLEditor();
        editorHtml.setPrefHeight(450);
        VBox.setVgrow(editorHtml, Priority.ALWAYS);

        editorBox.getChildren().addAll(txtTitulo, editorHtml);
        root.setCenter(editorBox);

        btnGuardar.setOnAction(e -> {
            if (!txtTitulo.getText().isEmpty() && cmbCat.getValue() != null && cmbGrupo.getValue() != null && !editorHtml.getHtmlText().isEmpty()) {
                controller.guardarApunte(txtTitulo.getText(), cmbCat.getValue(), cmbGrupo.getValue(), editorHtml.getHtmlText());
                actualizarTableroNotas();
                popup.close();
                NotificacionManager.mostrarNotificacion("Guardado", "Nota docente fijada en el pizarrón.", "INFO");
            } else {
                NotificacionManager.mostrarNotificacion("Información Incompleta", "Selecciona categoría, grupo y ponle un título válido.", "URGENTE");
            }
        });

        Scene scene = new Scene(root, 900, 650);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - scene.getWidth()) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - scene.getHeight()) / 2);
        }
        popup.show();
    }

    // Método auxiliar para que los ComboBox se vean bien en modo oscuro y claro
    private void aplicarEstiloDesplegable(ComboBox<String> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    setStyle("-fx-background-color: " + colorBgPanel + ";"); 
                } else {
                    setText(item);
                    setStyle("-fx-background-color: " + colorBgPanel + "; -fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold; -fx-padding: 8;");
                }
            }
        });
        comboBox.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    setStyle("-fx-text-fill: " + colorTextMuted + ";"); 
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void mostrarVisorDocumento(NotasMaestroController.ApunteDocenteDTO apunte) {
        Stage popup = new Stage();
        if (stagePrincipal != null) popup.initOwner(stagePrincipal);
        popup.initStyle(StageStyle.UNDECORATED);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setStyle("-fx-background-color: " + apunte.colorHex + "; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnCerrar = new Button("✖ Cerrar");
        btnCerrar.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-text-fill: #2D3436; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 15;");
        btnCerrar.setOnAction(e -> popup.close());
        
        Label lblTit = new Label(apunte.titulo);
        lblTit.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2D3436;");
        
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        
        Label lblCat = new Label(apunte.categoria + " | " + apunte.grupo);
        lblCat.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2D3436; -fx-background-color: rgba(255,255,255,0.4); -fx-padding: 5 15; -fx-background-radius: 15;");
        
        topBar.getChildren().addAll(btnCerrar, lblTit, sp1, lblCat);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 20, 0));

        WebView visorWeb = new WebView();
        // Integrar color de fondo del visor web con el Post-it y asegurar que la letra sea oscura
        String htmlVisual = "<div style='font-family: \"Segoe UI\", Arial, sans-serif; padding: 20px; background-color: " + apunte.colorHex + "; color: #2D3436; line-height: 1.6; font-size: 16px;'>" 
                          + apunte.contenido + "</div>";
        visorWeb.getEngine().loadContent(htmlVisual);
        
        visorWeb.setStyle("-fx-background-color: transparent;");
        root.setCenter(visorWeb);

        Scene scene = new Scene(root, 800, 650);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - scene.getWidth()) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - scene.getHeight()) / 2);
        }
        popup.show();
    }
}