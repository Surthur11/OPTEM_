package com.optem.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Actividad implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaVencimiento;
    private String estado; 
    private String materia;

    public Actividad(String id, String nombre, String tipo, String descripcion, LocalDate fecha, LocalTime horaVencimiento, String materia) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.horaVencimiento = horaVencimiento;
        this.materia = materia;
        this.estado = "PENDIENTE"; 
    }

    public String getId() { return id; }
    public void setId(String id) { this.id=id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraVencimiento() { return horaVencimiento; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMateria() { return materia; }

    public boolean isHecho() {
        return "COMPLETADO".equals(estado);
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setHoraVencimiento(LocalTime horaVencimiento) {
        this.horaVencimiento = horaVencimiento;
    }
}