package com.optem.ui;

import java.util.prefs.Preferences;

import com.optem.controllers.PomodoroController;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public class PomodoroView extends BorderPane {

    private final PomodoroController controller = new PomodoroController();
    private Timeline timeline;
    private ScaleTransition animacionLatido;
    private RotateTransition animacionRotacion;
    
    private Label lblReloj;
    private Group grupoSegmentos;
    private Line[] segmentosReloj;
    private Arc arcoProgreso;
    
    private Button btnMainAction;
    private Button btnModoEnfoque, btnModoDescanso;

    private String colorFuerteGlobal;
    private String colorRelojActual;
    private String colorBgBase, colorBgPanel, colorBorder, colorTextMain, colorTextMuted, fontSizeActual;
    
    public PomodoroView() {
        leerPreferencias();
        colorRelojActual = colorFuerteGlobal; 
        controller.configurarTiempos(25, 5); // 25 min enfoque, 5 min descanso para el Alumno
        
        this.setPadding(new Insets(30, 40, 40, 40));
        this.setStyle("-fx-background-color: transparent; -fx-font-size: " + fontSizeActual + ";");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        HBox panelModos = new HBox(0); 
        btnModoEnfoque = new Button("🍅 Enfoque (25m)");
        btnModoDescanso = new Button("☕ Descanso (5m)");
        
        panelModos.getChildren().addAll(btnModoEnfoque, btnModoDescanso);
        topBar.getChildren().add(panelModos);
        this.setTop(topBar);

        VBox cardCentral = new VBox(25);
        cardCentral.setAlignment(Pos.CENTER);
        cardCentral.setPadding(new Insets(35, 50, 35, 50));
        cardCentral.setMaxWidth(550);
        cardCentral.setMaxHeight(600);
        cardCentral.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 20; -fx-border-color: " + colorBorder + "; -fx-border-radius: 20; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 5);");

        VBox header = new VBox(8); header.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Temporizador de Estudio Libre");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: " + colorTextMain + ";"); 
        
        Label lblSub = new Label("Concéntrate sin distracciones. Este módulo no afecta tu nivel de XP.");
        lblSub.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 13px;");
        header.getChildren().addAll(lblTitulo, lblSub);

        StackPane relojUI = crearRelojRadial();
        HBox panelColores = crearPaletaColoresReloj();

        HBox panelControles = new HBox(20); panelControles.setAlignment(Pos.CENTER);
        
        btnMainAction = new Button("▶ Iniciar");
        btnMainAction.setStyle("-fx-background-color: " + colorFuerteGlobal + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 25; -fx-cursor: hand; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        
        Button btnReset = new Button("↺ Reiniciar");
        btnReset.setStyle("-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 25; -fx-padding: 12 30; -fx-font-size: 14px; -fx-cursor: hand;");

        panelControles.getChildren().addAll(btnMainAction, btnReset);
        cardCentral.getChildren().addAll(header, relojUI, panelColores, panelControles);
        this.setCenter(cardCentral);

        btnModoEnfoque.setOnAction(e -> seleccionarModo(true));
        btnModoDescanso.setOnAction(e -> seleccionarModo(false));
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            boolean termino = controller.tick();
            actualizarTextoReloj();
            actualizarArcoProgreso();
            if(termino) detenerReloj();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        btnMainAction.setOnAction(e -> {
            boolean enMarcha = controller.alternarTemporizador();
            if (enMarcha) {
                timeline.play();
                animacionLatido.play();
                animacionRotacion.play();
                btnMainAction.setText("⏸ Pausar");
            } else {
                detenerReloj();
                btnMainAction.setText("▶ Reanudar");
            }
        });

        btnReset.setOnAction(e -> {
            detenerReloj();
            controller.resetearTemporizador();
            btnMainAction.setText("▶ Iniciar");
            actualizarTextoReloj();
            actualizarArcoProgreso();
        });

        seleccionarModo(true);
    }

    private void leerPreferencias() {
        Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
        String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        
        String fontPref = prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal");
        fontSizeActual = fontPref.equals("Pequeño") ? "12px" : fontPref.equals("Grande") ? "17px" : "14px";
        
        boolean isDark = prefs.getBoolean("PREF_DARK_MODE_" + correoActual, false);
        colorFuerteGlobal = prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D");
        colorBgBase = isDark ? "#121212" : "#f1f5f9";
        colorBgPanel = isDark ? "#1e1e1e" : "#ffffff";
        colorTextMain = isDark ? "#f8f9fa" : "#1e293b";
        colorTextMuted = isDark ? "#94a3b8" : "#64748b";
        colorBorder = isDark ? "#333333" : "#e2e8f0";
    }
    
    private void detenerReloj() {
        timeline.pause();
        animacionLatido.stop();
        animacionRotacion.pause();
        lblReloj.setScaleX(1.0);
        lblReloj.setScaleY(1.0);
    }

    private void seleccionarModo(boolean isEnfoque) {
        controller.setModo(!isEnfoque);
        
        String azulDescanso = "#0288D1"; 
        
        String stEnfActivo = "-fx-background-color: " + colorFuerteGlobal + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 15 0 0 15; -fx-cursor: hand;";
        String stEnfInactivo = "-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-color: " + colorBorder + "; -fx-border-radius: 15 0 0 15; -fx-cursor: hand;";
        
        String stDesActivo = "-fx-background-color: " + azulDescanso + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 0 15 15 0; -fx-cursor: hand;";
        String stDesInactivo = "-fx-background-color: transparent; -fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-color: " + colorBorder + "; -fx-border-radius: 0 15 15 0; -fx-cursor: hand;";
        
        btnModoEnfoque.setStyle(isEnfoque ? stEnfActivo : stEnfInactivo);
        btnModoDescanso.setStyle(!isEnfoque ? stDesActivo : stDesInactivo);
        
        if(timeline != null) detenerReloj();
        if(btnMainAction != null) btnMainAction.setText("▶ Iniciar");
        
        colorRelojActual = isEnfoque ? colorFuerteGlobal : azulDescanso;
        aplicarColorAlReloj(colorRelojActual);
        actualizarTextoReloj();
        actualizarArcoProgreso();
    }

    private StackPane crearRelojRadial() {
        StackPane root = new StackPane();
        grupoSegmentos = new Group();
        
        int numSegmentos = 48; 
        segmentosReloj = new Line[numSegmentos];
        double innerRadius = 115;
        double outerRadius = 135;

        for (int i = 0; i < numSegmentos; i++) {
            double angle = 2 * Math.PI * i / numSegmentos;
            double x1 = Math.cos(angle) * innerRadius;
            double y1 = Math.sin(angle) * innerRadius;
            double x2 = Math.cos(angle) * outerRadius;
            double y2 = Math.sin(angle) * outerRadius;

            Line segmento = new Line(x1, y1, x2, y2);
            segmento.setStrokeWidth(4);
            segmento.setStrokeLineCap(StrokeLineCap.ROUND); 
            
            segmentosReloj[i] = segmento;
            grupoSegmentos.getChildren().add(segmento);
        }
        
        // Arco de progreso sólido (fondo y relleno)
        Circle fondoArco = new Circle(0, 0, 150);
        fondoArco.setFill(Color.TRANSPARENT);
        fondoArco.setStyle("-fx-stroke: " + colorBorder + ";");
        fondoArco.setStrokeWidth(6);
        
        arcoProgreso = new Arc(0, 0, 150, 150, 90, 0);
        arcoProgreso.setFill(Color.TRANSPARENT);
        arcoProgreso.setStrokeWidth(6);
        arcoProgreso.setStrokeLineCap(StrokeLineCap.ROUND); 
        
        Group grupoProgreso = new Group(fondoArco, arcoProgreso);

        animacionRotacion = new RotateTransition(Duration.seconds(20), grupoSegmentos);
        animacionRotacion.setByAngle(360);
        animacionRotacion.setCycleCount(Animation.INDEFINITE);
        animacionRotacion.setInterpolator(Interpolator.LINEAR);

        lblReloj = new Label();
        lblReloj.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        
        animacionLatido = new ScaleTransition(Duration.seconds(0.8), lblReloj);
        animacionLatido.setFromX(1.0); animacionLatido.setFromY(1.0);
        animacionLatido.setToX(1.05); animacionLatido.setToY(1.05);
        animacionLatido.setAutoReverse(true);
        animacionLatido.setCycleCount(Animation.INDEFINITE);
        
        root.getChildren().addAll(grupoProgreso, grupoSegmentos, lblReloj);
        return root;
    }
    
    private void actualizarArcoProgreso() {
        double progreso = controller.getProgreso(); // Va de 0.0 a 1.0
        arcoProgreso.setLength(-360 * progreso);
    }
    
    private HBox crearPaletaColoresReloj() {
        HBox caja = new HBox(12);
        caja.setAlignment(Pos.CENTER);
        
        String[] colores = {colorFuerteGlobal, "#F57C00", "#1976D2", "#E53935", "#7B1FA2", "RGB"};
        
        for (String col : colores) {
            Circle circuloColor = new Circle(12);
            circuloColor.setStyle("-fx-cursor: hand;");
            
            if (col.equals("RGB")) {
                Stop[] stops = new Stop[] { new Stop(0, Color.RED), new Stop(0.33, Color.LIME), new Stop(0.66, Color.BLUE), new Stop(1, Color.RED) };
                circuloColor.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops));
            } else {
                circuloColor.setFill(Color.web(col));
            }
            
            circuloColor.setOnMouseEntered(e -> {
                Paint relleno = circuloColor.getFill();
                Color shadowColor = (relleno instanceof Color) ? (Color) relleno : Color.GRAY;
                circuloColor.setEffect(new DropShadow(10, shadowColor));
            });
            circuloColor.setOnMouseExited(e -> circuloColor.setEffect(null));
            
            circuloColor.setOnMouseClicked(e -> {
                colorRelojActual = col;
                aplicarColorAlReloj(col);
            });
            
            caja.getChildren().add(circuloColor);
        }
        return caja;
    }
    
    private void aplicarColorAlReloj(String tipoColor) {
        for (int i = 0; i < segmentosReloj.length; i++) {
            if (tipoColor.equals("RGB")) {
                double hue = i * (360.0 / segmentosReloj.length);
                segmentosReloj[i].setStroke(Color.hsb(hue, 0.85, 0.9));
            } else {
                Color base = Color.web(tipoColor);
                double opacidad = 0.2 + (0.8 * ((double)i / segmentosReloj.length));
                segmentosReloj[i].setStroke(Color.color(base.getRed(), base.getGreen(), base.getBlue(), opacidad));
            }
        }
        
        String applyColor = tipoColor.equals("RGB") ? colorFuerteGlobal : tipoColor;
        arcoProgreso.setStyle("-fx-stroke: " + applyColor + ";");
        lblReloj.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-text-fill: " + applyColor + "; -fx-font-family: 'Segoe UI';");
    }

    private void actualizarTextoReloj() {
        lblReloj.setText(controller.getTiempoFormateado());
    }
}