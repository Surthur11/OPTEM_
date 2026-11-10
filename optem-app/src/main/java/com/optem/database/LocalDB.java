package com.optem.database;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class LocalDB {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, List<Map<String, Object>>> cachePersonal = new HashMap<>();
    private static List<Map<String, Object>> cacheGlobal = null;

    private static final String DATA_PATH = "data/";

    public static List<Map<String, Object>> cargarPersonal(String key) {
        if (cachePersonal.containsKey(key)) return cachePersonal.get(key);
        
        File file = new File(DATA_PATH + "personal_" + key + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                List<Map<String, Object>> data = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
                cachePersonal.put(key, data);
                return data;
            } catch (IOException e) {
                System.err.println("Error al cargar datos personales: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public static void guardarPersonal(String key, List<Map<String, Object>> lista) {
        cachePersonal.put(key, lista);
        File file = new File(DATA_PATH + "personal_" + key + ".json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar datos personales: " + e.getMessage());
        }
    }

    public static long obtenerActividadesPendientesHoy(String key) {
        String hoy = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return cargarPersonal(key).stream()
            .filter(a -> hoy.equals(a.get("fecha")))
            .filter(a -> !(boolean)a.getOrDefault("hecho", false))
            .count();
    }

    public static void sumarXP(String key, int puntos) {
        System.out.println("¡Logro desbloqueado! + " + puntos + " XP para el usuario " + key);
    }
}