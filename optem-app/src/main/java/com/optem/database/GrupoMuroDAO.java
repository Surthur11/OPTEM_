package com.optem.database;

import java.sql.*;
import java.util.*;

/** Publicaciones reales y permisos por grupo para docente y alumno. */
public final class GrupoMuroDAO {
    private GrupoMuroDAO(){}
    public record Publicacion(long id,String autor,String correo,String contenido,boolean prioritario,String fecha) {}
    private static boolean esDocente(Connection c,int grupo,String correo) throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM GRUPOS WHERE id_grupo=? AND correo_docente=?")){
            p.setInt(1,grupo);p.setString(2,correo);try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }
    private static void permitir(Connection c,int grupo,String correo) throws SQLException{
        String q="SELECT 1 FROM GRUPOS g WHERE g.id_grupo=? AND (g.correo_docente=? OR EXISTS("+
          "SELECT 1 FROM CARGA_ACADEMICA ca WHERE ca.id_grupo=g.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito'))";
        try(PreparedStatement p=c.prepareStatement(q)){
            p.setInt(1,grupo);p.setString(2,correo);p.setString(3,correo);
            try(ResultSet r=p.executeQuery()){
                if(!r.next())throw new SQLException("No perteneces a este grupo");
            }
        }
    }
    public static boolean profesor(int grupo){
        try(Connection c=MySQL.conectar()){
            permitir(c,grupo,MockDB.sesion());return esDocente(c,grupo,MockDB.sesion());
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    public static List<Publicacion> publicaciones(int grupo){
        List<Publicacion> out=new ArrayList<>();
        String q="SELECT p.id_publicacion,CONCAT(u.nombre,' ',u.apellidos),p.correo_autor,p.contenido,p.prioritario,p.fecha_creacion " +
          "FROM PUBLICACIONES_GRUPO p JOIN USUARIOS u ON u.correo=p.correo_autor WHERE p.id_grupo=? ORDER BY p.fecha_creacion,p.id_publicacion LIMIT 200";
        try(Connection c=MySQL.conectar()){
            permitir(c,grupo,MockDB.sesion());
            try(PreparedStatement p=c.prepareStatement(q)){
                p.setInt(1,grupo);try(ResultSet r=p.executeQuery()){
                    while(r.next())out.add(new Publicacion(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getBoolean(5),r.getString(6)));
                }
            }
        }catch(SQLException e){throw new IllegalStateException("No se pudo cargar General: "+e.getMessage(),e);}
        return out;
    }
    public static void publicar(int grupo,String contenido,boolean prioritario){
        if(contenido==null||contenido.isBlank())throw new IllegalArgumentException("Escribe un mensaje");
        if(contenido.length()>5000)throw new IllegalArgumentException("Máximo 5000 caracteres");
        try(Connection c=MySQL.conectar()){
            permitir(c,grupo,MockDB.sesion());
            if(prioritario && !esDocente(c,grupo,MockDB.sesion()))throw new SQLException("Solo el docente puede marcar prioridades");
            try(PreparedStatement p=c.prepareStatement("INSERT INTO PUBLICACIONES_GRUPO(id_grupo,correo_autor,contenido,prioritario) VALUES(?,?,?,?)")){
                p.setInt(1,grupo);p.setString(2,MockDB.sesion());p.setString(3,contenido.trim());p.setBoolean(4,prioritario);p.executeUpdate();
            }
        }catch(SQLException ex){throw new IllegalStateException("No se publicó el mensaje: "+ex.getMessage(),ex);}
    }
    public static void borrar(long id){
        String q="DELETE p FROM PUBLICACIONES_GRUPO p JOIN GRUPOS g ON g.id_grupo=p.id_grupo " +
          "WHERE p.id_publicacion=? AND (p.correo_autor=? OR g.correo_docente=?)";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setLong(1,id);p.setString(2,MockDB.sesion());p.setString(3,MockDB.sesion());
            if(p.executeUpdate()!=1)throw new SQLException("No tienes permiso para eliminar esa publicación");
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public static Set<String> urgentesPendientes(String alumno){
        Set<String> ids=new HashSet<>();
        String q="SELECT DISTINCT CAST(p.id_grupo AS CHAR) FROM PUBLICACIONES_GRUPO p " +
          "JOIN GRUPOS g ON g.id_grupo=p.id_grupo " +
          "JOIN CARGA_ACADEMICA ca ON ca.id_grupo=p.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito' " +
          "LEFT JOIN OPTEM_PUBLICACIONES_LEIDAS l ON l.id_publicacion=p.id_publicacion AND l.correo_usuario=? " +
          "WHERE p.prioritario=1 AND l.id_publicacion IS NULL AND g.archivado=0";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,alumno);p.setString(2,alumno);
            try(ResultSet r=p.executeQuery()){while(r.next())ids.add(r.getString(1));}
        }catch(SQLException ex){throw new IllegalStateException(ex);}return ids;
    }
    public static void marcarLeidos(int grupo){
        try(Connection c=MySQL.conectar()){
            permitir(c,grupo,MockDB.sesion());
            try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO OPTEM_PUBLICACIONES_LEIDAS(correo_usuario,id_publicacion) " +
                "SELECT ?,id_publicacion FROM PUBLICACIONES_GRUPO WHERE id_grupo=? AND prioritario=1")){
                p.setString(1,MockDB.sesion());p.setInt(2,grupo);p.executeUpdate();
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
}
