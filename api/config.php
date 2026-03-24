<?php
// ============================================================
//  RESTILOC - Configuration de la connexion à la base de données
//  Ce fichier est inclus dans tous les services web.
//  Ne jamais versionner ce fichier en situation réelle !
// ============================================================

$host    = 'localhost';
$db      = 'restiloc_db';
$user    = 'root';       // Modifier si besoin (MAMP/WAMP)
$pass    = '';           // Modifier si besoin
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";

$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION, // Erreurs via exceptions
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,       // Tableaux associatifs
    PDO::ATTR_EMULATE_PREPARES   => false,                  // Vraies requêtes préparées
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (\PDOException $e) {
    // En production : ne jamais afficher le message d'erreur !
    http_response_code(500);
    die(json_encode(["erreur" => "Erreur de connexion au SGBD."]));
}
