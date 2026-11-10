package com.optem.controllers;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.database.MockDB;
import com.optem.ui.DashboardView;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class AjustesController {

    private final Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
    private final String correoUsuario;

    public AjustesController(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public Map<String, String> getPerfilDesdeMock() { return MockDB.obtenerPerfilUsuario(); }

    public String getRutaFotoPerfil() {
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();
            java.sql.PreparedStatement st=c.prepareStatement("SELECT foto_perfil,foto_mime FROM PREFERENCIAS_USUARIO WHERE correo_usuario=?")){
            st.setString(1,correoUsuario);
            try(java.sql.ResultSet r=st.executeQuery()){
                if(r.next()){
                    byte[] img=r.getBytes(1);
                    if(img!=null && img.length>0){
                        String suf="image/png".equals(r.getString(2))?".png":".jpg";
                        java.nio.file.Path file=java.nio.file.Files.createTempFile("optem_foto_",suf);
                        java.nio.file.Files.write(file,img);file.toFile().deleteOnExit();return file.toAbsolutePath().toString();
                    }
                }
            }
        }catch(Exception ex){throw new IllegalStateException("No fue posible cargar la fotografía de MySQL: "+ex.getMessage(),ex);}
        return "";
    }
    public void guardarRutaFotoPerfil(String path) {
        try{
            java.nio.file.Path archivo=java.nio.file.Path.of(path);
            if(java.nio.file.Files.size(archivo)>6_000_000)throw new IllegalArgumentException("La fotografía debe pesar menos de 6MB");
            byte[] contenido=java.nio.file.Files.readAllBytes(archivo);
            String mime=java.nio.file.Files.probeContentType(archivo);
            if(mime==null || !mime.startsWith("image/"))throw new IllegalArgumentException("Selecciona una imagen válida");
            String sql="INSERT INTO PREFERENCIAS_USUARIO(correo_usuario,foto_perfil,foto_mime) VALUES (?,?,?) ON DUPLICATE KEY UPDATE foto_perfil=VALUES(foto_perfil),foto_mime=VALUES(foto_mime)";
            try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement p=c.prepareStatement(sql)){
                p.setString(1,correoUsuario);p.setBytes(2,contenido);p.setString(3,mime);p.executeUpdate();
            }
        }catch(Exception ex){throw new IllegalStateException("No se guardó la fotografía: "+ex.getMessage(),ex);}
    }

    public Image recortarImagenCuadrada(Image original) {
        if (original == null || original.isError()) return original;
        int w = (int) original.getWidth(); int h = (int) original.getHeight(); int size = Math.min(w, h);
        return new WritableImage(original.getPixelReader(), (w - size) / 2, (h - size) / 2, size, size);
    }

    public String getNivelNotificaciones(){
        return com.optem.database.NotificacionesDAO.leer(correoUsuario);
    }
    public void guardarNivelNotificaciones(String modo){
        com.optem.database.NotificacionesDAO.guardar(correoUsuario,modo);
        prefs.put("PREF_NOTIFICATIONS_"+correoUsuario,modo);
    }
    public String getColorFuerte() { return prefs.get("PREF_ACCENT_COLOR_" + correoUsuario, "#3D5A4D"); }
    public void guardarColoresAcento(Color c) {
        String hexFuerte = String.format("#%02X%02X%02X", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
        prefs.put("PREF_ACCENT_COLOR_" + correoUsuario, hexFuerte);
    }

    public String getPrefString(String key, String def) { return prefs.get(key + "_" + correoUsuario, def); }
    public void setPrefString(String key, String val) { prefs.put(key + "_" + correoUsuario, val); }
    public boolean getPrefBoolean(String prefKey, boolean defaultValue) { return prefs.getBoolean(prefKey + "_" + correoUsuario, defaultValue); }
    
    public boolean alternarPrefBoolean(String prefKey, boolean defaultValue) {
        boolean newState = !getPrefBoolean(prefKey, defaultValue);
        prefs.putBoolean(prefKey + "_" + correoUsuario, newState);
        return newState;
    }

    public void moverCursorMouse(int dx, int dy) {
        try {
            Robot robot = new Robot();
            Point p = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x + dx, p.y + dy);
        } catch (Exception e) {}
    }

    public void scrollMouse(int cantidad) {
        try {
            new Robot().mouseWheel(cantidad);
        } catch (Exception e) {}
    }

    public void clickIzquierdoMouse() {
        try {
            Robot robot = new Robot();
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (Exception e) {}
    }
}