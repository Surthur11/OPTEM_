package com.optem.logic;

import com.optem.database.MySQL;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Puntos y rachas autoritativos en MySQL; operaciones idempotentes por asignacion. */
public final class XPService {
    private XPService() {}
    public static int xp(String correo) {
        String sql="SELECT COALESCE(SUM(puntos),0) FROM OPTEM_XP_EVENTOS WHERE correo_alumno=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){
                return r.next()?Math.max(0,r.getInt(1)):0;
            }
        }catch(SQLException e){throw new IllegalStateException("No se pudo leer XP: "+e.getMessage(),e);}
    }
    public static int racha(String correo){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("SELECT racha_dias,ultima_conexion FROM USUARIOS WHERE correo=?")){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){
                if(!r.next())return 0;
                Timestamp ultimo=r.getTimestamp(2);
                if(ultimo!=null && Duration.between(ultimo.toLocalDateTime(),LocalDateTime.now()).toHours()>=24)return 0;
                return r.getInt(1);
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    /** Actualiza la racha una sola vez al acceder, no por cada cambio de pantalla. */
    public static void registrarAcceso(String correo) {
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("SELECT ultima_conexion,racha_dias FROM USUARIOS WHERE correo=? FOR UPDATE")){
                p.setString(1,correo);
                try(ResultSet r=p.executeQuery()){
                    if(!r.next())throw new IllegalArgumentException("Usuario inexistente");
                    Timestamp fecha=r.getTimestamp(1);int dias=r.getInt(2);
                    LocalDateTime ahora=LocalDateTime.now();
                    if(fecha==null)dias=1;
                    else if(Duration.between(fecha.toLocalDateTime(),ahora).toMinutes()>=1440)dias=0;
                    else if(!fecha.toLocalDateTime().toLocalDate().equals(ahora.toLocalDate()))dias+=1;
                    try(PreparedStatement u=c.prepareStatement("UPDATE USUARIOS SET ultima_conexion=?,racha_dias=? WHERE correo=?")){
                        u.setTimestamp(1,Timestamp.valueOf(ahora));u.setInt(2,dias);u.setString(3,correo);u.executeUpdate();
                    }
                }
            }
            c.commit();
        }catch(SQLException ex){throw new IllegalStateException("No se pudo actualizar la racha: "+ex.getMessage(),ex);}
        // Las penalizaciones se recalculan al entrar y se registran una sola vez por tarea vencida.
        aplicarPenalizaciones(correo);
    }
    public static void aplicarPenalizaciones(String correo){
        String q="SELECT a.id_asignacion FROM OPTEM_ASIGNACIONES_DOCENTE a " +
          "JOIN CARGA_ACADEMICA ca ON ca.id_grupo=a.id_grupo AND ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito' " +
          "LEFT JOIN OPTEM_ENTREGAS_ASIGNACION e ON e.id_asignacion=a.id_asignacion AND e.correo_alumno=? " +
          "WHERE e.id_entrega IS NULL AND TIMESTAMP(a.fecha_limite,COALESCE(TIME(STR_TO_DATE(a.hora_limite,'%h:%i %p')),'23:59:59')) + INTERVAL 1 DAY < NOW() " +
          "";
        List<Long> pendientes=new ArrayList<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo);p.setString(2,correo);
            try(ResultSet r=p.executeQuery()){while(r.next())pendientes.add(r.getLong(1));}
        }catch(SQLException ex){throw new IllegalStateException("Error al evaluar vencimientos: "+ex.getMessage(),ex);}
        for(long id:pendientes){
            try(Connection c=MySQL.conectar()){
                c.setAutoCommit(false);
                int inserted;
                try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO OPTEM_XP_EVENTOS(correo_alumno,id_asignacion,tipo,puntos) VALUES (?,?,'PENALIZACION',-2)")){
                    p.setString(1,correo);p.setLong(2,id);inserted=p.executeUpdate();
                }
                if(inserted==1)sincronizarXP(c,correo);
                c.commit();
            }catch(SQLException ex){throw new IllegalStateException("Error al descontar XP: "+ex.getMessage(),ex);}
        }
    }
    public static void sincronizarXP(Connection c,String correo) throws SQLException{
        try(PreparedStatement p=c.prepareStatement("UPDATE USUARIOS SET xp_total=GREATEST(0,(SELECT COALESCE(SUM(puntos),0) FROM OPTEM_XP_EVENTOS WHERE correo_alumno=?)) WHERE correo=?")){
            p.setString(1,correo);p.setString(2,correo);p.executeUpdate();
        }
    }
}
