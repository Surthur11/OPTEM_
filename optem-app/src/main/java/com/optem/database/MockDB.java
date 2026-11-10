package com.optem.database;
import com.optem.controllers.ReinscripcionController.MateriaDTO;
import java.sql.*;
import java.util.*;
import java.util.prefs.Preferences;

/** Adaptador de compatibilidad para las pantallas originales de OPTEM.
 * El nombre histórico se mantiene para no cambiar las vistas JavaFX.
 * Todas las lecturas de usuarios, materias y grupos son reales de MySQL.
 */
public final class MockDB {
    private MockDB() {}
    public static String sesion() { return Preferences.userNodeForPackage(com.optem.ui.DashboardView.class).get("LAST_ACTIVE_ACCOUNT", ""); }
    public static boolean autenticar(String correo,String cuenta,String rol) {
        String sql="SELECT r.nombre_rol FROM USUARIOS u JOIN ROLES r ON r.id_rol=u.id_rol WHERE LOWER(u.correo)=LOWER(?) AND u.numero_cuenta=? AND u.activo=1";
        try(Connection c=MySQL.conectar(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,correo);p.setString(2,cuenta);
            try(ResultSet rs=p.executeQuery()){
                if(!rs.next())return false;
                String actual=rs.getString(1);
                return (rol.equals("MAESTRO") && actual.equalsIgnoreCase("Docente")) || (rol.equals("ESTUDIANTE") && actual.equalsIgnoreCase("Estudiante")) || (rol.equals("ADMINISTRADOR") && actual.equalsIgnoreCase("Administrador"));
            }
        } catch(SQLException ex){throw new IllegalStateException("No se pudo validar con MySQL: "+ex.getMessage(),ex);}
    }
    public static boolean esAdministrador(String correo,String cuenta) {
        if(correo==null || correo.isBlank() || cuenta==null || cuenta.isBlank())return false;
        String sql="SELECT 1 FROM USUARIOS u JOIN ROLES r ON u.id_rol=r.id_rol WHERE LOWER(u.correo)=LOWER(?) AND u.numero_cuenta=? AND r.nombre_rol='Administrador' AND u.activo=1";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,correo);p.setString(2,cuenta);try(ResultSet r=p.executeQuery()){return r.next();}
        }catch(SQLException ex){throw new IllegalStateException("Error al identificar administrador: "+ex.getMessage(),ex);}
    }
    public static Map<String,String> obtenerPerfilUsuario(){
        Map<String,String> perfil=new HashMap<>();String sql="SELECT nombre,apellidos,carrera,semestre,xp_total FROM USUARIOS WHERE correo=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,sesion());try(ResultSet r=p.executeQuery()){
                if(r.next()){
                    perfil.put("nombre_completo",r.getString("nombre")+" "+r.getString("apellidos"));
                    perfil.put("carrera",Objects.toString(r.getString("carrera"),""));
                    perfil.put("semestre",String.valueOf(r.getInt("semestre")));
                    perfil.put("promedio_general","Sin registrar");perfil.put("avance_creditos","Sin historial académico");
                    perfil.put("creditos_obtenidos","Sin historial académico");perfil.put("situacion","Sin determinar");
                }
            }
        }catch(SQLException e){throw new IllegalStateException("Perfil MySQL: "+e.getMessage(),e);}return perfil;
    }
    public static Map<String,String> obtenerEstudiantePorCuenta(String cuenta){
        String q="SELECT numero_cuenta,nombre,apellidos,correo FROM USUARIOS WHERE numero_cuenta=?";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,cuenta);try(ResultSet r=p.executeQuery()){
                if(!r.next())return null;
                Map<String,String> m=new HashMap<>();m.put("numero_cuenta",r.getString(1));m.put("nombre_completo",r.getString(2)+" "+r.getString(3));m.put("correo",r.getString(4));return m;
            }
        }catch(SQLException e){throw new IllegalStateException(e);}
    }
    public static List<String> obtenerDocumentos(String cuenta){return new ArrayList<>();}
    public static String obtenerRecompensasGenerales(){return "Aún no hay resultados académicos registrados.";}
    private static Map<String,String> convertirGrupo(ResultSet r) throws SQLException {
        Map<String,String> m=new HashMap<>();
        m.put("id_grupo",r.getString("id_grupo"));m.put("clave",r.getString("clave_materia"));
        m.put("nombre",r.getString("nombre_materia"));m.put("profesor",Objects.toString(r.getString("correo_docente"),"Sin asignar"));
        m.put("grupo",r.getString("nombre_grupo"));m.put("horario",Objects.toString(r.getString("horario_texto"),"Por definir"));
        m.put("total_alumnos",r.getString("total_alumnos"));
        m.put("reunion","");m.put("tarea","");m.put("mensaje","Sin avisos registrados");return m;
    }
    public static List<Map<String,String>> obtenerMateriasDelAlumno(String correo){
        String q="SELECT g.id_grupo,g.clave_materia,m.nombre_materia,g.nombre_grupo,g.correo_docente,g.horario_texto,(SELECT COUNT(*) FROM CARGA_ACADEMICA x WHERE x.id_grupo=g.id_grupo AND x.estado_inscripcion='inscrito') AS total_alumnos FROM CARGA_ACADEMICA ca JOIN GRUPOS g ON g.id_grupo=ca.id_grupo JOIN MATERIAS m ON m.clave_materia=g.clave_materia WHERE ca.correo_estudiante=? AND ca.estado_inscripcion='inscrito' AND g.archivado=0 ORDER BY m.nombre_materia";
        List<Map<String,String>> out=new ArrayList<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,correo);try(ResultSet r=p.executeQuery()){while(r.next())out.add(convertirGrupo(r));}
        }catch(SQLException e){throw new IllegalStateException("Materias del alumno: "+e.getMessage(),e);}return out;
    }
    public static List<Map<String,String>> obtenerTodosLosGrupos(){
        String q="SELECT g.id_grupo,g.clave_materia,m.nombre_materia,g.nombre_grupo,g.correo_docente,g.horario_texto,(SELECT COUNT(*) FROM CARGA_ACADEMICA x WHERE x.id_grupo=g.id_grupo AND x.estado_inscripcion='inscrito') AS total_alumnos FROM GRUPOS g JOIN MATERIAS m ON m.clave_materia=g.clave_materia WHERE (g.correo_docente=? OR EXISTS (SELECT 1 FROM USUARIOS u JOIN ROLES r ON r.id_rol=u.id_rol WHERE u.correo=? AND r.nombre_rol='Administrador')) AND g.archivado=0 ORDER BY g.id_grupo DESC";
        List<Map<String,String>> out=new ArrayList<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(q)){
            p.setString(1,sesion());p.setString(2,sesion());try(ResultSet r=p.executeQuery()){while(r.next())out.add(convertirGrupo(r));}
        }catch(SQLException e){throw new IllegalStateException("Grupos del docente: "+e.getMessage(),e);}return out;
    }
    public static void guardarNuevoGrupo(String clave,String nombre,String profesor,String grupo,String horario){
        String lookup="SELECT clave_materia FROM MATERIAS WHERE UPPER(nombre_materia)=UPPER(?) OR clave_materia=? LIMIT 1";
        try(Connection c=MySQL.conectar();PreparedStatement lp=c.prepareStatement(lookup)){
            lp.setString(1,nombre);lp.setString(2,nombre);String materia;
            try(ResultSet r=lp.executeQuery()){if(!r.next())throw new IllegalArgumentException("La materia no está registrada en MATERIAS: "+nombre);materia=r.getString(1);}
            String docente=sesion();
            try(PreparedStatement r=c.prepareStatement("SELECT ro.nombre_rol FROM USUARIOS u JOIN ROLES ro ON ro.id_rol=u.id_rol WHERE u.correo=?")){
                r.setString(1,docente);try(ResultSet roles=r.executeQuery()){
                    if(!roles.next())throw new IllegalArgumentException("Tu sesión no corresponde a USUARIOS");
                    if("Administrador".equalsIgnoreCase(roles.getString(1))){
                        try(PreparedStatement pr=c.prepareStatement("SELECT u.correo FROM USUARIOS u JOIN ROLES x ON x.id_rol=u.id_rol WHERE x.nombre_rol='Docente' AND (u.correo=? OR UPPER(CONCAT(u.nombre,' ',u.apellidos))=UPPER(?)) LIMIT 1")){
                            pr.setString(1,profesor);pr.setString(2,profesor);
                            try(ResultSet rr=pr.executeQuery()){
                                if(!rr.next())throw new IllegalArgumentException("Asigna un docente registrado en USUARIOS.");
                                docente=rr.getString(1);
                            }
                        }
                    } else if(!"Docente".equalsIgnoreCase(roles.getString(1)))throw new IllegalArgumentException("Solo docentes y administradores pueden crear grupos.");
                }
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO GRUPOS(clave_materia,nombre_grupo,correo_docente,horario_texto,codigo_unico) VALUES (?,?,?,?,?)")){
                String codigo=grupo==null||grupo.isBlank()?clave:grupo;
                p.setString(1,materia);p.setString(2,codigo);p.setString(3,docente);p.setString(4,horario);
                p.setString(5,codigo);p.executeUpdate();
            }
        }catch(SQLException e){throw new IllegalStateException("Crear grupo: "+e.getMessage(),e);}
    }
    public static void inscribirAlumno(String correoAlumno,String claveGrupo){
        String sql="INSERT INTO CARGA_ACADEMICA(correo_estudiante,id_grupo,id_periodo,estado_inscripcion) SELECT u.correo,g.id_grupo,p.id_periodo,'inscrito' FROM USUARIOS u JOIN ROLES r ON u.id_rol=r.id_rol JOIN GRUPOS g ON g.id_grupo=? AND g.correo_docente=? JOIN PERIODOS_ESCOLARES p ON p.estado='activo' WHERE r.nombre_rol='Estudiante' AND (LOWER(u.correo)=LOWER(?) OR u.numero_cuenta=? OR UPPER(CONCAT(u.nombre,' ',u.apellidos)) LIKE UPPER(?)) ORDER BY p.id_periodo DESC LIMIT 1";
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,Integer.parseInt(claveGrupo));p.setString(2,sesion());p.setString(3,correoAlumno.trim());p.setString(4,correoAlumno.trim());p.setString(5,"%"+correoAlumno.trim()+"%");
            int n=p.executeUpdate();if(n!=1)throw new IllegalArgumentException("No se inscribió: verifica alumno, grupo y periodo escolar activo.");
        }catch(SQLException e){throw new IllegalStateException("Inscribir alumno: "+e.getMessage(),e);}
    }
    public static List<MateriaDTO> obtenerMateriasReinscripcion(){
        List<MateriaDTO> out=new ArrayList<>();
        String sql="SELECT m.clave_materia,m.nombre_materia,m.creditos,m.es_mixta,plan.semestre_sugerido,g.nombre_grupo,g.horario_texto " +
            "FROM MATERIAS m JOIN OPTEM_PLAN_MATERIAS plan ON plan.clave_materia=m.clave_materia " +
            "JOIN USUARIOS u ON u.correo=? AND u.semestre IS NOT NULL " +
            "LEFT JOIN OPTEM_MATERIAS_ADELANTABLES ad ON ad.clave_materia=m.clave_materia " +
            "LEFT JOIN GRUPOS g ON g.clave_materia=m.clave_materia AND g.archivado=0 " +
            "WHERE plan.semestre_sugerido=u.semestre+1 OR (plan.semestre_sugerido>u.semestre+1 AND ad.autorizada=1) " +
            "ORDER BY plan.semestre_sugerido,m.nombre_materia,g.id_grupo";
        class Pendiente{
            String nombre,creditos,categoria;boolean mixto;List<String> opciones=new ArrayList<>();
            Pendiente(String n,String c,boolean m,String cat){nombre=n;creditos=c;mixto=m;categoria=cat;opciones.add("— No cursar —");}
        }
        Map<String,Pendiente> materias=new LinkedHashMap<>();
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,sesion());try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    String clave=r.getString(1);int semestre=r.getInt(5);
                    Pendiente actual=materias.computeIfAbsent(clave,k->new Pendiente(rValor(r,2),rValor(r,3)+" créditos",rBoolean(r,4),"SEMESTRE "+semestre));
                    String grupo=r.getString(6);
                    if(grupo!=null)actual.opciones.add(grupo+" · "+Objects.toString(r.getString(7),"Horario por definir"));
                }
            }
        }catch(SQLException e){throw new IllegalStateException("Materias por semestre: "+e.getMessage()+". Ejecuta la migración de mejoras.",e);}
        for(Pendiente d:materias.values())out.add(new MateriaDTO(d.nombre,d.creditos,d.mixto,"#3D5A4D",d.categoria,d.opciones.toArray(String[]::new)));
        return out;
    }
    private static String rValor(ResultSet r,int i){try{return r.getString(i);}catch(SQLException ex){throw new IllegalStateException(ex);}}
    private static boolean rBoolean(ResultSet r,int i){try{return r.getBoolean(i);}catch(SQLException ex){throw new IllegalStateException(ex);}}
    public static Map<String,String> obtenerHorarioIA(String preferencia){return new HashMap<>();}
}
