package com.optem.ui;

import java.util.prefs.Preferences;
import java.sql.*;
import com.optem.database.MySQL;
import javafx.scene.control.Alert;

import com.optem.controllers.DashboardController;
import com.optem.logic.NotificacionManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AdminEstadisticasView {

    private final DashboardController controller = new DashboardController();
    private String colorFuerte, colorBgBase, colorBgPanel, colorTextMain, colorTextMuted, colorBorder;

    public VBox getView() {
        leerPreferencias();

        VBox layoutMain = new VBox(25);
        layoutMain.setPadding(new Insets(30, 40, 30, 40));
        layoutMain.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label titulo = new Label("📊 Estadísticas y Tráfico de Servidores");
        titulo.setStyle("-fx-font-weight: 800; -fx-font-size: 28px; -fx-text-fill: " + colorTextMain + ";");
        Label subtitulo = new Label("Monitoreo en tiempo real del uso de Optem y carga de Base de Datos.");
        subtitulo.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRespaldo = new Button("💾 Ejecutar Respaldo Manual");
        btnRespaldo.setStyle("-fx-background-color: " + colorFuerte + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRespaldo.setOnAction(e -> {
            new Alert(Alert.AlertType.INFORMATION,"Los respaldos íntegros se generan desde Ubuntu Server con: sudo mysqldump optem_db > optem_respaldo.sql. No se creó un respaldo desde esta pantalla.").showAndWait();
        });

        header.getChildren().addAll(titleBox, spacer, btnRespaldo);

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-padding: 12 20; -fx-background-radius: 8; -fx-border-color: " + colorBorder + "; -fx-border-radius: 8;");
        
        Circle dotStatus = new Circle(5, Color.web("#f59e0b"));
        Label lblStatus = new Label("Verificando MySQL en segundo plano…");
        lblStatus.setStyle("-fx-text-fill: " + colorTextMain + "; -fx-font-weight: bold;");
        statusBox.getChildren().addAll(dotStatus, lblStatus);

        HBox kpiBox = new HBox(20);
        VBox cajaUsuarios=crearTarjetaKPI("👥 Usuarios Activos", "…", "Cuentas activas", "#3b82f6");
        VBox cajaCpu=crearTarjetaKPI("💻 CPU servidor", "…", "Ubuntu Server", colorFuerte);
        VBox cajaRam=crearTarjetaKPI("🗄️ RAM servidor", "…", "Ubuntu Server", "#f59e0b");
        VBox cajaGpu=crearTarjetaKPI("🎮 GPU servidor", "…", "Solo si Ubuntu la expone", "#10b981");
        VBox cajaConsultas=crearTarjetaKPI("🔄 Consultas MySQL", "…", "Total desde inicio de MySQL", "#8b5cf6");
        kpiBox.getChildren().addAll(cajaUsuarios,cajaCpu,cajaRam,cajaGpu,cajaConsultas);

        VBox chartBox = new VBox(15);
        chartBox.setPadding(new Insets(20));
        chartBox.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 2, 2);");
        
        Label lblChartTit = new Label("Consultas de MySQL por hora (Últimas 24h)");
        lblChartTit.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + colorTextMain + ";");

        NumberAxis xAxis = new NumberAxis(0, 24, 2);
        xAxis.setLabel("Horas transcurridas desde hace 24h");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Consultas / hora");
        
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setPrefHeight(350);
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Consultas registradas por el servidor");
        
        lineChart.getData().add(series);

        String chartCss = "CHART_COLOR_1: " + colorFuerte + "; " +
                          "-fx-background-color: transparent; " +
                          "-fx-text-fill: " + colorTextMain + ";";
        lineChart.setStyle(chartCss);
        
        chartBox.getChildren().addAll(lblChartTit, lineChart);

        VBox logBox = new VBox(10);
        Label lblLogTit = new Label("Terminal de Registros (System Logs)");
        lblLogTit.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorTextMain + ";");
        
        VBox avisoLogs = new VBox(8);
        avisoLogs.setAlignment(Pos.CENTER);
        avisoLogs.setPadding(new Insets(30));
        avisoLogs.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-background-radius: 12; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12;");
        Label iconoHerramientas = new Label("🛠");
        iconoHerramientas.setStyle("-fx-font-size: 45px; -fx-text-fill: #10b981;");
        Label mensajeLogs = new Label("Logs en proceso de construcción");
        mensajeLogs.setStyle("-fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #10b981;");
        avisoLogs.getChildren().addAll(iconoHerramientas,mensajeLogs);
        logBox.getChildren().addAll(lblLogTit, avisoLogs);

        layoutMain.getChildren().addAll(header, statusBox, kpiBox, chartBox, logBox);
        
        ScrollPane scroll = new ScrollPane(layoutMain);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox layoutFinal = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        Runnable actualizar=()->{
            javafx.concurrent.Task<com.optem.database.MonitorDAO.Metrica> tarea=new javafx.concurrent.Task<>(){
                @Override protected com.optem.database.MonitorDAO.Metrica call() throws Exception{
                    return com.optem.database.MonitorDAO.leer();
                }
            };
            tarea.setOnSucceeded(e->{
                var m=tarea.getValue();
                dotStatus.setFill(Color.web("#10b981"));lblStatus.setText("MySQL: Conectado | "+m.conexiones()+" conexiones");
                ((Label)cajaUsuarios.getChildren().get(1)).setText(String.valueOf(m.usuarios()));
                ((Label)cajaCpu.getChildren().get(1)).setText(m.cpu());
                ((Label)cajaRam.getChildren().get(1)).setText(m.ram());
                ((Label)cajaGpu.getChildren().get(1)).setText(m.gpu());
                ((Label)cajaConsultas.getChildren().get(1)).setText(String.valueOf(m.consultasGlobales()));
                series.getData().clear();
                for(var punto:m.puntos())series.getData().add(new XYChart.Data<>(punto.hora(),punto.consultas()));
            });
            tarea.setOnFailed(e->{
                dotStatus.setFill(Color.web("#ef4444"));lblStatus.setText("MySQL: Sin conexión");
                // Logs no implementados: el recuadro de construcción permanece visible.
            });
            Thread hilo=new Thread(tarea,"optem-estadisticas");hilo.setDaemon(true);hilo.start();
        };
        javafx.animation.Timeline refresco=new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10),e->actualizar.run()));
        refresco.setCycleCount(javafx.animation.Animation.INDEFINITE);
        layoutFinal.sceneProperty().addListener((o,anterior,nueva)->{
            if(nueva==null)refresco.stop();else{actualizar.run();refresco.play();}
        });
        return layoutFinal;
    }

    private String consultaContador(String consulta){
        try(Connection c=MySQL.conectar();Statement p=c.createStatement();ResultSet r=p.executeQuery(consulta)){
            return r.next()?String.valueOf(r.getLong(1)):"0";
        }catch(SQLException ex){return "N/D";}
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

    private VBox crearTarjetaKPI(String titulo, String valor, String subtitulo, String colorHex) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(20));
        box.setPrefWidth(250);
        box.setStyle("-fx-background-color: " + colorBgPanel + "; -fx-border-color: " + colorBorder + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 2, 2);");
        
        Label lblTit = new Label(titulo);
        lblTit.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label lblVal = new Label(valor);
        lblVal.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: " + colorHex + ";");
        
        Label lblSub = new Label(subtitulo);
        lblSub.setStyle("-fx-text-fill: " + colorTextMuted + "; -fx-font-size: 11px;");
        
        box.getChildren().addAll(lblTit, lblVal, lblSub);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }
}