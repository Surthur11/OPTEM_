package com.optem.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.optem.logic.ActividadManager;
import com.optem.models.Actividad;

public class AgendaController {

    private final ActividadManager manager = ActividadManager.getInstance();
    
    private final String[] paletaMaterias = {"#E57373", "#81C784", "#64B5F6", "#FFD54F", "#BA68C8", "#4DB6AC", "#FF8A65"};
    private final Map<String, String> mapaColores = new HashMap<>();
    private int colorIndex = 0;

    public String getColorMateria(String materia) {
        if (materia == null || materia.trim().isEmpty()) return "#9E9E9E";
        if (!mapaColores.containsKey(materia)) {
            mapaColores.put(materia, paletaMaterias[colorIndex % paletaMaterias.length]);
            colorIndex++;
        }
        return mapaColores.get(materia);
    }

    public List<Actividad> getActividadesDelDia(LocalDate date, String filtroActual) {
        List<Actividad> actividades = manager.getActividadesDelDia(date);
        if (actividades == null) return new ArrayList<>();
        
        for(Actividad act : actividades) {
            if(isActividadCaducada(act) && !"COMPLETADO".equals(act.getEstado())) {
                manager.actualizarEstadoActividad(act.getId(), "COMPLETADO");
                act.setEstado("COMPLETADO");
            }
        }

        if (!"Todas".equals(filtroActual)) {
            return actividades.stream().filter(a -> {
                if ("Tareas".equals(filtroActual)) return "Tarea".equalsIgnoreCase(a.getTipo());
                if ("Actividades".equals(filtroActual)) return !"Tarea".equalsIgnoreCase(a.getTipo());
                if ("Pendientes".equals(filtroActual)) return !"COMPLETADO".equals(a.getEstado());
                return true;
            }).collect(Collectors.toList());
        }

        return actividades;
    }

    public Actividad buscarActividadPorId(String id) {
        for (Actividad a : manager.getAllActividades()) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    public int[] getProgresoSemanal(LocalDate inicioSemana) {
        int total = 0;
        int completadas = 0;
        for (int i = 0; i < 7; i++) {
            List<Actividad> acts = manager.getActividadesDelDia(inicioSemana.plusDays(i));
            if (acts != null) {
                for(Actividad a : acts) {
                    if("COMPLETADO".equals(a.getEstado()) && !isActividadCaducada(a)) completadas++;
                    total++;
                }
            }
        }
        return new int[]{completadas, total};
    }

    public LocalDate navegarFecha(LocalDate currentDate, String mode, int val) {
        if (mode.equals("DAY")) return currentDate.plusDays(val);
        if (mode.equals("WEEK")) return currentDate.plusWeeks(val);
        return currentDate.plusMonths(val);
    }

    public boolean isActividadCaducada(Actividad tarea) {
        if (tarea.getFecha() == null || tarea.getHoraVencimiento() == null) return false;
        try {
            LocalDateTime fechaLimite = LocalDateTime.of(tarea.getFecha(), tarea.getHoraVencimiento());
            return LocalDateTime.now().isAfter(fechaLimite);
        } catch (Exception e) { return false; }
    }
}