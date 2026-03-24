package fr.restiloc.controller;

import fr.restiloc.model.Mission;
import fr.restiloc.service.RestiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.TableRow;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur JavaFX de la vue missions.fxml.
 * Gère l'affichage des missions du jour via l'API RestiLoc.
 */
public class MissionsController {

    // Identifiant de l'expert connecté
    // (CHALLENGE 1 : Faille IDOR - On change l'ID à 2 pour espionner Holmes !)
    private static final int ID_EXPERT = 2;

    // Injection des composants FXML
    @FXML private TableView<Mission>         tableMissions;
    @FXML private TableColumn<Mission, String> colHeure;
    @FXML private TableColumn<Mission, String> colVille;
    @FXML private TableColumn<Mission, String> colGarage;
    @FXML private TableColumn<Mission, String> colImmat;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;

    /**
     * Méthode appelée automatiquement lors du chargement de la vue FXML.
     * Initialise les colonnes et charge les données.
     */
    @FXML
    public void initialize() {
        // --- Liaison colonnes ↔ propriétés du modèle Mission ---
        colHeure.setCellValueFactory( new PropertyValueFactory<>("heureDebut"));
        colVille.setCellValueFactory( new PropertyValueFactory<>("ville"));
        colGarage.setCellValueFactory(new PropertyValueFactory<>("nomGarage"));
        colImmat.setCellValueFactory( new PropertyValueFactory<>("immatriculation"));

        // Affichage de la date du jour
        String dateAujourdhui = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblDate.setText("Missions du " + dateAujourdhui);

        // --- Challenge 3 (Bonus) : Styliser les lignes si indisponible ---
        tableMissions.setRowFactory(tv -> new TableRow<Mission>() {
            @Override
            protected void updateItem(Mission item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isIndisponible()) {
                    // Si l'expert a marqué le client absent -> Ligne rouge clair
                    setStyle("-fx-background-color: #ef5350; -fx-text-background-color: white;");
                } else {
                    setStyle("");
                }
            }
        });

        // Premier chargement automatique
        chargerDonnees();
    }

    /**
     * Challenge 3 : Marquer la mission sélectionnée comme Indisponible.
     */
    @FXML
    private void marquerIndisponible() {
        // 1. Récupérer la ligne sélectionnée
        Mission selectedMission = tableMissions.getSelectionModel().getSelectedItem();
        
        if (selectedMission != null) {
            // 2. Modifier le modèle
            selectedMission.setIndisponible(true);
            
            // 3. Forcer le rafraîchissement visuel du tableau
            tableMissions.refresh();
            
            // 4. Mettre à jour le texte du statut
            lblStatut.setStyle("-fx-text-fill: #ef5350; -fx-font-weight: bold;");
            lblStatut.setText("Mission " + selectedMission.getIdMission() + 
                              " marquée INDISPONIBLE (Client absent).");
        } else {
            lblStatut.setStyle("-fx-text-fill: #ffb74d;");
            lblStatut.setText("Veuillez sélectionner une mission d'abord !");
        }
    }

    /**
     * Appelle l'API et remplit la TableView.
     * Relié au bouton "Actualiser" (onAction="#chargerDonnees").
     */
    @FXML
    private void chargerDonnees() {
        lblStatut.setText("");
        try {
            tableMissions.getItems().setAll(RestiClient.fetchMissions(ID_EXPERT));
            if (tableMissions.getItems().isEmpty()) {
                lblStatut.setText("Aucune mission aujourd'hui pour cet expert.");
            }
        } catch (Exception e) {
            lblStatut.setText("❌ Erreur de connexion à l'API : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
