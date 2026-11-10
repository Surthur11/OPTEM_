package com.optem.ui;

import java.util.prefs.Preferences;
import java.sql.SQLException;
import com.optem.database.UsuariosAdminDAO;
import com.optem.database.UsuariosAdminDAO.Usuario;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import java.util.Optional;

import com.optem.controllers.DashboardController;
import com.optem.logic.NotificacionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AdminPerfilesView {

    private final DashboardController controller = new DashboardController();
    private TextField txtBuscarCampo;
    private ComboBox<String> rolFiltro;
    private VBox listaUsuariosActual;
    private final java.util.concurrent.atomic.AtomicLong versionBusqueda=new java.util.concurrent.atomic.AtomicLong();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;

    public VBox getView() {
        leerPreferencias();
        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(5);
        Label titulo = new Label("👤 Gestión de Perfiles y Usuarios");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Administra cuentas de estudiantes, maestros y personal administrativo.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNuevo = new Button("➕ Crear Usuario");
        btnNuevo.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnNuevo.setOnAction(e -> { if (abrirEditor(null)) cargarUsuarios(txtBuscarCampo, rolFiltro, listaUsuariosActual); });
        header.getChildren().addAll(titleBox, spacer, btnNuevo);

        HBox filtrosBox = new HBox(15);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("🔍 Buscar por nombre, correo o matrícula...");
        txtBuscar.setPrefWidth(350);
        txtBuscar.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8; -fx-text-fill: " + colorTextMain + "; -fx-padding: 10;");
        
        ComboBox<String> cbRol = new ComboBox<>();
        cbRol.getItems().addAll("Todos los Roles", "Estudiantes", "Maestros", "Administradores");
        cbRol.setValue("Todos los Roles");
        cbRol.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8; -fx-text-fill: " + colorTextMain + ";");
        
        filtrosBox.getChildren().addAll(txtBuscar, cbRol);
        txtBuscarCampo=txtBuscar; rolFiltro=cbRol;

        VBox listaUsuarios = new VBox(10);
        listaUsuariosActual=listaUsuarios;
        javafx.animation.PauseTransition espera=new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        espera.setOnFinished(e->cargarUsuarios(txtBuscar,cbRol,listaUsuarios));
        txtBuscar.textProperty().addListener((obs,antes,ahora)->espera.playFromStart());
        cbRol.valueProperty().addListener((obs,antes,ahora)->cargarUsuarios(txtBuscar,cbRol,listaUsuarios));
        cargarUsuarios(txtBuscar,cbRol,listaUsuarios);

        ScrollPane scroll = new ScrollPane(listaUsuarios);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        layoutMain.getChildren().addAll(header, filtrosBox, scroll);
        return layoutMain;
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

    private void cargarUsuarios(TextField buscar,ComboBox<String> filtro,VBox destino) {
        destino.getChildren().setAll(new Label("Consultando usuarios…"));
        String rf=switch(filtro.getValue()) {
            case "Estudiantes" -> "Estudiante";
            case "Maestros" -> "Docente";
            case "Administradores" -> "Administrador";
            default -> "";
        };
        String texto=buscar.getText();long version=versionBusqueda.incrementAndGet();
        javafx.concurrent.Task<java.util.List<Usuario>> tarea=new javafx.concurrent.Task<>(){
            @Override protected java.util.List<Usuario> call() throws Exception{return UsuariosAdminDAO.listar(texto,rf);}
        };
        tarea.setOnSucceeded(e->{
            if(version!=versionBusqueda.get())return;
            destino.getChildren().clear();
            var usuarios=tarea.getValue();
            if(usuarios.isEmpty())destino.getChildren().add(new Label("No se encontraron usuarios registrados."));
            for(var u:usuarios)destino.getChildren().add(crearTarjetaUsuario(u));
        });
        tarea.setOnFailed(e->{
            if(version==versionBusqueda.get())destino.getChildren().setAll(
                new Label("No se pudieron consultar usuarios: "+tarea.getException().getMessage()));
        });
        Thread hilo=new Thread(tarea,"optem-busqueda-usuarios");hilo.setDaemon(true);hilo.start();
    }
    private boolean abrirEditor(Usuario original) {
        Dialog<Usuario> dialog=new Dialog<>();
        dialog.setTitle(original==null?"Crear Usuario":"Editar Usuario");
        var guardar=new ButtonType("Guardar",ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar,ButtonType.CANCEL);
        GridPane fields=new GridPane();fields.setHgap(10);fields.setVgap(10);fields.setPadding(new Insets(15));
        // El rol se selecciona antes de los campos dependientes.
        ComboBox<String> rol=new ComboBox<>();rol.getItems().addAll("Estudiante","Docente","Administrador");
        rol.setValue(original==null?"Estudiante":original.rol());
        TextField correo=new TextField(original==null?"":original.correo());
        TextField cuenta=new TextField(original==null?"":original.cuenta());
        TextField nombre=new TextField(original==null?"":original.nombre());
        TextField apellido=new TextField(original==null?"":original.apellidos());
        TextField carrera=new TextField(original==null?"":original.carrera()==null?"":original.carrera());
        TextField semestre=new TextField(original==null||original.semestre()==null?"":String.valueOf(original.semestre()));
        TextField personal1=new TextField();
        TextField personal2=new TextField();
        TextField carreraDocente=new TextField("Ingeniería de Software");
        if(original!=null && !"Estudiante".equals(original.rol())){
            try{
                var datos=UsuariosAdminDAO.datosPersonal(original.correo());
                personal1.setText(datos[0]);personal2.setText(datos[1]);
                carreraDocente.setText(UsuariosAdminDAO.carreraDocente(original.correo()));
            }catch(SQLException ex){error(ex.getMessage());}
        }
        Label cuentaEtiqueta=new Label();
        Label p1Etiqueta=new Label();Label p2Etiqueta=new Label();
        fields.addRow(0,new Label("Rol"),rol);
        fields.addRow(1,new Label("Correo"),correo);
        fields.addRow(2,cuentaEtiqueta,cuenta);
        fields.addRow(3,new Label("Nombre"),nombre);
        fields.addRow(4,new Label("Apellidos"),apellido);
        fields.addRow(5,new Label("Carrera"),carrera);
        Label carreraEtiqueta=(Label)fields.getChildren().get(fields.getChildren().size()-2);
        fields.addRow(6,new Label("Semestre"),semestre);
        Label semestreEtiqueta=(Label)fields.getChildren().get(fields.getChildren().size()-2);
        fields.addRow(7,p1Etiqueta,personal1);
        fields.addRow(8,p2Etiqueta,personal2);
        Label carreraDocenteEtiqueta=new Label("Carrera que imparte");
        fields.addRow(9,carreraDocenteEtiqueta,carreraDocente);
        Runnable ajustarCampos=()->{
            boolean alumno="Estudiante".equals(rol.getValue());
            cuentaEtiqueta.setText(alumno?"Matrícula":"Número de empleado");
            carrera.setVisible(alumno);carrera.setManaged(alumno);
            semestre.setVisible(alumno);semestre.setManaged(alumno);
            carreraEtiqueta.setVisible(alumno);carreraEtiqueta.setManaged(alumno);
            semestreEtiqueta.setVisible(alumno);semestreEtiqueta.setManaged(alumno);
            p1Etiqueta.setText("Docente".equals(rol.getValue())?"Departamento":"Área administrativa");
            p2Etiqueta.setText("Docente".equals(rol.getValue())?"Tipo de contrato":"Nivel de acceso");
            carreraDocenteEtiqueta.setVisible("Docente".equals(rol.getValue()));
            carreraDocenteEtiqueta.setManaged("Docente".equals(rol.getValue()));
            carreraDocente.setVisible("Docente".equals(rol.getValue()));
            carreraDocente.setManaged("Docente".equals(rol.getValue()));
            for(var nodo:new javafx.scene.Node[]{p1Etiqueta,p2Etiqueta,personal1,personal2}){
                nodo.setVisible(!alumno);nodo.setManaged(!alumno);
            }
        };
        rol.valueProperty().addListener((obs,antes,ahora)->ajustarCampos.run());
        ajustarCampos.run();
        dialog.getDialogPane().setContent(fields);
        dialog.setResultConverter(btn -> {
            if(btn!=guardar)return null;
            Integer sem=null;
            try {if("Estudiante".equals(rol.getValue()) && !semestre.getText().isBlank())sem=Integer.valueOf(semestre.getText().trim());}
            catch(NumberFormatException ex){sem=-1;}
            return new Usuario(correo.getText(),cuenta.getText(),nombre.getText(),apellido.getText(),
                "Estudiante".equals(rol.getValue())?carrera.getText():null,sem,rol.getValue(),true);
        });
        Optional<Usuario> result=dialog.showAndWait();
        if(result.isEmpty())return false;
        Usuario nuevo=result.get();
        if(nuevo.semestre()!=null&&(nuevo.semestre()<1||nuevo.semestre()>20)){
            error("Semestre debe ser un número entre 1 y 20.");return false;
        }
        try {
            UsuariosAdminDAO.guardar(nuevo,original==null?null:original.correo());
            if(!"Estudiante".equals(nuevo.rol())){
                UsuariosAdminDAO.guardarDatosPersonal(nuevo.correo(),nuevo.rol(),personal1.getText(),personal2.getText(),carreraDocente.getText());
            }
            return true;
        }
        catch(SQLException ex){error(ex.getMessage());return false;}
    }
    private void error(String msg){new Alert(Alert.AlertType.ERROR,msg,ButtonType.OK).showAndWait();}
    private HBox crearTarjetaUsuario(Usuario usuario) {
        String nombre=usuario.nombreCompleto(), correo=usuario.correo(), rol=usuario.rol();
        String estado=usuario.activo()?"Activo":"Suspendido";
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8;");

        Circle avatar = new Circle(20, Color.web(colorFuerte, 0.2));
        Label lblInicial = new Label(nombre.substring(0, 1));
        lblInicial.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");
        javafx.scene.layout.StackPane iconBox = new javafx.scene.layout.StackPane(avatar, lblInicial);

        VBox infoBox = new VBox(3);
        infoBox.setPrefWidth(250);
        Label lblNom = new Label(nombre);
        lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 14px;");
        Label lblCorr = new Label(correo);
        lblCorr.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(lblNom, lblCorr);

        Label lblRol = new Label(rol);
        lblRol.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-size: 12px;");
        lblRol.setPrefWidth(100);

        Label lblEstado = new Label(estado);
        String colorEstado = estado.equals("Activo") ? "#10b981" : "#ef4444";
        lblEstado.setStyle("-fx-text-fill: " + colorEstado + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        lblEstado.setPrefWidth(80);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏️ Editar");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand;");
        
        Button btnSuspender = new Button(estado.equals("Activo") ? "🚫 Suspender" : "✅ Activar");
        btnSuspender.setStyle("-fx-background-color: transparent; -fx-border-color: " + colorEstado + "; -fx-border-radius: 6; -fx-text-fill: " + colorEstado + "; -fx-cursor: hand;");

        btnEdit.setOnAction(e -> {if(abrirEditor(usuario))cargarUsuarios(txtBuscarCampo,rolFiltro,listaUsuariosActual);});
        btnSuspender.setOnAction(e -> {
            String accion=usuario.activo()?"suspender":"activar";
            Alert confirmar=new Alert(Alert.AlertType.CONFIRMATION,"¿Confirmas "+accion+" la cuenta "+correo+"?",ButtonType.YES,ButtonType.NO);
            if(confirmar.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
            try {UsuariosAdminDAO.cambiarActivo(usuario,!usuario.activo());cargarUsuarios(txtBuscarCampo,rolFiltro,listaUsuariosActual);}
            catch(SQLException ex){error(ex.getMessage());}
        });
        card.getChildren().addAll(iconBox, infoBox, lblRol, lblEstado, spacer, btnEdit, btnSuspender);
        return card;
    }
}