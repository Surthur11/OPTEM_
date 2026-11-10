package com.optem.ui;

import java.io.File;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.controllers.AjustesController;
import com.optem.controllers.DashboardController;
import com.optem.logic.NotificacionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class AjustesView {

    private final AjustesController controller;
    private final DashboardController dashController;
    private final Runnable onThemeChanged;
    
    private final String correoUsuario; 
    
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;

    public AjustesView(Runnable onThemeChanged, String correoUsuario) {
        this.onThemeChanged = onThemeChanged;
        this.correoUsuario = correoUsuario;
        this.controller = new AjustesController(correoUsuario);
        this.dashController = new DashboardController();
        this.dashController.inicializarUsuario(correoUsuario);
    }

    public VBox getView() {
        leerPreferencias();

        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        VBox titleBox = new VBox(5);
        Label titulo = new Label("⚙ Ajustes y Personalización");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Personaliza tu experiencia, notificaciones y accesibilidad en Optem.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        VBox secPerfil = crearContenedorSeccion();
        String rol = dashController.getRolUsuario();
        Map<String, String> perfilData = dashController.getPerfilUsuario();

        Label lblPerfilTit = new Label();
        lblPerfilTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 14px;");

        HBox perfilContent = new HBox(30);
        perfilContent.setAlignment(Pos.CENTER_LEFT);
        perfilContent.setPadding(new Insets(15, 0, 0, 0));

        VBox colFoto = new VBox(10);
        colFoto.setAlignment(Pos.CENTER);
        Circle imgPerfil = new Circle(40, Color.web(colorFuerte, 0.2));
        
        String savedPhotoPath = controller.getRutaFotoPerfil();
        if (!savedPhotoPath.isEmpty()) {
            File photoFile = new File(savedPhotoPath);
            if (photoFile.exists()) {
                try {
                    imgPerfil.setFill(new ImagePattern(new Image(photoFile.toURI().toString())));
                } catch (Exception ex) {}
            }
        }
        
        Button btnCambiarFoto = new Button("Cambiar foto");
        btnCambiarFoto.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorFuerte + "; -fx-underline: true; -fx-cursor: hand;");
        btnCambiarFoto.setOnAction(e -> {
            javafx.stage.FileChooser selector=new javafx.stage.FileChooser();
            selector.setTitle("Selecciona tu foto de perfil");
            selector.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Imágenes JPG / PNG", "*.png","*.jpg","*.jpeg"));
            File elegido=selector.showOpenDialog(btnCambiarFoto.getScene().getWindow());
            if(elegido==null)return;
            try{
                controller.guardarRutaFotoPerfil(elegido.getAbsolutePath());
                imgPerfil.setFill(new ImagePattern(new Image(elegido.toURI().toString())));
                NotificacionManager.mostrarNotificacion("Foto actualizada","Tu imagen se guardó en MySQL.","INFO");
            }catch(Exception ex){new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,ex.getMessage()).showAndWait();}
        });
        colFoto.getChildren().addAll(imgPerfil, btnCambiarFoto);

        GridPane formPerfil = new GridPane();
        formPerfil.setHgap(20);
        formPerfil.setVgap(15);
        
        TextField txtNombre = crearCampoDeshabilitado(perfilData.get("nombre_completo"));
        TextField txtCorreo = crearCampoDeshabilitado(this.correoUsuario);
        TextField txtVar1 = new TextField();
        TextField txtVar2 = new TextField();

        if (rol.equals("MAESTRO")) {
            lblPerfilTit.setText("👨‍🏫 Perfil del Docente (Recursos Humanos)");
            try{
                String[] datos=com.optem.database.UsuariosAdminDAO.datosPersonal(this.correoUsuario);
                txtVar1 = crearCampoDeshabilitado(datos[0]);txtVar2 = crearCampoDeshabilitado(datos[1]);
            }catch(java.sql.SQLException ex){txtVar1=crearCampoDeshabilitado("No disponible");txtVar2=crearCampoDeshabilitado("No disponible");}
            
            formPerfil.add(crearCajaLabel("Nombre completo", txtNombre), 0, 0);
            formPerfil.add(crearCajaLabel("Correo institucional", txtCorreo), 1, 0);
            formPerfil.add(crearCajaLabel("Departamento / Facultad", txtVar1), 0, 1);
            formPerfil.add(crearCajaLabel("Nivel / Contrato", txtVar2), 1, 1);
        } else if (rol.equals("ADMINISTRADOR")) {
            lblPerfilTit.setText("🛡️ Perfil del Administrador (Sistemas)");
            try{
                String[] datos=com.optem.database.UsuariosAdminDAO.datosPersonal(this.correoUsuario);
                txtVar1 = crearCampoDeshabilitado(datos[0]);txtVar2 = crearCampoDeshabilitado(datos[1]);
            }catch(java.sql.SQLException ex){txtVar1=crearCampoDeshabilitado("No disponible");txtVar2=crearCampoDeshabilitado("No disponible");}
            
            formPerfil.add(crearCajaLabel("Nombre completo", txtNombre), 0, 0);
            formPerfil.add(crearCajaLabel("Correo institucional", txtCorreo), 1, 0);
            formPerfil.add(crearCajaLabel("Área asignada", txtVar1), 0, 1);
            formPerfil.add(crearCajaLabel("Permisos", txtVar2), 1, 1);
        } else {
            lblPerfilTit.setText("👤 Perfil del Estudiante (Control Escolar)");
            txtVar1 = crearCampoDeshabilitado(perfilData.get("carrera"));
            txtVar2 = crearCampoDeshabilitado(perfilData.getOrDefault("semestre","No registrado")+"º Semestre"); 
            
            formPerfil.add(crearCajaLabel("Nombre completo", txtNombre), 0, 0);
            formPerfil.add(crearCajaLabel("Correo institucional", txtCorreo), 1, 0);
            formPerfil.add(crearCajaLabel("Carrera", txtVar1), 0, 1);
            formPerfil.add(crearCajaLabel("Semestre actual", txtVar2), 1, 1);
        }
        
        perfilContent.getChildren().addAll(colFoto, formPerfil);
        secPerfil.getChildren().addAll(lblPerfilTit, perfilContent);

        VBox secApariencia = crearContenedorSeccion();
        Label lblAparTit = new Label("🎨 Apariencia y Tema");
        lblAparTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 14px;");

        HBox rowDark = new HBox(15);
        rowDark.setAlignment(Pos.CENTER_LEFT);
        Label lblDark = new Label("🌙 Modo Oscuro");
        lblDark.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        CheckBox chkDark = new CheckBox();
        chkDark.setSelected(controller.getPrefBoolean("PREF_DARK_MODE", false));
        chkDark.setStyle("-fx-cursor: hand;");
        rowDark.getChildren().addAll(lblDark, sp1, chkDark);

        HBox rowColor = new HBox(15);
        rowColor.setAlignment(Pos.CENTER_LEFT);
        Label lblColor = new Label("🖌️ Color de acento");
        lblColor.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        ColorPicker colorPicker = new ColorPicker(Color.web(colorFuerte));
        colorPicker.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 4; -fx-cursor: hand;");
        rowColor.getChildren().addAll(lblColor, sp2, colorPicker);

        HBox rowFont = new HBox(15);
        rowFont.setAlignment(Pos.CENTER_LEFT);
        Label lblFont = new Label("🔠 Tamaño de fuente global");
        lblFont.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        Region sp3 = new Region(); HBox.setHgrow(sp3, Priority.ALWAYS);
        ComboBox<String> cbFont = new ComboBox<>();
        cbFont.getItems().addAll("Pequeño", "Normal", "Grande");
        cbFont.setValue(controller.getPrefString("PREF_FONT_SIZE", "Normal"));
        cbFont.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 4; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand;");
        rowFont.getChildren().addAll(lblFont, sp3, cbFont);

        HBox rowSave = new HBox();
        rowSave.setAlignment(Pos.CENTER_RIGHT);
        rowSave.setPadding(new Insets(10, 0, 0, 0));
        Button btnGuardarTema = new Button("Aplicar Cambios Visuales");
        btnGuardarTema.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        
        btnGuardarTema.setOnAction(e -> {
            Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
            prefs.putBoolean("PREF_DARK_MODE_" + this.correoUsuario, chkDark.isSelected());
            
            String hex = String.format("#%02X%02X%02X", (int)(colorPicker.getValue().getRed() * 255), (int)(colorPicker.getValue().getGreen() * 255), (int)(colorPicker.getValue().getBlue() * 255));
            prefs.put("PREF_ACCENT_COLOR_" + this.correoUsuario, hex);
            prefs.put("PREF_FONT_SIZE_" + this.correoUsuario, cbFont.getValue());
            
            if (onThemeChanged != null) onThemeChanged.run(); 
        });
        rowSave.getChildren().add(btnGuardarTema);

        secApariencia.getChildren().addAll(lblAparTit, rowDark, rowColor, rowFont, rowSave);

        VBox secAcc = crearContenedorSeccion();
        Label lblAccTit = new Label("🔔 Notificaciones y Accesibilidad");
        lblAccTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 14px;");

        VBox boxNoti = new VBox(5);
        boxNoti.setPadding(new Insets(10, 0, 10, 0));
        ToggleGroup tgNoti = new ToggleGroup();
        RadioButton rbOff = crearRadio("🔕 Apagado", tgNoti);
        RadioButton rbPrio = crearRadio("⚠️ Solo prioritario (Reuniones y Urgentes)", tgNoti);
        RadioButton rbFull = crearRadio("🔔 Completo (Tareas, Recordatorios, Clases)", tgNoti);
        String modo=controller.getNivelNotificaciones();
        rbOff.setSelected("APAGADO".equals(modo));
        rbPrio.setSelected("PRIORITARIO".equals(modo));
        rbFull.setSelected(!rbOff.isSelected()&&!rbPrio.isSelected());
        rbOff.setOnAction(e->guardarNotificaciones("APAGADO"));
        rbPrio.setOnAction(e->guardarNotificaciones("PRIORITARIO"));
        rbFull.setOnAction(e->guardarNotificaciones("COMPLETO"));
        boxNoti.getChildren().addAll(rbOff, rbPrio, rbFull);

        VBox boxAccs = new VBox(5);
        boxAccs.setPadding(new Insets(10, 0, 0, 0));
        
        CheckBox chkTeclado = crearCheckBox("⌨️ Activar navegación por teclado (Flechas y Enter)", controller.getPrefBoolean("PREF_ACC_KEYBOARD", false));
        chkTeclado.setOnAction(e -> {
            Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
            prefs.putBoolean("PREF_ACC_KEYBOARD_" + this.correoUsuario, chkTeclado.isSelected());
        });

        CheckBox chkVoz = crearCheckBox("🎙️ Habilitar Asistente de Voz IA (Tecla F2)", controller.getPrefBoolean("PREF_ACC_VOICE", false));
        chkVoz.setOnAction(e -> {
            Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
            prefs.putBoolean("PREF_ACC_VOICE_" + this.correoUsuario, chkVoz.isSelected());
        });
        
        boxAccs.getChildren().addAll(chkTeclado, chkVoz);

        secAcc.getChildren().addAll(lblAccTit, boxNoti, boxAccs);

        layoutMain.getChildren().addAll(titleBox, secPerfil, secApariencia, secAcc);
        
        ScrollPane scroll = new ScrollPane(layoutMain);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox layoutFinal = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return layoutFinal;
    }

    private void guardarNotificaciones(String modo){
        try{controller.guardarNivelNotificaciones(modo);}
        catch(RuntimeException ex){new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,ex.getMessage()).showAndWait();}
    }
    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + this.correoUsuario, false);
        
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + this.correoUsuario, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private VBox crearContenedorSeccion() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 10, 0, 2, 2);");
        return box;
    }

    private VBox crearCajaLabel(String titulo, TextField campo) {
        VBox box = new VBox(5);
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMuted + ";");
        box.getChildren().addAll(lbl, campo);
        return box;
    }

    private TextField crearCampoDeshabilitado(String texto) {
        TextField tf = new TextField(texto != null ? texto : "");
        tf.setEditable(false);
        tf.setPrefWidth(250);
        tf.setStyle("-fx-background-color: " + colorBgBase + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-text-fill: " + colorTextMain + "; -fx-opacity: 0.8;");
        return tf;
    }

    private RadioButton crearRadio(String texto, ToggleGroup grupo) {
        RadioButton rb = new RadioButton(texto);
        rb.setToggleGroup(grupo);
        rb.setMaxWidth(Double.MAX_VALUE);
        rb.setPadding(new Insets(8));
        rb.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;");
        
        rb.setOnMouseEntered(e -> rb.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;"));
        rb.setOnMouseExited(e -> rb.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;"));
        
        return rb;
    }

    private CheckBox crearCheckBox(String texto, boolean seleccionado) {
        CheckBox chk = new CheckBox(texto);
        chk.setSelected(seleccionado);
        chk.setMaxWidth(Double.MAX_VALUE);
        chk.setPadding(new Insets(8));
        chk.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;");
        
        chk.setOnMouseEntered(e -> chk.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;"));
        chk.setOnMouseExited(e -> chk.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-background-radius: 8;"));
        
        return chk;
    }
}