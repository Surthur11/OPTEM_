package com.optem.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;

import com.optem.controllers.ActividadesController;
import com.optem.logic.NotificacionManager;
import com.optem.models.Actividad;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ActividadesView extends VBox {

    private final ActividadesController controller = new ActividadesController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;
    
    private VBox panelTarjetas;
    
    private LocalDate fechaFormulario = LocalDate.now();
    private LocalDate fechaFiltro = LocalDate.now();
    
    private Stage stagePrincipal;

    public ActividadesView() {
        leerPreferencias();
        this.setPadding(new Insets(30, 40, 30, 40));
        this.setSpacing(25);
        this.setStyle("-fx-background-color: transparent;");

        Label lblTitulo = new Label("Gestión de Actividades Personales");
        lblTitulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");

        Label lblSubtitulo = new Label("Crea, edita y filtra tus eventos.");
        lblSubtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");

        VBox header = new VBox(5, lblTitulo, lblSubtitulo);

        HBox splitBox = new HBox(30);
        VBox.setVgrow(splitBox, Priority.ALWAYS);

        VBox formBox = crearFormularioAlta();
        VBox listaBox = crearPanelLista();

        splitBox.getChildren().addAll(formBox, listaBox);
        this.getChildren().addAll(header, splitBox);

        this.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) newS.windowProperty().addListener((o, oldW, newW) -> { if(newW != null) stagePrincipal = (Stage) newW; });
        });
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");

        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private VBox crearFormularioAlta() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(400); panel.setMaxWidth(400);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label lblTit = new Label("✨ Nueva Actividad");
        lblTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        lblTit.getStyleClass().add("lbl-titulo-seccion");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej. Partido de Fútbol, Cita...");
        txtNombre.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: transparent;");

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("Estudio", "Reunión", "Deporte", "Cultural", "Personal"));
        cmbTipo.setPromptText("Selecciona una categoría...");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 5; -fx-background-radius: 8;");

        Button btnFechaFormulario = new Button("📅 " + fechaFormulario.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        btnFechaFormulario.setMaxWidth(Double.MAX_VALUE);
        btnFechaFormulario.setAlignment(Pos.CENTER_LEFT);
        btnFechaFormulario.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        
        btnFechaFormulario.setOnAction(e -> mostrarSelectorCalendarioCustom(fechaFormulario, nuevaFecha -> {
            fechaFormulario = nuevaFecha;
            btnFechaFormulario.setText("📅 " + fechaFormulario.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }));

        HBox boxHora = new HBox(10);
        boxHora.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Integer> cmbHora = new ComboBox<>(); for (int i = 0; i < 24; i++) cmbHora.getItems().add(i); cmbHora.setValue(12);
        cmbHora.setStyle("-fx-background-color: " + colorBgBase + ";");
        ComboBox<Integer> cmbMinuto = new ComboBox<>(); for (int i = 0; i < 60; i+=5) cmbMinuto.getItems().add(i); cmbMinuto.setValue(0);
        cmbMinuto.setStyle("-fx-background-color: " + colorBgBase + ";");
        Label lblSeparador = new Label(":"); lblSeparador.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold;");
        boxHora.getChildren().addAll(crearLblForm("Hora (24h):"), cmbHora, lblSeparador, cmbMinuto);

        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Agrega notas (Opcional)...");
        txtDesc.setPrefRowCount(4);
        txtDesc.setStyle("-fx-control-inner-background: " + colorBgBase + "; -fx-background-color: transparent; -fx-text-fill: " + colorTextMain + ";");

        Button btnGuardar = new Button("💾 Guardar Actividad");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        
        btnGuardar.setOnAction(e -> {
            boolean guardado = controller.guardarNuevaActividad(
                txtNombre.getText(), cmbTipo.getValue(), txtDesc.getText(), 
                fechaFormulario, LocalTime.of(cmbHora.getValue(), cmbMinuto.getValue())
            );
            if (guardado) {
                txtNombre.clear(); txtDesc.clear(); cmbTipo.setValue(null);
                NotificacionManager.mostrarNotificacion("Éxito", "Actividad registrada.", "INFO");
                if (fechaFiltro.equals(fechaFormulario)) actualizarListaActividades(fechaFiltro);
            } else {
                NotificacionManager.mostrarNotificacion("Error", "El título y la categoría son obligatorios.", "URGENTE");
            }
        });

        panel.getChildren().addAll(
            lblTit, crearLblForm("Título de la actividad"), txtNombre,
            crearLblForm("Categoría"), cmbTipo,
            crearLblForm("Fecha programada"), btnFechaFormulario,
            boxHora, crearLblForm("Detalles extra"), txtDesc,
            new Region(), btnGuardar
        );
        return panel;
    }

    private Label crearLblForm(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px;");
        return l;
    }

    private VBox crearPanelLista() {
        VBox panel = new VBox(15);
        HBox.setHgrow(panel, Priority.ALWAYS);

        HBox topBox = new HBox(12);
        topBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblTit = new Label("📋 Registros del Día");
        lblTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        lblTit.getStyleClass().add("lbl-titulo-seccion");
        
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        
        Button btnAtras = new Button("◀"); 
        btnAtras.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        
        Button btnFiltroFecha = new Button("📅 " + fechaFiltro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        btnFiltroFecha.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-padding: 5 15;");
        
        Button btnAdelante = new Button("▶"); 
        btnAdelante.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        
        Button btnHoy = new Button("Hoy");
        btnHoy.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: " + colorFuerte + "; -fx-border-radius: 6; -fx-padding: 5 15;");
        
        btnAtras.setOnAction(e -> cambiarFechaFiltro(fechaFiltro.minusDays(1), btnFiltroFecha));
        btnAdelante.setOnAction(e -> cambiarFechaFiltro(fechaFiltro.plusDays(1), btnFiltroFecha));
        btnHoy.setOnAction(e -> cambiarFechaFiltro(LocalDate.now(), btnFiltroFecha));
        
        btnFiltroFecha.setOnAction(e -> mostrarSelectorCalendarioCustom(fechaFiltro, nuevaFecha -> cambiarFechaFiltro(nuevaFecha, btnFiltroFecha)));

        topBox.getChildren().addAll(lblTit, sp, btnAtras, btnFiltroFecha, btnAdelante, btnHoy);

        panelTarjetas = new VBox(12);
        panelTarjetas.setPadding(new Insets(0, 15, 20, 0));
        
        ScrollPane scroll = new ScrollPane(panelTarjetas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        actualizarListaActividades(fechaFiltro);

        panel.getChildren().addAll(topBox, scroll);
        return panel;
    }

    private void cambiarFechaFiltro(LocalDate nuevaFecha, Button btnActualizar) {
        fechaFiltro = nuevaFecha;
        btnActualizar.setText("📅 " + fechaFiltro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        actualizarListaActividades(fechaFiltro);
    }

    private void actualizarListaActividades(LocalDate fecha) {
        panelTarjetas.getChildren().clear();
        List<Actividad> acts = controller.getActividadesPorFecha(fecha);
        
        if (acts == null || acts.isEmpty()) {
            VBox vacio = new VBox(10); vacio.setAlignment(Pos.CENTER); vacio.setPadding(new Insets(80, 0, 0, 0));
            Label l1 = new Label("📭"); l1.setStyle("-fx-font-size: 50px;");
            Label l2 = new Label("No hay actividades registradas para este día."); l2.setStyle("-fx-text-fill: " + colorTextMuted + ";");
            l2.getStyleClass().add("lbl-text-normal");
            vacio.getChildren().addAll(l1, l2);
            panelTarjetas.getChildren().add(vacio);
            return;
        }

        for (Actividad act : acts) {
            panelTarjetas.getChildren().add(crearTarjetaActividad(act));
        }
    }

    private HBox crearTarjetaActividad(Actividad act) {
        boolean estaCompletada = "COMPLETADO".equals(act.getEstado());
        
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 10; -fx-border-color: " + colorBorder + "; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2); -fx-cursor: hand;");

        String tipo = act.getTipo() != null ? act.getTipo() : "";
        String emoji = switch(tipo) {
            case "Estudio" -> "📚"; case "Reunión" -> "👥"; case "Deporte" -> "⚽"; case "Cultural" -> "🎨"; default -> "📌";
        };
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 24px; -fx-font-family: 'Segoe UI Emoji';");
        
        VBox info = new VBox(3);
        String tit = (act.getNombre() != null) ? act.getNombre() : "Actividad";
        Label lblT = new Label(tit);
        lblT.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";" + (estaCompletada ? " -fx-strikethrough: true;" : ""));
        lblT.getStyleClass().add("lbl-text-normal");
        
        String tHora = (act.getHoraVencimiento() != null) ? act.getHoraVencimiento().format(DateTimeFormatter.ofPattern("HH:mm")) : "00:00";
        Label lblSub = new Label(tipo + " • " + tHora);
        lblSub.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        lblSub.getStyleClass().add("lbl-text-pequeno");
        info.getChildren().addAll(lblT, lblSub);
        
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label lblEstado = new Label(estaCompletada ? "Completado" : "Pendiente");
        lblEstado.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 10; " + (estaCompletada ? "-fx-text-fill: #388E3C; -fx-background-color: #E8F5E9;" : "-fx-text-fill: " + colorFuerte + "; -fx-background-color: " + colorFuerte + "20;"));
        
        Button btnBorrar = new Button("🗑");
        btnBorrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #D32F2F; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji';");
        btnBorrar.setOnAction(e -> {
            e.consume(); 
            controller.eliminarActividad(act);
            actualizarListaActividades(fechaFiltro);
            NotificacionManager.mostrarNotificacion("Eliminado", "La actividad fue borrada.", "INFO");
        });

        card.getChildren().addAll(icon, info, sp, lblEstado, btnBorrar);
        
        card.setOnMouseClicked(e -> mostrarVentanaEdicion(act));
        
        return card;
    }

    private void mostrarSelectorCalendarioCustom(LocalDate fechaInicial, java.util.function.Consumer<LocalDate> alSeleccionar) {
        Stage popup = new Stage();
        if (stagePrincipal != null) popup.initOwner(stagePrincipal);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 40, 0, 0, 10);");

        final LocalDate[] mesVisible = {fechaInicial}; 
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        Button btnAtras = new Button("◀"); btnAtras.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand;");
        Button btnAdel = new Button("▶"); btnAdel.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand;");
        Label lblMes = new Label(); lblMes.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + colorTextMain + "; -fx-min-width: 130px; -fx-alignment: center;");
        header.getChildren().addAll(btnAtras, lblMes, btnAdel);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        String[] dias = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(dias[i]);
            l.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold;");
            GridPane.setHalignment(l, HPos.CENTER);
            grid.add(l, i, 0);
        }

        Runnable renderGrid = () -> {
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
            YearMonth ym = YearMonth.from(mesVisible[0]);
            lblMes.setText(ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
            
            int offset = ym.atDay(1).getDayOfWeek().getValue() - 1; 
            int diasMes = ym.lengthOfMonth();
            int r = 1, c = offset;
            
            for (int d = 1; d <= diasMes; d++) {
                LocalDate fCelda = ym.atDay(d);
                Label lblNum = new Label(String.valueOf(d));
                lblNum.setStyle("-fx-text-fill: " + colorTextMain + ";");
                VBox btnDia = new VBox(lblNum);
                btnDia.setAlignment(Pos.CENTER);
                btnDia.setMinSize(30, 30);
                btnDia.setStyle("-fx-background-color: transparent; -fx-background-radius: 15; -fx-cursor: hand;");
                
                if (fCelda.equals(LocalDate.now())) {
                    btnDia.setStyle("-fx-background-color: " + colorFuerte + "40; -fx-background-radius: 15; -fx-cursor: hand;");
                    lblNum.setStyle("-fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold;");
                }
                if (fCelda.equals(fechaInicial)) {
                    btnDia.setStyle("-fx-background-color: " + colorFuerte + "; -fx-background-radius: 15; -fx-cursor: hand;");
                    lblNum.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }

                btnDia.setOnMouseClicked(e -> {
                    alSeleccionar.accept(fCelda);
                    popup.close();
                });

                GridPane.setHalignment(btnDia, HPos.CENTER);
                grid.add(btnDia, c, r);
                c++; if (c > 6) { c = 0; r++; }
            }
        };

        btnAtras.setOnAction(e -> { mesVisible[0] = mesVisible[0].minusMonths(1); renderGrid.run(); });
        btnAdel.setOnAction(e -> { mesVisible[0] = mesVisible[0].plusMonths(1); renderGrid.run(); });
        
        renderGrid.run(); 

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: transparent; -fx-text-fill: #D32F2F; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> popup.close());
        HBox boxCancel = new HBox(btnCancelar); boxCancel.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, grid, boxCancel);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - 300) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - 350) / 2);
        }
        popup.show();
    }

    private void mostrarVentanaEdicion(Actividad act) {
        Stage popup = new Stage();
        if (stagePrincipal != null) popup.initOwner(stagePrincipal);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setPrefWidth(450);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 40, 0, 0, 10);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTit = new Label("✏️ Editar Actividad");
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: " + colorTextMain + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnCerrar = new Button("✖");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-cursor: hand;");
        btnCerrar.setOnAction(e -> popup.close());
        header.getChildren().addAll(lblTit, sp, btnCerrar);

        TextField txtNombre = new TextField(act.getNombre());
        txtNombre.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10; -fx-background-radius: 8;");

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("Estudio", "Reunión", "Deporte", "Cultural", "Personal"));
        cmbTipo.setValue(act.getTipo());
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        cmbTipo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 5; -fx-background-radius: 8;");

        final LocalDate[] fechaEdicion = {act.getFecha() != null ? act.getFecha() : LocalDate.now()};
        Button btnFecha = new Button("📅 " + fechaEdicion[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        btnFecha.setMaxWidth(Double.MAX_VALUE); btnFecha.setAlignment(Pos.CENTER_LEFT);
        btnFecha.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        btnFecha.setOnAction(e -> mostrarSelectorCalendarioCustom(fechaEdicion[0], nuevaF -> {
            fechaEdicion[0] = nuevaF;
            btnFecha.setText("📅 " + fechaEdicion[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }));

        HBox boxHora = new HBox(10); boxHora.setAlignment(Pos.CENTER_LEFT);
        ComboBox<Integer> cmbHora = new ComboBox<>(); for (int i=0; i<24; i++) cmbHora.getItems().add(i);
        cmbHora.setValue(act.getHoraVencimiento() != null ? act.getHoraVencimiento().getHour() : 12);
        cmbHora.setStyle("-fx-background-color: " + colorBgBase + ";");
        ComboBox<Integer> cmbMinuto = new ComboBox<>(); for (int i=0; i<60; i+=5) cmbMinuto.getItems().add(i);
        cmbMinuto.setValue(act.getHoraVencimiento() != null ? act.getHoraVencimiento().getMinute() : 0);
        cmbMinuto.setStyle("-fx-background-color: " + colorBgBase + ";");
        boxHora.getChildren().addAll(crearLblForm("Hora (24h):"), cmbHora, new Label(":"), cmbMinuto);

        TextArea txtDesc = new TextArea(act.getDescripcion());
        txtDesc.setPrefRowCount(3);
        txtDesc.setStyle("-fx-control-inner-background: " + colorBgBase + "; -fx-background-color: transparent; -fx-text-fill: " + colorTextMain + ";");

        Button btnGuardar = new Button("💾 Guardar Cambios");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        
        btnGuardar.setOnAction(e -> {
            controller.actualizarActividad(
                act.getId(), txtNombre.getText(), cmbTipo.getValue(), txtDesc.getText(), 
                fechaEdicion[0], LocalTime.of(cmbHora.getValue(), cmbMinuto.getValue())
            );
            actualizarListaActividades(fechaFiltro);
            NotificacionManager.mostrarNotificacion("Actualizado", "Tus cambios han sido guardados.", "INFO");
            popup.close();
        });

        root.getChildren().addAll(
            header,
            crearLblForm("Título"), txtNombre,
            crearLblForm("Categoría"), cmbTipo,
            crearLblForm("Fecha"), btnFecha,
            boxHora,
            crearLblForm("Detalles"), txtDesc,
            new Region(), btnGuardar
        );

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - 450) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - 550) / 2);
        }
        popup.show();
    }
}