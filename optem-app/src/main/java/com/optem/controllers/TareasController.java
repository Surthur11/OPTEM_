package com.optem.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.optem.database.MockDB;
import com.optem.logic.ActividadManager;
import com.optem.models.Actividad;
import com.optem.ui.DashboardView;

public class TareasController {
    
    private final ActividadManager manager = ActividadManager.getInstance();
    private final String correoActual;

    public TareasController() {
        Preferences p = Preferences.userNodeForPackage(DashboardView.class);
        this.correoActual = p.get("LAST_ACTIVE_ACCOUNT", "default");
    }

    public List<String> getNombresMaterias() {
        List<String> nombres = new ArrayList<>();
        try {
            List<Map<String,String>> clases = MockDB.obtenerMateriasDelAlumno(correoActual);
            if (clases != null) {
                for (Map<String, String> c : clases) {
                    nombres.add(c.get("nombre"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener materias: " + e.getMessage());
        }
        
        nombres.add("Personal");
        nombres.add("Trámite Escolar");
        nombres.add("Otro");
        return nombres;
    }

    public void crearNuevaTarea(String titulo, String materia, String descripcion, LocalDate fecha, LocalTime hora) {
        String id = String.valueOf(System.currentTimeMillis());
        Actividad nuevaTarea = new Actividad(id, titulo, "Tarea", descripcion, fecha, hora, materia);
        nuevaTarea.setEstado("PENDIENTE"); 
        manager.agregarActividad(nuevaTarea);
    }

    public List<Actividad> getTareasPorEstado(String estado) {
        return manager.getAllActividades().stream()
            .filter(act -> "Tarea".equalsIgnoreCase(act.getTipo()))
            .filter(act -> {
                return estado.equalsIgnoreCase(act.getEstado());
            })
            .collect(Collectors.toList());
    }

    public void moverTarea(Actividad tarea, String estadoDestino) {
        manager.actualizarEstadoActividad(tarea.getId(), estadoDestino);
        tarea.setEstado(estadoDestino);
    }

    public boolean isTareaCaducada(Actividad tarea) {
        if (tarea.getFecha() == null || tarea.getHoraVencimiento() == null) return false;
        try {
            LocalDateTime limite = LocalDateTime.of(tarea.getFecha(), tarea.getHoraVencimiento());
            return LocalDateTime.now().isAfter(limite);
        } catch (Exception e) {
            return false;
        }
    }
}