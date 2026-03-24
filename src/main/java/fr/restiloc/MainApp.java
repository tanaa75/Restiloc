package fr.restiloc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JavaFX RestiLoc.
 * Charge la vue FXML et affiche la fenêtre principale.
 *
 * ⚠️ Ne pas lancer cette classe directement !
 * Utiliser Launcher.java pour éviter l'erreur "JavaFX runtime components are missing".
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fr/restiloc/view/missions.fxml")
        );
        Scene scene = new Scene(loader.load(), 650, 450);
        stage.setTitle("RestiLoc — Expert Mobile");
        stage.setScene(scene);
        stage.show();
    }
}
