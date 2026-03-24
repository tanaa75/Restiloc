<?php
// ============================================================
//  RESTILOC - Service Web : getLesMissionsByIdExpert.php
//  ETAPE 2 : Version sécurisée (Requêtes préparées PDO)
//  Répond à la Question 4.3.1 (anti-injection SQL)
// ============================================================

header('Content-Type: application/json; charset=utf-8');

// Inclusion de la configuration PDO
require_once __DIR__ . '/config.php';

// --- Récupération et validation de l'idExpert ---
// filter_input évite l'injection SQL ET la faille IDOR simple
$idExpert = filter_input(INPUT_GET, 'idExpert', FILTER_VALIDATE_INT);

if (!$idExpert) {
    // Paramètre absent ou non numérique => erreur 400
    http_response_code(400);
    echo json_encode(["erreur" => "Paramètre idExpert manquant ou invalide."]);
    exit;
}

// --- Requête préparée sécurisée (Mission 3 & Question 4.3.1) ---
// Remplace la concaténation dangereuse du Document 6 original
$sql = "SELECT M.idMission,
               M.heureDebut,
               G.ville,
               M.immatriculation,
               G.nom AS nomGarage
        FROM   MissionExpertise M
        JOIN   Garage G ON M.idGarage = G.idGarage
        WHERE  M.idExpert   = :idExpert
          AND  M.dateMission = :dateJour
        ORDER BY M.heureDebut ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    'idExpert' => $idExpert,
    'dateJour' => date('Y-m-d'),
]);

$resultat = $stmt->fetchAll();

echo json_encode($resultat);
