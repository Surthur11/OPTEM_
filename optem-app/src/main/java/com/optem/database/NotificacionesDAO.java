package com.optem.database;

import java.sql.*;

/** Nivel compartido por usuario. No depende del registro local de Windows. */
public final class NotificacionesDAO {
    private NotificacionesDAO(){}
    public static String leer(String correo){
        String q="SELECT modo FROM OPTEM_PREF_NOTIFICACIONES WHERE correo_usuario=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):"COMPLETO";}
        }catch(SQLException ex){throw new IllegalStateException("No se pudieron leer las notificaciones: "+ex.getMessage(),ex);}
    }
    public static void guardar(String correo,String modo){
        if(!java.util.Set.of("APAGADO","PRIORITARIO","COMPLETO").contains(modo))throw new IllegalArgumentException("Modo inválido");
        String q="INSERT INTO OPTEM_PREF_NOTIFICACIONES(correo_usuario,modo) VALUES(?,?) ON DUPLICATE KEY UPDATE modo=VALUES(modo)";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo);p.setString(2,modo);p.executeUpdate();
        }catch(SQLException ex){throw new IllegalStateException("No se guardaron las notificaciones: "+ex.getMessage(),ex);}
    }
}
