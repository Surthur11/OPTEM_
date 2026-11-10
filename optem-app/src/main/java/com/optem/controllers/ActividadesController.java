package com.optem.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.optem.logic.ActividadManager;
import com.optem.models.Actividad;

public class ActividadesController {

    private final ActividadManager manager = ActividadManager.getInstance();

    public List<Actividad> getActividadesFiltradas(String categoria, String busqueda, String materia) {
        return manager.getAllActividades().stream()
            .filter(act -> !"Tarea".equalsIgnoreCase(act.getTipo()))
            .filter(act -> categoria == null || categoria.equals("Todas") || act.getTipo().equals(categoria))
            .filter(act -> busqueda == null || busqueda.isEmpty() || act.getNombre().toLowerCase().contains(busqueda.toLowerCase()))
            .filter(act -> materia == null || materia.equals("Todas") || act.getMateria().equals("General") || act.getMateria().equals(materia))
            .collect(Collectors.toList());
    }

    public List<Actividad> getActividadesPorFecha(LocalDate fecha) {
        List<Actividad> todas = manager.getActividadesDelDia(fecha);
        if (todas == null) return new ArrayList<>();
        
        return todas.stream()
            .filter(act -> !"Tarea".equalsIgnoreCase(act.getTipo()))
            .collect(Collectors.toList());
    }

    public boolean guardarNuevaActividad(String titulo, String tipo, String descripcion, LocalDate fecha, LocalTime hora) {
        if (titulo == null || titulo.trim().isEmpty() || tipo == null) return false;
        String id = String.valueOf(System.currentTimeMillis()); 
        Actividad nuevaAct = new Actividad(id, titulo, tipo, descripcion, fecha, hora, "General");
        manager.agregarActividad(nuevaAct);
        return true;
    }

    public boolean actualizarActividad(String id, String titulo, String tipo, String descripcion, LocalDate fecha, LocalTime hora) {
        if (titulo == null || titulo.trim().isEmpty() || tipo == null) return false;
        
        for (Actividad act : manager.getAllActividades()) {
            if (act.getId().equals(id)) {
                act.setNombre(titulo);
                act.setTipo(tipo);
                act.setDescripcion(descripcion);
                act.setFecha(fecha);
                act.setHoraVencimiento(hora);
                manager.actualizarActividad(act);
                return true;
            }
        }
        return false;
    }

    public void marcarComoEntregado(Actividad act) {
        manager.actualizarEstadoActividad(act.getId(), "COMPLETADO");
        act.setEstado("COMPLETADO");
    }

    public boolean eliminarActividad(Actividad act) {
        manager.eliminarActividad(act.getId());
        return true;
    }
}