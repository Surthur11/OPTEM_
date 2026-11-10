package com.optem.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.optem.controllers.AjustesController;
import com.optem.controllers.DashboardController;
import com.optem.controllers.TareasMaestroController;
import com.optem.database.MockDB;
import com.optem.logic.NotificacionManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class InicioMaestroView {

    private final DashboardController controller = new DashboardController();
    private final TareasMaestroController tareasController = new TareasMaestroController(); // NUEVO CONTROLLER
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;

    public VBox getView() {
        leerPreferencias();

        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 45, 30, 45));
        mainContent.setStyle("-fx-background-color: transparent;");

        Map<String, String> perfil = controller.getPerfilUsuario();
        String primerNombre = perfil.get("nombre_completo").split(" ")[0];

        StackPane bannerContainer = new StackPane(); 
        bannerContainer.setPrefHeight(160); 
        Pane bannerBackground = new Pane(); 
        bannerBackground.setStyle("-fx-background-color: linear-gradient(to right, " + colorFuerte + ", #1e293b); -fx-background-radius: 20;");
        
        HBox topBannerContent = new HBox(); 
        topBannerContent.setPadding(new Insets(25, 40, 25, 40)); 
        topBannerContent.setAlignment(Pos.CENTER_LEFT);
        
        VBox greetBox = new VBox(10); 
        greetBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblGreet = new Label("¡Bienvenido, Profesor " + primerNombre + "!"); 
        lblGreet.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label lblCareer = new Label("Facultad de Ingeniería | Ciclo 2026-B");
        lblCareer.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255, 255, 255, 0.8);");
        
        greetBox.getChildren().addAll(lblGreet, lblCareer);
        Region spacer = new Region(); 
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox clockContainer = new VBox(-5); 
        clockContainer.setAlignment(Pos.CENTER_RIGHT);
        Label lblClock = new Label(); 
        lblClock.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;"); 
        Label lblDateGlass = new Label(); 
        lblDateGlass.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255, 255, 255, 0.8);");
        
        initClock(lblClock, lblDateGlass); 
        clockContainer.getChildren().addAll(lblClock, lblDateGlass); 
        
        topBannerContent.getChildren().addAll(greetBox, spacer, clockContainer); 
        bannerContainer.getChildren().addAll(bannerBackground, topBannerContent);
        bannerContainer.setEffect(new DropShadow(15, 0, 8, Color.rgb(0,0,0,0.15)));

        // --- CONECTANDO LAS MÉTRICAS ---
        List<Map<String,String>> misClases = MockDB.obtenerTodosLosGrupos();
        int totalAlumnos = (misClases != null) ? misClases.stream().mapToInt(c->Integer.parseInt(c.getOrDefault("total_alumnos","0"))).sum() : 0;
        
        long tareasPendientes = tareasController.getTodasLasAsignaciones().stream()
                .filter(t -> t.get("estado").equals("ABIERTA"))
                .count();

        HBox metricasBox = new HBox(20);
        metricasBox.setAlignment(Pos.CENTER);
        
        metricasBox.getChildren().addAll(
            crearTarjetaMetrica("👥", "Total de Alumnos", totalAlumnos + " inscritos", colorFuerte),
            crearTarjetaMetrica("📝", "Asignaciones Activas", tareasPendientes + " abiertas", "#D32F2F"),
            crearTarjetaMetrica("📅", "Próxima Clase", "Revisar Agenda", "#f59e0b")
        );

        VBox materiasPane = new VBox(15); 
        materiasPane.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-padding: 25; -fx-background-radius: 15; -fx-border-color: " + colorBorder + "; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 2, 0);");
        
        Label lblMateriasTitle = new Label("👨‍🏫 Mis Clases Impartiendo"); 
        lblMateriasTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 1.3em; -fx-text-fill: " + colorTextMain + ";"); 

        VBox listaTarjetas = new VBox(10);
        
        // --- CONECTANDO CLASES AL MOCKDB ---
        if (misClases != null && !misClases.isEmpty()) {
            for (Map<String, String> clase : misClases) {
                // Estimamos 25 inscritos como demo visual, en un entorno real sería una consulta a DB
                listaTarjetas.getChildren().add(
                    crearFilaClaseMaestro(clase.get("nombre"), clase.get("clave"), clase.get("horario"), "Grupo: " + clase.get("grupo"),
                        Integer.parseInt(clase.getOrDefault("total_alumnos","0")),Integer.parseInt(clase.get("id_grupo")))
                );
            }
        } else {
            Label vacio = new Label("No tienes clases asignadas este semestre.");
            vacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-style: italic;");
            listaTarjetas.getChildren().add(vacio);
        }

        materiasPane.getChildren().addAll(lblMateriasTitle, listaTarjetas);

        mainContent.getChildren().addAll(bannerContainer, metricasBox, materiasPane);
        
        ScrollPane scrollGlobal = new ScrollPane(mainContent);
        scrollGlobal.setFitToWidth(true);
        scrollGlobal.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox layoutFinal = new VBox(scrollGlobal);
        VBox.setVgrow(scrollGlobal, Priority.ALWAYS);
        return layoutFinal;
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

    private void initClock(Label lblClock, Label lblDate) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm"); 
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"));
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> { 
            LocalDateTime now = LocalDateTime.now(); 
            lblClock.setText(now.format(timeFmt)); 
            lblDate.setText(now.format(dateFmt)); 
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE); 
        clock.play();
    }

    private HBox crearTarjetaMetrica(String icono, String titulo, String valor, String colorIco) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 2, 2);");

        Label icon = new Label(icono);
        icon.setStyle("-fx-font-size: 24px; -fx-background-color: " + colorIco + "20; -fx-text-fill: " + colorIco + "; -fx-padding: 10; -fx-background-radius: 12;");

        VBox textos = new VBox(3);
        Label lblTit = new Label(titulo);
        lblTit.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 0.9em; -fx-font-weight: bold;");
        Label lblVal = new Label(valor);
        lblVal.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-size: 1.15em; -fx-font-weight: bold;");
        textos.getChildren().addAll(lblTit, lblVal);

        card.getChildren().addAll(icon, textos);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox crearFilaClaseMaestro(String nombreMateria, String clave, String horario, String grupo, int inscritos,int idGrupo) {
        HBox row = new HBox(15); 
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + colorBgBase + "; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " + colorBorder + "; -fx-border-radius: 10; -fx-cursor: hand;");
        
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " + colorFuerte + "; -fx-border-radius: 10; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: " + colorBgBase + "; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " + colorBorder + "; -fx-border-radius: 10;"));
        
        row.setOnMouseClicked(e -> mostrarDetallesClase(nombreMateria, grupo, inscritos, row.getScene().getWindow()));

        StackPane iconPane = new StackPane(); 
        Circle circle = new Circle(20, Color.web(colorFuerte, 0.15));
        Label initial = new Label(nombreMateria.substring(0, 1)); 
        initial.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + colorFuerte + ";");
        iconPane.getChildren().addAll(circle, initial);
        
        VBox details = new VBox(3);
        Label lblNom = new Label(clave + " - " + nombreMateria); 
        lblNom.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-text-fill: " + colorTextMain + ";"); 
        Label lblAlumnos = new Label("👥 Clic para ver estatus de tareas y alumnos"); 
        lblAlumnos.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 0.85em;");
        details.getChildren().addAll(lblNom, lblAlumnos);
        
        Region spc = new Region(); 
        HBox.setHgrow(spc, Priority.ALWAYS);
        
        VBox timeBox = new VBox(3); 
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblHorario = new Label(horario); 
        lblHorario.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-background-color: " + colorFuerte + "15; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 0.85em;");
        Label lblGrupo = new Label(grupo); 
        lblGrupo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 0.85em;");
        timeBox.getChildren().addAll(lblHorario, lblGrupo);
        
        Button general=new Button("💬 General");
        general.setStyle("-fx-background-color: "+colorFuerte+"; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
        general.setOnAction(e->{e.consume();GrupoMuroView.abrir(row.getScene().getWindow(),idGrupo,nombreMateria+" · "+grupo);});
        row.getChildren().addAll(iconPane, details, spc, timeBox,general);
        return row;
    }

    private void mostrarDetallesClase(String materia, String grupo, int inscritos, Window owner) {
        Stage modal = new Stage();
        modal.initOwner(owner);
        modal.initStyle(StageStyle.UNDECORATED);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        String fontPref = new AjustesController("").getPrefString("PREF_FONT_SIZE", "Normal");
        String fontSize = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";
        
        layout.setStyle(
            "-fx-background-color: " + colorBgPanel + ";" +
            "-fx-border-color: " + colorFuerte + ";" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-font-size: " + fontSize + ";"
        );

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titulos = new VBox(2);
        Label lblTit = new Label("Control: " + materia);
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 1.4em; -fx-text-fill: " + colorTextMain + ";");
        Label lblSub = new Label(grupo + " | Alumnos Activos");
        lblSub.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 0.9em;");
        titulos.getChildren().addAll(lblTit, lblSub);
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 18px; -fx-cursor: hand;");
        btnCerrar.setOnAction(e -> modal.close());
        header.getChildren().addAll(titulos, spacer, btnCerrar);

        HBox contenido = new HBox(20);
        
        VBox colTareas = new VBox(15);
        colTareas.setPrefWidth(300);
        Label lblTareasTit = new Label("📊 Tareas publicadas en este grupo");
        lblTareasTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 1.1em;");
        colTareas.getChildren().add(lblTareasTit);
        
        // --- CONECTAMOS LAS TAREAS REALES DE ESTA MATERIA ---
        List<Map<String,String>> tareasDeEstaMateria = tareasController.getTodasLasAsignaciones().stream()
                .filter(t -> t.get("materia").contains(materia))
                .collect(Collectors.toList());
                
        if (tareasDeEstaMateria.isEmpty()) {
            Label vacio = new Label("No has publicado tareas aquí.");
            vacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-style: italic;");
            colTareas.getChildren().add(vacio);
        } else {
            for (Map<String, String> t : tareasDeEstaMateria) {
                int actuales = Integer.parseInt(t.get("entregas_actuales"));
                int tot = Integer.parseInt(t.get("entregas_total"));
                colTareas.getChildren().add(crearMétricaTarea(t.get("titulo"), actuales, tot, colorFuerte));
            }
        }

        VBox colAlumnos = new VBox(10);
        colAlumnos.setPrefWidth(280);
        Label lblAlumnosTit = new Label("👥 Registro de Últimas Entregas");
        lblAlumnosTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 1.1em;");
        
        VBox lista = new VBox(8);
        
        // --- CONECTAMOS A LOS ALUMNOS QUE HAN ENTREGADO TAREAS REALES ---
        boolean hayEntregas = false;
        for (Map<String, String> t : tareasDeEstaMateria) {
            List<Map<String,String>> entregas = tareasController.obtenerEntregasDeTarea(t.get("id"));
            for (Map<String, String> e : entregas) {
                lista.getChildren().add(crearItemAlumno(e.get("alumno"), "Entregó: " + t.get("titulo"), "#10b981"));
                hayEntregas = true;
            }
        }
        
        if(!hayEntregas) {
             Label vacio = new Label("Nadie ha entregado tareas aún.");
             vacio.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-style: italic;");
             lista.getChildren().add(vacio);
        }
        
        ScrollPane scrollAlumnos = new ScrollPane(lista);
        scrollAlumnos.setPrefHeight(150);
        scrollAlumnos.setStyle("-fx-background: " + colorBgPanel + "; -fx-background-color: transparent; -fx-border-color: transparent;");
        colAlumnos.getChildren().addAll(lblAlumnosTit, scrollAlumnos);

        contenido.getChildren().addAll(colTareas, colAlumnos);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCalificar = new Button("✏️ Calificar Entregas");
        btnCalificar.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCalificar.setOnAction(e -> {
            modal.close();
            javafx.scene.Node nodo=owner.getScene().getRoot().lookup("#optem-menu-tareas");
            if(nodo instanceof Button b)b.fire();
            else NotificacionManager.mostrarNotificacion("Tareas","Selecciona Asignar Tareas en el menú lateral.","INFO");
        });
        
        footer.getChildren().add(btnCalificar);

        layout.getChildren().addAll(header, contenido, footer);
        
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        modal.setScene(scene);
        
        modal.show();
        modal.setX(owner.getX() + (owner.getWidth() - modal.getWidth()) / 2);
        modal.setY(owner.getY() + (owner.getHeight() - modal.getHeight()) / 2);
    }

    private VBox crearMétricaTarea(String nombre, int entregas, int total, String colorBarra) {
        VBox box = new VBox(5);
        HBox top = new HBox();
        Label lblNom = new Label(nombre);
        lblNom.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-size: 0.9em;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblStat = new Label(entregas + "/" + total);
        lblStat.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-font-size: 0.9em;");
        top.getChildren().addAll(lblNom, sp, lblStat);
        
        ProgressBar pb = new ProgressBar(total == 0 ? 0 : (double)entregas/total);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent: " + colorBarra + "; -fx-control-inner-background: " + colorBgBase + ";");
        
        box.getChildren().addAll(top, pb);
        return box;
    }

    private HBox crearItemAlumno(String nombre, String estatus, String colorEstatus) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: " + colorBgBase + "; -fx-background-radius: 6; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        
        Circle dot = new Circle(4, Color.web(colorEstatus));
        Label lblNom = new Label(nombre);
        lblNom.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-size: 0.9em; -fx-font-weight: bold;");
        
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label lblEst = new Label(estatus);
        lblEst.setStyle("-fx-text-fill: " + colorEstatus + "; -fx-font-size: 0.8em;");
        
        row.getChildren().addAll(dot, lblNom, sp, lblEst);
        return row;
    }
}