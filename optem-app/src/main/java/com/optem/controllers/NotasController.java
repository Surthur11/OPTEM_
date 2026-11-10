package com.optem.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.Preferences;

import com.optem.database.MockDB;
import com.optem.ui.DashboardView;

public class NotasController {

    private final String usuarioActual;
    private final Map<String, String> mapaColores = new HashMap<>();
    
    // Paleta premium de colores pastel para notas tipo "Sticky Notes"
    private final String[] coloresKeep = {"#FFADAD", "#FFD6A5", "#FDFFB6", "#CAFFBF", "#9BF6FF", "#A0C4FF", "#BDB2FF", "#FFC6FF"};
    
    private final String archivoNotas;

    public NotasController() {
        Preferences p = Preferences.userNodeForPackage(DashboardView.class);
        this.usuarioActual = p.get("LAST_ACTIVE_ACCOUNT", "default");
        
        // Se genera un archivo de notas exclusivo para este alumno
        String cuentaLimpia = this.usuarioActual.replaceAll("[^a-zA-Z0-9]", "_");
        this.archivoNotas = "notas_alumno_" + cuentaLimpia + ".dat";
    }

    public static class ApunteDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id, titulo, materia, contenido, fecha, colorHex;
        public ApunteDTO(String id, String titulo, String materia, String contenido, String fecha, String colorHex) {
            this.id = id; this.titulo = titulo; this.materia = materia;
            this.contenido = contenido; this.fecha = fecha; this.colorHex = colorHex;
        }
    }

    public List<String> getMateriasDisponibles() {
        List<String> nombresMaterias = new ArrayList<>();
        
        nombresMaterias.add("Nota Personal");
        nombresMaterias.add("Recordatorio Urgente");
        
        mapaColores.put("Nota Personal", "#E0E0E0"); // Gris muy suave
        mapaColores.put("Recordatorio Urgente", "#FFB4A2"); // Salmón suave
        
        List< Map<String, String> > clases = MockDB.obtenerMateriasDelAlumno(usuarioActual);
        
        int colorIndex = 0;
        if (clases != null) {
            for(Map<String, String> clase : clases) {
                String nombre = clase.get("nombre");
                nombresMaterias.add(nombre);
                if (!mapaColores.containsKey(nombre)) {
                    mapaColores.put(nombre, coloresKeep[colorIndex % coloresKeep.length]);
                    colorIndex++;
                }
            }
        }
        return nombresMaterias;
    }

    public String getColorParaMateria(String materia) {
        return mapaColores.getOrDefault(materia, "#F8F9FA");
    }

    public void guardarApunte(String titulo, String materia, String contenido) {
        String sql="INSERT INTO OPTEM_NOTAS_PERSONALES(id,correo_usuario,titulo,materia,contenido,color_hex) VALUES (?,?,?,?,?,?)";
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement(sql)){
            st.setString(1,UUID.randomUUID().toString());st.setString(2,usuarioActual);st.setString(3,titulo);
            st.setString(4,materia);st.setString(5,contenido);st.setString(6,getColorParaMateria(materia));st.executeUpdate();
        }catch(java.sql.SQLException ex){throw new IllegalStateException("No se guardó la nota en MySQL: "+ex.getMessage(),ex);}
    }

    public void eliminarApunte(String id) {
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement("DELETE FROM OPTEM_NOTAS_PERSONALES WHERE id=? AND correo_usuario=?")){
            st.setString(1,id);st.setString(2,usuarioActual);st.executeUpdate();
        }catch(java.sql.SQLException ex){throw new IllegalStateException("No se eliminó la nota: "+ex.getMessage(),ex);}
    }

    public List<ApunteDTO> cargarApuntes(){
        List<ApunteDTO> out=new ArrayList<>();
        String q="SELECT id,titulo,materia,contenido,DATE(fecha_actualizacion),color_hex FROM OPTEM_NOTAS_PERSONALES WHERE correo_usuario=? ORDER BY fecha_actualizacion DESC";
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement(q)){
            st.setString(1,usuarioActual);try(java.sql.ResultSet r=st.executeQuery()){
                while(r.next())out.add(new ApunteDTO(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)));
            }
        }catch(java.sql.SQLException ex){throw new IllegalStateException("Error al leer notas desde MySQL: "+ex.getMessage(),ex);}
        return out;
    }
}
