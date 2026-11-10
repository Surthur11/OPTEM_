package com.optem.controllers;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.optem.database.MockDB;
import com.optem.ui.DashboardView;

public class LogrosController {

    private String correoUsuarioActual = "";
    private final TareasMaestroController tareasController = new TareasMaestroController();

    public LogrosController() {
    }

    public LogrosController(String correo) {
        this.correoUsuarioActual = correo;
    }

    // --- LÓGICA DE XP REAL (ALUMNOS - EXCLUSIVO DE TAREAS) ---
    public int getXPTotalAlumno() {
        if (correoUsuarioActual.isEmpty()) return 0;
        return com.optem.logic.XPService.xp(correoUsuarioActual);
    }

    // NUEVO: Calcula el total de XP posible basado en las tareas que realmente le han asignado
    public int calcularXPMetaTotal() {
        List<Map<String,String>> misClases = MockDB.obtenerMateriasDelAlumno(correoUsuarioActual);
        if (misClases == null || misClases.isEmpty()) return 50; // Mínimo de seguridad

        int totalTareasAsignadas = 0;
        List<Map<String,String>> todasLasTareas = tareasController.getTodasLasAsignaciones();
        
        for (Map<String, String> tarea : todasLasTareas) {
            String materiaTarea = tarea.get("materia").toUpperCase();
            for (Map<String, String> clase : misClases) {
                if (materiaTarea.contains(clase.get("nombre").toUpperCase())) {
                    totalTareasAsignadas++;
                    break;
                }
            }
        }
        
        return totalTareasAsignadas == 0 ? 10 : totalTareasAsignadas * 10;
    }

    public String obtenerNivelActualAlumno(int xpTotal) {
        // Intervalos ajustados al semestre escolar
        if (xpTotal >= 800) return "Diamante";   // Fin de semestre
        if (xpTotal >= 600) return "Experto";    // Segundo Parcial
        if (xpTotal >= 400) return "Avanzado";   // Mitad de semestre
        if (xpTotal >= 200) return "Intermedio"; // Primer Parcial
        return "Novato";                         // Inicio
    }

    // --- LÓGICA DE RACHAS (NO SE PIERDE SI NO HAY TAREAS) ---
    public int calcularDiasRachaAlumno() {
        if(correoUsuarioActual.isBlank())return 0;
        return com.optem.logic.XPService.racha(correoUsuarioActual); 
    }

    public double calcularBonoRacha(int diasRacha) {
        if (diasRacha >= 15) return 2.0;
        if (diasRacha >= 5) return 1.5;
        if (diasRacha >= 3) return 1.2;
        return 1.0; 
    }

    // --- LÓGICA DE MEDALLAS (ALUMNO) ---
    public boolean tieneMedallaPrimeraSangre() {
         return getXPTotalAlumno() >= 5; // Entregó su primera tarea
    }
    
    public boolean tieneMedallaImparable() {
        return getXPTotalAlumno() >= 30; // Entregó varias tareas
    }
    
    // NUEVA LÓGICA SOBREVIVIENTE: Se activa si la XP ganada es igual a la meta total (0 pendientes)
    public boolean tieneMedallaSobreviviente() {
        int totalPosible = calcularXPMetaTotal();
        int xpGanada = getXPTotalAlumno();
        return xpGanada > 0 && xpGanada >= totalPosible; 
    }

    // --- LÓGICA PARA MAESTROS (EXCLUSIVA DE TAREAS CALIFICADAS/CERRADAS) ---
    private long getTareasCerradasPorMaestro() {
        return tareasController.getTodasLasAsignaciones().stream()
                .filter(t -> t.get("estado").equals("CERRADA"))
                .count();
    }

    public int getNivelMaestro() {
        long cerradas = getTareasCerradasPorMaestro();
        int nivel = (int) (cerradas / 2) + 1;
        return Math.min(nivel, 5); 
    }

    public double getProgresoBonoMaestro() {
        long cerradas = getTareasCerradasPorMaestro();
        long modulo = cerradas % 2;
        return modulo == 0 ? 0.0 : 0.5; 
    }
    
    public int getXPAdicionalMaestro() {
         return (int) getTareasCerradasPorMaestro() * 100;
    }
}