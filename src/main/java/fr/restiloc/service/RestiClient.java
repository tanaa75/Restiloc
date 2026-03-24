package fr.restiloc.service;

import fr.restiloc.model.Mission;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Service HTTP qui consomme l'API REST de RestiLoc.
 * Remplace la logique de GestionURL.java de l'application Android.
 * Utilise java.net.http.HttpClient (disponible depuis Java 11).
 */
public class RestiClient {

    // URL de base de l'API (port 8080 = Laragon en mode non-standard)
    private static final String URL_API =
        "http://localhost:8080/restiloc/api/getLesMissionsByIdExpert.php?idExpert=";

    /**
     * Récupère la liste des missions du jour pour un expert donné.
     * @param idExpert identifiant de l'expert
     * @return liste de missions (peut être vide si aucune mission aujourd'hui)
     */
    public static List<Mission> fetchMissions(int idExpert) throws Exception {
        // 1. Création du client HTTP
        HttpClient client = HttpClient.newHttpClient();

        // 2. Construction de la requête GET
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_API + idExpert))
                .build();

        // 3. Envoi de la requête et récupération de la réponse
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // 4. Parsing JSON → liste de Mission
        List<Mission> missions = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.body());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            missions.add(new Mission(
                obj.getInt("idMission"),
                obj.getString("heureDebut"),
                obj.getString("ville"),
                obj.getString("immatriculation"),
                obj.getString("nomGarage")
            ));
        }
        return missions;
    }
}
