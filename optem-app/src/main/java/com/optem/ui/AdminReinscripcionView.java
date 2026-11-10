package com.optem.ui;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.controllers.AdminController;
import com.optem.controllers.DashboardController;
import com.optem.controllers.ReinscripcionController;
import com.optem.logic.NotificacionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

public class AdminReinscripcionView {

    private final DashboardController controller = new DashboardController();
    private final AdminController adminController = new AdminController();
    private final ReinscripcionController reinController = new ReinscripcionController();

    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, fontSizeActual;

    private boolean periodoAbierto = false;
    private Preferences prefsGlobales;
    
    private VBox listaAlumnosRegistrados; 

    public VBox getView() {
        leerPreferencias();
        
        prefsGlobales = Preferences.userRoot().node("optem_erp_global"); // solo preferencia local histórica
        periodoAbierto = adminController.isReinscripcionActiva();

        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label titulo = new Label("📑 Centro de Reinscripciones y Oferta");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Controla la apertura del ciclo, asigna maestros y entrega hojas de pago.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNotificar = new Button("📢 Notificar a Estudiantes");
        btnNotificar.setStyle("-fx-background-color: transparent; -fx-border-color: " + colorFuerte + "; -fx-border-radius: 8; -fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        btnNotificar.setOnAction(e -> NotificacionManager.mostrarNotificacion("Avisos", "Los correos masivos todavía no están integrados. Utiliza la sección Avisos del administrador.", "INFO"));

        header.getChildren().addAll(titleBox, spacer, btnNotificar);

        HBox controlBox = new HBox(20);
        controlBox.setAlignment(Pos.CENTER_LEFT);
        controlBox.setPadding(new Insets(25));
        controlBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 2, 2);");

        VBox infoEstado = new VBox(5);
        Label lblEstadoTit = new Label("Estado Actual del Sistema:");
        lblEstadoTit.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        
        HBox estadoVisual = new HBox(10);
        estadoVisual.setAlignment(Pos.CENTER_LEFT);
        Circle dotEstado = new Circle(6, Color.web(periodoAbierto ? "#10b981" : "#ef4444"));
        Label lblEstadoVal = new Label(periodoAbierto ? "PERIODO DE REINSCRIPCIÓN: ABIERTO" : "PERIODO DE REINSCRIPCIÓN: CERRADO");
        lblEstadoVal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + (periodoAbierto ? "#10b981" : "#ef4444") + ";");
        estadoVisual.getChildren().addAll(dotEstado, lblEstadoVal);
        
        Label lblAlerta = new Label(periodoAbierto ? "Los estudiantes ahora pueden acceder al módulo y generar sus referencias." : "El módulo de estudiantes está bloqueado. Nadie puede reinscribirse.");
        lblAlerta.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-size: 12px;");
        infoEstado.getChildren().addAll(lblEstadoTit, estadoVisual, lblAlerta);

        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button btnToggleEstado = new Button(periodoAbierto ? "🔒 CERRAR PERIODO" : "🔓 HABILITAR REINSCRIPCIONES");
        btnToggleEstado.setStyle("-fx-background-color: " + (periodoAbierto ? "#ef4444" : "#10b981") + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        
        btnToggleEstado.setOnAction(e -> {
            try{
                boolean nuevoEstado=!periodoAbierto;
                adminController.toggleReinscripcion(nuevoEstado);
                periodoAbierto=nuevoEstado;
            }catch(RuntimeException ex){
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,ex.getMessage()).showAndWait();
                return;
            } 
            
            dotEstado.setFill(Color.web(periodoAbierto ? "#10b981" : "#ef4444"));
            lblEstadoVal.setText(periodoAbierto ? "PERIODO DE REINSCRIPCIÓN: ABIERTO" : "PERIODO DE REINSCRIPCIÓN: CERRADO");
            lblEstadoVal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + (periodoAbierto ? "#10b981" : "#ef4444") + ";");
            lblAlerta.setText(periodoAbierto ? "Los estudiantes ahora pueden acceder al módulo y generar sus referencias." : "El módulo de estudiantes está bloqueado.");
            
            btnToggleEstado.setText(periodoAbierto ? "🔒 CERRAR PERIODO" : "🔓 HABILITAR REINSCRIPCIONES");
            btnToggleEstado.setStyle("-fx-background-color: " + (periodoAbierto ? "#ef4444" : "#10b981") + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
            NotificacionManager.mostrarNotificacion("Cambio de Estado", periodoAbierto ? "Se ha habilitado la reinscripción." : "Las reinscripciones han concluido.", "INFO");
        });
        controlBox.getChildren().addAll(infoEstado, spacer2, btnToggleEstado);

