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

public class NotasMaestroController {
    private final String usuarioActual;
    private final Map<String, String> mapaColores = new HashMap<>();
    
    private final String archivoNotas;

    public NotasMaestroController() {
        Preferences p = Preferences.userNodeForPackage(DashboardView.class);
        this.usuarioActual = p.get("LAST_ACTIVE_ACCOUNT", "default");
        
        // Se genera un archivo de notas exclusivo para este maestro
        String cuentaLimpia = this.usuarioActual.replaceAll("[^a-zA-Z0-9]", "_");
        this.archivoNotas = "notas_maestro_" + cuentaLimpia + ".dat";
    }

    public static class ApunteDocenteDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id, titulo, categoria, grupo, contenido, fecha, colorHex;
        public ApunteDocenteDTO(String id, String titulo, String categoria, String grupo, String contenido, String fecha, String colorHex) {
            this.id = id; this.titulo = titulo; this.categoria = categoria; this.grupo = grupo;
            this.contenido = contenido; this.fecha = fecha; this.colorHex = colorHex;
        }
    }

    public List<String> getGruposDisponibles() {
        List<String> grupos = new ArrayList<>();
        grupos.add("General / Ninguno");
        List< Map<String, String> > misGrupos = MockDB.obtenerTodosLosGrupos(); 
        if (misGrupos != null) {
            for(Map<String, String> g : misGrupos) {
                grupos.add(g.get("nombre") + " (" + g.get("grupo") + ")");
            }
        }
        return grupos;
    }

    public List<String> getCategoriasDisponibles() {
        List<String> cat = new ArrayList<>();
        cat.add("Planeación de Clase");
        cat.add("Rúbrica de Evaluación");
        cat.add("Reunión de Academia");
        cat.add("Nota Personal Docente");
        
        mapaColores.put("Planeación de Clase", "#A0C4FF");
        mapaColores.put("Rúbrica de Evaluación", "#FFADAD");
        mapaColores.put("Reunión de Academia", "#FDFFB6");
        mapaColores.put("Nota Personal Docente", "#CAFFBF");
        return cat;
    }

    public String getColorParaCategoria(String cat) {
        return mapaColores.getOrDefault(cat, "#ffffff");
    }

    public void guardarApunte(String titulo,String categoria,String grupo,String contenido){
        String q="INSERT INTO NOTAS_DOCENTE(correo_docente,id_grupo,titulo,categoria,contenido,color_hex) VALUES (?,?,?,?,?,?)";
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement(q)){
            st.setString(1,usuarioActual);
            Integer idGrupo=null;
            if(grupo!=null && !grupo.equals("General / Ninguno")){
                for(Map<String,String> g:MockDB.obtenerTodosLosGrupos())
                    if((g.get("nombre")+" ("+g.get("grupo")+")").equals(grupo)){
                        idGrupo=Integer.parseInt(g.get("id_grupo"));break;
                    }
            }
            if(idGrupo==null)st.setNull(2,java.sql.Types.INTEGER);else st.setInt(2,idGrupo);
            st.setString(3,titulo);st.setString(4,categoria);st.setString(5,contenido);
            st.setString(6,getColorParaCategoria(categoria));st.executeUpdate();
        }catch(java.sql.SQLException ex){throw new IllegalStateException("No se guardó nota docente: "+ex.getMessage(),ex);}
    }
    public void eliminarApunte(String id){
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement("DELETE FROM NOTAS_DOCENTE WHERE id_nota_docente=? AND correo_docente=?")){
            st.setLong(1,Long.parseLong(id));st.setString(2,usuarioActual);st.executeUpdate();
        }catch(java.sql.SQLException ex){throw new IllegalStateException("No se pudo eliminar nota docente: "+ex.getMessage(),ex);}
    }
    public List<ApunteDocenteDTO> cargarApuntes(){
        List<ApunteDocenteDTO> result=new ArrayList<>();
        String q="SELECT n.id_nota_docente,n.titulo,n.categoria,CONCAT(m.nombre_materia,' (',g.nombre_grupo,')') AS grupo,n.contenido,DATE(n.fecha_actualizacion) fecha,n.color_hex FROM NOTAS_DOCENTE n LEFT JOIN GRUPOS g ON g.id_grupo=n.id_grupo LEFT JOIN MATERIAS m ON g.clave_materia=m.clave_materia WHERE n.correo_docente=? ORDER BY n.fecha_actualizacion DESC";
        try(java.sql.Connection c=com.optem.database.MySQL.conectar();java.sql.PreparedStatement st=c.prepareStatement(q)){
            st.setString(1,usuarioActual);try(java.sql.ResultSet r=st.executeQuery()){
                while(r.next())result.add(new ApunteDocenteDTO(r.getString(1),r.getString(2),r.getString(3),r.getString(4)==null?"General / Ninguno":r.getString(4),r.getString(5),r.getString(6),r.getString(7)));
            }
        }catch(java.sql.SQLException ex){throw new IllegalStateException("Error leyendo Cuaderno Docente desde MySQL: "+ex.getMessage(),ex);}
        return result;
    }
}
