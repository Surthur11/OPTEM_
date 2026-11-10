package com.optem.logic;
import com.optem.database.MySQL;
import com.optem.models.Actividad;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import java.util.prefs.Preferences;

/** Almacén de actividades privadas en MySQL, por usuario. */
public class ActividadManager {
    private static final ActividadManager INSTANCE=new ActividadManager();
    private String usuarioActual;
    private ActividadManager(){}
    public static ActividadManager getInstance(){return INSTANCE;}
    public void setUsuarioActual(String correo){this.usuarioActual=correo;}
    private String correo(){
        String prefs=Preferences.userNodeForPackage(com.optem.ui.DashboardView.class).get("LAST_ACTIVE_ACCOUNT","");
        return usuarioActual==null || usuarioActual.isBlank()?prefs:usuarioActual;
    }
    public List<Actividad> getAllActividades(){
        List<Actividad> out=new ArrayList<>();
        String sql="SELECT a.id_actividad,a.titulo,a.tipo,a.descripcion,a.fecha,a.hora_vencimiento,a.estado,COALESCE(e.materia,'General') FROM ACTIVIDADES a LEFT JOIN OPTEM_ETIQUETAS_ACTIVIDAD e ON e.id_actividad=a.id_actividad WHERE a.correo_usuario=? ORDER BY a.fecha DESC,a.id_actividad DESC";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,correo());try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    java.sql.Date f=r.getDate(5);java.sql.Time h=r.getTime(6);
                    Actividad a=new Actividad(r.getString(1),r.getString(2),r.getString(3),r.getString(4),f==null?LocalDate.now():f.toLocalDate(),h==null?LocalTime.MIDNIGHT:h.toLocalTime(),r.getString(8));
                    String estado=r.getString(7);a.setEstado("completada".equalsIgnoreCase(estado)?"COMPLETADO":("en_progreso".equalsIgnoreCase(estado)?"EN_PROCESO":"PENDIENTE"));out.add(a);
                }
            }
        }catch(SQLException ex){throw new IllegalStateException("Error leyendo actividades: "+ex.getMessage(),ex);}return out;
    }
    public List<Actividad> getActividadesDelDia(LocalDate fecha){return getAllActividades().stream().filter(a->fecha.equals(a.getFecha())).collect(Collectors.toList());}
    public Actividad buscarActividadPorId(String id){return getAllActividades().stream().filter(a->a.getId().equals(id)).findFirst().orElse(null);}
    public void agregarActividad(Actividad a){
        String sql="INSERT INTO ACTIVIDADES(correo_usuario,titulo,tipo,descripcion,fecha,hora_vencimiento,estado) VALUES (?,?,?,?,?,?,'pendiente')";
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                p.setString(1,correo());p.setString(2,a.getNombre());p.setString(3,a.getTipo());p.setString(4,a.getDescripcion());p.setDate(5,java.sql.Date.valueOf(a.getFecha()));p.setTime(6,java.sql.Time.valueOf(a.getHoraVencimiento()));p.executeUpdate();
                try(ResultSet r=p.getGeneratedKeys()){if(r.next())a.setId(String.valueOf(r.getLong(1)));}
                try(PreparedStatement et=c.prepareStatement("INSERT INTO OPTEM_ETIQUETAS_ACTIVIDAD(id_actividad,materia) VALUES (?,?)")){
                    et.setLong(1,Long.parseLong(a.getId()));et.setString(2,a.getMateria());et.executeUpdate();
                }
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }catch(SQLException ex){throw new IllegalStateException("Error guardando actividad: "+ex.getMessage(),ex);}
    }
    public void actualizarActividad(Actividad a){
        String sql="UPDATE ACTIVIDADES SET titulo=?,tipo=?,descripcion=?,fecha=?,hora_vencimiento=? WHERE id_actividad=? AND correo_usuario=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,a.getNombre());p.setString(2,a.getTipo());p.setString(3,a.getDescripcion());p.setDate(4,java.sql.Date.valueOf(a.getFecha()));p.setTime(5,java.sql.Time.valueOf(a.getHoraVencimiento()));p.setLong(6,Long.parseLong(a.getId()));p.setString(7,correo());p.executeUpdate();
        }catch(SQLException ex){throw new IllegalStateException("No se pudo editar actividad: "+ex.getMessage(),ex);}
    }
    public void eliminarActividad(String id){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("DELETE FROM ACTIVIDADES WHERE id_actividad=? AND correo_usuario=?")){
            p.setLong(1,Long.parseLong(id));p.setString(2,correo());p.executeUpdate();
        }catch(SQLException ex){throw new IllegalStateException("Error eliminando actividad: "+ex.getMessage(),ex);}
    }
    public List<Actividad> obtenerActividadesPorMateria(String m){return getAllActividades().stream().filter(a->m.equals(a.getMateria())).collect(Collectors.toList());}
    public void actualizarEstadoActividad(String id,String nuevoEstado){
        String estado=switch(nuevoEstado.toUpperCase(Locale.ROOT)){
            case "COMPLETADO","COMPLETADA"->"completada";
            case "EN_PROCESO","EN_PROGRESO"->"en_progreso";
            default->"pendiente";
        };
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try(PreparedStatement q=c.prepareStatement("SELECT estado,tipo FROM ACTIVIDADES WHERE id_actividad=? AND correo_usuario=? FOR UPDATE")){
                q.setLong(1,Long.parseLong(id));q.setString(2,correo());
                try(ResultSet r=q.executeQuery()){
                    if(!r.next())throw new IllegalArgumentException("Actividad inexistente");
                    boolean sumar="completada".equals(estado) && !"completada".equalsIgnoreCase(r.getString(1)) && "Tarea".equalsIgnoreCase(r.getString(2));
                    try(PreparedStatement u=c.prepareStatement("UPDATE ACTIVIDADES SET estado=? WHERE id_actividad=? AND correo_usuario=?")){
                        u.setString(1,estado);u.setLong(2,Long.parseLong(id));u.setString(3,correo());u.executeUpdate();
                    }
                    if(sumar){try(PreparedStatement xp=c.prepareStatement("UPDATE USUARIOS SET xp_total=xp_total+50 WHERE correo=?")){
                        xp.setString(1,correo());xp.executeUpdate();}}
                }
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }catch(Exception ex){throw new IllegalStateException("Error actualizando estado: "+ex.getMessage(),ex);}
    }
}
