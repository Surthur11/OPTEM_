package com.optem.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.database.MockDB;
import com.optem.ui.DashboardView;

public class ReunionesController {

    private final List<UsuarioDirectorio> directorio = new ArrayList<>();
    private final String correoActual;

    public ReunionesController() {
        Preferences p = Preferences.userNodeForPackage(DashboardView.class);
        this.correoActual = p.get("LAST_ACTIVE_ACCOUNT", "default");

        directorio.add(new UsuarioDirectorio("2021001", "Marcela Camacho Avila", "M. EN CCO.", "marcela@uaemex.mx", "Conectado"));
        directorio.add(new UsuarioDirectorio("2021002", "Jose Esteban Ruiz Melo", "M. EN ADM.", "esteban@uaemex.mx", "Ocupado"));
        directorio.add(new UsuarioDirectorio("2021003", "Griselda Areli Matias", "DR. EN C.", "griselda@uaemex.mx", "Ausente"));
        directorio.add(new UsuarioDirectorio("2021004", "Daniela Sanchez", "Estudiante", "daniela@alumno.uaemex.mx", "Conectado"));
        directorio.add(new UsuarioDirectorio("2021005", "Alexis Hernandez", "Estudiante", "alexis@alumno.uaemex.mx", "Conectado"));
        directorio.add(new UsuarioDirectorio("2021006", "Mauricio Gomez", "Estudiante", "mauricio@alumno.uaemex.mx", "Desconectado"));
    }

    public List<Map<String,String>> getGruposParaLlamada() {
        return MockDB.obtenerMateriasDelAlumno(correoActual);
    }

    public List<UsuarioDirectorio> buscarUsuarios(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(directorio);
        }
        List<UsuarioDirectorio> filtrados = new ArrayList<>();
        String q = query.toLowerCase();
        for (UsuarioDirectorio u : directorio) {
            if (u.nombre.toLowerCase().contains(q) || u.correo.toLowerCase().contains(q) || u.matricula.contains(q)) {
                filtrados.add(u);
            }
        }
        return filtrados;
    }

    public static class UsuarioDirectorio {
        public String matricula, nombre, rol, correo, estado;
        public UsuarioDirectorio(String m, String n, String r, String c, String e) {
            this.matricula = m; this.nombre = n; this.rol = r; this.correo = c; this.estado = e;
        }
    }
}