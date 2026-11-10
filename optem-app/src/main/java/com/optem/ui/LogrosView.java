package com.optem.ui;

import java.util.prefs.Preferences;

import com.optem.controllers.LogrosController;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LogrosView extends VBox {

    private LogrosController controller;
    private String colorFuerte, colorBgPanel, colorBgBase, colorTextMain, colorTextMuted, colorBorder, fontSizeActual;

    public LogrosView() {
        leerPreferencias();
        this.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        VBox contenido = new VBox(35);
        contenido.setPadding(new Insets(30, 40, 40, 40));
        contenido.setStyle("-fx-background-color: transparent;");

        VBox header = new VBox(5);
        Label titulo = new Label("Sistema de Logros y Niveles");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        
        Label subtitulo = new Label("Gana XP exclusivamente al entregar tus tareas oficiales a tiempo.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        
        header.getChildren().addAll(titulo, subtitulo);

        HBox panelMedallas = new HBox(15);
        panelMedallas.setAlignment(Pos.CENTER_LEFT);
        
        panelMedallas.getChildren().addAll(
            crearMedalla("🥉", "Primer Paso", "Entrega tu primera tarea del día.", controller.tieneMedallaPrimeraSangre()),
            crearMedalla("🥇", "Imparable", "Entrega todas tus tareas del día.", controller.tieneMedallaImparable()),
            crearMedalla("🛡", "Sobreviviente", "Sin tareas pendientes en la semana.", controller.tieneMedallaSobreviviente())
        );

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        
        int xpTotal = controller.getXPTotalAlumno();
        int xpMetaTotal = controller.calcularXPMetaTotal(); // El total de TODAS tus tareas
        
        int diasRacha = controller.calcularDiasRachaAlumno();
        double bonoRacha = controller.calcularBonoRacha(diasRacha);
        String nivelActual = controller.obtenerNivelActualAlumno(xpTotal);
        
        VBox boxXP = crearTarjetaEstadistica(crearProgresoXP(xpTotal, xpMetaTotal, nivelActual));
        VBox boxRacha = crearTarjetaEstadistica(crearRachaDias(diasRacha, bonoRacha));
        
        HBox.setHgrow(boxXP, Priority.ALWAYS);
        HBox.setHgrow(boxRacha, Priority.ALWAYS);
        statsBox.getChildren().addAll(boxXP, boxRacha);

        VBox mapaNiveles = new VBox(15);
        mapaNiveles.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 15; -fx-padding: 30; -fx-border-color: " + colorBorder + "; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        Label lblMapa = new Label("🗺 Mapa de niveles (Camino al Diamante)");
        lblMapa.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 16px; -fx-font-family: 'Segoe UI Symbol';");
        
        mapaNiveles.getChildren().add(lblMapa);
        mapaNiveles.getChildren().addAll(
            crearFilaNivel("🌱", "Novato", "Inicio de semestre (0 – 199 XP)", nivelActual.equals("Novato"), true),
            crearFilaNivel("⚡", "Intermedio", "Primer Parcial (200 – 399 XP)", nivelActual.equals("Intermedio"), xpTotal >= 200),
            crearFilaNivel("🚀", "Avanzado", "Mitad de Semestre (400 – 599 XP)", nivelActual.equals("Avanzado"), xpTotal >= 400),
            crearFilaNivel("👑", "Experto", "Segundo Parcial (600 – 799 XP)", nivelActual.equals("Experto"), xpTotal >= 600),
            crearFilaNivel("💎", "Diamante", "Fin de Semestre (800+ XP)", nivelActual.equals("Diamante"), xpTotal >= 800)
        );

        HBox banner = new HBox(30);
        banner.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 15; -fx-border-color: " + colorBorder + "; -fx-border-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        banner.setAlignment(Pos.CENTER_LEFT);

        Node animacionEscudo = crearAnimacionEscudo();
        banner.getChildren().add(animacionEscudo);

        VBox bannerTextos = new VBox(5);
        Label lblBannerTitle = new Label(xpTotal > 0 ? "¡Excelente trabajo con tus tareas!" : "¡Sube tu primera tarea para ganar XP!");
        lblBannerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-font-size: 16px;");
        Label lblBannerSub = new Label("Tu racha depende de iniciar sesión: se reinicia tras 24 horas sin entrar.");
        lblBannerSub.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        bannerTextos.getChildren().addAll(lblBannerTitle, lblBannerSub);
        
        banner.getChildren().add(bannerTextos);

        contenido.getChildren().addAll(header, panelMedallas, statsBox, mapaNiveles, banner);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        this.getChildren().add(scroll);
    }
    
    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        controller = new LogrosController(correoActual);
        
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";

        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerte = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }

    private VBox crearTarjetaEstadistica(Node contenido) {
        VBox card = new VBox(contenido);
        card.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 15; -fx-padding: 30; -fx-border-color: " + colorBorder + "; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private Node crearAnimacionEscudo() {
        StackPane contenedorAnimacion = new StackPane();
        contenedorAnimacion.setPrefSize(70, 70);
        contenedorAnimacion.setAlignment(Pos.CENTER);
        
        Circle fondo = new Circle(35);
        fondo.setFill(Color.web(colorFuerte, 0.15));

        Text textIcono = new Text("★");
        textIcono.setFont(Font.font("Segoe UI Symbol", FontWeight.BOLD, 35));
        textIcono.setStyle("-fx-fill: " + colorFuerte + ";");

        RotateTransition rotate = new RotateTransition(Duration.millis(3000), textIcono);
        rotate.setByAngle(25);
        rotate.setCycleCount(Timeline.INDEFINITE);
        rotate.setAutoReverse(true);
        rotate.play();

        contenedorAnimacion.getChildren().addAll(fondo, textIcono);
        return contenedorAnimacion;
    }
    
    private VBox crearMedalla(String icono, String titulo, String desc, boolean desbloqueada) {
        VBox medalla = new VBox(5);
        medalla.setAlignment(Pos.CENTER);
        medalla.setPadding(new Insets(15));
        medalla.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 10; -fx-border-color: " + colorBorder + "; -fx-border-radius: 10;");
        medalla.setPrefWidth(210);
        
        Label lblIcon = new Label(desbloqueada ? icono : "🔒");
        String colorIcono = desbloqueada ? "-color-text-main" : "-color-text-muted";
        lblIcon.setStyle("-fx-font-size: 32px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: " + colorIcono + "; -fx-opacity: " + (desbloqueada ? "1.0" : "0.4") + ";");
        
        Label lblTit = new Label(titulo);
        lblTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 11px; -fx-wrap-text: true; -fx-text-alignment: center;");
        
        medalla.getChildren().addAll(lblIcon, lblTit, lblDesc);
        return medalla;
    }

    private VBox crearProgresoXP(int xpTotal, int xpMetaTotal, String nivelActual) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);

        Group grupoCirculos = new Group();
        Circle anilloFondo = new Circle(0, 0, 70);
        anilloFondo.setFill(Color.TRANSPARENT);
        anilloFondo.setStyle("-fx-stroke: " + colorBorder + ";");
        anilloFondo.setStrokeWidth(12);

        // --- CÁLCULO EXACTO DEL CÍRCULO ---
        // Se llena en base a las tareas entregadas vs el total de tareas asignadas
        double porcentaje = xpMetaTotal == 0 ? 0 : Math.min(1.0, (double) xpTotal / xpMetaTotal);
        double gradosDeLlenado = -360 * porcentaje;
        
        Arc anilloProgreso = new Arc(0, 0, 70, 70, 90, gradosDeLlenado);
        anilloProgreso.setFill(Color.TRANSPARENT);
        anilloProgreso.setStyle("-fx-stroke: " + colorFuerte + ";");
        anilloProgreso.setStrokeWidth(12);
        anilloProgreso.setStrokeLineCap(StrokeLineCap.ROUND); 

        grupoCirculos.getChildren().addAll(anilloFondo, anilloProgreso);
        StackPane contenedorCirculo = new StackPane(grupoCirculos);

        Label lblNivel = new Label(nivelActual);
        lblNivel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + "; -fx-background-color: " + colorFuerte + "15; -fx-padding: 5 15; -fx-background-radius: 20;");
        
        Label lblXP = new Label(xpTotal + " XP");
        lblXP.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 32px;");
        
        Label lblFaltan = new Label("Total tareas entregadas: " + xpTotal + " / " + xpMetaTotal + " XP");
        lblFaltan.setStyle("-fx-text-fill: " + colorTextMuted + ";");

        layout.getChildren().addAll(contenedorCirculo, lblNivel, lblXP, lblFaltan);
        return layout;
    }

    private VBox crearRachaDias(int dias, double bono) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);

        Label iconoFuego = new Label(dias > 0 ? "🔥" : "🧊");
        iconoFuego.setStyle("-fx-font-size: 55px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: " + (dias > 0 ? "#E65100" : "-color-text-muted") + ";");
        
        Label lblDias = new Label(dias + " Días");
        lblDias.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + "; -fx-font-size: 32px;"); 
        
        Label lblTexto = new Label("Racha de entregas (Semestre)");
        lblTexto.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        
        Label lblBono = new Label("Bono multiplicador: x" + bono + " XP");
        lblBono.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorFuerte + ";");

        layout.getChildren().addAll(iconoFuego, lblDias, lblTexto, lblBono);
        return layout;
    }

    private HBox crearFilaNivel(String icono, String titulo, String xpRango, boolean esActual, boolean desbloqueado) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + (esActual ? colorBgBase : "transparent") + "; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: " + (esActual ? colorFuerte : "transparent") + "; -fx-border-radius: 10;");

        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: " + (desbloqueado ? colorFuerte + "20" : colorBgBase) + "; -fx-background-radius: 12; -fx-min-width: 50px; -fx-min-height: 50px;");
        
        Label lblIcono = new Label(desbloqueado ? icono : "🔒");
        lblIcono.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Emoji'; -fx-text-fill: " + (desbloqueado ? colorFuerte : "-color-text-muted") + ";");
        iconBox.getChildren().add(lblIcono);

        VBox textos = new VBox(2);
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: " + (desbloqueado ? colorTextMain : colorTextMuted) + ";");
        Label lblRango = new Label(xpRango);
        lblRango.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 13px;");
        textos.getChildren().addAll(lblTitulo, lblRango);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label();
        if (esActual) {
            status.setText("● Nivel actual");
            status.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15; -fx-background-radius: 15;");
        } else if (desbloqueado) {
            status.setText("✔ Superado");
            status.setStyle("-fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold;");
        } else {
            status.setText("Bloqueado");
            status.setStyle("-fx-text-fill: " + colorTextMuted + ";");
        }

        card.getChildren().addAll(iconBox, textos, spacer, status);
        return card;
    }
}