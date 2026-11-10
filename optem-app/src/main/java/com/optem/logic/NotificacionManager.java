package com.optem.logic;

import java.awt.Toolkit;
import java.util.prefs.Preferences;

import com.optem.ui.DashboardView;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificacionManager {

    public static void mostrarNotificacion(String titulo, String mensaje, String tipo) {
        Platform.runLater(() -> {
            Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
            String correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
            String nivel = prefs.get("PREF_NOTIFICATIONS_" + correoActual, "COMPLETO");

            if (nivel.equals("APAGADO")) return;
            if (nivel.equals("PRIORITARIO") && !tipo.equals("URGENTE") && !tipo.equals("REUNION")) return;

            Toolkit.getDefaultToolkit().beep();

            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            VBox toastLayout = new VBox(5);
            toastLayout.setPadding(new Insets(15, 20, 15, 20));
            toastLayout.setStyle("-fx-background-color: rgba(30, 41, 59, 0.95); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10;");

            Label lblTitulo = new Label(titulo);
            lblTitulo.setStyle("-fx-text-fill: #E9EDC9; -fx-font-weight: bold; -fx-font-size: 15px;");
            
            Label lblMensaje = new Label(mensaje);
            lblMensaje.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-wrap-text: true;");

            toastLayout.getChildren().addAll(lblTitulo, lblMensaje);

            Scene scene = new Scene(toastLayout);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            toastStage.show();
            toastStage.setX(bounds.getMaxX() - toastStage.getWidth() - 20);
            
            toastStage.setY(bounds.getMaxY());
            FadeTransition ftIn = new FadeTransition(Duration.millis(300), toastLayout);
            ftIn.setFromValue(0); ftIn.setToValue(1); ftIn.play();
            
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(300), toastLayout);
            tt.setFromY(50); tt.setToY(-20 - toastStage.getHeight()); tt.play();

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {
                FadeTransition ftOut = new FadeTransition(Duration.millis(300), toastLayout);
                ftOut.setFromValue(1); ftOut.setToValue(0);
                ftOut.setOnFinished(ev -> toastStage.close());
                ftOut.play();
            });
            delay.play();
        });
    }
}