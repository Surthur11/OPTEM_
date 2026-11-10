package com.optem.ui;

import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.controllers.TareasMaestroController;
import com.optem.database.MockDB;
import com.optem.logic.NotificacionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class TareasView {

    private final TareasMaestroController controller = new TareasMaestroController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder, correoActual, nombreUsuarioSesion;
    
    private VBox colPendientes, colCompletadas;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(20);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        VBox titleBox = new VBox(5);
        Label titulo = new Label("📚 Mis Tareas Oficiales");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Descarga el material de tus profesores y sube tus trabajos antes de la fecha límite.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        HBox tableroBox = new HBox(20);
        VBox.setVgrow(tableroBox, Priority.ALWAYS);

        VBox boxPendientes = new VBox(15);
        HBox.setHgrow(boxPendientes, Priority.ALWAYS);
        boxPendientes.setPrefWidth(300);
        
        Label lblPend = new Label("📌 Pendientes");
        lblPend.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-alignment: center;");
        lblPend.setMaxWidth(Double.MAX_VALUE);
        
        colPendientes = new VBox(15);
        ScrollPane scrollPend = new ScrollPane(colPendientes);
        scrollPend.setFitToWidth(true);
        scrollPend.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPend, Priority.ALWAYS);
        boxPendientes.getChildren().addAll(lblPend, scrollPend);

        VBox boxCompletadas = new VBox(15);
        HBox.setHgrow(boxCompletadas, Priority.ALWAYS);
        boxCompletadas.setPrefWidth(300);
        
        Label lblComp = new Label("✅ Entregadas");
        lblComp.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-alignment: center;");
        lblComp.setMaxWidth(Double.MAX_VALUE);
        
        colCompletadas = new VBox(15);
        ScrollPane scrollComp = new ScrollPane(colCompletadas);
        scrollComp.setFitToWidth(true);
        scrollComp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollComp, Priority.ALWAYS);
        boxCompletadas.getChildren().addAll(lblComp, scrollComp);

        tableroBox.getChildren().addAll(boxPendientes, boxCompletadas);
        layoutMain.getChildren().addAll(titleBox, tableroBox);

        cargarTareasDelAlumno();
        return layoutMain;
    }

    private void cargarTareasDelAlumno() {
        colPendientes.getChildren().clear();
        colCompletadas.getChildren().clear();

        List< Map<String, String> > misClases = MockDB.obtenerMateriasDelAlumno(correoActual);
        List< Map<String, String> > todasLasTareas = controller.getTodasLasAsignaciones();
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);

        for (Map<String, String> tarea : todasLasTareas) {
            String materiaTarea = tarea.get("materia").toUpperCase();
            boolean esMiMateria = false;

            if (misClases != null) {
                for (Map<String, String> clase : misClases) {
                    if (tarea.get("id_grupo").equals(clase.get("id_grupo"))) {
                        esMiMateria = true;
                        break;
                    }
                }
            }

            if (esMiMateria) {
                boolean yaEntregada = "1".equals(tarea.get("entregado"));
                VBox tarjeta = crearTarjetaTarea(tarea, yaEntregada);
                
                if (yaEntregada) colCompletadas.getChildren().add(tarjeta);
                else colPendientes.getChildren().add(tarjeta);
            }
        }
        
        if (colPendientes.getChildren().isEmpty()) colPendientes.getChildren().add(crearMensajeVacio("No tienes tareas pendientes. ¡Buen trabajo!"));
        if (colCompletadas.getChildren().isEmpty()) colCompletadas.getChildren().add(crearMensajeVacio("Aún no has entregado ninguna tarea."));
    }

    private VBox crearTarjetaTarea(Map<String, String> tarea, boolean yaEntregada) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 2, 2);");

        Label lblMat = new Label(tarea.get("materia"));
        lblMat.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");
        
        Label lblTit = new Label(tarea.get("titulo"));
        lblTit.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-wrap-text: true;");
        
        Label lblDesc = new Label(tarea.get("descripcion"));
        lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: " + colorTextMuted + "; -fx-wrap-text: true;");

        Label lblFecha = new Label("Vence: " + tarea.get("fecha") + " " + tarea.get("hora"));
        lblFecha.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");

        VBox boxBotones = new VBox(10);
        boxBotones.setAlignment(Pos.CENTER_LEFT);

        String rutaArchivo = tarea.get("ruta_archivo");
        if (rutaArchivo != null && !rutaArchivo.isEmpty()) {
            Button btnDescargar = new Button("📎 Abrir Material del Profesor");
            btnDescargar.setStyle("-fx-background-color: transparent; -fx-border-color: #3b82f6; -fx-text-fill: #3b82f6; -fx-border-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
            btnDescargar.setMaxWidth(Double.MAX_VALUE);
            btnDescargar.setOnAction(e -> {
                try {
                    File file = new File(controller.descargarAdjuntoProfesor(tarea.get("id")));
                    if (file.exists()) Desktop.getDesktop().open(file);
                    else NotificacionManager.mostrarNotificacion("Error", "El profesor movió o eliminó el archivo original.", "ERROR");
                } catch (Exception ex) { NotificacionManager.mostrarNotificacion("Error", "No se pudo abrir el archivo.", "ERROR"); }
            });
            boxBotones.getChildren().add(btnDescargar);
        }

        boolean estaCerrada = tarea.get("estado").equals("CERRADA");
        
        if (yaEntregada) {
            Label lblExito = new Label("✅ Trabajo entregado y registrado");
            lblExito.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            boxBotones.getChildren().add(lblExito);
            Map<String,String> evaluacion=java.util.Map.of(
                "nota",tarea.getOrDefault("nota",""),"escala",tarea.getOrDefault("escala","100"),
                "comentario",tarea.getOrDefault("comentario",""));
            if(!evaluacion.getOrDefault("nota","").isEmpty()){
                Label lblNota=new Label("⭐ Calificación: "+evaluacion.get("nota")+" / "+evaluacion.get("escala"));
                lblNota.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");
                Label comentario=new Label("Comentario del profesor: "+evaluacion.get("comentario"));
                comentario.setWrapText(true);comentario.setStyle("-fx-text-fill: " + colorTextMuted + ";");
                boxBotones.getChildren().addAll(lblNota,comentario);
            }
            Button btnDeshacer=new Button("↩ Deshacer entrega");
            btnDeshacer.setStyle("-fx-border-color: " + colorFuerte + "; -fx-background-color: transparent; -fx-text-fill: " + colorFuerte + "; -fx-background-radius: 6; -fx-border-radius: 6;");
            btnDeshacer.setOnAction(e->{
                javafx.scene.control.Alert conf=new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "¿Eliminar tu entrega? También se eliminará la calificación y los puntos XP obtenidos por ella.",
                    javafx.scene.control.ButtonType.YES,javafx.scene.control.ButtonType.NO);
                if(conf.showAndWait().orElse(javafx.scene.control.ButtonType.NO)!=javafx.scene.control.ButtonType.YES)return;
                try{controller.deshacerEntregaAlumno(tarea.get("id"));cargarTareasDelAlumno();}
                catch(Exception ex){NotificacionManager.mostrarNotificacion("No se pudo deshacer",ex.getMessage(),"ERROR");}
            });
            boxBotones.getChildren().add(btnDeshacer);
        } else if (estaCerrada) {
            Label lblCerrado = new Label("🚫 Recepción Cerrada por el Profesor");
            lblCerrado.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            boxBotones.getChildren().add(lblCerrado);
            card.setOpacity(0.6);
        } else {
            VBox boxSubida = new VBox(8);
            Label lblArchivoSubido = new Label("Ningún documento seleccionado");
            lblArchivoSubido.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 11px; -fx-font-style: italic;");
            
            Button btnAdjuntar = new Button("📎 Seleccionar Archivo");
            btnAdjuntar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-cursor: hand;");
            btnAdjuntar.setMaxWidth(Double.MAX_VALUE);

            Button btnEnviar = new Button("📤 Enviar Trabajo");
            btnEnviar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            btnEnviar.setMaxWidth(Double.MAX_VALUE);
            btnEnviar.setDisable(true); 

            final String[] rutaMiTrabajo = {""};

            btnAdjuntar.setOnAction(e -> {
                FileChooser fc = new FileChooser();
                fc.setTitle("Selecciona tu tarea (PDF, Word, ZIP)");
                File miArchivo = fc.showOpenDialog(card.getScene().getWindow());
                if (miArchivo != null) {
                    rutaMiTrabajo[0] = miArchivo.getAbsolutePath();
                    lblArchivoSubido.setText("📎 " + miArchivo.getName());
                    lblArchivoSubido.setStyle("-fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                    btnEnviar.setDisable(false); 
                }
            });
            
            btnEnviar.setOnAction(e -> {
                String nombreAlumnoFinal = (nombreUsuarioSesion != null && !nombreUsuarioSesion.isEmpty()) ? nombreUsuarioSesion : "Alumno";
                try {
                    btnEnviar.setDisable(true);
                    javafx.concurrent.Task<Void> enviar=new javafx.concurrent.Task<>(){
                        @Override protected Void call(){
                            controller.registrarEntregaAlumno(tarea.get("id"),nombreAlumnoFinal,rutaMiTrabajo[0]);return null;
                        }
                    };
                    enviar.setOnSucceeded(ev->{
                        NotificacionManager.mostrarNotificacion("¡Entregado!","Tu archivo se guardó en MySQL.","INFO");
                        cargarTareasDelAlumno();
                    });
                    enviar.setOnFailed(ev->{
                        btnEnviar.setDisable(false);
                        NotificacionManager.mostrarNotificacion("No se pudo enviar",enviar.getException().getMessage(),"ERROR");
                    });
                    Thread hilo=new Thread(enviar,"optem-entrega-archivo");hilo.setDaemon(true);hilo.start();
                }catch(RuntimeException ex){
                    btnEnviar.setDisable(false);
                    NotificacionManager.mostrarNotificacion("No se pudo enviar",ex.getMessage(),"ERROR");
                }
            });
            
            boxSubida.getChildren().addAll(btnAdjuntar, lblArchivoSubido, btnEnviar);
            boxBotones.getChildren().add(boxSubida);
        }

        card.getChildren().addAll(lblMat, lblTit, lblDesc, lblFecha, boxBotones);
        return card;
    }

    private Label crearMensajeVacio(String msj) {
        Label l = new Label(msj);
        l.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-style: italic; -fx-padding: 20;");
        return l;
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        // LEEMOS EL NOMBRE DEL USUARIO DESDE LAS PREFERENCIAS
        nombreUsuarioSesion = prefs.get("PREF_NAME_" + correoActual, "Alumno");
        
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9"; colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b"; colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
}