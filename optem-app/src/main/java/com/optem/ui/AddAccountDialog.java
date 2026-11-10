package com.optem.ui;

import com.optem.controllers.AddAccountController;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AddAccountDialog {

    private UserData result = null;
    
    private final AddAccountController controller = new AddAccountController();

    public static class UserData {
        public String user;
        public String pass;
        public String nombre;
        public String apellido;

        public UserData(String user, String pass, String nombre, String apellido) {
            this.user = user;
            this.pass = pass;
            this.nombre = nombre;
            this.apellido = apellido;
        }
    }

    public UserData showAndWait(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.TRANSPARENT); 

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        
        root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                      "-fx-background-radius: 20; " +
                      "-fx-border-color: rgba(0,0,0,0.06); " +
                      "-fx-border-width: 1.2; " +
                      "-fx-border-radius: 20;");
        
        root.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.1)));

        Label lblTitle = new Label("Vincular Cuenta");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #3D5A4D; -fx-font-family: 'Segoe UI';");
        
        Button btnCerrar = new Button("✕");
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 16; -fx-cursor: hand; -fx-font-weight: bold;");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox header = new HBox(lblTitle, new Region(), btnCerrar);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre(s)");
        txtNombre.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-family: 'Segoe UI';");

        TextField txtApellido = new TextField();
        txtApellido.setPromptText("Apellidos (Opcional)");
        txtApellido.setStyle(txtNombre.getStyle());

        TextField txtUser = new TextField();
        txtUser.setPromptText("Correo institucional");
        txtUser.setStyle(txtNombre.getStyle());

        ContextMenu domainMenu = new ContextMenu();
        domainMenu.getStyleClass().add("menu-dominio-oro"); 
        
        MenuItem optAlumno = new MenuItem("@alumno.uaemex.mx");
        MenuItem optUaemex = new MenuItem("@uaemex.mx");
        domainMenu.getItems().addAll(optAlumno, optUaemex);

        txtUser.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.endsWith("@")) {
                double x = txtUser.localToScreen(txtUser.getBoundsInLocal()).getMinX();
                double y = txtUser.localToScreen(txtUser.getBoundsInLocal()).getMaxY();
                domainMenu.show(txtUser, x, y);
            } else if (!newVal.contains("@")) {
                domainMenu.hide();
            }
        });

        optAlumno.setOnAction(e -> { txtUser.setText(txtUser.getText() + "alumno.uaemex.mx"); Platform.runLater(txtUser::end); });
        optUaemex.setOnAction(e -> { txtUser.setText(txtUser.getText() + "uaemex.mx"); Platform.runLater(txtUser::end); });

        StackPane passContainer = new StackPane();
        PasswordField txtPassHidden = new PasswordField();
        txtPassHidden.setPromptText("Número de cuenta");
        txtPassHidden.setStyle(txtNombre.getStyle() + " -fx-padding-right: 40;");

        TextField txtPassShown = new TextField();
        txtPassShown.setPromptText("Número de cuenta");
        txtPassShown.setStyle(txtPassHidden.getStyle());
        txtPassShown.setVisible(false);

        Button btnTogglePass = new Button("👁");
        btnTogglePass.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-cursor: hand; -fx-font-size: 14;");
        StackPane.setAlignment(btnTogglePass, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnTogglePass, new Insets(0, 8, 0, 0));

        btnTogglePass.setOnAction(e -> {
            if (txtPassHidden.isVisible()) {
                txtPassShown.setText(txtPassHidden.getText());
                txtPassHidden.setVisible(false); txtPassShown.setVisible(true);
            } else {
                txtPassHidden.setText(txtPassShown.getText());
                txtPassShown.setVisible(false); txtPassHidden.setVisible(true);
            }
        });
        passContainer.getChildren().addAll(txtPassHidden, txtPassShown, btnTogglePass);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        lblError.setVisible(false);

        Button btnAceptar = new Button("Vincular");
        btnAceptar.setMaxWidth(Double.MAX_VALUE);
        btnAceptar.setStyle("-fx-background-color: #3D5A4D; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        
        btnAceptar.setOnAction(e -> {
            String password = txtPassHidden.isVisible() ? txtPassHidden.getText() : txtPassShown.getText();
            String usuario = txtUser.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();

            result = controller.procesarVinculacion(usuario, password, nombre, apellido, lblError);
            
            if (result != null) {
                dialog.close();
            }
        });

        root.getChildren().addAll(header, txtNombre, txtApellido, txtUser, passContainer, lblError, btnAceptar);
        
        Scene scene = new Scene(root, 360, 380);
        scene.setFill(Color.TRANSPARENT); 
        
        scene.getStylesheets().add("data:text/css," +
            ".menu-dominio-oro { -fx-background-color: rgba(255, 255, 255, 0.98); -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8; }" +
            ".menu-dominio-oro .menu-item { -fx-padding: 8 15 8 12; -fx-cursor: hand; }" +
            ".menu-dominio-oro .menu-item .label { -fx-text-fill: #444; -fx-font-size: 13; -fx-font-family: 'Segoe UI'; }" +
            ".menu-dominio-oro .menu-item:focused { -fx-background-color: #B38E5D; -fx-background-radius: 6; }" + 
            ".menu-dominio-oro .menu-item:focused .label { -fx-text-fill: white; -fx-font-weight: bold; }"
        );

        dialog.setScene(scene);
        dialog.showAndWait();

        return result;
    }
}