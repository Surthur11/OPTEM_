package com.optem.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.database.MockDB;
import com.optem.logic.ActividadManager;
import com.optem.ui.DashboardView;

public class DashboardController {

    private final Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
    private String correoActual;

    public void inicializarUsuario(String correoUsuario) {
        this.correoActual = correoUsuario;
        ActividadManager.getInstance().setUsuarioActual(correoUsuario);
        prefs.put("LAST_ACTIVE_ACCOUNT", correoUsuario);
        String listaCuentas = prefs.get("ACCOUNTS_LIST", "");
        if (!listaCuentas.contains(correoUsuario)) {
            prefs.put("ACCOUNTS_LIST", listaCuentas.isEmpty() ? correoUsuario : listaCuentas + "," + correoUsuario);
        }
    }

    public Map<String, String> getPerfilUsuario() { return MockDB.obtenerPerfilUsuario(); }
    
    // AQUÍ SE CORRIGIÓ EL NOMBRE DEL MÉTODO Y SE PASÓ EL CORREO
    public List<Map<String,String>> getClasesRegistradas() { return MockDB.obtenerMateriasDelAlumno(correoActual); }

    public String getNombreEstudiante() {
        if (getRolUsuario().equals("ADMINISTRADOR")) return "Administrador Principal";
        return MockDB.obtenerPerfilUsuario().get("nombre_completo");
    }

    public String getRolUsuario() { return prefs.get("ROLE_" + correoActual, "ESTUDIANTE"); }
    public String getFotoPerfil() { return new AjustesController(correoActual).getRutaFotoPerfil(); }
    public String getColorFuerte() { return prefs.get("PREF_ACCENT_COLOR_" + correoActual, "#3D5A4D"); }
    public String getColorClaro() { return prefs.get("PREF_ACCENT_LIGHT_" + correoActual, "#E9EDC9"); }
    public boolean isAnimacionesActivas() { return prefs.getBoolean("PREF_ANIMATIONS_" + correoActual, true); }
    public String getTamanoFuente() { return prefs.get("PREF_FONT_SIZE_" + correoActual, "Normal"); }
    public boolean isTecladoActivo() { return prefs.getBoolean("PREF_ACC_KEYBOARD_" + correoActual, false); }
    public boolean isVozActivo() { return prefs.getBoolean("PREF_ACC_VOICE_" + correoActual, false); }
    public String getNivelNotificaciones() { return new AjustesController(correoActual).getNivelNotificaciones(); }

    public void cerrarSesion() {
        prefs.remove("LAST_ACTIVE_ACCOUNT");
    }

    public List<Map<String,String>> getUsuariosDisponibles() {
        List<Map<String,String>> list = new ArrayList<>();
        String saved = prefs.get("ACCOUNTS_LIST", "");
        if (saved.isEmpty()) return list;
        
        for (String c : saved.split(",")) {
            Map<String, String> m = new HashMap<>();
            m.put("correo", c);
            m.put("rol", prefs.get("ROLE_" + c, "ESTUDIANTE"));
            m.put("nombre", m.get("rol").equals("ADMINISTRADOR") ? "Admin System" : "Usuario " + c.split("@")[0]);
            m.put("foto", new AjustesController(c).getRutaFotoPerfil());
            list.add(m);
        }
        return list;
    }

    public void removerCuenta(String correo) {
        String saved = prefs.get("ACCOUNTS_LIST", "");
        if (saved.isEmpty()) return;
        List<String> valid = new ArrayList<>();
        for (String c : saved.split(",")) if (!c.equals(correo)) valid.add(c);
        prefs.put("ACCOUNTS_LIST", String.join(",", valid));
        if (correoActual != null && correoActual.equals(correo)) prefs.remove("LAST_ACTIVE_ACCOUNT");
    }
}