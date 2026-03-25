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

public class RestiClient {

    private static final String URL_API = "http://localhost:8080/restiloc/api/getLesMissionsByIdExpert.php?idExpert=";

    public static List<Mission> fetchMissions(int idExpert) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + idExpert)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
