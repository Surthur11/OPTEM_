package com.optem.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.controllers.AgendaController;
import com.optem.database.MockDB;
import com.optem.logic.NotificacionManager;
import com.optem.models.Actividad;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AgendaView {

    private final AgendaController controller = new AgendaController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;
    private String correoActual;
    
    private LocalDate fechaActual = LocalDate.now();
    private String vistaActual = "WEEK"; 
    private String filtroActual = "Todas"; 
    
    private VBox view;
    private BorderPane layoutMain;
    private VBox panelCentral;
    private GridPane gridMiniCalendario;
    private Label lblMesMini;
    private VBox dashboardProductividad; 
    private Stage stagePrincipal;

    public AgendaView() {
        try {
            leerPreferencias();
            view = new VBox();
            view.setStyle("-fx-background-color: transparent;");

            layoutMain = new BorderPane();
            layoutMain.setPadding(new Insets(25, 30, 25, 30));
            VBox.setVgrow(layoutMain, Priority.ALWAYS);

            HBox header = crearCabeceraAgenda();
            layoutMain.setTop(header);
            BorderPane.setMargin(header, new Insets(0, 0, 20, 0));

            VBox panelIzquierdo = crearPanelLateralIzquierdo();
            layoutMain.setLeft(panelIzquierdo);
            BorderPane.setMargin(panelIzquierdo, new Insets(0, 20, 0, 0));

            panelCentral = new VBox();
            panelCentral.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
            
            ScrollPane scrollCentral = new ScrollPane(panelCentral);
            scrollCentral.setFitToWidth(true);
            scrollCentral.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            layoutMain.setCenter(scrollCentral);

            renderizarVistaActual();

            view.getChildren().add(layoutMain);

            view.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) newS.windowProperty().addListener((o, oldW, newW) -> { if(newW != null) stagePrincipal = (Stage) newW; });
            });
            
        } catch (Exception e) {
            System.err.println("❌ ERROR AL CARGAR LA AGENDA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public VBox getView() { return view; }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");

        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    // =========================================================================
    // NUEVO ALGORITMO: Fusiona clases oficiales del MockDB con actividades
    // =========================================================================
    private List<Actividad> obtenerClasesOficialesComoActividades(LocalDate dia) {
        List<Actividad> clasesDelDia = new ArrayList<>();
        
        // CORRECCIÓN: Convierte el día de la semana actual al formato abreviado (Lun, Mar, etc.)
        String diaCorto = "";
        switch (dia.getDayOfWeek().getValue()) {
            case 1: diaCorto = "Lun"; break;
            case 2: diaCorto = "Mar"; break;
            case 3: diaCorto = "Mié"; break;
            case 4: diaCorto = "Jue"; break;
            case 5: diaCorto = "Vie"; break;
            case 6: diaCorto = "Sáb"; break;
            case 7: diaCorto = "Dom"; break;
        }
        
        List<Map<String,String>> misMaterias = MockDB.obtenerMateriasDelAlumno(correoActual);
        
        if (misMaterias != null) {
            for (Map<String, String> mat : misMaterias) {
                String horario = mat.get("horario"); // Ej: "Lun 11:30-13:30 / Mié 11:30-13:30"
                
                // Si el horario de esta materia contiene el día actual (ej. "Lun")
                if (horario.contains(diaCorto)) {
                    
                    String[] partesHorario = horario.split("/");
                    String horaInicioStr = "07:00"; 
                    
                    for (String parte : partesHorario) {
                        if (parte.contains(diaCorto)) {
                            try {
                                String[] detalles = parte.trim().split(" ");
                                String rangoHoras = detalles[1]; 
                                horaInicioStr = rangoHoras.split("-")[0]; 
                            } catch (Exception e) {}
                            break;
                        }
                    }
                    
                    try {
                        LocalTime hora = LocalTime.of(Integer.parseInt(horaInicioStr.split(":")[0]), Integer.parseInt(horaInicioStr.split(":")[1]));
                        Actividad act = new Actividad("CLASE_" + mat.get("clave"), "Clase: " + mat.get("nombre"), "Clase", "Horario Oficial: " + horario + "\nProfesor: " + mat.get("profesor"), dia, hora, mat.get("nombre"));
                        act.setEstado("PENDIENTE");
                        clasesDelDia.add(act);
                    } catch (Exception e) {}
                }
            }
        }
        return clasesDelDia;
    }

    private HBox crearCabeceraAgenda() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label titulo = new Label("Mi Agenda Universitaria");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Tus clases oficiales y pendientes integrados");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox selectorVista = new HBox(0);
        selectorVista.setStyle("-fx-background-color: " + colorBgBase + "; -fx-background-radius: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8;");
        selectorVista.getChildren().addAll(
            crearBotonVista("Día", "DAY"), crearBotonVista("Semana", "WEEK"), crearBotonVista("Mes", "MONTH")
        );

        HBox navBox = new HBox(5);
        Button btnAtras = new Button("◀"); btnAtras.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        Button btnAdelante = new Button("▶"); btnAdelante.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6;");
        Button btnHoy = new Button("Hoy"); btnHoy.setStyle("-fx-background-color: " + colorBgBase + "; -fx-text-fill: " + colorTextMain + "; -fx-cursor: hand; -fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-font-weight: bold;");

        btnAtras.setOnAction(e -> { fechaActual = controller.navegarFecha(fechaActual, vistaActual, -1); renderizarVistaActual(); });
        btnAdelante.setOnAction(e -> { fechaActual = controller.navegarFecha(fechaActual, vistaActual, 1); renderizarVistaActual(); });
        btnHoy.setOnAction(e -> { fechaActual = LocalDate.now(); renderizarVistaActual(); });
        
        navBox.getChildren().addAll(btnAtras, btnHoy, btnAdelante);
        header.getChildren().addAll(titleBox, spacer, selectorVista, navBox);
        return header;
    }

    private Button crearBotonVista(String texto, String idVista) {
        Button btn = new Button(texto);
        if (idVista.equals(vistaActual)) {
            btn.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 15;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");
        }
        btn.setOnAction(e -> {
            vistaActual = idVista;
            if (btn.getParent() != null) {
                for (javafx.scene.Node n : ((HBox) btn.getParent()).getChildren()) {
                    n.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");
                }
            }
            btn.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 8 15;");
            renderizarVistaActual();
        });
        return btn;
    }

    private VBox crearPanelLateralIzquierdo() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(280); panel.setMaxWidth(280);
        
        VBox boxCalendario = new VBox(15);
        boxCalendario.setPadding(new Insets(20));
        boxCalendario.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-width: 2; -fx-border-radius: 12;");

        lblMesMini = new Label();
        lblMesMini.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + colorTextMain + ";");

        gridMiniCalendario = new GridPane();
        gridMiniCalendario.setHgap(8); gridMiniCalendario.setVgap(8); 
        
        String[] diasCortos = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(diasCortos[i]);
            l.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            GridPane.setHalignment(l, HPos.CENTER);
            gridMiniCalendario.add(l, i, 0);
        }

        boxCalendario.getChildren().addAll(lblMesMini, gridMiniCalendario);
        actualizarMiniCalendario();

        VBox boxFiltros = new VBox(12);
        Label lblFil = new Label("Filtros rápidos:");
        lblFil.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + colorTextMuted + ";");
        
        FlowPane pildoras = new FlowPane();
        pildoras.setHgap(8); pildoras.setVgap(8);
        pildoras.getChildren().addAll(
            crearPildoraFiltro("Todas"), 
            crearPildoraFiltro("Clases"), 
            crearPildoraFiltro("Tareas"), 
            crearPildoraFiltro("Actividades"), 
            crearPildoraFiltro("Pendientes")
        );
        boxFiltros.getChildren().addAll(lblFil, pildoras);

        dashboardProductividad = new VBox(15);
        dashboardProductividad.setAlignment(Pos.CENTER);
        dashboardProductividad.setStyle("-fx-background-color: " + colorBgBase + "; -fx-background-radius: 12; -fx-padding: 20;");
        actualizarAnilloProgreso();

        panel.getChildren().addAll(boxCalendario, boxFiltros, dashboardProductividad);
        return panel;
    }
    
    private Button crearPildoraFiltro(String texto) {
        Button btn = new Button(texto);
        String estiloActivo = "-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 14;";
        String estiloInactivo = "-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 20; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 5 13;";
        
        btn.setStyle(filtroActual.equals(texto) ? estiloActivo : estiloInactivo);
        
        btn.setOnAction(e -> {
            filtroActual = texto;
            if (btn.getParent() != null) {
                for (javafx.scene.Node n : ((FlowPane) btn.getParent()).getChildren()) n.setStyle(estiloInactivo);
            }
            btn.setStyle(estiloActivo);
            renderizarVistaActual();
        });
        return btn;
    }
    
    private void actualizarAnilloProgreso() {
        dashboardProductividad.getChildren().clear();
        int[] datos = controller.getProgresoSemanal(fechaActual.with(DayOfWeek.MONDAY));
        int completadas = datos[0];
        int total = datos[1];
        
        Label lblT = new Label("Productividad Semanal");
        lblT.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 14px;");
        
        StackPane anilloContenedor = new StackPane();
        anilloContenedor.setAlignment(Pos.CENTER);
        
        Circle fondo = new Circle(45);
        fondo.setFill(Color.TRANSPARENT);
        fondo.setStroke(Color.web(colorBorder));
        fondo.setStrokeWidth(8);
        
        double porcentaje = total == 0 ? 0 : (double) completadas / total;
        Arc arco = new Arc(0, 0, 45, 45, 90, -360 * porcentaje);
        arco.setFill(Color.TRANSPARENT);
        arco.setStroke(Color.web(colorFuerte));
        arco.setStrokeWidth(8);
        arco.setStrokeLineCap(StrokeLineCap.ROUND);
        
        javafx.scene.Group formas = new javafx.scene.Group(fondo, arco);
        
        Label lblPorc = new Label((int)(porcentaje * 100) + "%");
        lblPorc.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 20px;");
        
        anilloContenedor.getChildren().addAll(formas, lblPorc);
        
        Label lblInfo = new Label(completadas + " de " + total + " tareas terminadas");
        lblInfo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 12px;");
        
        dashboardProductividad.getChildren().addAll(lblT, anilloContenedor, lblInfo);
    }

    private void actualizarMiniCalendario() {
        gridMiniCalendario.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
        YearMonth ym = YearMonth.from(fechaActual);
        lblMesMini.setText(ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
        LocalDate primerDiaMes = ym.atDay(1);
        int offset = primerDiaMes.getDayOfWeek().getValue() - 1; 
        int diasEnMes = ym.lengthOfMonth();
        int row = 1; int col = offset;
        
        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fechaCelda = ym.atDay(dia);
            Label lblNum = new Label(String.valueOf(dia));
            lblNum.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-size: 13px;");
            
            VBox btnDia = new VBox(lblNum);
            btnDia.setAlignment(Pos.CENTER);
            btnDia.setMinSize(28, 28);
            btnDia.setStyle("-fx-background-color: transparent; -fx-background-radius: 15; -fx-cursor: hand;");
            
            if (fechaCelda.equals(LocalDate.now())) {
                btnDia.setStyle("-fx-background-color: " + colorFuerte + "40; -fx-background-radius: 15; -fx-cursor: hand;");
                lblNum.setStyle("-fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold;");
            }
            if (fechaCelda.equals(fechaActual)) {
                btnDia.setStyle("-fx-background-color: " + colorFuerte + "; -fx-background-radius: 15; -fx-cursor: hand;");
                lblNum.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }

            btnDia.setOnMouseClicked(e -> { fechaActual = fechaCelda; vistaActual = "DAY"; renderizarVistaActual(); });
            GridPane.setHalignment(btnDia, HPos.CENTER);
            gridMiniCalendario.add(btnDia, col, row);
            col++; if (col > 6) { col = 0; row++; }
        }
    }

    private void renderizarVistaActual() {
        panelCentral.getChildren().clear();
        actualizarMiniCalendario();
        actualizarAnilloProgreso();

        if (vistaActual.equals("DAY")) renderizarVistaDia(fechaActual);
        else if (vistaActual.equals("WEEK")) renderizarVistaSemana(fechaActual.with(DayOfWeek.MONDAY));
        else renderizarVistaMes(YearMonth.from(fechaActual));
    }

    private void renderizarVistaDia(LocalDate dia) {
        Label lblDia = new Label(dia.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")).toUpperCase());
        lblDia.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 18px; -fx-padding: 20;");
        panelCentral.getChildren().add(lblDia);

        AnchorPane timeGrid = new AnchorPane();
        timeGrid.setPrefHeight(1000); 
        
        for (int i = 7; i <= 22; i++) {
            double posY = (i - 7) * 60;
            Label lHora = new Label(String.format("%02d:00", i));
            lHora.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 11px;");
            AnchorPane.setLeftAnchor(lHora, 10.0);
            AnchorPane.setTopAnchor(lHora, posY - 8);
            
            Line line = new Line(55, 0, 1500, 0); 
            line.setStyle("-fx-stroke: " + colorBorder + "; -fx-stroke-width: 0.5; -fx-stroke-dash-array: 5 5;");
            AnchorPane.setLeftAnchor(line, 55.0);
            AnchorPane.setTopAnchor(line, posY);
            timeGrid.getChildren().addAll(lHora, line);
        }

        List<Actividad> actividades = new ArrayList<>();
        List<Actividad> actsPers = controller.getActividadesDelDia(dia, filtroActual);
        if (actsPers != null) actividades.addAll(actsPers);
        
        if (filtroActual.equals("Todas") || filtroActual.equals("Clases")) {
            actividades.addAll(obtenerClasesOficialesComoActividades(dia));
        }

        for (Actividad act : actividades) {
            LocalTime t = (act.getHoraVencimiento() != null) ? act.getHoraVencimiento() : LocalTime.of(12, 0);
            double posY = (t.getHour() - 7) * 60 + t.getMinute();
            if(posY < 0) posY = 0; 
            
            HBox card = crearTarjetaTimeBlock(act);
            AnchorPane.setLeftAnchor(card, 65.0);
            AnchorPane.setTopAnchor(card, posY);
            AnchorPane.setRightAnchor(card, 20.0);
            timeGrid.getChildren().add(card);
        }

        if (dia.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            if (now.getHour() >= 7 && now.getHour() <= 22) {
                double yNow = (now.getHour() - 7) * 60 + now.getMinute();
                Line redLine = new Line(55, 0, 1500, 0);
                redLine.setStyle("-fx-stroke: #D32F2F; -fx-stroke-width: 2;");
                Circle redDot = new Circle(55, 0, 4, Color.web("#D32F2F"));
                AnchorPane.setLeftAnchor(redLine, 55.0); AnchorPane.setTopAnchor(redLine, yNow);
                AnchorPane.setLeftAnchor(redDot, 51.0); AnchorPane.setTopAnchor(redDot, yNow);
                timeGrid.getChildren().addAll(redLine, redDot);
            }
        }

        ScrollPane scrollGrid = new ScrollPane(timeGrid);
        scrollGrid.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollGrid.setFitToWidth(true);
        VBox.setVgrow(scrollGrid, Priority.ALWAYS);
        panelCentral.getChildren().add(scrollGrid);
    }

    private HBox crearTarjetaTimeBlock(Actividad act) {
        String colorMat = controller.getColorMateria(act.getMateria());
        boolean caducada = controller.isActividadCaducada(act) && "COMPLETADO".equals(act.getEstado());
        
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-color: " + colorMat + "20; -fx-background-radius: 8; -fx-border-color: " + colorMat + "; -fx-border-radius: 8; -fx-border-width: 0 0 0 4; -fx-cursor: hand;");
        if (caducada) card.setOpacity(0.5);

        VBox infoBox = new VBox(2);
        String titulo = (act.getNombre() != null) ? act.getNombre() : "Sin Título";
        Label lblTit = new Label(titulo);
        lblTit.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorTextMain + ";");
        
        Label lblSub = new Label((act.getMateria() != null ? act.getMateria() : "General") + " • " + act.getHoraVencimiento());
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorTextMuted + ";");
        
        infoBox.getChildren().addAll(lblTit, lblSub);
        card.getChildren().add(infoBox);
        
        card.setOnMouseClicked(e -> mostrarVentanaDetalles(act, caducada, "COMPLETADO".equals(act.getEstado()) && !caducada));
        return card;
    }

    private void renderizarVistaSemana(LocalDate inicioSemana) {
        Label lblSemana = new Label("SEMANA DEL " + inicioSemana.format(DateTimeFormatter.ofPattern("d MMM")) + " AL " + inicioSemana.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM yyyy")).toUpperCase());
        lblSemana.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 18px; -fx-padding: 20;");
        panelCentral.getChildren().add(lblSemana);

        HBox gridSemana = new HBox(10);
        gridSemana.setPadding(new Insets(0, 20, 20, 20));
        String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            VBox colDia = new VBox(10);
            colDia.setPrefWidth(200); HBox.setHgrow(colDia, Priority.ALWAYS);
            colDia.setStyle("-fx-padding: 5; -fx-background-radius: 8; -fx-border-color: transparent;");
            
            Label lblNum = new Label(String.valueOf(dia.getDayOfMonth()));
            lblNum.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: " + (dia.equals(LocalDate.now()) ? colorFuerte : colorTextMain) + ";");
            
            Label lblNom = new Label(nombresDias[i]);
            lblNom.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorTextMuted + ";");
            
            VBox headerDia = new VBox(lblNom, lblNum);
            headerDia.setAlignment(Pos.CENTER);
            headerDia.setStyle("-fx-padding: 10; -fx-border-color: " + colorBorder + "; -fx-border-width: 0 0 2 0;");
            colDia.getChildren().add(headerDia);

            List<Actividad> acts = new ArrayList<>();
            List<Actividad> actsBD = controller.getActividadesDelDia(dia, filtroActual);
            if(actsBD != null) acts.addAll(actsBD);
            
            if (filtroActual.equals("Todas") || filtroActual.equals("Clases")) {
                acts.addAll(obtenerClasesOficialesComoActividades(dia));
            }

            for (Actividad act : acts) {
                VBox bloque = crearBloqueEventoMini(act);
                
                bloque.setOnDragDetected(e -> {
                    Dragboard db = bloque.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(act.getId()); 
                    db.setContent(content);
                    e.consume();
                });
                colDia.getChildren().add(bloque);
            }

            colDia.setOnDragOver(e -> {
                if (e.getGestureSource() != colDia && e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });
            colDia.setOnDragEntered(e -> colDia.setStyle("-fx-padding: 5; -fx-background-radius: 8; -fx-background-color: " + colorFuerte + "15; -fx-border-color: " + colorFuerte + "; -fx-border-style: dashed; -fx-border-radius: 8;"));
            colDia.setOnDragExited(e -> colDia.setStyle("-fx-padding: 5; -fx-background-radius: 8; -fx-background-color: transparent; -fx-border-color: transparent;"));
            colDia.setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String id = db.getString();
                    if (id.startsWith("CLASE_")) {
                        NotificacionManager.mostrarNotificacion("Acción Denegada", "No puedes reprogramar los horarios de tus clases oficiales.", "URGENTE");
                    } else {
                        Actividad aMover = controller.buscarActividadPorId(id);
                        if (aMover != null) {
                            aMover.setFecha(dia); 
                            success = true;
                            renderizarVistaActual(); 
                            NotificacionManager.mostrarNotificacion("Reprogramado", "Actividad movida al " + nombresDias[dia.getDayOfWeek().getValue()-1], "INFO");
                        }
                    }
                }
                e.setDropCompleted(success);
                e.consume();
            });

            gridSemana.getChildren().add(colDia);
        }
        panelCentral.getChildren().add(gridSemana);
    }

    private void renderizarVistaMes(YearMonth mes) {
        Label lblMes = new Label(mes.format(DateTimeFormatter.ofPattern("MMMM yyyy")).toUpperCase());
        lblMes.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + colorTextMain + "; -fx-padding: 20;");
        panelCentral.getChildren().add(lblMes);

        GridPane gridMes = new GridPane();
        gridMes.setPadding(new Insets(0, 20, 20, 20));
        gridMes.setHgap(5); gridMes.setVgap(5);
        
        String[] diasCortos = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(diasCortos[i]);
            l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + colorTextMuted + "; -fx-padding: 10;");
            GridPane.setHalignment(l, HPos.CENTER);
            
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(14.28);
            gridMes.getColumnConstraints().add(cc);
            gridMes.add(l, i, 0);
        }

        LocalDate primerDia = mes.atDay(1);
        int offset = primerDia.getDayOfWeek().getValue() - 1; 
        int diasEnMes = mes.lengthOfMonth();
        int row = 1; int col = offset;

        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fechaCelda = mes.atDay(dia);
            VBox celdaDia = new VBox(2);
            celdaDia.setPadding(new Insets(5));
            celdaDia.setMinHeight(90); 
            celdaDia.setStyle("-fx-border-color: " + colorBorder + "; -fx-border-radius: 6; -fx-background-color: " + (fechaCelda.equals(LocalDate.now()) ? colorFuerte+"10" : colorBgBase) + ";");
            
            Label lblNum = new Label(String.valueOf(dia));
            lblNum.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + (fechaCelda.equals(LocalDate.now()) ? colorFuerte : colorTextMain) + ";");
            celdaDia.getChildren().add(lblNum);

            List<Actividad> acts = new ArrayList<>();
            List<Actividad> actsBD = controller.getActividadesDelDia(fechaCelda, filtroActual);
            if(actsBD != null) acts.addAll(actsBD);
            
            if (filtroActual.equals("Todas") || filtroActual.equals("Clases")) {
                acts.addAll(obtenerClasesOficialesComoActividades(fechaCelda));
            }

            int mostrados = 0;
            for (Actividad act : acts) {
                if (mostrados >= 2) {
                    Label lblMas = new Label("+" + (acts.size() - 2) + " más");
                    lblMas.setStyle("-fx-font-size: 10px; -fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold;");
                    celdaDia.getChildren().add(lblMas);
                    break;
                }
                celdaDia.getChildren().add(crearMicroPildora(act));
                mostrados++;
            }
            
            celdaDia.setOnMouseClicked(e -> {
                fechaActual = fechaCelda; vistaActual = "DAY"; renderizarVistaActual();
            });

            gridMes.add(celdaDia, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
        panelCentral.getChildren().add(gridMes);
    }

    private VBox crearBloqueEventoMini(Actividad act) {
        String colorMat = controller.getColorMateria(act.getMateria());
        boolean caducada = controller.isActividadCaducada(act) && "COMPLETADO".equals(act.getEstado());
        
        VBox bloque = new VBox(2);
        bloque.setPadding(new Insets(8));
        bloque.setStyle("-fx-background-color: " + (caducada ? colorBgBase : colorMat+"20") + "; -fx-background-radius: 6; -fx-border-color: " + (caducada ? "#D32F2F" : colorMat) + "; -fx-border-width: 0 0 0 3; -fx-cursor: hand;");
        if(caducada) bloque.setOpacity(0.6);

        String titulo = (act.getNombre() != null) ? act.getNombre() : "Evento";
        Label lTit = new Label(titulo);
        lTit.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + colorTextMain + ";");
        
        String textoHora = (act.getHoraVencimiento() != null) ? act.getHoraVencimiento().toString() : "00:00";
        Label lHora = new Label(textoHora);
        lHora.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 10px;");
        
        bloque.getChildren().addAll(lTit, lHora);
        bloque.setOnMouseClicked(e -> mostrarVentanaDetalles(act, caducada, "COMPLETADO".equals(act.getEstado()) && !caducada));
        return bloque;
    }

    private Label crearMicroPildora(Actividad act) {
        String colorMat = controller.getColorMateria(act.getMateria());
        boolean caducada = controller.isActividadCaducada(act) && "COMPLETADO".equals(act.getEstado());
        String titulo = (act.getNombre() != null) ? act.getNombre() : "Evento";
        Label lbl = new Label("• " + titulo);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (caducada ? "#D32F2F" : colorMat) + "; -fx-font-weight: bold;");
        return lbl;
    }

    private void mostrarVentanaDetalles(Actividad act, boolean caducada, boolean completada) {
        Stage popup = new Stage();
        if (stagePrincipal != null) popup.initOwner(stagePrincipal);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + (caducada ? "#D32F2F" : colorFuerte) + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 40, 0, 0, 10);");

        HBox topBar = new HBox(15); topBar.setAlignment(Pos.CENTER_LEFT);
        String tipoInfo = (act.getTipo() != null) ? act.getTipo() : "Actividad";
        Label icon = new Label(tipoInfo.equals("Tarea") ? "📝" : "📅");
        icon.setStyle("-fx-font-size: 30px; -fx-font-family: 'Segoe UI Emoji';");
        
        VBox titBox = new VBox(2);
        String titulo = (act.getNombre() != null) ? act.getNombre() : "Sin Título";
        Label lblT = new Label(titulo); lblT.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        
        String materiaInfo = (act.getMateria() != null) ? act.getMateria() : "General";
        Label lblM = new Label(materiaInfo + " • " + tipoInfo); lblM.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorTextMuted + ";");
        titBox.getChildren().addAll(lblT, lblM);
        
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnCerrar = new Button("✖");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-cursor: hand; -fx-font-size: 16px;");
        btnCerrar.setOnAction(e -> popup.close());
        
        topBar.getChildren().addAll(icon, titBox, sp, btnCerrar);

        GridPane gridTiempo = new GridPane();
        gridTiempo.setHgap(30); gridTiempo.setVgap(10);
        gridTiempo.setStyle("-fx-background-color: " + colorBgBase + "; -fx-padding: 15; -fx-background-radius: 8;");
        
        String fechaRegistro = (act.getFecha() != null) ? act.getFecha().minusDays(1).toString() : "---";
        gridTiempo.add(crearInfoLabel("REGISTRO", fechaRegistro), 0, 0); 
        
        String fechaTexto = (act.getFecha() != null) ? act.getFecha().toString() : "---";
        String horaTexto = (act.getHoraVencimiento() != null) ? act.getHoraVencimiento().toString() : "---";
        gridTiempo.add(crearInfoLabel(tipoInfo.equals("Tarea") ? "FECHA LÍMITE" : "HORA EVENTO", fechaTexto + " • " + horaTexto), 1, 0);
        
        Label lblEstado = new Label(caducada ? "❌ CADUCADA" : (completada ? "✅ COMPLETADA" : "⏳ PENDIENTE"));
        lblEstado.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + (caducada ? "#D32F2F" : (completada ? "#388E3C" : colorFuerte)) + ";");
        gridTiempo.add(crearInfoLabel("ESTADO ACTUAL", lblEstado.getText()), 2, 0);

        VBox descBox = new VBox(10);
        Label lD = new Label("Mensaje / Instrucciones:"); lD.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        String descripcion = (act.getDescripcion() != null) ? act.getDescripcion() : "Sin detalles adicionales.";
        Label txtD = new Label(descripcion); txtD.setStyle("-fx-font-size: 14px; -fx-text-fill: " + colorTextMain + "; -fx-wrap-text: true; -fx-line-spacing: 5px;");
        descBox.getChildren().addAll(lD, txtD);

        root.getChildren().addAll(topBar, gridTiempo, descBox);

        Scene scene = new Scene(root, 600, 400);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        
        if (stagePrincipal != null) {
            popup.setX(stagePrincipal.getX() + (stagePrincipal.getWidth() - scene.getWidth()) / 2);
            popup.setY(stagePrincipal.getY() + (stagePrincipal.getHeight() - scene.getHeight()) / 2);
        }
        popup.show();
    }
    
    private VBox crearInfoLabel(String titulo, String valor) {
        VBox v = new VBox(2);
        Label t = new Label(titulo); t.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMuted + ";");
        Label val = new Label(valor); val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        v.getChildren().addAll(t, val);
        return v;
    }
}