package fr.restiloc.model;

/**
 * Classe métier représentant une mission d'expertise.
 * Correspond à la vue VehiculeExpertise + MissionExpertise + Garage de la BDD.
 * Utilisée pour alimenter la TableView JavaFX.
 */
public class Mission {
    private int    idMission;
    private String heureDebut;
    private String ville;
    private String immatriculation;
    private String nomGarage;
    private boolean indisponible; // Nouveau pour Challenge 3

    public Mission(int idMission, String heureDebut, String ville,
                   String immatriculation, String nomGarage) {
        this.idMission      = idMission;
        this.heureDebut     = heureDebut;
        this.ville          = ville;
        this.immatriculation = immatriculation;
        this.nomGarage      = nomGarage;
        this.indisponible   = false; // Par défaut, non indisponible
    }

    // --- Getters (obligatoires pour PropertyValueFactory de JavaFX) ---
    public int    getIdMission()      { return idMission; }
    public String getHeureDebut()     { return heureDebut; }
    public String getVille()          { return ville; }
    public String getImmatriculation(){ return immatriculation; }
    public String getNomGarage()      { return nomGarage; }

    // --- Challenge 3 : Gestion Indisponibilité ---
    public boolean isIndisponible() { return indisponible; }
    public void setIndisponible(boolean indisponible) { this.indisponible = indisponible; }
}
