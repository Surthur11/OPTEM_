package com.optem.ui;

import com.optem.database.GrupoMuroDAO;
import com.optem.database.MockDB;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.util.*;
import java.util.prefs.Preferences;

/** Ventana General compartida por alumnos y docentes; conserva estética de tarjetas del original. */
public final class GrupoMuroView {
    private GrupoMuroView(){}
    public static void abrir(Window owner,int grupo,String titulo){
        Preferences pref=Preferences.userNodeForPackage(DashboardView.class);
        String correo=MockDB.sesion();
        boolean oscuro=pref.getBoolean("PREF_DARK_MODE_"+correo,false);
        String acento=pref.get("PREF_ACCENT_COLOR_"+correo,"#3D5A4D");
        String fondo=oscuro?"#1e1e1e":"#ffffff";
        String base=oscuro?"#121212":"#f1f5f9";
        String texto=oscuro?"#f8f9fa":"#1e293b";
        Stage popup=new Stage();popup.initOwner(owner);popup.initStyle(StageStyle.UNDECORATED);
        VBox raiz=new VBox(12);
        raiz.setStyle("-fx-background-color: "+fondo+"; -fx-border-color: "+acento+"; -fx-border-width: 2;");
        HBox cabecera=new HBox(15);cabecera.setAlignment(Pos.CENTER_LEFT);cabecera.setPadding(new Insets(15));
        cabecera.setStyle("-fx-background-color: "+acento+";");
        Label tituloGrupo=new Label("💬 General · "+titulo);tituloGrupo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
        Button cerrar=new Button("✕");cerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");cerrar.setOnAction(e->popup.close());
        cabecera.getChildren().addAll(tituloGrupo,spacer,cerrar);
        VBox publicaciones=new VBox(10);publicaciones.setPadding(new Insets(16));
        ScrollPane scroll=new ScrollPane(publicaciones);scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll,Priority.ALWAYS);
        Runnable refrescar=()->{
            publicaciones.getChildren().clear();
            try{
                boolean esDocente=GrupoMuroDAO.profesor(grupo);
                for(GrupoMuroDAO.Publicacion p:GrupoMuroDAO.publicaciones(grupo)){
                    VBox tarjeta=new VBox(7);tarjeta.setPadding(new Insets(12));
                    tarjeta.setStyle("-fx-background-color: "+base+"; -fx-background-radius: 8;");
                    Label encabezado=new Label((p.prioritario()?"❗ PRIORITARIO · ":"")+p.autor()+" · "+p.fecha());
                    encabezado.setStyle("-fx-font-weight: bold; -fx-text-fill: "+(p.prioritario()?"#dc2626":texto)+";");
                    Label cuerpo=new Label(p.contenido());cuerpo.setWrapText(true);cuerpo.setStyle("-fx-text-fill: "+texto+";");
                    tarjeta.getChildren().addAll(encabezado,cuerpo);
                    if(p.correo().equalsIgnoreCase(correo)||esDocente){
                        Button borrar=new Button("🗑 Eliminar");
                        borrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-cursor: hand;");
                        borrar.setOnAction(e->{
                            Alert confirmar=new Alert(Alert.AlertType.CONFIRMATION,"¿Eliminar esta publicación?",ButtonType.YES,ButtonType.NO);
                            if(confirmar.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
                            try{GrupoMuroDAO.borrar(p.id());tarjeta.getChildren().clear();publicaciones.getChildren().remove(tarjeta);}
                            catch(Exception ex){error(ex.getMessage());}
                        });
                        tarjeta.getChildren().add(borrar);
                    }
                    publicaciones.getChildren().add(tarjeta);
                }
                GrupoMuroDAO.marcarLeidos(grupo);
            }catch(Exception ex){error(ex.getMessage());}
        };
        HBox acciones=new HBox(10);acciones.setPadding(new Insets(15));
        TextField mensaje=new TextField();mensaje.setPromptText("Escribe un mensaje para General...");
        mensaje.setStyle("-fx-background-color: "+base+"; -fx-text-fill: "+texto+";");HBox.setHgrow(mensaje,Priority.ALWAYS);
        Button enviar=new Button("Publicar");enviar.setStyle("-fx-background-color: "+acento+"; -fx-text-fill: white; -fx-font-weight: bold;");
        Button actualizar=new Button("↻");actualizar.setStyle(enviar.getStyle());actualizar.setOnAction(e->refrescar.run());
        enviar.setOnAction(e->{
            boolean prioritario=false;
            try{
                if(GrupoMuroDAO.profesor(grupo)){
                    ChoiceDialog<String> elegir=new ChoiceDialog<>("Publicar", "Publicar","Marcar como prioritario");
                    elegir.setTitle("Tipo de publicación");elegir.setHeaderText("¿Cómo deseas publicar el mensaje?");
                    var seleccionado=elegir.showAndWait();if(seleccionado.isEmpty())return;
                    prioritario="Marcar como prioritario".equals(seleccionado.get());
                }
                GrupoMuroDAO.publicar(grupo,mensaje.getText(),prioritario);
                mensaje.clear();refrescar.run();scroll.setVvalue(1.0);
            }catch(Exception ex){error(ex.getMessage());}
        });
        acciones.getChildren().addAll(mensaje,actualizar,enviar);
        raiz.getChildren().addAll(cabecera,scroll,acciones);
        Scene escena=new Scene(raiz,680,560);popup.setScene(escena);
        refrescar.run();popup.show();
        if(owner!=null){popup.setX(owner.getX()+(owner.getWidth()-680)/2);popup.setY(owner.getY()+(owner.getHeight()-560)/2);}
    }
    private static void error(String mensaje){new Alert(Alert.AlertType.ERROR,mensaje,ButtonType.OK).showAndWait();}
}
