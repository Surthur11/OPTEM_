package com.optem.controllers;
import com.optem.database.*;
import com.optem.logic.NotificacionManager;
import java.util.*;
public class GruposMaestroController {
    public String generarCodigoClase(){return UUID.randomUUID().toString().substring(0,6).toUpperCase();}
    public List<Map<String,String>> obtenerTodosLosGrupos(){
        List<Map<String,String>> grupos=MockDB.obtenerTodosLosGrupos();
        for(Map<String,String> m:grupos)m.put("clave",m.get("id_grupo"));
        return grupos;
    }
    public boolean crearGrupo(String materia,String horario){
        if(materia==null||materia.isBlank()||horario==null||horario.isBlank())return false;
        try{MockDB.guardarNuevoGrupo(generarCodigoClase(),materia,"",generarCodigoClase(),horario);return true;}
        catch(RuntimeException e){NotificacionManager.mostrarNotificacion("No se pudo crear",e.getMessage(),"ERROR");return false;}
    }
    public boolean agregarAlumnoManual(String idGrupo,String busqueda){
        if(busqueda==null||busqueda.isBlank()){NotificacionManager.mostrarNotificacion("Error","Ingresa nombre, correo o número de cuenta","ERROR");return false;}
        try{MockDB.inscribirAlumno(busqueda,idGrupo);return true;}
        catch(RuntimeException e){NotificacionManager.mostrarNotificacion("No se pudo inscribir",e.getMessage(),"ERROR");return false;}
    }
}
