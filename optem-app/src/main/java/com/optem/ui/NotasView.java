package com.optem.ui;

import com.optem.controllers.NotasController;
import com.optem.logic.NotificacionManager;
import java.util.List;
import java.util.prefs.Preferences;

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

public class NotasView {
    
    private final NotasController controller = new NotasController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, fontSizeActual;
    
    private FlowPane gridNotas;
    private Stage stagePrincipal; 

    public VBox getView() {
        leerPreferencias();
        controller.getMateriasDisponibles(); 

        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label titulo = new Label("Cuaderno de Notas Mágicas");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Tus apuntes visuales tipo Post-it. Da clic en uno para leerlo.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevo = new Button("✍ Crear Nota Nueva");
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
        
        // Colores de texto dinámicos según el modo
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private void actualizarTableroNotas() {
        gridNotas.getChildren().clear();
        List<NotasController.ApunteDTO> apuntes = controller.cargarApuntes();

        if (apuntes.isEmpty()) {
            VBox vacioBox = new VBox(15);
            vacioBox.setAlignment(Pos.CENTER);
            vacioBox.setPadding(new Insets(100, 0, 0, 0));
            Label iconoVacio = new Label("📝");
            iconoVacio.setStyle("-fx-font-size: 60px; -fx-font-family: 'Segoe UI Emoji';");
            Label msjVacio = new Label("Tu pared está vacía. ¡Agrega tu primer Post-it!");
            msjVacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 16px;");
            vacioBox.getChildren().addAll(iconoVacio, msjVacio);
            gridNotas.getChildren().add(vacioBox);
            return;
        }

        for (NotasController.ApunteDTO apunte : apuntes) {
            gridNotas.getChildren().add(crearTarjetaKeep(apunte));
        }
    }

    private VBox crearTarjetaKeep(NotasController.ApunteDTO apunte) {
        VBox card = new VBox(10);
        card.setPrefWidth(260); 
        card.setMaxWidth(260);
        card.setPadding(new Insets(20));
        
        card.setStyle("-fx-background-color: " + apunte.colorHex + "; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-cursor: hand;");

        // El texto del post-it DEBE ser oscuro siempre, porque los colores de fondo del post-it son pasteles claros (incluso en modo oscuro)
        String colorTextoNota = "#2D3436"; 
        String colorSubTextoNota = "#636E72";

        String icono = apunte.materia.equals("Nota Personal") ? "📌 " : (apunte.materia.equals("Recordatorio Urgente") ? "🚨 " : "📚 ");
        
        Label lblMateria = new Label(icono + apunte.materia);
        lblMateria.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: " + colorTextoNota + "; -fx-background-color: rgba(255,255,255,0.4); -fx-padding: 4 10; -fx-background-radius: 12;");
        
        Label lblTit = new Label(apunte.titulo);
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + colorTextoNota + "; -fx-wrap-text: true;");
        
        Label lblFecha = new Label(apunte.fecha);
        lblFecha.setStyle("-fx-font-size: 11px; -fx-text-fill: " + colorSubTextoNota + "; -fx-font-weight: bold;");

        String previewLimpio = apunte.contenido.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
        Label lblTexto = new Label(previewLimpio);
        lblTexto.setStyle("-fx-text-fill: " + colorTextoNota + "; -fx-wrap-text: true; -fx-line-spacing: 5px; -fx-font-size: 13px;");
        lblTexto.setMaxHeight(110); 
        
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button btnEliminar = new Button("🗑");
        btnEliminar.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: #D32F2F; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;");
        
        btnEliminar.setOnMouseEntered(e -> btnEliminar.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;"));
        btnEliminar.setOnMouseExited(e -> btnEliminar.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: #D32F2F; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 10;"));
        
        btnEliminar.setOnAction(e -> {
            e.consume();
            controller.eliminarApunte(apunte.id);
            actualizarTableroNotas();
            NotificacionManager.mostrarNotificacion("Eliminado", "La nota se hizo bolita y se tiró a la basura.", "INFO");
        });
        footer.getChildren().add(btnEliminar);

        card.getChildren().addAll(lblMateria, lblTit, lblFecha, lblTexto, footer);

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
        
        ComboBox<String> cmbMateria = new ComboBox<>();
        cmbMateria.setPromptText("📌 Asignar a Categoría...");
        cmbMateria.getItems().addAll(controller.getMateriasDisponibles());
        
        // AJUSTE: El ComboBox ahora usa text-fill para el modo oscuro
        cmbMateria.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2px; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5 10; -fx-text-fill: " + colorTextMain + ";");
        
        // Estilo para los items del menú desplegable del ComboBox
        cmbMateria.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    setStyle("-fx-background-color: " + colorBgPanel + ";"); 
                } else {
                    String icon = item.equals("Nota Personal") ? "📌 " : (item.equals("Recordatorio Urgente") ? "🚨 " : "📚 ");
                    setText(icon + item);
                    setStyle("-fx-background-color: " + colorBgPanel + "; -fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold; -fx-padding: 8;");
                }
            }
        });
        // Estilo para el botón principal del ComboBox cuando se selecciona un item
        cmbMateria.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); 
                    // Cuando está vacío, usa el color mutado (para el prompt text)
                    setStyle("-fx-text-fill: " + colorTextMuted + ";"); 
                } 
                else {
                    String icon = item.equals("Nota Personal") ? "📌 " : (item.equals("Recordatorio Urgente") ? "🚨 " : "📚 ");
                    setText(icon + item);
                    // Cuando tiene selección, usa el color principal
                    setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold;");
                }
            }
        });

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCancelar.setOnAction(e -> popup.close());
        
        Button btnGuardar = new Button("💾 Guardar Nota");
        btnGuardar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        topBar.getChildren().addAll(cmbMateria, sp1, btnCancelar, btnGuardar);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 20, 0));

        VBox editorBox = new VBox(15);
        
        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Escribe el Título de tu Apunte...");
        // AJUSTE: El TextField de título ahora responde al colorTextMain y colorTextMuted para el prompt
        txtTitulo.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 24px; -fx-font-weight: bold; -fx-prompt-text-fill: " + colorTextMuted + ";");
        
        HTMLEditor editorHtml = new HTMLEditor();
        editorHtml.setPrefHeight(450);
        VBox.setVgrow(editorHtml, Priority.ALWAYS);

        editorBox.getChildren().addAll(txtTitulo, editorHtml);
        root.setCenter(editorBox);

        btnGuardar.setOnAction(e -> {
            if (!txtTitulo.getText().isEmpty() && cmbMateria.getValue() != null && !editorHtml.getHtmlText().isEmpty()) {
                controller.guardarApunte(txtTitulo.getText(), cmbMateria.getValue(), editorHtml.getHtmlText());
                actualizarTableroNotas();
                popup.close();
                NotificacionManager.mostrarNotificacion("Apunte Creado", "Tu nota ha sido pegada en el tablero.", "INFO");
            } else {
                NotificacionManager.mostrarNotificacion("Información Incompleta", "No puedes dejar un post-it vacío. Llénalo bien.", "URGENTE");
            }
        });

        Scene scene = new Scene(root, 850, 650);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - scene.getWidth()) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - scene.getHeight()) / 2);
        }
        popup.show();
    }

    private void mostrarVisorDocumento(NotasController.ApunteDTO apunte) {
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
        
        Label lblMateria = new Label(apunte.materia);
        lblMateria.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2D3436; -fx-background-color: rgba(255,255,255,0.4); -fx-padding: 5 15; -fx-background-radius: 15;");
        
        topBar.getChildren().addAll(btnCerrar, lblTit, sp1, lblMateria);
        root.setTop(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 20, 0));

        WebView visorWeb = new WebView();
        // El WebView fuerza su fondo y su texto para que se integren con el color pastel del post-it
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