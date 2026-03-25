<?php
require_once 'config.php';

$idExpert = filter_input(INPUT_GET, 'idExpert', FILTER_VALIDATE_INT);

if (!$idExpert) {
    echo json_encode(['error' => 'Paramètre idExpert manquant ou invalide.']);
    exit;
}

$dateJour = date("Y-m-d");

$sql = "SELECT idMission, heureDebut, ville, V.immatriculation AS immatriculation, marque, modele, G.nom AS nomGarage
        FROM MissionExpertise M
        JOIN VehiculeExpertise V ON M.idMission = V.idMission
        JOIN Garage G ON M.idGarage = G.idGarage
        WHERE M.idExpert = :idExpert AND M.dateMission = :dateMission
        ORDER BY heureDebut ASC";

try {
    $stmt = $bdd->prepare($sql);
    $stmt->execute([
        ':idExpert'   => $idExpert,
        ':dateMission' => $dateJour
    ]);

    $resultat = $stmt->fetchAll();

    echo json_encode($resultat);

} catch (PDOException $e) {
    echo json_encode(['error' => 'Erreur lors de l\'exécution de la requête : ' . $e->getMessage()]);
}
?>
