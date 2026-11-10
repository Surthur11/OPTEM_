package com.optem.ui;
import com.optem.database.GruposDAO;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.List;
/** Diálogo de alumnos; usa los datos y operaciones reales de MySQL. */
public final class DialogoAlumnos {
  private DialogoAlumnos(){}
  public static void mostrar(int grupo,List<GruposDAO.Alumno> alumnos,Runnable actualizar){
    Dialog<Void> d=new Dialog<>();d.setTitle("Alumnos inscritos");d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    VBox body=new VBox(10);body.setPadding(new Insets(12));
    if(alumnos.isEmpty())body.getChildren().add(new Label("Este grupo todavía no tiene alumnos inscritos."));
    for(var a:alumnos){
      HBox line=new HBox(12);Label nombre=new Label(a.nombre()+" · "+a.cuenta());
      Button quitar=new Button("Quitar alumno");
      quitar.setOnAction(e->{
        Alert confirmar=new Alert(Alert.AlertType.CONFIRMATION,"¿Dar de baja a "+a.nombre()+" de este grupo?",ButtonType.YES,ButtonType.NO);
        if(confirmar.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
        try{GruposDAO.quitarAlumno(grupo,a.correo());body.getChildren().remove(line);actualizar.run();}
        catch(Exception ex){new Alert(Alert.AlertType.ERROR,ex.getMessage()).showAndWait();}
      });
      Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
      line.getChildren().addAll(nombre,spacer,quitar);body.getChildren().add(line);
    }
    ScrollPane scroll=new ScrollPane(body);scroll.setFitToWidth(true);scroll.setPrefViewportWidth(540);scroll.setPrefViewportHeight(360);
    d.getDialogPane().setContent(scroll);d.showAndWait();
  }
}
