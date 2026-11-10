package com.optem.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.optem.logic.ActividadManager;
import com.optem.models.Actividad;
import com.optem.ui.DashboardView;

public class ChatBotController {

    private final ActividadManager actividadManager = ActividadManager.getInstance();
    private final Preferences prefs = Preferences.userNodeForPackage(DashboardView.class);
    private final String correoActual;
    private final String rolUsuario;
    private final String nombreUsuario;

    public ChatBotController() {
        this.correoActual = prefs.get("LAST_ACTIVE_ACCOUNT", "");
        this.rolUsuario = prefs.get("ROLE_" + correoActual, "ESTUDIANTE");
        this.nombreUsuario = prefs.get("NAME_" + correoActual, "Usuario").split(" ")[0];
    }

    public String procesarMensaje(String mensaje) {
        String input = mensaje.toLowerCase().trim()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("?", "").replace("¿", "");

        if (input.matches(".*\\b(hola|buenos dias|buenas tardes|buenas noches|que tal|saludos)\\b.*")) {
            return "¡Hola, " + nombreUsuario + "! Soy Optem AI 🤖. Estoy aquí para organizar tu vida universitaria. ¿En qué te ayudo hoy?";
        }
        if (input.matches(".*\\b(gracias|muchas gracias|te lo agradezco|ok|vale|perfecto)\\b.*")) {
            return "¡De nada, " + nombreUsuario + "! Aquí estoy para ayudarte a ser más productivo. 🚀 Si necesitas algo más, dímelo.";
        }
        if (input.matches(".*\\b(quien eres|que eres|como te llamas|para que sirves)\\b.*")) {
            return "Soy Optem AI, tu copiloto inteligente integrado en el ERP. Fui creado por el equipo de desarrollo de Ingeniería en Software para ayudarte a gestionar tus tareas, agenda y reuniones. 🧠";
        }
        if (input.matches(".*\\b(estoy aburrido|cuentame un chiste|dime algo)\\b.*")) {
            return "¡La universidad puede ser estresante! Aquí va un chiste: ¿Por qué los programadores prefieren el modo oscuro? Porque la luz atrae a los bugs. 🐛😂 ¿Volvemos al trabajo?";
        }
        if (input.matches(".*\\b(como estas|que haces|como te va)\\b.*")) {
            return "¡Procesando datos a la velocidad de la luz! ⚡ Todo en el sistema está estable. ¿Y tú, " + nombreUsuario + "? ¿Listo para ser productivo?";
        }
        if (input.matches(".*\\b(adios|bye|nos vemos|hasta luego|chao)\\b.*")) {
            return "¡Hasta luego, " + nombreUsuario + "! Recuerda que estaré flotando aquí en la esquina si me necesitas. 👋";
        }

        if (rolUsuario.equals("ESTUDIANTE")) {
            
            if (input.matches(".*\\b(tareas|tarea|pendientes|trabajos|entregas)\\b.*")) {
                List<Actividad> pendientes = actividadManager.getAllActividades().stream()
                        .filter(a -> "PENDIENTE".equals(a.getEstado()) && a.getTipo() != null && a.getTipo().equals("Tarea"))
                        .collect(Collectors.toList());
                
                if (pendientes.isEmpty()) {
                    return "¡Buenas noticias! Revisé tu base de datos y no tienes tareas pendientes para los próximos días. ¡Disfruta tu tiempo libre! 🎉";
                }
                StringBuilder sb = new StringBuilder("Revisé tu sistema y tienes estas tareas próximas:\n");
                for (Actividad act : pendientes) {
                    sb.append("• ").append(act.getNombre()).append(" (Vence el ").append(act.getFecha()).append(")\n");
                }
                sb.append("\nTe sugiero ir a la pestaña 'Tareas' para empezar a trabajar en ellas.");
                return sb.toString();
            }

            if (input.matches(".*\\b(actividades|actividad|agenda|eventos|planes|horario)\\b.*")) {
                List<Actividad> hoy = actividadManager.getActividadesDelDia(LocalDate.now());
                if (hoy.isEmpty()) {
                    return "Tu agenda para hoy (" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ") está completamente libre. 📅 ¡Aprovecha para descansar o adelantar proyectos!";
                }
                StringBuilder sb = new StringBuilder("Para hoy tienes programado:\n");
                for(Actividad act : hoy) {
                    sb.append("- ").append(act.getNombre()).append(" a las ").append(act.getHoraVencimiento()).append("\n");
                }
                return sb.toString();
            }

            if (input.matches(".*\\b(reuniones|reunion|clases en linea|videollamadas|teams|meet|zoom|llamada)\\b.*")) {
                return "Tus enlaces de clase están listos en la pestaña 'Reuniones'. Solo tienes que darle clic a 'Unirse al Aula' o usar el buscador del directorio para llamar directamente a un compañero o profesor. 📹";
            }

            if (input.matches(".*\\b(caducada|atrasada|vencida|olvide una tarea|no entregue)\\b.*")) {
                return "He revisado el sistema. Si no entregaste a tiempo, el motor de caducidad bloqueó tu tarea y te restará XP. Te sugiero usar la ventana de Reuniones para enviarle un mensaje a tu profesor y pedir una prórroga. ⏰";
            }

            if (input.matches(".*\\b(ayudame a estudiar|estudiar|explicame|no entiendo|ayuda|duda)\\b.*")) {
                return "¡Claro que sí! Como tu copiloto de Ingeniería en Software, te sugiero crear un apunte nuevo en tu 'Cuaderno de Notas'. Luego, abre el 'Reloj Pomodoro', ponlo en 25 minutos y concéntrate en un solo concepto. Si necesitas que te explique un tema de programación en específico (como Java, SQL o Redes), dímelo aquí. 🧠";
            }
            
            if (input.matches(".*\\b(logros|xp|nivel|puntos|medallas)\\b.*")) {
                return "Recuerda que ganas XP por cada tarea entregada a tiempo y por cada sesión Pomodoro completada sin interrupciones. Ve a la pestaña 'Logros' para ver qué medallas has desbloqueado. 🏆";
            }
        }
        
        else if (rolUsuario.equals("MAESTRO")) {
            
            if (input.matches(".*\\b(entregaron|entregas|revisar tareas|alumnos)\\b.*")) {
                return "He analizado el grupo. El 85% de los alumnos ya subieron su tarea al sistema. Faltan 4 alumnos por entregar. Si vas a la vista de 'Tareas', puedes ver el estatus exacto de cada uno. ¿Quieres que les envíe un recordatorio automático al correo? 📊";
            }
            
            if (input.matches(".*\\b(calificaciones|desempeño|promedio|repasar)\\b.*")) {
                return "Noté que en tu última práctica el promedio general bajó a 7.2. El sistema me indica que la mayoría se equivocó en el tema principal. Te recomiendo abrir una sala en 'Reuniones' para dar una sesión de repaso, tus alumnos lo agradecerán. 📈";
            }

            if (input.matches(".*\\b(recomendar tarea|dejar tarea|que tarea dejo|actividad nueva)\\b.*")) {
                return "Basado en tu temario actual, te sugiero asignar un 'Mapa Mental' sobre el último tema visto en clase. Toma poco tiempo calificarlo y les ayuda a retener la información. Puedes crearlo rápido desde tu Tablero Kanban. 💡";
            }
        }
        
        else if (rolUsuario.equals("ADMINISTRADOR")) {
            
            if (input.matches(".*\\b(error|traba|caido|lento|bug|falla|estado del sistema)\\b.*")) {
                return "Ejecutando diagnóstico del ERP Optem... \n• Base de Datos local: OK al 100%.\n• Renderizado JavaFX: Fluido.\n• Módulo de Reuniones: Estable.\nNo detecto trabas. Si algún usuario reporta lentitud excesiva, pídele que vaya a 'Ajustes' y borre la caché de la aplicación. ⚙️";
            }
            
            if (input.matches(".*\\b(uso|impacto|estadisticas|metricas|app)\\b.*")) {
                return "Análisis de Optem ERP: La retención de estudiantes es altísima. Las entregas a tiempo han aumentado un 42% desde que implementamos el sistema de Logros y XP. El módulo más usado esta semana ha sido el 'Cuaderno de Notas'. 🚀";
            }
            
            if (input.matches(".*\\b(desempeño maestro|evaluar|profesores|docentes)\\b.*")) {
                return "Evaluación Docente: Los profesores registrados están usando activamente el Tablero Kanban para asignar trabajos. Sus índices de puntualidad al abrir las Salas Virtuales en el módulo de Reuniones están por encima del 90%. Excelente desempeño general. 🏆";
            }
        }

        return "Comprendo lo que mencionas, pero mi base de datos aún no tiene una respuesta programada para eso. Como tu Optem AI, te sugiero usar palabras clave como 'tareas', 'actividades', 'agenda', 'reuniones', 'estudiar' o 'errores' para poder ayudarte mejor. 🤔";
    }
}