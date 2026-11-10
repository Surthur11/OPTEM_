package com.optem.controllers;
import com.optem.database.*;
import java.io.*;
import java.sql.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** Asignaciones y entregas compartidas por grupo con archivos guardados en MySQL. */
public class TareasMaestroController {
    private String correo(){return MockDB.sesion();}
    public List<String> getMisGruposOficiales(){
        List<String> list=new ArrayList<>();
        for(Map<String,String> g:MockDB.obtenerTodosLosGrupos())list.add(g.get("nombre")+" ("+g.get("grupo")+")");
        return list;
    }
    public void publicarNuevaTarea(String titulo,String materia,String descripcion,LocalDate fecha,String hora,String rutaArchivo){
        if(fecha==null||titulo==null||titulo.isBlank()||materia==null)throw new IllegalArgumentException("Selecciona grupo, título y fecha límite");
        String sql="INSERT INTO OPTEM_ASIGNACIONES_DOCENTE(id_grupo,correo_docente,titulo,descripcion,fecha_limite,hora_limite,archivo_nombre,archivo_contenido,estado) VALUES (?,?,?,?,?,?,?,?, 'ABIERTA')";
        byte[] bytes=leerArchivo(rutaArchivo);
        try(Connection c=MySQL.conectar()){
            int idGrupo=-1;
            for(Map<String,String> g:MockDB.obtenerTodosLosGrupos()){
                if((g.get("nombre")+" ("+g.get("grupo")+")").equals(materia)) {idGrupo=Integer.parseInt(g.get("id_grupo"));break;}
            }
            if(idGrupo<0)throw new IllegalArgumentException("Ese grupo no pertenece a tu cuenta docente");
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setInt(1,idGrupo);p.setString(2,correo());p.setString(3,titulo);p.setString(4,descripcion);
                p.setDate(5,java.sql.Date.valueOf(fecha));p.setString(6,hora);
                if(bytes==null){p.setNull(7,Types.VARCHAR);p.setNull(8,Types.BLOB);}
                else {p.setString(7,Path.of(rutaArchivo).getFileName().toString());p.setBytes(8,bytes);}
                p.executeUpdate();
            }
        }catch(SQLException ex){throw new IllegalStateException("No se pudo publicar la asignación: "+ex.getMessage(),ex);}
    }
    public List<Map<String,String>> getTodasLasAsignaciones(){
        String q="SELECT a.id_asignacion,a.titulo,a.descripcion,a.fecha_limite,a.hora_limite,a.estado,a.archivo_nombre,m.nombre_materia,g.nombre_grupo,a.id_grupo,"+
           "(SELECT COUNT(*) FROM OPTEM_ENTREGAS_ASIGNACION e WHERE e.id_asignacion=a.id_asignacion) entregadas,"+
           "(SELECT COUNT(*) FROM CARGA_ACADEMICA ca WHERE ca.id_grupo=g.id_grupo AND ca.estado_inscripcion='inscrito') inscritas, " +
           "ee.id_entrega,ee.calificacion,ee.escala_calificacion,ee.comentario_docente " +
           "FROM OPTEM_ASIGNACIONES_DOCENTE a JOIN GRUPOS g ON g.id_grupo=a.id_grupo JOIN MATERIAS m ON m.clave_materia=g.clave_materia " +
           "LEFT JOIN OPTEM_ENTREGAS_ASIGNACION ee ON ee.id_asignacion=a.id_asignacion AND ee.correo_alumno=? " +
           "WHERE a.correo_docente=? OR EXISTS (SELECT 1 FROM CARGA_ACADEMICA ca WHERE ca.id_grupo=a.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito') ORDER BY a.id_asignacion DESC";
        List<Map<String,String>> result=new ArrayList<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo());p.setString(2,correo());p.setString(3,correo());try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    Map<String,String> t=new HashMap<>();
                    t.put("id",r.getString("id_asignacion"));t.put("titulo",r.getString("titulo"));
                    t.put("descripcion",Objects.toString(r.getString("descripcion"),""));t.put("materia",r.getString("nombre_materia")+" ("+r.getString("nombre_grupo")+")");
                    t.put("id_grupo",r.getString("id_grupo"));
                    t.put("fecha",r.getString("fecha_limite"));t.put("hora",r.getString("hora_limite"));
                    t.put("estado",r.getString("estado"));t.put("entregas_actuales",r.getString("entregadas"));t.put("entregas_total",r.getString("inscritas"));
                    t.put("ruta_archivo",Objects.toString(r.getString("archivo_nombre"),""));
                    t.put("entregado",r.getObject("id_entrega")==null?"0":"1");
                    t.put("nota",Objects.toString(r.getString("calificacion"),""));
                    t.put("escala",Objects.toString(r.getString("escala_calificacion"),"100"));
                    t.put("comentario",Objects.toString(r.getString("comentario_docente"),""));
                    result.add(t);
                }
            }
        }catch(SQLException ex){throw new IllegalStateException("No se pudieron consultar asignaciones: "+ex.getMessage(),ex);}
        return result;
    }
    public void cerrarRecepcionTarea(String id){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("UPDATE OPTEM_ASIGNACIONES_DOCENTE SET estado='CERRADA' WHERE id_asignacion=? AND correo_docente=?")){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());if(p.executeUpdate()!=1)throw new IllegalArgumentException("No se encontró la asignación de tu cuenta");
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public boolean yaEntregado(String id){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("SELECT 1 FROM OPTEM_ENTREGAS_ASIGNACION WHERE id_asignacion=? AND correo_alumno=?")){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());try(ResultSet r=p.executeQuery()){return r.next();}
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public void registrarEntregaAlumno(String id,String nombreAlumno,String rutaArchivo){
        byte[] data=leerArchivo(rutaArchivo);if(data==null)throw new IllegalArgumentException("Selecciona un archivo válido");
        String sql="INSERT INTO OPTEM_ENTREGAS_ASIGNACION(id_asignacion,correo_alumno,archivo_nombre,archivo_contenido) " +
           "SELECT a.id_asignacion,?, ?,? FROM OPTEM_ASIGNACIONES_DOCENTE a WHERE a.id_asignacion=? AND a.estado='ABIERTA' AND " +
           "EXISTS (SELECT 1 FROM CARGA_ACADEMICA ca WHERE ca.id_grupo=a.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito')";
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(sql)){
                p.setString(1,correo());p.setString(2,Path.of(rutaArchivo).getFileName().toString());p.setBytes(3,data);p.setLong(4,Long.parseLong(id));p.setString(5,correo());
                if(p.executeUpdate()!=1)throw new IllegalArgumentException("Recepción cerrada, grupo no inscrito o asignación inexistente");
                int puntos;
                try(PreparedStatement score=c.prepareStatement("SELECT CASE WHEN NOW()<=TIMESTAMP(fecha_limite,COALESCE(TIME(STR_TO_DATE(hora_limite,'%h:%i %p')),'23:59:59')) THEN 10 ELSE 5 END FROM OPTEM_ASIGNACIONES_DOCENTE WHERE id_asignacion=?")){
                    score.setLong(1,Long.parseLong(id));try(ResultSet rr=score.executeQuery()){
                        if(!rr.next())throw new SQLException("Asignación desconocida");puntos=rr.getInt(1);
                    }
                }
                try(PreparedStatement ev=c.prepareStatement("INSERT INTO OPTEM_XP_EVENTOS(correo_alumno,id_asignacion,tipo,puntos) VALUES (?,?,'ENTREGA',?)")){
                    ev.setString(1,correo());ev.setLong(2,Long.parseLong(id));ev.setInt(3,puntos);ev.executeUpdate();
                }
                com.optem.logic.XPService.sincronizarXP(c,correo());
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }catch(Exception ex){throw new IllegalStateException("No se guardó la entrega: "+ex.getMessage(),ex);}
    }
    public List<Map<String,String>> obtenerEntregasDeTarea(String id){
        String sql="SELECT e.id_entrega,u.nombre,u.apellidos,e.fecha_entrega,e.archivo_nombre,e.calificacion,e.comentario_docente,e.escala_calificacion FROM OPTEM_ENTREGAS_ASIGNACION e JOIN USUARIOS u ON u.correo=e.correo_alumno JOIN OPTEM_ASIGNACIONES_DOCENTE a ON a.id_asignacion=e.id_asignacion WHERE e.id_asignacion=? AND a.correo_docente=? ORDER BY e.fecha_entrega DESC";
        List<Map<String,String>> list=new ArrayList<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    Map<String,String> m=new HashMap<>();m.put("id_entrega",r.getString(1));
                    m.put("alumno",r.getString(2)+" "+r.getString(3));m.put("fecha_entrega",r.getString(4));
                    m.put("archivo_nombre",r.getString(5));m.put("calificacion",Objects.toString(r.getString(6),""));
                    m.put("comentario",Objects.toString(r.getString(7),""));m.put("escala",r.getString(8));list.add(m);
                }
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}return list;
    }
    /** Archivo solo a demanda; nunca en listados. Identidad y pertenencia verificadas en SQL. */
    public String descargarAdjuntoProfesor(String id){
        String sql="SELECT a.archivo_nombre,a.archivo_contenido FROM OPTEM_ASIGNACIONES_DOCENTE a WHERE a.id_asignacion=? AND (a.correo_docente=? OR EXISTS(SELECT 1 FROM CARGA_ACADEMICA ca WHERE ca.id_grupo=a.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito'))";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());p.setString(3,correo());
            try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new IllegalArgumentException("Sin permiso para ver el adjunto");
                return archivoTemporal(r.getString(1),r.getBytes(2));
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public String descargarEntrega(String idEntrega){
        String sql="SELECT e.archivo_nombre,e.archivo_contenido FROM OPTEM_ENTREGAS_ASIGNACION e JOIN OPTEM_ASIGNACIONES_DOCENTE a ON a.id_asignacion=e.id_asignacion WHERE e.id_entrega=? AND a.correo_docente=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,Long.parseLong(idEntrega));p.setString(2,correo());
            try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new IllegalArgumentException("Sin permiso para consultar esta entrega");
                return archivoTemporal(r.getString(1),r.getBytes(2));
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public void calificar(String idEntrega,double calificacion,int escala,String comentario){
        if(escala!=10 && escala!=100)throw new IllegalArgumentException("Escala inválida");
        if(!Double.isFinite(calificacion)||calificacion<0||calificacion>escala)throw new IllegalArgumentException("La nota debe estar entre 0 y "+escala);
        String sql="UPDATE OPTEM_ENTREGAS_ASIGNACION e JOIN OPTEM_ASIGNACIONES_DOCENTE a ON a.id_asignacion=e.id_asignacion " +
          "SET e.calificacion=?,e.escala_calificacion=?,e.comentario_docente=?,e.fecha_calificacion=NOW() WHERE e.id_entrega=? AND a.correo_docente=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setDouble(1,calificacion);p.setInt(2,escala);p.setString(3,comentario);p.setLong(4,Long.parseLong(idEntrega));p.setString(5,correo());
            if(p.executeUpdate()!=1)throw new IllegalArgumentException("No se pudo calificar la entrega");
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public Map<String,String> estadoEntregaAlumno(String id){
        String q="SELECT CAST(calificacion AS CHAR),CAST(escala_calificacion AS CHAR),COALESCE(comentario_docente,'') FROM OPTEM_ENTREGAS_ASIGNACION WHERE id_asignacion=? AND correo_alumno=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());
            try(ResultSet r=p.executeQuery()){
                if(!r.next())return Collections.emptyMap();
                Map<String,String> m=new HashMap<>();m.put("nota",Objects.toString(r.getString(1),""));
                m.put("escala",r.getString(2));m.put("comentario",r.getString(3));return m;
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public void deshacerEntregaAlumno(String id){
        long asignacion=Long.parseLong(id);
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try{
                int puntos=0;
                try(PreparedStatement lock=c.prepareStatement("SELECT puntos FROM OPTEM_XP_EVENTOS WHERE id_asignacion=? AND correo_alumno=? AND tipo='ENTREGA' FOR UPDATE")){
                    lock.setLong(1,asignacion);lock.setString(2,correo());
                    try(ResultSet r=lock.executeQuery()){if(r.next())puntos=r.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM OPTEM_ENTREGAS_ASIGNACION WHERE id_asignacion=? AND correo_alumno=?")){
                    p.setLong(1,asignacion);p.setString(2,correo());
                    if(p.executeUpdate()!=1)throw new IllegalArgumentException("No hay una entrega vigente");
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM OPTEM_XP_EVENTOS WHERE id_asignacion=? AND correo_alumno=? AND tipo='ENTREGA'")){
                    p.setLong(1,asignacion);p.setString(2,correo());p.executeUpdate();
                }
                com.optem.logic.XPService.sincronizarXP(c,correo());
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }catch(Exception ex){throw new IllegalStateException("No se pudo deshacer la entrega: "+ex.getMessage(),ex);}
    }
    private static byte[] leerArchivo(String ruta){
        if(ruta==null||ruta.isBlank())return null;
        try{Path p=Path.of(ruta);long tam=Files.size(p);if(tam>20_000_000L)throw new IllegalArgumentException("Archivo máximo 20MB");return Files.readAllBytes(p);}
        catch(IOException ex){throw new IllegalStateException("No se pudo leer el archivo: "+ex.getMessage(),ex);}
    }
    private static String archivoTemporal(String nombre,byte[] contenido){
        if(contenido==null)return "";
        try{
            String n=nombre==null?"archivo.bin":Path.of(nombre).getFileName().toString();
            String extension=n.contains(".")?n.substring(n.lastIndexOf('.')):".bin";
            if(extension.length()>15)extension=".bin";
            Path p=Files.createTempFile("optem_archivo_",extension);Files.write(p,contenido);p.toFile().deleteOnExit();return p.toString();
        }catch(IOException ex){throw new IllegalStateException("No se pudo abrir adjunto: "+ex.getMessage(),ex);}
    }
}
