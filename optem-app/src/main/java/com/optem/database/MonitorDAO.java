package com.optem.database;

import java.sql.*;
import java.util.*;

/** Datos verdaderos: consultas JDBC atribuidas a roles y muestras desde Ubuntu Server. */
public final class MonitorDAO {
    private MonitorDAO(){}
    public record Punto(int hora,long consultas){}
    public record Metrica(long usuarios,long estudiantes,long docentes,long consultasGlobales,
                         long consultasEstudiantes,long consultasDocentes,long conexiones,
                         String ram,String gpu,String cpu,String trafico,List<Punto> puntos){}
    public static Metrica leer() throws SQLException{
        try(Connection c=MySQL.conectar()){
            long usuarios=0,estudiantes=0,docentes=0,glob=0,ce=0,cd=0,conn=0;
            String ram="Sin agente",gpu="N/D",cpu="Sin agente",trafico="No disponible";
            try(PreparedStatement p=c.prepareStatement("SELECT r.nombre_rol,COUNT(*) FROM USUARIOS u JOIN ROLES r ON r.id_rol=u.id_rol WHERE u.activo=1 GROUP BY r.nombre_rol");ResultSet r=p.executeQuery()){
                while(r.next()){
                    usuarios+=r.getLong(2);
                    if("Estudiante".equals(r.getString(1)))estudiantes=r.getLong(2);
                    if("Docente".equals(r.getString(1)))docentes=r.getLong(2);
                }
            }
            try(PreparedStatement p=c.prepareStatement("SELECT r.nombre_rol,COALESCE(SUM(t.total),0) FROM OPTEM_CONTADOR_CONSULTAS t JOIN USUARIOS u ON u.correo=t.correo_usuario JOIN ROLES r ON r.id_rol=u.id_rol GROUP BY r.nombre_rol");ResultSet r=p.executeQuery()){
                while(r.next()){
                    if("Estudiante".equals(r.getString(1)))ce=r.getLong(2);
                    if("Docente".equals(r.getString(1)))cd=r.getLong(2);
                }
            }
            try(Statement p=c.createStatement();ResultSet r=p.executeQuery("SHOW GLOBAL STATUS WHERE Variable_name IN ('Questions','Threads_connected')")){
                while(r.next()){
                    if("Questions".equals(r.getString(1)))glob=r.getLong(2);
                    if("Threads_connected".equals(r.getString(1)))conn=r.getLong(2);
                }
            }
            try(Statement p=c.createStatement();ResultSet r=p.executeQuery("SELECT ram_usada_mb,ram_total_mb,cpu_porcentaje,gpu_porcentaje,bytes_recibidos,bytes_enviados,TIMESTAMPDIFF(SECOND,capturado_en,NOW()) FROM OPTEM_SERVER_METRICAS ORDER BY id DESC LIMIT 1")){
                if(r.next() && r.getLong(7)<30){
                    ram=r.getInt(1)+" / "+r.getInt(2)+" MB";
                    cpu=r.getString(3)==null?"N/D":r.getString(3)+" %";
                    gpu=r.getString(4)==null?"N/D (sin GPU expuesta)":r.getString(4)+" %";
                    trafico="Recibido "+(r.getLong(5)/1_000_000)+" MB · Enviado "+(r.getLong(6)/1_000_000)+" MB desde inicio MySQL";
                }
            }
            List<Punto> puntos=new ArrayList<>();
            try(Statement p=c.createStatement();ResultSet r=p.executeQuery(
                "SELECT TIMESTAMPDIFF(HOUR,DATE_SUB(NOW(),INTERVAL 24 HOUR),capturado_en) AS tramo, " +
                "GREATEST(0,MAX(consultas_mysql)-MIN(consultas_mysql)) AS consultas FROM OPTEM_SERVER_METRICAS " +
                "WHERE capturado_en>=NOW()-INTERVAL 24 HOUR GROUP BY tramo ORDER BY tramo")){
                while(r.next())puntos.add(new Punto(r.getInt(1),r.getLong(2)));
            }
            return new Metrica(usuarios,estudiantes,docentes,glob,ce,cd,conn,ram,gpu,cpu,trafico,puntos);
        }
    }
}