        VBox creacionGruposBox = new VBox(15);
        creacionGruposBox.setPadding(new Insets(20));
        creacionGruposBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label lblOfertaTit = new Label("👨‍🏫 Apertura de Grupos (Asignación de Maestros)");
        lblOfertaTit.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + colorTextMain + ";");

        GridPane gridOferta = new GridPane();
        gridOferta.setHgap(15); gridOferta.setVgap(15);

        javafx.scene.control.ComboBox<Integer> cbSemestre=new javafx.scene.control.ComboBox<>();
        cbSemestre.getItems().addAll(adminController.semestresDisponibles());
        cbSemestre.setPromptText("Semestre");
        javafx.scene.control.ComboBox<String> cbMateria=new javafx.scene.control.ComboBox<>();
        cbMateria.setPromptText("Selecciona materia");
        java.util.Map<String,String> materias=new java.util.LinkedHashMap<>();
        cbSemestre.valueProperty().addListener((o,a,n)->{
            cbMateria.getItems().clear();materias.clear();
            if(n!=null){materias.putAll(adminController.materiasPorSemestre(n));
                for(var ent:materias.entrySet())cbMateria.getItems().add(ent.getKey()+" - "+ent.getValue());}
        });
        TextField txtMateria = new TextField(); txtMateria.setPromptText("Ej: Arquitectura de Software");
        txtMateria.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        java.util.Map<String,String> docentes=adminController.docentes();
        javafx.scene.control.ComboBox<String> cbMaestro=new javafx.scene.control.ComboBox<>();
        cbMaestro.setPromptText("Docente");
        for(var ent:docentes.entrySet())cbMaestro.getItems().add(ent.getValue()+" · "+ent.getKey());
        cbMaestro.setOnShowing(ev->{String antes=cbMaestro.getValue();cbMaestro.getItems().clear();for(var ent:adminController.docentes().entrySet())cbMaestro.getItems().add(ent.getValue()+" · "+ent.getKey());if(cbMaestro.getItems().contains(antes))cbMaestro.setValue(antes);});
        TextField txtMaestro = new TextField(); txtMaestro.setPromptText("Nombre del Profesor");
        txtMaestro.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        TextField txtHorario = new TextField(); txtHorario.setPromptText("Ej: Lun/Mié 09:00-11:00");
        txtHorario.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        TextField txtGrupo = new TextField(); txtGrupo.setPromptText("Generación automática");
        txtGrupo.setEditable(false);
        cbMateria.valueProperty().addListener((o,a,n)->{
            if(n!=null)txtGrupo.setText(adminController.generarClaveGrupo(n));
        });
        txtGrupo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");

        gridOferta.add(crearLabelSimple("Semestre:"), 0, 0); gridOferta.add(cbSemestre, 0, 1);
        gridOferta.add(crearLabelSimple("Materia a ofertar:"), 1, 0); gridOferta.add(cbMateria, 1, 1);
        gridOferta.add(crearLabelSimple("Maestro Asignado:"), 2, 0); gridOferta.add(cbMaestro, 2, 1);
        gridOferta.add(crearLabelSimple("Horario Asignado:"), 3, 0); gridOferta.add(txtHorario, 3, 1);
        gridOferta.add(crearLabelSimple("Clave de Grupo:"), 4, 0); gridOferta.add(txtGrupo, 4, 1);

        Button btnCrearGrupo = new Button("Registrar Grupo en Sistema");
        btnCrearGrupo.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCrearGrupo.setOnAction(e -> {
            if(cbMateria.getValue()==null || cbMaestro.getValue()==null || txtHorario.getText().isBlank()){
                NotificacionManager.mostrarNotificacion("Faltan datos","Selecciona semestre, materia, docente y horario.","ERROR");return;
            }
            String clave=cbMateria.getValue().split(" - ",2)[0];
            String docente=cbMaestro.getValue().substring(cbMaestro.getValue().lastIndexOf(" · ")+3);
            try{
                adminController.crearGrupoOficial(clave,docente,txtGrupo.getText(),txtHorario.getText());
                NotificacionManager.mostrarNotificacion("Grupo registrado","Guardado en MySQL con la clave "+txtGrupo.getText(),"INFO");
                cbMateria.setValue(null);cbMaestro.setValue(null);txtHorario.clear();txtGrupo.clear();
            }catch(RuntimeException ex){NotificacionManager.mostrarNotificacion("No se pudo crear",ex.getMessage(),"ERROR");}
        });

        creacionGruposBox.getChildren().addAll(lblOfertaTit, gridOferta, btnCrearGrupo);

        VBox enviosBox = new VBox(15);
        enviosBox.setPadding(new Insets(20));
        enviosBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label lblEnviosTit = new Label("📥 Solicitudes de Estudiantes");
        lblEnviosTit.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + colorTextMain + ";");

        listaAlumnosRegistrados = new VBox(10);
        cargarListaSolicitudes();

        ScrollPane scrollSolicitudes = new ScrollPane(listaAlumnosRegistrados);
        scrollSolicitudes.setPrefHeight(250);
        scrollSolicitudes.setFitToWidth(true);
        scrollSolicitudes.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        enviosBox.getChildren().addAll(lblEnviosTit, scrollSolicitudes);

        layoutMain.getChildren().addAll(header, controlBox, creacionGruposBox, enviosBox);
        
        ScrollPane scroll = new ScrollPane(layoutMain);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox layoutFinal = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return layoutFinal;
    }

    private void cargarListaSolicitudes() {
        listaAlumnosRegistrados.getChildren().clear();
        // CORRECCIÓN: Tipo de lista correcto
        List<Map<String,String>> solicitudes = reinController.obtenerTodasLasSolicitudes();

        if (solicitudes.isEmpty()) {
            Label lblVacio = new Label("Aún no hay alumnos reinscritos en este periodo.");
            lblVacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-style: italic;");
            listaAlumnosRegistrados.getChildren().add(lblVacio);
            return;
        }

        for (Map<String, String> sol : solicitudes) {
            HBox fila = new HBox(15);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(15));
            fila.setStyle("-fx-background-color: " + colorBgBase + "; -fx-background-radius: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8;");

            VBox info = new VBox(2);
            Label lblNom = new Label("👤 " + sol.get("nombre"));
            lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
            
            boolean yaEnviada = sol.get("estado").equals("HOJA ENVIADA");
            Label lblEst = new Label(yaEnviada ? "✅ Hoja de Pago Enviada" : "⏳ Pendiente de Generar Pago");
            lblEst.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (yaEnviada ? "#10b981" : "#f59e0b") + ";");
            Label materiasResumen=new Label(sol.get("nombre_periodo")+" · "+sol.get("cantidad")+" materia(s)\n"+sol.get("materia"));
            materiasResumen.setWrapText(true);
            materiasResumen.setMaxWidth(650);
            materiasResumen.setStyle("-fx-text-fill: " + colorTextMuted + ";");
            info.getChildren().addAll(lblNom,materiasResumen,lblEst);

            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

            if (yaEnviada) {
                Label lblListo = new Label("Trámite Concluido");
                lblListo.setStyle("-fx-text-fill: " + colorTextMuted + ";");
                fila.getChildren().addAll(info, sp, lblListo);
            } else {
                Button btnSubirHoja = new Button("📎 Subir Hoja de Pago (PDF)");
                btnSubirHoja.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnSubirHoja.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Seleccionar Hoja de Pago para " + sol.get("nombre"));
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));
                    File file = fc.showOpenDialog(fila.getScene().getWindow());
                    
                    if (file != null) {
                        reinController.subirHojaDePago(sol.get("correo"), file.getAbsolutePath());
                        NotificacionManager.mostrarNotificacion("Documento Enviado", "La hoja de pago se adjuntó al perfil del alumno.", "INFO");
                        cargarListaSolicitudes(); 
                    }
                });
                fila.getChildren().addAll(info, sp, btnSubirHoja);
            }

            Button btnEliminar=new Button("🗑 Eliminar solicitud");
            btnEliminar.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
            btnEliminar.setOnAction(e->{
                var confirmacion=new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "¿Eliminar TODA la solicitud de "+sol.get("nombre")+" para "+sol.get("nombre_periodo")+"? Se retirarán las "+sol.get("cantidad")+" materias solicitadas de este periodo.",
                    javafx.scene.control.ButtonType.YES,javafx.scene.control.ButtonType.NO);
                if(confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.NO)!=javafx.scene.control.ButtonType.YES)return;
                try{reinController.eliminarSolicitudCompleta(sol.get("correo"),Integer.parseInt(sol.get("periodo")));cargarListaSolicitudes();}
                catch(RuntimeException ex){new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,ex.getMessage()).showAndWait();}
            });
            fila.getChildren().add(btnEliminar);
            listaAlumnosRegistrados.getChildren().add(fila);
        }
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";

        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9"; colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b"; colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private Label crearLabelSimple(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold;");
        return lbl;
    }
}