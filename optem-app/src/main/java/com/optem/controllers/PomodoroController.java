package com.optem.controllers;

import com.optem.logic.NotificacionManager;

import javafx.application.Platform;

public class PomodoroController {

    private int tiempoTotalSegundos; 
    private int tiempoActualSegundos;
    private boolean enMarcha = false;
    private boolean esDescanso = false;
    
    // Tiempos por defecto para el alumno
    private int minutosEnfoque = 25;
    private int minutosDescanso = 5;

    public PomodoroController() {
        this.tiempoTotalSegundos = minutosEnfoque * 60;
        this.tiempoActualSegundos = tiempoTotalSegundos;
    }

    public void configurarTiempos(int minutosEnf, int minutosDes) {
        this.minutosEnfoque = minutosEnf;
        this.minutosDescanso = minutosDes;
        setModo(this.esDescanso); // Refrescar
    }

    public void setModo(boolean descanso) {
        this.esDescanso = descanso;
        this.tiempoTotalSegundos = (descanso ? minutosDescanso : minutosEnfoque) * 60;
        resetearTemporizador();
    }
    
    public boolean isDescanso() {
        return esDescanso;
    }

    public boolean alternarTemporizador() {
        if (enMarcha) {
            enMarcha = false;
        } else {
            if (tiempoActualSegundos <= 0) resetearTemporizador(); 
            enMarcha = true;
        }
        return enMarcha;
    }

    public void resetearTemporizador() {
        tiempoActualSegundos = tiempoTotalSegundos;
        enMarcha = false;
    }

    public boolean tick() {
        if (tiempoActualSegundos > 0) {
            tiempoActualSegundos--;
            return false; // Sigue corriendo
        } else {
            enMarcha = false;
            finalizarPomodoro();
            return true; // Terminó
        }
    }
    
    public double getProgreso() {
        if (tiempoTotalSegundos == 0) return 0;
        return 1.0 - ((double) tiempoActualSegundos / tiempoTotalSegundos);
    }
    
    private void finalizarPomodoro() {
        Platform.runLater(() -> {
            if (!esDescanso) {
                NotificacionManager.mostrarNotificacion("🍅 ¡Tiempo de Enfoque Terminado!", "Gran trabajo. Tómate un respiro antes de continuar.", "INFO");
            } else {
                NotificacionManager.mostrarNotificacion("☕ ¡Descanso Terminado!", "El receso acabó. Volvamos a la acción.", "INFO");
            }
        });
    }

    public String getTiempoFormateado() {
        int minutos = tiempoActualSegundos / 60;
        int segundos = tiempoActualSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
}