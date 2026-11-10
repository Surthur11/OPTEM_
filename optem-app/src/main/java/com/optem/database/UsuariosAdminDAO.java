package com.optem.database;

import java.sql.*;
import java.util.*;

/** Acceso real a USUARIOS/ROLES; ninguna cuenta de ejemplo se crea en la interfaz. */
public final class UsuariosAdminDAO {
    private UsuariosAdminDAO() {}
    public record Usuario(String correo,String cuenta,String nombre,String apellidos,String carrera,Integer semestre,String rol,boolean activo) {
        public String nombreCompleto(){return (nombre+" "+apellidos).trim();}
    }
    public static List<Usuario> listar(String filtro,String rolFiltro) throws SQLException {
        List<Usuario> out=new ArrayList<>();
        String sql="SELECT u.correo,u.numero_cuenta,u.nombre,u.apellidos,u.carrera,u.semestre,r.nombre_rol,u.activo " +
            "FROM USUARIOS u JOIN ROLES r ON r.id_rol=u.id_rol " +
            "WHERE (?='' OR LOWER(CONCAT_WS(' ',u.nombre,u.apellidos,u.correo,u.numero_cuenta)) LIKE ?) " +
            "AND (?='' OR r.nombre_rol=?) ORDER BY u.apellidos,u.nombre";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)) {
            String f=filtro==null?"":filtro.trim().toLowerCase(Locale.ROOT);
            String rol=rolFiltro==null?"":rolFiltro;
            p.setString(1,f);p.setString(2,"%"+f+"%");p.setString(3,rol);p.setString(4,rol);
            try(ResultSet rs=p.executeQuery()) {while(rs.next()){
                int sem=rs.getInt(6);
                out.add(new Usuario(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.wasNull()?null:sem,rs.getString(7),rs.getBoolean(8)));
            }}
        }
        return out;
    }
    public static void guardar(Usuario u,String correoAnterior) throws SQLException {
        if(u.correo()==null||u.correo().isBlank()||u.nombre()==null||u.nombre().isBlank()||u.apellidos()==null||u.apellidos().isBlank()||u.cuenta()==null||u.cuenta().isBlank())
            throw new SQLException("Completa correo, matrícula, nombre y apellidos.");
        if(!"Estudiante".equals(u.rol()) && u.semestre()!=null)throw new SQLException("Solo estudiantes pueden tener semestre.");
        String sql=correoAnterior==null?
          "INSERT INTO USUARIOS(correo,numero_cuenta,nombre,apellidos,carrera,semestre,id_rol,activo) VALUES(?,?,?,?,?,?,(SELECT id_rol FROM ROLES WHERE nombre_rol=?),1)":
          "UPDATE USUARIOS SET correo=?,numero_cuenta=?,nombre=?,apellidos=?,carrera=?,semestre=?,id_rol=(SELECT id_rol FROM ROLES WHERE nombre_rol=?) WHERE correo=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,u.correo().trim());p.setString(2,u.cuenta().trim());p.setString(3,u.nombre().trim());p.setString(4,u.apellidos().trim());p.setString(5,u.carrera());
            if(u.semestre()==null)p.setNull(6,Types.INTEGER);else p.setInt(6,u.semestre());p.setString(7,u.rol());
            if(correoAnterior!=null)p.setString(8,correoAnterior);
            if(p.executeUpdate()!=1)throw new SQLException("No se guardó el usuario. Comprueba el correo y el rol.");
        }
    }
    public static String[] datosPersonal(String correo) throws SQLException{
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(
           "SELECT COALESCE(departamento,''),COALESCE(tipo_contrato,''),COALESCE(area_administrativa,''),COALESCE(nivel_acceso,'') FROM OPTEM_PERFILES_PERSONAL WHERE correo_usuario=?")){
            p.setString(1,correo);
            try(ResultSet r=p.executeQuery()){
                if(!r.next())return new String[]{"",""};
                try(PreparedStatement rol=c.prepareStatement("SELECT nombre_rol FROM ROLES WHERE id_rol=(SELECT id_rol FROM USUARIOS WHERE correo=?)")){
                    rol.setString(1,correo);try(ResultSet rr=rol.executeQuery()){
                        return rr.next()&&"Docente".equals(rr.getString(1))?new String[]{r.getString(1),r.getString(2)}:new String[]{r.getString(3),r.getString(4)};
                    }
                }
            }
        }
    }
    public static String carreraDocente(String correo) throws SQLException{
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("SELECT COALESCE(carrera_docente,'') FROM OPTEM_PERFILES_PERSONAL WHERE correo_usuario=?")){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):"";}
        }
    }
    public static void guardarDatosPersonal(String correo,String rol,String uno,String dos,String carrera) throws SQLException{
        String q="INSERT INTO OPTEM_PERFILES_PERSONAL(correo_usuario,departamento,tipo_contrato,area_administrativa,nivel_acceso,carrera_docente) VALUES(?,?,?,?,?,?) " +
          "ON DUPLICATE KEY UPDATE departamento=VALUES(departamento),tipo_contrato=VALUES(tipo_contrato),area_administrativa=VALUES(area_administrativa),nivel_acceso=VALUES(nivel_acceso),carrera_docente=VALUES(carrera_docente)";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo);
            boolean docente="Docente".equals(rol);
            p.setString(2,docente?uno:null);p.setString(3,docente?dos:null);
            p.setString(4,!docente?uno:null);p.setString(5,!docente?dos:null);
            p.setString(6,docente?carrera:null);
            p.executeUpdate();
        }
    }
    public static void cambiarActivo(Usuario u,boolean activo) throws SQLException {
        if(u.correo().equalsIgnoreCase(MockDB.sesion()) && !activo)throw new SQLException("No puedes suspender tu propia sesión.");
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("UPDATE USUARIOS SET activo=? WHERE correo=?")) {
            p.setBoolean(1,activo);p.setString(2,u.correo());if(p.executeUpdate()!=1)throw new SQLException("El usuario ya no existe.");
        }
    }
}
