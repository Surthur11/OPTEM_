package com.optem.database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Persistencia de agenda del docente; no registra XP ficticio. */
public final class AgendaDocenteDAO {
    private AgendaDocenteDAO() {}
    public record Evento(long id, Integer idGrupo, String titulo, String descripcion, String hora, String tipo, boolean completado) {}

    public static List<Evento> eventos(String docente, LocalDate fecha) throws SQLException {
        List<Evento> eventos = new ArrayList<>();
        String sql = "SELECT id_evento,id_grupo,titulo,descripcion,hora,tipo,completado " +
            "FROM OPTEM_AGENDA_DOCENTE WHERE correo_docente=? AND fecha=? ORDER BY hora,id_evento";
        try (Connection c=MySQL.conectar(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,docente); p.setDate(2,Date.valueOf(fecha));
            try(ResultSet rs=p.executeQuery()) {
                while(rs.next()) {
                    int grupo=rs.getInt(2);
                    eventos.add(new Evento(rs.getLong(1),rs.wasNull()?null:grupo,rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getString(6),rs.getBoolean(7)));
                }
            }
        }
        return eventos;
    }
    public static void crear(String docente,LocalDate fecha,String hora,String titulo,String descripcion,String tipo) throws SQLException {
        if(docente==null||docente.isBlank()) throw new SQLException("Inicia sesión como docente");
        if(titulo==null||titulo.isBlank()) throw new SQLException("Escribe el título del evento");
        if(hora==null||!hora.matches("(?:[01]\\d|2[0-3]):[0-5]\\d"))throw new SQLException("Usa una hora HH:mm válida");
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(
            "INSERT INTO OPTEM_AGENDA_DOCENTE(correo_docente,fecha,hora,titulo,descripcion,tipo,completado) VALUES(?,?,?,?,?,?,0)")){
            p.setString(1,docente);p.setDate(2,Date.valueOf(fecha));p.setString(3,hora);p.setString(4,titulo.trim());
            p.setString(5,descripcion);p.setString(6,tipo);p.executeUpdate();
        }
    }
    public static void completarEvento(String docente,long id) throws SQLException {
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(
            "UPDATE OPTEM_AGENDA_DOCENTE SET completado=1 WHERE correo_docente=? AND id_evento=?")) {
            p.setString(1,docente);p.setLong(2,id);
            if(p.executeUpdate()!=1)throw new SQLException("No se encontró el evento o ya está completado");
        }
    }
    public static void completarClase(String docente,int grupo,LocalDate fecha,String hora,String titulo,String descripcion) throws SQLException {
        try(Connection c=MySQL.conectar();PreparedStatement p=c.prepareStatement(
            "INSERT INTO OPTEM_AGENDA_DOCENTE(correo_docente,id_grupo,fecha,hora,titulo,descripcion,tipo,completado) " +
            "SELECT ?,g.id_grupo,?,?,?,?, 'clase',1 FROM GRUPOS g WHERE g.id_grupo=? AND g.correo_docente=? " +
            "ON DUPLICATE KEY UPDATE completado=1")) {
            p.setString(1,docente);p.setDate(2,Date.valueOf(fecha));p.setString(3,hora);
            p.setString(4,titulo);p.setString(5,descripcion);p.setInt(6,grupo);p.setString(7,docente);
            if(p.executeUpdate()==0)throw new SQLException("No se pudo completar la clase de este docente");
        }
    }
}
