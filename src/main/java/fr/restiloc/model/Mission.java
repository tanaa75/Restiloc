package fr.restiloc.model;

public class Mission {
    private int idMission;
    private String heureDebut;
    private String ville;
    private String immatriculation;
    private String nomGarage;
    private boolean indisponible;

    public Mission(int idMission, String heureDebut, String ville, String immatriculation, String nomGarage) {
        this.idMission = idMission;
        this.heureDebut = heureDebut;
        this.ville = ville;
        this.immatriculation = immatriculation;
        this.nomGarage = nomGarage;
        this.indisponible = false;
    }

    public int getIdMission() { return idMission; }
    public String getHeureDebut() { return heureDebut; }
    public String getVille() { return ville; }
    public String getImmatriculation() { return immatriculation; }
    public String getNomGarage() { return nomGarage; }
    public boolean isIndisponible() { return indisponible; }
    public void setIndisponible(boolean indisponible) { this.indisponible = indisponible; }
}
