package com.optem.controllers;
import com.optem.database.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class ReinscripcionController {
    public static class MateriaDTO {
        public String titulo, creditos, colorBorde, categoria;
        public boolean mixta;
        public String[] opciones;
        public MateriaDTO(String t,String c,boolean m,String cb,String cat,String[] o){titulo=t;creditos=c;mixta=m;colorBorde=cb;categoria=cat;opciones=o;}
    }
    public static class BloqueHorario {
        public String texto,colorHex;public int colDia,rowHora,duracion;
        public BloqueHorario(String t,int c,int r,int d,String h){texto=t;colDia=c;rowHora=r;duracion=d;colorHex=h;}
    }
    public boolean periodoAbierto(){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("SELECT valor FROM CONFIG_SISTEMA WHERE clave='REINSCRIPCION_ABIERTA'")){
            try(ResultSet r=p.executeQuery()){return r.next() && ("1".equals(r.getString(1)) || "true".equalsIgnoreCase(r.getString(1)));}
        }catch(SQLException e){throw new IllegalStateException("No fue posible consultar el periodo de reinscripción: "+e.getMessage(),e);}
    }
    public List<MateriaDTO> getMateriasDisponibles(){return MockDB.obtenerMateriasReinscripcion();}
    public Map<String,String> generarHorarioInteligente(String preferencia){return Collections.emptyMap();}

    /** Registra solicitud real para grupo existente; no crea inscripciones ficticias. */
    public void inscribirAlumnoMateria(String correo,String materia,String opcion){
        if(opcion==null || opcion.startsWith("—"))return;
        String nombreGrupo=opcion.split(" · ",2)[0].trim();
        String sql="INSERT INTO SOLICITUD_REINSCRIPCION(correo_estudiante,id_grupo,id_periodo,estado) " +
          "SELECT ?,g.id_grupo,p.id_periodo,'pendiente' FROM GRUPOS g JOIN MATERIAS m ON m.clave_materia=g.clave_materia " +
          "JOIN PERIODOS_ESCOLARES p ON p.estado='activo' " +
          "JOIN USUARIOS u ON u.correo=? JOIN OPTEM_PLAN_MATERIAS plan ON plan.clave_materia=g.clave_materia " +
          "LEFT JOIN OPTEM_MATERIAS_ADELANTABLES ad ON ad.clave_materia=g.clave_materia " +
          "WHERE UPPER(m.nombre_materia)=UPPER(?) AND g.nombre_grupo=? AND g.archivado=0 AND " +
          "(plan.semestre_sugerido=u.semestre+1 OR (plan.semestre_sugerido>u.semestre+1 AND ad.autorizada=1)) " +
          "AND (SELECT valor FROM CONFIG_SISTEMA WHERE clave='REINSCRIPCION_ABIERTA')='1' ORDER BY p.id_periodo DESC LIMIT 1";
        try(Connection c=MySQL.conectar();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,correo);ps.setString(2,correo);ps.setString(3,materia);ps.setString(4,nombreGrupo);
            if(ps.executeUpdate()!=1)throw new IllegalArgumentException("No hay grupo/periodo activo para "+materia);
        }catch(SQLIntegrityConstraintViolationException ex){throw new IllegalArgumentException("Ya habías solicitado inscripción a "+materia,ex);}
        catch(SQLException ex){throw new IllegalStateException("No se pudo registrar "+materia+": "+ex.getMessage(),ex);}
    }
    public void inscribirAlumnoMateria(String correo,String materia){throw new IllegalArgumentException("Selecciona un grupo para la materia "+materia);}
    /** Guarda todas las materias elegidas de forma atómica. */
    public void registrarSeleccion(String correo,Map<String,String> elecciones){
        if(elecciones==null || elecciones.isEmpty())throw new IllegalArgumentException("No elegiste materias");
        if(!periodoAbierto())throw new IllegalStateException("El periodo de reinscripción está cerrado");
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try{
                int periodo;
                try(PreparedStatement q=c.prepareStatement("SELECT id_periodo FROM PERIODOS_ESCOLARES WHERE estado='activo' ORDER BY id_periodo DESC LIMIT 1");ResultSet r=q.executeQuery()){
                    if(!r.next())throw new IllegalStateException("No existe un periodo escolar activo.");
                    periodo=r.getInt(1);
                }
                for(Map.Entry<String,String> e:elecciones.entrySet()){
                    String opcion=e.getValue();
                    if(opcion==null || opcion.startsWith("—"))continue;
                    String grupo=opcion.split(" · ",2)[0].trim();
                    int idGrupo;
                    try(PreparedStatement q=c.prepareStatement(
                        "SELECT g.id_grupo FROM GRUPOS g JOIN MATERIAS m ON m.clave_materia=g.clave_materia " +
                        "JOIN OPTEM_PLAN_MATERIAS plan ON plan.clave_materia=g.clave_materia JOIN USUARIOS u ON u.correo=? " +
                        "LEFT JOIN OPTEM_MATERIAS_ADELANTABLES ad ON ad.clave_materia=g.clave_materia " +
                        "WHERE UPPER(m.nombre_materia)=UPPER(?) AND g.nombre_grupo=? AND g.archivado=0 AND " +
                        "(plan.semestre_sugerido=u.semestre+1 OR (plan.semestre_sugerido>u.semestre+1 AND ad.autorizada=1)) LIMIT 1")){
                        q.setString(1,correo);q.setString(2,e.getKey());q.setString(3,grupo);
                        try(ResultSet r=q.executeQuery()){
                            if(!r.next())throw new IllegalArgumentException("El grupo seleccionado no existe: "+e.getKey());
                            idGrupo=r.getInt(1);
                        }
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO SOLICITUD_REINSCRIPCION(correo_estudiante,id_grupo,id_periodo,estado) VALUES (?,?,?,'pendiente')")){
                        p.setString(1,correo);p.setInt(2,idGrupo);p.setInt(3,periodo);p.executeUpdate();
                    }
                }
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }catch(Exception e){throw new IllegalStateException("No se pudo guardar la reinscripción: "+e.getMessage(),e);}
    }
    public void registrarSolicitudCompleta(String correo,String nombre){
        // Las solicitudes individuales fueron guardadas en SOLICITUD_REINSCRIPCION.
    }
    /** Una ficha por alumno y periodo, pero conservando cada materia en sus filas reales. */
    public List<Map<String,String>> obtenerTodasLasSolicitudes(){
        List<Map<String,String>> out=new ArrayList<>();
        String sql="SELECT MIN(sr.id_solicitud),sr.correo_estudiante,u.nombre,u.apellidos," +
            "CASE WHEN SUM(sr.estado='HOJA ENVIADA')=COUNT(*) THEN 'HOJA ENVIADA' ELSE 'pendiente' END," +
            "GROUP_CONCAT(CONCAT(m.nombre_materia,' — ',g.nombre_grupo) ORDER BY m.nombre_materia SEPARATOR ' | ')," +
            "COUNT(*),sr.id_periodo,p.nombre_periodo " +
            "FROM SOLICITUD_REINSCRIPCION sr JOIN USUARIOS u ON sr.correo_estudiante=u.correo " +
            "JOIN GRUPOS g ON g.id_grupo=sr.id_grupo JOIN MATERIAS m ON m.clave_materia=g.clave_materia " +
            "JOIN PERIODOS_ESCOLARES p ON p.id_periodo=sr.id_periodo " +
            "GROUP BY sr.correo_estudiante,u.nombre,u.apellidos,sr.id_periodo,p.nombre_periodo " +
            "ORDER BY MAX(sr.fecha_solicitud) DESC";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next()){
                Map<String,String> m=new HashMap<>();m.put("id",r.getString(1));
                m.put("correo",r.getString(2));m.put("nombre",r.getString(3)+" "+r.getString(4));
                m.put("estado",r.getString(5));m.put("materia",r.getString(6));
                m.put("grupo","");m.put("cantidad",r.getString(7));
                m.put("periodo",r.getString(8));m.put("nombre_periodo",r.getString(9));out.add(m);
            }
        }catch(SQLException ex){throw new IllegalStateException("No fue posible consultar las solicitudes: "+ex.getMessage(),ex);}
        return out;
    }
    /** Elimina el trámite del estudiante para UN periodo, incluidas sus materias. */
    public void eliminarSolicitudCompleta(String correo,int periodo){
        try(Connection c=MySQL.conectar()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement p=c.prepareStatement(
                    "DELETE FROM CARGA_ACADEMICA WHERE correo_estudiante=? AND id_periodo=? " +
                    "AND id_grupo IN (SELECT id_grupo FROM SOLICITUD_REINSCRIPCION WHERE correo_estudiante=? AND id_periodo=?)")){
                    p.setString(1,correo);p.setInt(2,periodo);p.setString(3,correo);p.setInt(4,periodo);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM SOLICITUD_REINSCRIPCION WHERE correo_estudiante=? AND id_periodo=?")){
                    p.setString(1,correo);p.setInt(2,periodo);p.executeUpdate();
                }
                c.commit();
            }catch(Exception ex){c.rollback();throw ex;}
        }catch(Exception ex){throw new IllegalStateException("No se eliminó la reinscripción: "+ex.getMessage(),ex);}
    }
    public void subirHojaDePago(String correo,String ruta){
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement("UPDATE SOLICITUD_REINSCRIPCION SET estado='HOJA ENVIADA' WHERE correo_estudiante=?")){
            p.setString(1,correo);if(p.executeUpdate()==0)throw new IllegalArgumentException("El alumno no tiene solicitudes");
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public Map<String,String> verificarEstadoInscripcion(String correo){
        String sql="SELECT estado FROM SOLICITUD_REINSCRIPCION WHERE correo_estudiante=? ORDER BY fecha_solicitud DESC LIMIT 1";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){
                if(!r.next())return null;
                Map<String,String> m=new HashMap<>();m.put("correo",correo);m.put("estado",r.getString(1));m.put("ruta_hoja_pago","");return m;
            }
        }catch(SQLException ex){throw new IllegalStateException(ex);}
    }
    public List<BloqueHorario> procesarSeleccion(String materia, String seleccion, String color) {
        List<BloqueHorario> bloques = new ArrayList<>();
        if (seleccion == null || seleccion.contains("— No cursar —")) return bloques;
        
        String nombreCorto = materia.split("\\(")[0].trim();
        
        List<Integer> dias = new ArrayList<>();
        if (seleccion.contains("Lun")) dias.add(1);
        if (seleccion.contains("Mar")) dias.add(2);
        if (seleccion.contains("Mié")) dias.add(3);
        if (seleccion.contains("Jue")) dias.add(4);
        if (seleccion.contains("Vie")) dias.add(5);
        if (seleccion.contains("Sáb")) dias.add(6);

        int horaInicio = 7, duracion = 2; 
        
        Matcher m = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*-\\s*(\\d{1,2}):(\\d{2})").matcher(seleccion);
        if (m.find()) {
            horaInicio = Integer.parseInt(m.group(1));
            int horaFin = Integer.parseInt(m.group(3));
            int minutoInicio = Integer.parseInt(m.group(2));
            int minutoFin = Integer.parseInt(m.group(4));
            duracion = Math.max(1,(int)Math.ceil(((horaFin * 60 + minutoFin)-(horaInicio * 60 + minutoInicio))/60.0));
        }

        int rowHora = Math.max(0,Math.min(12,horaInicio - 7));
        duracion = Math.min(duracion,13-rowHora);
        for (int dia : dias) {
            bloques.add(new BloqueHorario(nombreCorto, dia, rowHora, duracion, color));
        }
        return bloques;
    }

    public Map<String,List<String>> getMapaCurricularEstudiante(){
        Map<String,List<String>> mapa=new LinkedHashMap<>();
        for(int i=1;i<=10;i++)mapa.put("Periodo "+i,new ArrayList<>());
        for(MateriaDTO m:getMateriasDisponibles()){
            if(m.categoria.startsWith("SEMESTRE ")){
                String periodo="Periodo "+m.categoria.substring(9);
                mapa.computeIfAbsent(periodo,k->new ArrayList<>()).add(m.titulo+"|transparent");
            }
        }
        return mapa;
    }
}
