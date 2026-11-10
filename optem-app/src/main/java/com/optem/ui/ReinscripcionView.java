package com.optem.ui;

import java.awt.Desktop;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.controllers.ReinscripcionController;
import com.optem.logic.NotificacionManager;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ReinscripcionView extends BorderPane {

    private final ReinscripcionController controller = new ReinscripcionController();

    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, correoActual, nombreActual, fontSizeActual;
    private final Map<String, ComboBox<String>> combosMaterias = new HashMap<>();
    private GridPane gridHorario;
    private boolean existeEmpalme = false;

    public ReinscripcionView() {
        leerPreferencias();
        this.setPadding(new Insets(20, 30, 20, 30));
        this.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        BorderPane header = new BorderPane();
        VBox titleBox = new VBox(5);
        
        Label titulo = new Label("Reinscripción ISW");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        
        Label subtitulo = new Label("Arma tu horario inteligente y recibe tu hoja de pago.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        
        titleBox.getChildren().addAll(titulo, subtitulo);
        header.setLeft(titleBox);
        this.setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0));

        Preferences prefsGlobales = Preferences.userRoot().node("optem_erp_global");
        boolean periodoAbierto = controller.periodoAbierto();
        
        Map<String, String> estadoMiInscripcion = controller.verificarEstadoInscripcion(correoActual);

        if (!periodoAbierto) {
            mostrarPantallaCerrada("El periodo de reinscripción se encuentra CERRADO."); 
        } else if (estadoMiInscripcion != null) {
            mostrarPantallaEstado(estadoMiInscripcion);
        } else {
            construirPantallaInscripcion(); 
        }
    }

    private void construirPantallaInscripcion() {
        VBox layoutVertical = new VBox(20);
        
        HBox body = new HBox(20);
        body.setAlignment(Pos.TOP_CENTER);

        VBox colIzq = new VBox(15);
        colIzq.setPrefWidth(420);
        colIzq.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12;");

        VBox listaMaterias = new VBox(10);
        
        // Las mismas tarjetas y estilos del original, agrupadas según el plan académico.
        java.util.Map<String,java.util.List<ReinscripcionController.MateriaDTO>> porSemestre=new java.util.LinkedHashMap<>();
        for(int n=1;n<=10;n++)porSemestre.put("SEMESTRE "+n,new java.util.ArrayList<>());
        porSemestre.put("OPTATIVAS",new java.util.ArrayList<>());
        for(ReinscripcionController.MateriaDTO m : controller.getMateriasDisponibles())
            porSemestre.computeIfAbsent(m.categoria,k->new java.util.ArrayList<>()).add(m);
        for(java.util.Map.Entry<String,java.util.List<ReinscripcionController.MateriaDTO>> bloque:porSemestre.entrySet()){
            if(bloque.getValue().isEmpty())continue;
            listaMaterias.getChildren().add(crearTituloSeccion("📚 "+bloque.getKey()));
            for(ReinscripcionController.MateriaDTO m:bloque.getValue())
                listaMaterias.getChildren().add(crearCardMateria(m));
        }

        ScrollPane scrollMaterias = new ScrollPane(listaMaterias);
        scrollMaterias.setFitToWidth(true);
        scrollMaterias.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollMaterias, Priority.ALWAYS);

        Button btnInscribir = new Button("Validar e Inscribir Horario");
        btnInscribir.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        btnInscribir.setMaxWidth(Double.MAX_VALUE);
        
        btnInscribir.setOnAction(e -> {
            if (existeEmpalme) {
                NotificacionManager.mostrarNotificacion("❌ Error de Validación", "Tienes materias chocando en el mismo horario.", "URGENTE");
            } else {
                guardarInscripcionOficial();
            }
        });

        colIzq.getChildren().addAll(scrollMaterias, btnInscribir);

        VBox colDer = new VBox(15);
        colDer.setPrefWidth(700);
        HBox.setHgrow(colDer, Priority.ALWAYS);

        HBox panelIA = new HBox(10);
        panelIA.setAlignment(Pos.CENTER_LEFT);
        panelIA.setStyle("-fx-background-color: " + colorFuerte + "15; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        
        Label lblIA = new Label("🤖 Auto-IA:");
        lblIA.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");
        
        panelIA.getChildren().addAll(lblIA, crearBotonIA("Matutino", "MATUTINO"), crearBotonIA("Mañanas Libres", "TARDES"));

        VBox contCalendario = new VBox(10);
        contCalendario.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-padding: 15; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12;");
        VBox.setVgrow(contCalendario, Priority.ALWAYS);

        gridHorario = new GridPane();
        gridHorario.setGridLinesVisible(false);
        gridHorario.setHgap(5); gridHorario.setVgap(5);
        
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(i == 0 ? 10 : 15);
            gridHorario.getColumnConstraints().add(cc);
        }

        String[] dias = {"Hora", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < dias.length; i++) {
            Label l = new Label(dias[i]);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-alignment: center;");
            l.setMaxWidth(Double.MAX_VALUE);
            gridHorario.add(l, i, 0);
        }

        for (int i = 0; i < 13; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(25); 
            gridHorario.getRowConstraints().add(rc);
            
            Label lHora = new Label((i + 7) + ":00");
            lHora.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 10px; -fx-alignment: top_right; -fx-padding: 0 5 0 0;");
            lHora.setMaxWidth(Double.MAX_VALUE);
            gridHorario.add(lHora, 0, i + 1);
        }

        contCalendario.getChildren().add(gridHorario);
        colDer.getChildren().addAll(panelIA, contCalendario);

        body.getChildren().addAll(colIzq, colDer);
        layoutVertical.getChildren().add(body);
        
        ScrollPane scrollGlobal = new ScrollPane(layoutVertical);
        scrollGlobal.setFitToWidth(true);
        scrollGlobal.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        this.setCenter(scrollGlobal);
        
        repintarHorario();
    }
    
    private void guardarInscripcionOficial() {
        java.util.Map<String,String> seleccionadas=new java.util.LinkedHashMap<>();
        for(Map.Entry<String,ComboBox<String>> entry:combosMaterias.entrySet()){
            String grupo=entry.getValue().getValue();
            if(grupo!=null && !grupo.contains("— No cursar —"))seleccionadas.put(entry.getKey(),grupo);
        }
        if(seleccionadas.isEmpty()){
            NotificacionManager.mostrarNotificacion("Advertencia","Selecciona materias con grupos reales.","URGENTE");return;
        }
        try{
            controller.registrarSeleccion(correoActual,seleccionadas);
            Map<String,String> estado=controller.verificarEstadoInscripcion(correoActual);
            if(estado!=null)mostrarPantallaEstado(estado);
            NotificacionManager.mostrarNotificacion("Enviado","Solicitud registrada en MySQL.","INFO");
        }catch(RuntimeException ex){
            NotificacionManager.mostrarNotificacion("No se pudo inscribir",ex.getMessage(),"ERROR");
        }
    }

    private void mostrarPantallaEstado(Map<String, String> estado) {
        VBox panelCentral = new VBox(20);
        panelCentral.setAlignment(Pos.CENTER);
        panelCentral.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-padding: 50; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20;");
        
        boolean yaEnviada = estado.get("estado").equals("HOJA ENVIADA");

        Label lblIcono = new Label(yaEnviada ? "📄" : "⏳");
        lblIcono.setStyle("-fx-font-size: 70px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: " + colorTextMain + ";");
        
        Label lblMensaje = new Label(yaEnviada ? "¡Tu hoja de pago está lista!" : "Horario validado y enviado.");
        lblMensaje.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        
        Label lblSub = new Label(yaEnviada ? "Descarga el archivo PDF adjunto para realizar tu pago bancario." : "Administración está validando tus datos para generar tu hoja de pago.");
        lblSub.setStyle("-fx-text-fill: " + colorTextMuted + ";");

        panelCentral.getChildren().addAll(lblIcono, lblMensaje, lblSub);

        if (yaEnviada) {
            Button btnDescargar = new Button("📥 Descargar Hoja de Pago (PDF)");
            btnDescargar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
            
            btnDescargar.setOnAction(e -> {
                try {
                    File file = new File(estado.get("ruta_hoja_pago"));
                    if (file.exists()) Desktop.getDesktop().open(file);
                    else NotificacionManager.mostrarNotificacion("Error", "El documento no se encuentra en el servidor.", "ERROR");
                } catch (Exception ex) {}
            });
            
            panelCentral.getChildren().add(btnDescargar);
        }

        VBox centerWrap = new VBox(panelCentral);
        centerWrap.setAlignment(Pos.CENTER);
        this.setCenter(centerWrap);
    }

    private void mostrarPantallaCerrada(String mensaje) {
        VBox panelCerrado = new VBox(20);
        panelCerrado.setAlignment(Pos.CENTER);
        panelCerrado.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-padding: 50; -fx-border-color: #D32F2F; -fx-border-width: 2; -fx-border-radius: 20; -fx-background-radius: 20;");
        
        Label lblIcono = new Label("🔒");
        lblIcono.setStyle("-fx-font-size: 70px; -fx-font-family: 'Segoe UI Emoji';");
        
        Label lblMensaje = new Label(mensaje);
        lblMensaje.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #D32F2F;");
        
        panelCerrado.getChildren().addAll(lblIcono, lblMensaje);
        
        VBox centerWrap = new VBox(panelCerrado);
        centerWrap.setAlignment(Pos.CENTER);
        this.setCenter(centerWrap);
    }

    private Label crearTituloSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10 0 5 0; -fx-border-color: " + colorBorder + "; -fx-border-width: 0 0 1 0;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private VBox crearCardMateria(ReinscripcionController.MateriaDTO m) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: " + m.colorBorde + "; -fx-border-width: 0 0 0 4; -fx-background-color: " + colorBgBase + "; -fx-background-radius: 0 6 6 0;");

        Label lblNom = new Label(m.titulo);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        
        ComboBox<String> cmbGrupos = new ComboBox<>();
        cmbGrupos.getItems().addAll(m.opciones);
        cmbGrupos.getSelectionModel().selectFirst();
        cmbGrupos.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 4;");
        cmbGrupos.setMaxWidth(Double.MAX_VALUE);
        
        cmbGrupos.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-background-color: " + colorBgPanel + "; -fx-text-fill: " + colorTextMain + ";");
            }
        });
        cmbGrupos.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: " + colorTextMain + ";");
            }
        });

        cmbGrupos.setOnAction(e -> repintarHorario());
        combosMaterias.put(m.titulo, cmbGrupos);
        card.getChildren().addAll(lblNom, cmbGrupos);
        return card;
    }

    private Button crearBotonIA(String texto, String perfil) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorFuerte + "; -fx-border-radius: 6; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand;");
        btn.setOnAction(e -> {
            for (ComboBox<String> combo : combosMaterias.values()) combo.getSelectionModel().select(0);
            
            Map<String, String> recomendacion = controller.generarHorarioInteligente(perfil);
            for (Map.Entry<String, String> entry : recomendacion.entrySet()) {
                if (combosMaterias.containsKey(entry.getKey())) {
                    combosMaterias.get(entry.getKey()).setValue(entry.getValue());
                }
            }
            repintarHorario();
            NotificacionManager.mostrarNotificacion("IA Aplicada", "Horario generado: " + texto, "INFO");
        });
        return btn;
    }

    private void repintarHorario() {
        existeEmpalme = false;
        gridHorario.getChildren().removeIf(node -> GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) > 0 && GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        for (int col = 1; col < 7; col++) {
            for (int row = 1; row < 14; row++) {
                Region celdaVacia = new Region();
                celdaVacia.setStyle("-fx-background-color: " + colorBgBase + "; -fx-background-radius: 4; -fx-border-color: " + colorBorder + "; -fx-border-width: 0.5;");
                gridHorario.add(celdaVacia, col, row);
            }
        }

        boolean[][] ocupacion = new boolean[7][15]; 

        for (ReinscripcionController.MateriaDTO m : controller.getMateriasDisponibles()) {
            ComboBox<String> combo = combosMaterias.get(m.titulo);
            if (combo == null) continue;
            
            String seleccion = combo.getValue();
            List<ReinscripcionController.BloqueHorario> bloques = controller.procesarSeleccion(m.titulo, seleccion, m.colorBorde);
            
            for (ReinscripcionController.BloqueHorario b : bloques) {
                boolean hayEmpalmeLocal = false;
                for (int i = 0; i < b.duracion; i++) {
                    if (ocupacion[b.colDia][b.rowHora + i]) {
                        hayEmpalmeLocal = true;
                        existeEmpalme = true;
                    }
                    ocupacion[b.colDia][b.rowHora + i] = true;
                }

                VBox tarjetaClase = new VBox(2);
                tarjetaClase.setAlignment(Pos.CENTER);
                tarjetaClase.setPadding(new Insets(2));
                
                if (hayEmpalmeLocal) {
                    tarjetaClase.setStyle("-fx-background-color: #ffe6e6; -fx-border-color: #D32F2F; -fx-border-width: 2; -fx-background-radius: 6;");
                    Label lblN = new Label("¡EMPALME!"); lblN.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-font-size: 10px;");
                    tarjetaClase.getChildren().add(lblN);
                } else {
                    tarjetaClase.setStyle("-fx-background-color: " + b.colorHex + "30; -fx-border-color: " + b.colorHex + "; -fx-border-width: 0 0 0 4; -fx-background-radius: 4;");
                    Label lblN = new Label(b.texto); lblN.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold; -fx-font-size: 9px; -fx-alignment: center; -fx-text-alignment: center; -fx-wrap-text: true;");
                    tarjetaClase.getChildren().add(lblN);
                }

                gridHorario.add(tarjetaClase, b.colDia, b.rowHora + 1, 1, b.duracion);
                GridPane.setHalignment(tarjetaClase, HPos.CENTER);
                GridPane.setValignment(tarjetaClase, VPos.CENTER);
            }
        }
    }
    
    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        // CORRECCIÓN: Leemos el nombre directamente de las Preferences
        nombreActual = prefs.get("PREF_NAME_" + correoActual, "Estudiante Optem");
        
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";

        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9"; colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b"; colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
}