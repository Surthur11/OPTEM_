package com.optem.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.optem.controllers.LoginController;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginView {

    private final LoginController controller = new LoginController();

    private TextField[] txtDigitos = new TextField[7];
    private String[] cuentaReal = new String[7];
    private boolean ocultarCuenta = true; 
    
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = true;

    public void show(Stage stage) {
        
        if (controller.checkAutoLogin(stage)) {
            return;
        }

        if (!stage.isShowing()) {
            stage.initStyle(StageStyle.UNDECORATED); 
        }

        String colorFuerte = "#3D5A4D";
        String colorClaro = "#E9EDC9";
        String colorOroUaemex = "#CBA052"; 
        final boolean animacionesActivas = true;

        VBox absoluteRoot = new VBox();
        absoluteRoot.setStyle("-fx-background-color: linear-gradient(to right, #3D5A4D 0%, #3D5A4D 25%, rgba(61,90,77,0.8) 40%, rgba(61,90,77,0.4) 55%, rgba(61,90,77,0.1) 70%, #ffffff 85%, #ffffff 100%);");

        HBox titleBar = new HBox(15);
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(10, 15, 5, 0));
        titleBar.setStyle("-fx-background-color: transparent;");

        titleBar.setOnMousePressed(event -> { xOffset = event.getSceneX(); yOffset = event.getSceneY(); });
        titleBar.setOnMouseDragged(event -> {
            if (!isMaximized) { stage.setX(event.getScreenX() - xOffset); stage.setY(event.getScreenY() - yOffset); }
        });

        String btnControlStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15;";
        String btnControlHover = "-fx-background-color: rgba(0,0,0,0.1); -fx-text-fill: #000000; -fx-background-radius: 5;";
        
        Button btnMinimizar = new Button("—");
        btnMinimizar.setStyle(btnControlStyle);
        btnMinimizar.setOnMouseEntered(e -> btnMinimizar.setStyle(btnControlStyle + btnControlHover));
        btnMinimizar.setOnMouseExited(e -> btnMinimizar.setStyle(btnControlStyle));
        btnMinimizar.setOnAction(e -> stage.setIconified(true));

        Button btnMaximizar = new Button("🗗");
        btnMaximizar.setStyle(btnControlStyle);
        btnMaximizar.setOnMouseEntered(e -> btnMaximizar.setStyle(btnControlStyle + btnControlHover));
        btnMaximizar.setOnMouseExited(e -> btnMaximizar.setStyle(btnControlStyle));
        btnMaximizar.setOnAction(e -> {
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            if (isMaximized) {
                stage.setX(bounds.getWidth() * 0.1); stage.setY(bounds.getHeight() * 0.1);
                stage.setWidth(bounds.getWidth() * 0.8); stage.setHeight(bounds.getHeight() * 0.8);
                isMaximized = false; btnMaximizar.setText("🗖"); 
            } else {
                stage.setX(bounds.getMinX()); stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth()); stage.setHeight(bounds.getHeight());
                isMaximized = true; btnMaximizar.setText("🗗");
            }
        });

        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle(btnControlStyle);
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(btnControlStyle + "-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-background-radius: 5;"));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(btnControlStyle));
        btnCerrar.setOnAction(e -> { Platform.exit(); System.exit(0); });

        titleBar.getChildren().addAll(btnMinimizar, btnMaximizar, btnCerrar);

        StackPane mainContent = new StackPane();
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        Pane bubblesPane = new Pane();
        bubblesPane.setManaged(false); 
        bubblesPane.setMouseTransparent(false); 
        
        mainContent.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            bubblesPane.resize(newVal.getWidth(), newVal.getHeight());
        });
        
        for (int i = 0; i < 35; i++) { bubblesPane.getChildren().add(createNormalBubble(bubblesPane)); }

        AnimationTimer physicsEngine = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = bubblesPane.getWidth() > 0 ? bubblesPane.getWidth() : 1280;
                double h = bubblesPane.getHeight() > 0 ? bubblesPane.getHeight() : 720;
                List<Node> toRemove = new ArrayList<>();

                for (Node n : bubblesPane.getChildren()) {
                    if (n instanceof Circle) {
                        Circle c = (Circle) n;
                        Double dx = (Double) c.getProperties().get("dx");
                        Double dy = (Double) c.getProperties().get("dy");
                        Double life = (Double) c.getProperties().get("life");
                        Double baseOpacity = (Double) c.getProperties().get("baseOpacity");
                        if (baseOpacity == null) baseOpacity = 0.15;

                        if (dx != null && dy != null) {
                            double nx = c.getLayoutX() + dx;
                            double ny = c.getLayoutY() + dy;
                            double r = c.getRadius();

                            if (nx - r < 0) { nx = r; dx = -dx; c.getProperties().put("dx", dx); }
                            if (nx + r > w) { nx = w - r; dx = -dx; c.getProperties().put("dx", dx); }
                            if (ny - r < 0) { ny = r; dy = -dy; c.getProperties().put("dy", dy); }
                            if (ny + r > h) { ny = h - r; dy = -dy; c.getProperties().put("dy", dy); }

                            c.setLayoutX(nx); c.setLayoutY(ny);
                            
                            Color colorBlanco = Color.WHITE; Color colorVerde = Color.web("#3D5A4D"); Color colorActual;
                            if (nx < w * 0.35) { colorActual = colorBlanco; } 
                            else if (nx > w * 0.65) { colorActual = colorVerde; } 
                            else {
                                double fraction = (nx - (w * 0.35)) / (w * 0.30);
                                colorActual = colorBlanco.interpolate(colorVerde, fraction);
                            }
                            
                            c.setFill(new Color(colorActual.getRed(), colorActual.getGreen(), colorActual.getBlue(), baseOpacity));
                            c.setStroke(new Color(colorActual.getRed(), colorActual.getGreen(), colorActual.getBlue(), baseOpacity + 0.1));
                        }
                        if (life != null) {
                            life -= 0.005; c.setOpacity(life); c.getProperties().put("life", life);
                            if (life <= 0) toRemove.add(c);
                        }
                    }
                }
                bubblesPane.getChildren().removeAll(toRemove);
            }
        };
        
        if (animacionesActivas) { physicsEngine.start(); }

        HBox uiLayer = new HBox();
        uiLayer.setPickOnBounds(false); 

        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(550);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(50, 50, 50, 80));
        leftPanel.setPickOnBounds(false);

        HBox logosBox = new HBox(40);
        logosBox.setAlignment(Pos.CENTER_LEFT);
        try {
            InputStream isUaem = getClass().getResourceAsStream("/images/uaem_escudo.png");
            if (isUaem != null) { ImageView ivUaem = new ImageView(new Image(isUaem)); ivUaem.setPreserveRatio(true); ivUaem.setFitHeight(90); logosBox.getChildren().add(ivUaem); }

            InputStream isOptem = getClass().getResourceAsStream("/images/logo_optem_white.png");
            if (isOptem != null) { ImageView ivOptem = new ImageView(new Image(isOptem)); ivOptem.setPreserveRatio(true); ivOptem.setFitHeight(160); logosBox.getChildren().add(ivOptem); }
        } catch(Exception ex) {}

        Label lblTitleOptem = new Label("OPTEM");
        lblTitleOptem.setStyle("-fx-font-size: 72; -fx-font-weight: bold; -fx-text-fill: " + colorClaro + "; -fx-font-family: 'Segoe UI';");
        lblTitleOptem.setEffect(new DropShadow(5, Color.rgb(0,0,0,0.1)));
        VBox.setMargin(lblTitleOptem, new Insets(10, 0, 10, 0));

        VBox featuresBox = new VBox(25);
        featuresBox.setPickOnBounds(false);
        featuresBox.getChildren().addAll(
            createFeatureLabel("📅", "Calendario Académico", "Agenda y horario académico"),
            createFeatureLabel("⏱", "Administración del Tiempo", "Técnica Pomodoro"),
            createFeatureLabel("🏆", "XP, Rachas y Logros", "Sistema de recompensas"),
            createFeatureLabel("🔔", "Alertas y Notificaciones", "Alertas de entregas")
        );

        leftPanel.getChildren().addAll(logosBox, lblTitleOptem, featuresBox);

        VBox rightPanel = new VBox(30);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(40, 120, 40, 120));
        rightPanel.setPickOnBounds(false);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        VBox formBox = new VBox(18);
        formBox.setMaxWidth(520);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65); -fx-padding: 30; -fx-background-radius: 15;");

        Label lblTitle = new Label("Iniciar sesión");
        lblTitle.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: #1B263B; -fx-font-family: 'Segoe UI';");
        VBox.setMargin(lblTitle, new Insets(0, 0, 10, 0));

        TextField txtNombre = createModernInput("Nombre(s)", colorOroUaemex);
        TextField txtApellido = createModernInput("Apellidos (Opcional)", colorOroUaemex);
        TextField txtCorreo = createModernInput("Correo institucional", colorOroUaemex);

        ContextMenu domainMenu = new ContextMenu();
        domainMenu.getStyleClass().add("menu-dominio-oro"); 
        MenuItem optAlumno = new MenuItem("@alumno.uaemex.mx");
        MenuItem optUaemex = new MenuItem("@uaemex.mx");
        domainMenu.getItems().addAll(optAlumno, optUaemex);

        txtCorreo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.endsWith("@")) { double x = txtCorreo.localToScreen(txtCorreo.getBoundsInLocal()).getMinX(); double y = txtCorreo.localToScreen(txtCorreo.getBoundsInLocal()).getMaxY(); domainMenu.show(txtCorreo, x, y); } 
            else if (!newVal.contains("@")) { domainMenu.hide(); }
        });

        optAlumno.setOnAction(e -> { txtCorreo.setText(txtCorreo.getText() + "alumno.uaemex.mx"); Platform.runLater(txtCorreo::end); });
        optUaemex.setOnAction(e -> { txtCorreo.setText(txtCorreo.getText() + "uaemex.mx"); Platform.runLater(txtCorreo::end); });

        Label lblError = new Label(); lblError.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';"); lblError.setVisible(false);

        HBox boxDigitos = new HBox(10); 
        boxDigitos.setAlignment(Pos.CENTER_LEFT);
        
        for (int i = 0; i < 7; i++) {
            cuentaReal[i] = "";
            TextField tf = new TextField();
            tf.setPrefSize(45, 52); 
            tf.setAlignment(Pos.CENTER);
            
            String normalStyle = "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-width: 1.2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 20; -fx-font-weight: bold;";
            String focusStyle = "-fx-background-color: white; -fx-border-color: " + colorOroUaemex + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 20; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(203,160,82,0.4), 8, 0, 0, 0);";
            
            tf.setStyle(normalStyle);
            tf.focusedProperty().addListener((obs, o, n) -> { tf.setStyle(n ? focusStyle : normalStyle); });

            final int index = i;
            
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.equals("•")) return; 
                if (newVal.length() > 0) {
                    String lastChar = newVal.substring(newVal.length() - 1);
                    if (lastChar.matches("\\d")) { 
                        cuentaReal[index] = lastChar;
                        Platform.runLater(() -> tf.setText(ocultarCuenta ? "•" : lastChar));
                        StringBuilder pwd = new StringBuilder();
                        for (String c : cuentaReal) pwd.append(c);
                        
                        if (com.optem.database.MockDB.esAdministrador(txtCorreo.getText().trim(), pwd.toString())) {
                            Platform.runLater(() -> {
                                if (animacionesActivas) physicsEngine.stop();
                                controller.handleLogin(txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(), pwd.toString(), "ADMINISTRADOR", lblError, stage);
                                if (lblError.isVisible() && animacionesActivas) {
                                    physicsEngine.start();
                                }
                            });
                        } else if (index < 6) {
                            txtDigitos[index + 1].requestFocus();
                        }

                    } else {
                        Platform.runLater(() -> tf.setText(oldVal));
                    }
                } else {
                    cuentaReal[index] = "";
                }
            });

            tf.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.BACK_SPACE && tf.getText().isEmpty() && index > 0) {
                    txtDigitos[index - 1].requestFocus();
                    txtDigitos[index - 1].clear();
                    cuentaReal[index - 1] = "";
                }
            });
            txtDigitos[i] = tf;
            boxDigitos.getChildren().add(tf);
        }

        Label btnTogglePass = new Label("🔒");
        btnTogglePass.setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #555555; -fx-padding: 0 0 0 5;");
        btnTogglePass.setMinWidth(40); 
        
        Tooltip tooltipOjo = new Tooltip("Mostrar");
        tooltipOjo.setStyle("-fx-font-size: 12px;");
        btnTogglePass.setTooltip(tooltipOjo);

        btnTogglePass.setOnMouseEntered(e -> btnTogglePass.setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #000000; -fx-padding: 0 0 0 5;"));
        btnTogglePass.setOnMouseExited(e -> btnTogglePass.setStyle("-fx-cursor: hand; -fx-font-size: 22px; -fx-text-fill: #555555; -fx-padding: 0 0 0 5;"));

        btnTogglePass.setOnMouseClicked(e -> {
            ocultarCuenta = !ocultarCuenta;
            for (int j = 0; j < 7; j++) {
                if (cuentaReal[j].isEmpty()) continue;
                txtDigitos[j].setText(ocultarCuenta ? "•" : cuentaReal[j]);
            }
            btnTogglePass.setText(ocultarCuenta ? "🔒" : "🔓");
            tooltipOjo.setText(ocultarCuenta ? "Mostrar" : "Ocultar");
        });

        HBox boxesContainer = new HBox(10, boxDigitos, btnTogglePass);
        boxesContainer.setAlignment(Pos.CENTER_LEFT);

        Label lblCuentaHint = new Label("Ingresa tu número de cuenta");
        lblCuentaHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-family: 'Segoe UI';");

        VBox accountFullContainer = new VBox(5, boxesContainer, lblCuentaHint);

        Label lblTipo = new Label("Tipo de cuenta"); lblTipo.setStyle("-fx-font-size: 13; -fx-text-fill: #888; -fx-margin-top: 10; -fx-font-family: 'Segoe UI';");

        HBox buttonsBox = new HBox(20); buttonsBox.setAlignment(Pos.CENTER_LEFT);
        Button btnEstudiante = createAccountTypeButton("🎓", "Estudiante", colorFuerte, colorClaro);
        Button btnDocente = createAccountTypeButton("🏛", "Docente", colorFuerte, colorClaro);
        buttonsBox.getChildren().addAll(btnEstudiante, btnDocente);

        btnEstudiante.setOnAction(e -> {
            if (animacionesActivas) physicsEngine.stop(); 
            StringBuilder passwordBuilder = new StringBuilder();
            for(int i=0; i<7; i++) passwordBuilder.append(cuentaReal[i]);
            
            controller.handleLogin(txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(), passwordBuilder.toString(), "ESTUDIANTE", lblError, stage);
            
            if (lblError.isVisible() && animacionesActivas) {
                physicsEngine.start();
            }
        });

        btnDocente.setOnAction(e -> {
            if (animacionesActivas) physicsEngine.stop(); 
            StringBuilder passwordBuilder = new StringBuilder();
            for(int i=0; i<7; i++) passwordBuilder.append(cuentaReal[i]);
            
            controller.handleLogin(txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(), passwordBuilder.toString(), "MAESTRO", lblError, stage);
            
            if (lblError.isVisible() && animacionesActivas) {
                physicsEngine.start();
            }
        });

        formBox.getChildren().addAll(lblTitle, txtNombre, txtApellido, txtCorreo, accountFullContainer, lblError, lblTipo, buttonsBox);
        rightPanel.getChildren().add(formBox);

        uiLayer.getChildren().addAll(leftPanel, rightPanel);
        mainContent.getChildren().addAll(bubblesPane, uiLayer);
        absoluteRoot.getChildren().addAll(titleBar, mainContent);

        Scene scene = new Scene(absoluteRoot, 1280, 720);
        
        String cssSeguro = ".menu-dominio-oro { -fx-background-color: rgba(255, 255, 255, 0.98); -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8; } " +
            ".menu-dominio-oro .menu-item { -fx-padding: 8 15 8 12; -fx-cursor: hand; } " +
            ".menu-dominio-oro .menu-item .label { -fx-text-fill: #444; -fx-font-size: 13; -fx-font-family: 'Segoe UI'; } " +
            ".menu-dominio-oro .menu-item:focused { -fx-background-color: " + colorClaro + "; -fx-background-radius: 6; } " + 
            ".menu-dominio-oro .menu-item:focused .label { -fx-text-fill: " + colorFuerte + "; -fx-font-weight: bold; }";
        scene.getStylesheets().add("data:text/css," + cssSeguro.replace(" ", "%20").replace("#", "%23"));

        stage.setScene(scene);
        stage.setTitle("Optem - Iniciar Sesión");
        
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        
        stage.setOnCloseRequest(e -> physicsEngine.stop());
        stage.show();
    }

    private TextField createModernInput(String prompt, String colorOro) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        String baseStyle = "-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1.2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-font-family: 'Segoe UI';";
        String focusStyle = "-fx-background-color: #ffffff; -fx-border-color: " + colorOro + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 11; -fx-font-size: 14; -fx-font-family: 'Segoe UI'; -fx-effect: dropshadow(gaussian, rgba(203,160,82,0.4), 8, 0, 0, 0);";
        tf.setStyle(baseStyle);
        tf.focusedProperty().addListener((obs, oldVal, newVal) -> { tf.setStyle(newVal ? focusStyle : baseStyle); });
        return tf;
    }

    private Circle createNormalBubble(Pane pane) {
        Circle bubble = new Circle(Math.random() * 45 + 15);
        double baseOpacity = 0.05 + Math.random() * 0.1;
        bubble.getProperties().put("baseOpacity", baseOpacity);
        bubble.setFill(Color.web("#FFFFFF", baseOpacity)); 
        bubble.setStroke(Color.web("#FFFFFF", baseOpacity + 0.1));
        
        bubble.setLayoutX(Math.random() * 1000 + 50); 
        bubble.setLayoutY(Math.random() * 600 + 50);
        bubble.getProperties().put("dx", (Math.random() - 0.5) * 2.5); 
        bubble.getProperties().put("dy", (Math.random() - 0.5) * 2.5);

        bubble.setOnMouseEntered(e -> {
            Color currentColor = (Color) bubble.getFill();
            bubble.setFill(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 0.3));
        }); 
        
        bubble.setOnMouseExited(e -> {
            Color currentColor = (Color) bubble.getFill();
            Double bOp = (Double) bubble.getProperties().get("baseOpacity");
            bubble.setFill(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), bOp));
        });

        bubble.setOnMouseClicked(e -> {
            pane.getChildren().remove(bubble);
            for (int i = 0; i < (int)(Math.random() * 3) + 6; i++) {
                Circle mini = new Circle(bubble.getRadius() / 4); 
                mini.setFill(Color.TRANSPARENT);
                mini.setStroke(Color.TRANSPARENT);
                
                mini.getProperties().put("baseOpacity", 0.4);
                mini.setLayoutX(bubble.getLayoutX()); mini.setLayoutY(bubble.getLayoutY());
                mini.getProperties().put("dx", (Math.random() - 0.5) * 8); mini.getProperties().put("dy", (Math.random() - 0.5) * 8); mini.getProperties().put("life", 1.0); 
                pane.getChildren().add(mini);
            }
            pane.getChildren().add(createNormalBubble(pane));
        });
        return bubble;
    }

    private HBox createFeatureLabel(String icon, String title, String description) {
        StackPane iconCircle = new StackPane(); iconCircle.setPrefSize(40, 40); iconCircle.setMinSize(40, 40);
        iconCircle.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 20;"); 
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-family: 'Segoe UI Emoji';"); iconCircle.getChildren().add(iconLbl);
        VBox textBox = new VBox(0); textBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title); titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI';");
        Label descLbl = new Label(description); descLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12; -fx-font-family: 'Segoe UI Light';");
        textBox.getChildren().addAll(titleLbl, descLbl);
        HBox card = new HBox(15, iconCircle, textBox); card.setAlignment(Pos.CENTER_LEFT); return card;
    }

    private Button createAccountTypeButton(String icon, String text, String colorFuerte, String colorClaro) {
        VBox box = new VBox(8); box.setAlignment(Pos.CENTER);
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size: 32; -fx-text-fill: " + colorFuerte + ";");
        Label textLbl = new Label(text); textLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B263B; -fx-font-size: 15; -fx-font-family: 'Segoe UI';");
        Label subLbl = new Label("Entrar"); subLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-background-color: #f1f5f9; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-family: 'Segoe UI';");
        
        box.getChildren().addAll(iconLbl, textLbl, subLbl);
        Button btn = new Button(); btn.setGraphic(box); btn.setPrefSize(180, 140);
        
        String defaultStyle = "-fx-background-color: white; -fx-border-color: " + colorFuerte + "; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + colorClaro + "; -fx-border-color: " + colorFuerte + "; -fx-border-width: 1.5; -fx-border-radius: 16; -fx-background-radius: 16; -fx-cursor: hand;";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        return btn;
    }
}