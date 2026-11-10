package com.optem.ui;

import com.optem.controllers.AdminController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminViews {

    private final AdminController controller = new AdminController();

    public VBox getVistaEstadisticas() {
        VBox root = crearContenedorBase("📊 Estadísticas y Tráfico de Servidores", "Monitoreo en tiempo real del uso de Optem.");
        
        Label lblSalud = new Label(controller.obtenerEstadoServidor());
        lblSalud.getStyleClass().add("label-bold-14");

        NumberAxis xAxis = new NumberAxis(0, 24, 2); xAxis.setLabel("Hora del Día (24 hrs)");
        NumberAxis yAxis = new NumberAxis(); yAxis.setLabel("Usuarios Conectados");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Tráfico de Servidores");
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Tráfico de Hoy");
        series.getData().add(new XYChart.Data<>(7, 1200)); 
        series.getData().add(new XYChart.Data<>(9, 1500));
        series.getData().add(new XYChart.Data<>(12, 800));
        series.getData().add(new XYChart.Data<>(14, 1800)); 
        series.getData().add(new XYChart.Data<>(20, 400));
        chart.getData().add(series);

        Button btnRespaldo = new Button("💾 Ejecutar Respaldo Manual");
        btnRespaldo.getStyleClass().add("chat-btn");
        btnRespaldo.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 8;");
        
        VBox panel = new VBox(20, lblSalud, chart, btnRespaldo);
        panel.getStyleClass().add("section-panel");
        root.getChildren().add(panel);
        return root;
    }

    public VBox getVistaPerfiles() {
        VBox root = crearContenedorBase("👤 Gestión de Perfiles", "Consulta y edición de información de contacto.");
        
        HBox buscador = new HBox(10); buscador.setAlignment(Pos.CENTER_LEFT);
        TextField txtCuenta = new TextField(); txtCuenta.setPromptText("No. Cuenta..."); txtCuenta.getStyleClass().add("chat-input");
        Button btnBuscar = new Button("Buscar"); btnBuscar.getStyleClass().add("btn-resume-book");
        
        GridPane grid = new GridPane(); grid.setHgap(15); grid.setVgap(15); grid.setVisible(false);
        
        btnBuscar.setOnAction(e -> {
            String[] datos = controller.obtenerInfoContacto(txtCuenta.getText());
            if(datos != null) {
                grid.getChildren().clear();
                String[] labels = {"Nombre*", "Teléfono*", "Correo*", "Calle", "Colonia", "Población", "Código Postal", "País", "Estado", "Municipio"};
                for(int i=0; i<labels.length; i++) {
                    Label lbl = new Label(labels[i]); lbl.getStyleClass().add("label-muted-14");
                    TextField txt = new TextField(datos[i]); txt.setEditable(false); txt.getStyleClass().add("chat-input"); txt.setPrefWidth(300);
                    grid.add(lbl, 0, i); grid.add(txt, 1, i);
                }
                grid.setVisible(true);
            }
        });
        
        buscador.getChildren().addAll(txtCuenta, btnBuscar);
        VBox panel = new VBox(20, buscador, grid); panel.getStyleClass().add("section-panel");
        root.getChildren().add(panel);
        return root;
    }

    public VBox getVistaDocumentos() {
        VBox root = crearContenedorBase("📁 Documentos y Trayectoria Académica", "Análisis por ciclo e indicadores de riesgo.");
        
        TextField txtCuenta = new TextField(); txtCuenta.setPromptText("Buscar No. Cuenta..."); txtCuenta.getStyleClass().add("chat-input");
        Button btnAnalizar = new Button("Analizar Expediente"); btnAnalizar.getStyleClass().add("btn-resume-book");
        HBox buscador = new HBox(10, txtCuenta, btnAnalizar);
        
        VBox panel = new VBox(20, buscador); panel.getStyleClass().add("section-panel");
        
        btnAnalizar.setOnAction(e -> {
            panel.getChildren().clear();
            panel.getChildren().add(buscador);
            
            String[] riesgo = controller.obtenerIndicadoresRiesgo(txtCuenta.getText());
            Label lblRiesgoTitle = new Label("INDICADORES DE RIESGO ACADÉMICO"); lblRiesgoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #3D5A4D;");
            Label lblRiesgoData = new Label("Grado de avance: " + riesgo[0] + "\nPromedio general: " + riesgo[1] + "\nPromedio último periodo: " + riesgo[2] + "\nSituación actual: " + riesgo[3] + "\nBandera: " + riesgo[5]);
            lblRiesgoData.getStyleClass().add("label-bold-14");
            
            Label lblDocs = new Label("Documentos de Ingreso"); lblDocs.setStyle("-fx-font-weight: bold; -fx-text-fill: #3D5A4D;");
            VBox listDocs = new VBox(5);
            for(String doc : controller.obtenerDocumentosIngreso(txtCuenta.getText())) {
                Label d = new Label(doc); d.getStyleClass().add("label-muted-14"); listDocs.getChildren().add(d);
            }
            
            panel.getChildren().addAll(new Separator(), lblRiesgoTitle, lblRiesgoData, new Separator(), lblDocs, listDocs);
        });
        
        root.getChildren().add(panel); return root;
    }

    public VBox getVistaConfiguracion() {
        VBox root = crearContenedorBase("⚙️ Configuración del Sistema", "Abre o cierra los módulos globales de Optem.");
        
        Label lblInfo = new Label("Control de periodo de Reinscripción para Alumnos:"); lblInfo.getStyleClass().add("label-bold-14");
        ToggleButton btnToggleReins = new ToggleButton(controller.isReinscripcionActiva() ? "🟢 Reinscripción ABIERTA" : "🔴 Reinscripción CERRADA");
        btnToggleReins.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        
        btnToggleReins.setOnAction(e -> {
            controller.toggleReinscripcion(btnToggleReins.isSelected());
            btnToggleReins.setText(btnToggleReins.isSelected() ? "🟢 Reinscripción ABIERTA" : "🔴 Reinscripción CERRADA");
        });
        
        VBox panel = new VBox(20, lblInfo, btnToggleReins); panel.getStyleClass().add("section-panel");
        root.getChildren().add(panel); return root;
    }

    public VBox getVistaIA() {
        VBox root = crearContenedorBase("🤖 Motor de Inteligencia Artificial", "Análisis de seriación de materias y asignación de recompensas (XP).");
        
        VBox boxHorarios = new VBox(10); boxHorarios.getStyleClass().add("section-panel");
        Label lblHorarios = new Label("Generador de Horarios Inteligente (Verificar Seriadas)"); lblHorarios.getStyleClass().add("label-bold-14");
        HBox busquedaHorario = new HBox(10);
        TextField txtCuentaHorario = new TextField(); txtCuentaHorario.setPromptText("No. Cuenta Alumno..."); txtCuentaHorario.getStyleClass().add("chat-input");
        Button btnGenerar = new Button("Simular Horario IA"); btnGenerar.getStyleClass().add("btn-resume-book");
        TextArea txtHorarioRes = new TextArea(); txtHorarioRes.setEditable(false); txtHorarioRes.setPrefHeight(120);
        
        btnGenerar.setOnAction(e -> txtHorarioRes.setText(controller.generarHorarioInteligente(txtCuentaHorario.getText())));
        busquedaHorario.getChildren().addAll(txtCuentaHorario, btnGenerar);
        boxHorarios.getChildren().addAll(lblHorarios, busquedaHorario, txtHorarioRes);

        VBox boxRecompensas = new VBox(10); boxRecompensas.getStyleClass().add("section-panel");
        Label lblXP = new Label("Evaluador de Rendimiento y Recompensas (Fin de Semestre)"); lblXP.getStyleClass().add("label-bold-14");
        Button btnEvaluar = new Button("🏆 Ejecutar Evaluación Global"); btnEvaluar.getStyleClass().add("chat-btn"); btnEvaluar.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        TextArea txtXpRes = new TextArea(); txtXpRes.setEditable(false); txtXpRes.setPrefHeight(150);
        
        btnEvaluar.setOnAction(e -> txtXpRes.setText(controller.evaluarRecompensas()));
        boxRecompensas.getChildren().addAll(lblXP, btnEvaluar, txtXpRes);

        root.getChildren().addAll(boxHorarios, boxRecompensas); return root;
    }

    private VBox crearContenedorBase(String titulo, String subtitulo) {
        VBox root = new VBox(25); root.setPadding(new Insets(30, 40, 30, 40));
        Label title = new Label(titulo); title.getStyleClass().add("section-title"); title.setStyle("-fx-font-size: 26px;");
        Label sub = new Label(subtitulo); sub.getStyleClass().add("label-muted-14");
        root.getChildren().addAll(new VBox(5, title, sub));
        return root;
    }
}