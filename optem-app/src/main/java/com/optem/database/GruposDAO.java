package com.optem.database;
import java.sql.*;
import java.util.*;
/** Operaciones de grupos y matrículas contra MySQL. */
public final class GruposDAO {
    private GruposDAO(){}
    public record Alumno(String correo,String cuenta,String nombre){}
    private static void verificarGrupo(Connection c,int grupo) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM GRUPOS g WHERE g.id_grupo=? AND (g.correo_docente=? OR EXISTS(SELECT 1 FROM USUARIOS u JOIN ROLES r ON u.id_rol=r.id_rol WHERE u.correo=? AND r.nombre_rol='Administrador'))")){
            p.setInt(1,grupo);p.setString(2,MockDB.sesion());p.setString(3,MockDB.sesion());
            try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No tienes permiso sobre este grupo.");}
        }
    }
    public static List<Alumno> alumnos(int id) throws SQLException {
        List<Alumno> out=new ArrayList<>();
        try(Connection c=MySQL.conectar()){
            verificarGrupo(c,id);
            try(PreparedStatement p=c.prepareStatement("SELECT DISTINCT u.correo,u.numero_cuenta,CONCAT(u.nombre,' ',u.apellidos) AS nombre FROM CARGA_ACADEMICA ca JOIN USUARIOS u ON u.correo=ca.correo_estudiante WHERE ca.id_grupo=? AND ca.estado_inscripcion='inscrito' ORDER BY nombre")) {
                p.setInt(1,id);try(ResultSet rs=p.executeQuery()){while(rs.next())out.add(new Alumno(rs.getString(1),rs.getString(2),rs.getString(3)));}
            }
        }
        return out;
    }
    public static void quitarAlumno(int id,String correo) throws SQLException {
        try(Connection c=MySQL.conectar()){
            verificarGrupo(c,id);
            try(PreparedStatement p=c.prepareStatement("UPDATE CARGA_ACADEMICA SET estado_inscripcion='baja' WHERE id_grupo=? AND correo_estudiante=? AND estado_inscripcion='inscrito'")) {
                p.setInt(1,id);p.setString(2,correo);if(p.executeUpdate()<1)throw new SQLException("El alumno no estaba inscrito.");
            }
        }
    }
    public static void horario(int id,String h) throws SQLException {
        if(h==null||h.isBlank())throw new SQLException("Escribe un horario.");
        try(Connection c=MySQL.conectar()){
            verificarGrupo(c,id);try(PreparedStatement p=c.prepareStatement("UPDATE GRUPOS SET horario_texto=? WHERE id_grupo=?")) {
                p.setString(1,h.trim());p.setInt(2,id);if(p.executeUpdate()!=1)throw new SQLException("No se actualizó el grupo.");
            }
        }
    }
    public static void archivar(int id) throws SQLException {
        try(Connection c=MySQL.conectar()){
            verificarGrupo(c,id);try(PreparedStatement p=c.prepareStatement("UPDATE GRUPOS SET archivado=1 WHERE id_grupo=?")) {
                p.setInt(1,id);if(p.executeUpdate()!=1)throw new SQLException("No se archivó el grupo.");
            }
        }
    }
    public static List<Map<String,String>> gruposArchivados() throws SQLException{
        List<Map<String,String>> out=new ArrayList<>();
        String q="SELECT g.id_grupo,m.nombre_materia,g.nombre_grupo,COALESCE(g.horario_texto,'') FROM GRUPOS g JOIN MATERIAS m ON m.clave_materia=g.clave_materia WHERE g.correo_docente=? AND g.archivado=1 ORDER BY g.id_grupo DESC";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,MockDB.sesion());try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    Map<String,String> x=new HashMap<>();x.put("id",r.getString(1));x.put("materia",r.getString(2));
                    x.put("grupo",r.getString(3));x.put("horario",r.getString(4));out.add(x);
                }
            }
        }return out;
    }
    public static void desarchivar(int id) throws SQLException{
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("UPDATE GRUPOS SET archivado=0 WHERE id_grupo=? AND correo_docente=?")){
            p.setInt(1,id);p.setString(2,MockDB.sesion());
            if(p.executeUpdate()!=1)throw new SQLException("Grupo archivado no encontrado");
        }
    }
    public static int contarInscritos() throws SQLException {
        String q="SELECT COUNT(DISTINCT ca.correo_estudiante) FROM CARGA_ACADEMICA ca JOIN GRUPOS g ON g.id_grupo=ca.id_grupo WHERE ca.estado_inscripcion='inscrito' AND g.archivado=0 AND (g.correo_docente=? OR EXISTS(SELECT 1 FROM USUARIOS u JOIN ROLES r ON r.id_rol=u.id_rol WHERE u.correo=? AND r.nombre_rol='Administrador'))";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,MockDB.sesion());p.setString(2,MockDB.sesion());try(ResultSet rs=p.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }
}
