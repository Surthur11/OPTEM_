package com.optem.database;

import java.sql.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.Preferences;

/** Centraliza JDBC y cuenta consultas de OPTEM sin un INSERT adicional por cada lectura. */
public final class MySQL {
    private MySQL(){}
    private static final ConcurrentMap<String,AtomicLong> consultas=new ConcurrentHashMap<>();
    private static final ScheduledExecutorService monitor=Executors.newSingleThreadScheduledExecutor(r->{
        Thread t=new Thread(r,"optem-consultas-sync");t.setDaemon(true);return t;
    });
    static {
        monitor.scheduleWithFixedDelay(MySQL::sincronizarConsultas,8,8,TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(MySQL::sincronizarConsultas));
    }
    /** Conexion sin instrumentar, para evitar recursion al persistir contadores. */
    private static Connection abrirDirecta() throws SQLException{
        String host=System.getenv().getOrDefault("OPTEM_DB_HOST","100.67.119.35");
        String port=System.getenv().getOrDefault("OPTEM_DB_PORT","3306");
        String db=System.getenv().getOrDefault("OPTEM_DB_NAME","optem_db");
        String user=System.getenv("OPTEM_DB_USER"),pass=System.getenv("OPTEM_DB_PASSWORD");
        if(user==null||user.isBlank()||pass==null||pass.isBlank())
            throw new SQLException("Configura OPTEM_DB_USER y OPTEM_DB_PASSWORD en VS Code.");
        return DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+db+
            "?useUnicode=true&characterEncoding=UTF-8&connectTimeout=6000&socketTimeout=10000",user,pass);
    }
    public static Connection conectar() throws SQLException{
        Connection raw=abrirDirecta();
        Preferences pref=Preferences.userNodeForPackage(com.optem.ui.DashboardView.class);
        String correo=pref.get("LAST_ACTIVE_ACCOUNT","");
        return (Connection)Proxy.newProxyInstance(MySQL.class.getClassLoader(),new Class<?>[]{Connection.class},
            (proxy,method,args)->{
                try{
                    Object result=method.invoke(raw,args);
                    if(result instanceof Statement st){
                        Class<?> iface=result instanceof CallableStatement?CallableStatement.class:
                            result instanceof PreparedStatement?PreparedStatement.class:Statement.class;
                        return Proxy.newProxyInstance(MySQL.class.getClassLoader(),new Class<?>[]{iface},
                            (sproxy,smethod,sargs)->{
                                try{
                                    Object value=smethod.invoke(st,sargs);
                                    if(smethod.getName().startsWith("execute") && !correo.isBlank())
                                        consultas.computeIfAbsent(correo,k->new AtomicLong()).incrementAndGet();
                                    return value;
                                }catch(InvocationTargetException ex){throw ex.getCause();}
                            });
                    }
                    return result;
                }catch(InvocationTargetException ex){throw ex.getCause();}
            });
    }
    private static void sincronizarConsultas(){
        for(Map.Entry<String,AtomicLong> e:consultas.entrySet()){
            long cantidad=e.getValue().getAndSet(0);
            if(cantidad<=0)continue;
            try(Connection c=abrirDirecta();PreparedStatement p=c.prepareStatement(
                "INSERT INTO OPTEM_CONTADOR_CONSULTAS(correo_usuario,total) VALUES(?,?) " +
                "ON DUPLICATE KEY UPDATE total=total+VALUES(total),actualizado_en=NOW()")){
                p.setString(1,e.getKey());p.setLong(2,cantidad);p.executeUpdate();
            }catch(Exception error){
                e.getValue().addAndGet(cantidad);
                System.err.println("OPTEM: contador de consultas pendiente: "+error.getMessage());
            }
        }
    }
}
