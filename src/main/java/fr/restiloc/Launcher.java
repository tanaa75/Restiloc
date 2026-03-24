package fr.restiloc;

/**
 * Point d'entrée du programme.
 *
 * Cette classe est INDISPENSABLE avec Java 11+ et JavaFX.
 * Sans elle, IntelliJ lèverait l'erreur :
 * "Error: JavaFX runtime components are missing, and are required to run this application"
 *
 * La solution : cette classe ne hérite PAS d'Application,
 * donc Java ne cherche pas les modules JavaFX au démarrage.
 * Elle délègue juste le lancement à MainApp.
 */
public class Launcher {
    public static void main(String[] args) {
        // Démarre l'application JavaFX
        javafx.application.Application.launch(MainApp.class, args);
    }
}
