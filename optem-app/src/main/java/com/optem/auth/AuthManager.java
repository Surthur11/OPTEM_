package com.optem.auth;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AuthManager {
    private final String patternInstitucional = "^[a-zA-Z0-9._%+-]+@(alumno\\.uaemex\\.mx|uaemex\\.mx)$";
    private List<Map<String, String>> usuariosRegistrados;

    public AuthManager() {
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try (Reader reader = new FileReader("data/usuarios.json")) {
            usuariosRegistrados = new Gson().fromJson(reader, new TypeToken<List<Map<String, String>>>(){}.getType());
        } catch (Exception e) {
            usuariosRegistrados = new ArrayList<>();
            System.err.println("No se pudo cargar la base de usuarios registrados.");
        }
    }

    public Map<String, String> iniciarSesion(String correo, String numCuenta) {
        Map<String, String> res = new HashMap<>();
        
        if (!Pattern.compile(patternInstitucional).matcher(correo).matches()) {
            res.put("status", "error");
            res.put("message", "Usa un correo @alumno.uaemex.mx o @uaemex.mx");
            return res;
        }

        Optional<Map<String, String>> usuario = usuariosRegistrados.stream()
                .filter(u -> u.get("correo").equalsIgnoreCase(correo) && u.get("num_cuenta").equals(numCuenta))
                .findFirst();

        if (usuario.isPresent()) {
            res.put("status", "success");
            res.put("nombre", usuario.get().get("nombre"));
            res.put("file_key", usuario.get().get("num_cuenta"));
        } else {
            res.put("status", "success");
            res.put("nombre", "Invitado UAEMéx");
            res.put("file_key", "default"); 
        }
        return res;
    }
}