package com.optem.controllers;

import com.optem.ui.AddAccountDialog.UserData;

import javafx.scene.control.Label;

public class AddAccountController {

    /**
     * Procesa y valida los datos para vincular una cuenta.
     * Retorna un objeto UserData si es válido, o null si hay errores.
     */
    public UserData procesarVinculacion(String usuario, String password, String nombre, String apellido, Label lblError) {
        
        if (!password.matches("\\d{7}")) {
            lblError.setText("La contraseña debe tener exactamente 7 números.");
            lblError.setVisible(true);
            return null;
        }

        if (usuario.isEmpty() || nombre.isEmpty()) {
            lblError.setText("El nombre y el correo son obligatorios.");
            lblError.setVisible(true);
            return null;
        }

        if (!usuario.contains("@")) {
            if (usuario.matches("\\d+")) {
                usuario += "@alumno.uaemex.mx";
            } else {
                usuario += "@uaemex.mx";
            }
        }

        lblError.setVisible(false);
        return new UserData(usuario, password, nombre, apellido);
    }
}