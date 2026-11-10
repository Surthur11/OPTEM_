package com.optem.controllers;

import java.util.prefs.Preferences;
 
import com.optem.database.MockDB;
import com.optem.ui.DashboardView;

import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginController {

    public boolean checkAutoLogin(Stage stage) {
        Preferences prefsDirect = Preferences.userNodeForPackage(DashboardView.class);
        
        if (prefsDirect.get("ACCOUNTS_LIST", "").isEmpty()) {
            try {
                prefsDirect.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String lastActive = prefsDirect.get("LAST_ACTIVE_ACCOUNT", "");
        if (!lastActive.isEmpty()) {
            try{com.optem.logic.XPService.registrarAcceso(lastActive);}
            catch(RuntimeException ex){System.err.println("No se pudo actualizar la racha: "+ex.getMessage());}
            try{prefsDirect.put("PREF_NOTIFICATIONS_"+lastActive,com.optem.database.NotificacionesDAO.leer(lastActive));}
            catch(RuntimeException ex){System.err.println("No se cargaron notificaciones: "+ex.getMessage());}
            new DashboardView().show(stage, lastActive, "");
            return true; 
        }
        return false;
    }

    public void handleLogin(String nombre, String apellido, String correo, String password, String rol, Label lblError, Stage stage) {
        
        if (correo.isEmpty()) { 
            lblError.setText("Por favor ingresa tu correo institucional."); 
            lblError.setVisible(true); 
            return; 
        }

        if (!rol.equals("ADMINISTRADOR") && nombre.isEmpty()) { 
            lblError.setText("Por favor ingresa tu nombre."); 
            lblError.setVisible(true); 
            return; 
        }
        
        if (!password.matches("\\d{7}")) { 
            lblError.setText("Cuenta incorrecta. Debe contener exactamente 7 números."); 
            lblError.setVisible(true); 
            return; 
        }
        
        if (!correo.contains("@")) { 
            if (correo.matches("\\d+")) {
                correo += "@alumno.uaemex.mx"; 
            } else {
                correo += "@uaemex.mx"; 
            }
        }

        boolean accesoConcedido;
        try { accesoConcedido = MockDB.autenticar(correo, password, rol); }
        catch (RuntimeException ex) { lblError.setText(ex.getMessage()); lblError.setVisible(true); return; }

        if (accesoConcedido) {
            lblError.setVisible(false);

            Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
            
            String nombreFinal = nombre.isEmpty() ? "Administrador" : nombre;
            String nombreCompleto = nombreFinal + (apellido.isEmpty() ? "" : " " + apellido.trim());
            
            prefs.put("NAME_" + correo, nombreCompleto);
            prefs.put("ROLE_" + correo, rol); 
            prefs.put("LAST_ACTIVE_ACCOUNT", correo);

            try{com.optem.logic.XPService.registrarAcceso(correo);}
            catch(RuntimeException ex){System.err.println("No se pudo actualizar la racha: "+ex.getMessage());}
            try{prefs.put("PREF_NOTIFICATIONS_"+correo,com.optem.database.NotificacionesDAO.leer(correo));}
            catch(RuntimeException ex){System.err.println("No se cargaron notificaciones: "+ex.getMessage());}
            DashboardView dashboard = new DashboardView();
            dashboard.show(stage, correo, "");
            
        } else {
            lblError.setText("❌ Correo o número de cuenta incorrectos."); 
            lblError.setVisible(true); 
        }
    }
}