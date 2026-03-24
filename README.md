#  ÉTUDE DE CAS RESTILOC
**Date :** 23/03/2026

##  Mission 1 – Gestion des prestations de remise en état (PREE)

Pour intégrer la gestion des PREE (Prestations de Remise en État), voici les nouvelles tables à ajouter au schéma relationnel :

* **PREE** (`refDossier`, `numOrdre`, `libellePrestation`, `description`, `photo`)
  * **Clé Primaire (PK)** : (`refDossier`, `numOrdre`)
  * **Clé Étrangère (FK)** : `refDossier` en référence à `Dossier(refDossier)`

* **PREE_Peinture** (`refDossier`, `numOrdre`, `elementCarrosserie`, `traitement`)
  * **Clé Primaire (PK)** : (`refDossier`, `numOrdre`, `elementCarrosserie`)
  * **Clé Étrangère (FK)** : (`refDossier`, `numOrdre`) en référence à `PREE(refDossier, numOrdre)`

* **PREE_Piece** (`refDossier`, `numOrdre`, `referencePiece`, `libellePiece`, `quantite`)
  * **Clé Primaire (PK)** : (`refDossier`, `numOrdre`, `referencePiece`)
  * **Clé Étrangère (FK)** : (`refDossier`, `numOrdre`) en référence à `PREE(refDossier, numOrdre)`

---

##  Mission 2 – Gestion des rendez-vous

### 2.1 Justification de la classe abstraite
Une classe abstraite est une classe qui ne peut pas être instanciée (il est impossible de créer un objet avec `new Expertise()`). Dans le contexte de Restiloc, une "expertise" générique n'existe pas : c'est obligatoirement soit un `Pool_Garage`, soit un `RDV_Client`.
Le choix d'une classe abstraite permet donc de factoriser (regrouper) les attributs et les méthodes communs à ces deux types de rendez-vous afin d'éviter la duplication de code, tout en forçant l'utilisation exclusive de ses classes filles.

### 2.2 Méthode AjouterExpertisePool
```csharp
public void AjouterExpertisePool(string dossier, DateTime dateHeure, string lieu, string adresse, string immat, string marque, string modele) 
{
    Pool_Garage nouveauPool = new Pool_Garage(dossier, dateHeure, lieu, adresse, immat, marque, modele);
    this.lesExpertises.Add(nouveauPool);
}
```

### 2.3 Méthode GetMotif
```csharp
public string GetMotif() 
{
    return motif;
}
```

### 2.4 Méthode LesExpertisesIndispos
```csharp
public List<Expertise> LesExpertisesIndispos() {
    List<Expertise> listeFiltree = new List<Expertise>();
    foreach (Expertise exp in this.lesExpertises) {
        if (exp.GetIndisponibilite() != null) {
            listeFiltree.Add(exp);
        }
    }
    return listeFiltree;
}
```

### 2.5 Méthode NbIndisponibilites
```csharp
public int NbIndisponibilites(string leMotif) {
    int compteur = 0;
    foreach (Expertise exp in this.lesExpertises) {
        Indisponibilite indispo = exp.GetIndisponibilite();
        if (indispo != null && indispo.GetMotif() == leMotif) {
            compteur++;
        }
    }
    return compteur;
}
```

---

##  Mission 3 – Application mobile pour les experts

### 3.1 Requêtes SQL
**Requête 1 :** Liste des informations triée par date/heure croissantes
```sql
SELECT M.dateMission, M.heureDebut, V.immatriculation, V.marque, V.modele, E.nom, E.prenom
FROM MissionExpertise M
JOIN VehiculeExpertise V ON M.idMission = V.idMission
JOIN Expert E ON M.idExpert = E.idExpert
ORDER BY M.dateMission ASC, M.heureDebut ASC;
```

**Requête 2 :** Nombre de véhicules expertisés en 2018 pour chaque expert
```sql
SELECT E.nom, E.prenom, COUNT(M.idMission) AS NbVehicules
FROM Expert E
JOIN MissionExpertise ME ON E.idExpert = ME.idExpert
WHERE YEAR(ME.dateMission) = 2018
GROUP BY E.idexpert, E.nom, E.prenom;
```

**Requête 3 :** Liste des garages ayant plus de 100 missions créées
```sql
SELECT G.idGarage, G.nom, G.ville, G.tel
FROM Garage G
JOIN MissionExpertise M ON G.idGarage = M.idGarage
GROUP BY G.idGarage, G.nom, G.ville, G.tel
HAVING COUNT(M.idMission) > 100;
```

### 3.3 Corrections de code
* **Côté Serveur (Document 6 - Fichier `getLesMissionsByIdExpert.php`) :**
  Il faut ajouter `G.nom` à la requête. 
  `$sql = "SELECT idMission, heureDebut, ville, V.immatriculation AS immatriculation, marque, modele, G.nom AS nomGarage";`
* **Côté Client (Document 9 - Fichier `ListAdapter.java`) :**
  Ajouter une ligne dans le bloc `try` :
  `txtGarage.setText(list.get(position).getString("nomGarage"));` (en supposant que le TextView `txtGarage` a bien été déclaré juste au-dessus).

---

##  Mission 4 – Cybersécurité et conformité RGPD

### 4.1.1 Analyse des risques (DICP)
* **Risque 1 (Confidentialité) :** Vol ou fuite des données personnelles des clients ou des experts (noms, adresses, mots de passe).
  * **Mesure :** Chiffrement des données en base et utilisation du protocole HTTPS pour les échanges.
* **Risque 2 (Intégrité) :** Modification frauduleuse des frais de remise en état par un expert ou un pirate.
  * **Mesure :** Mise en place d'un système de droits stricts (contrôle d'accès) et de triggers d'audit pour tracer toutes les modifications.

### 4.1.2 Critère DICP de la photo PREE
C’est le critère de **Preuve** parce que la photo fige l'état réel du véhicule. En cas de contestation du client sur les frais facturés, la photo prouve que le dommage existait réellement au moment de l'expertise.

### 4.2.1 Chiffrement vs Hachage
* **Chiffrement** : réversible (avec une clé, on retrouve le texte en clair).
* **Hachage** : irréversible. C'est préférable car même si un pirate accède à la BDD, il ne pourra pas relire les mots de passe.
* **Algorithme recommandé ANSSI :** SHA-256 (ou plus récent comme Argon2).

### 4.3 Faille d'injection SQL et Contrôle d'Accès
* **Faille classique :** Sans contrôle, un pirate modifie `?idExpert=1` en `?idExpert=1 OR 1=1` = il voit toute la base (Injection SQL). Ou bien `?idExpert=3` pour surveiller un collègue (faille IDOR).
* **Correction PDO (requête préparée sécurisée) :**
```php
$sql .= " WHERE idExpert = :id AND dateMission = '".DATE("Y-m-d")."' ORDER BY heureDebut ASC";
$stmt = $bdd->prepare($sql);
$stmt->execute(['id' => $id]);
$resultat = $stmt->fetchAll(PDO::FETCH_ASSOC);
```

### 4.4.1 Trigger d'audit de suppression
```sql
CREATE TRIGGER trig_audit_delete_mission
AFTER DELETE ON missionexpertise
FOR EACH ROW
BEGIN
    INSERT INTO audit_suppression (idMissionSupprimee, dateSuppression)
    VALUES (OLD.idMission, NOW());
END;
```

---

##  Missions 5 & 6 – Pôle Pratique (Prototypage & Challenges)

Le dépôt contient l'implémentation complète du prototype applicatif :
- **Base de données SQL (`sql/restiloc_db.sql`)** : Structure + Données générées + Triggers de sécurité.
- **API REST PHP (`api/`)** : Webservice PHP sécurisé via PDO (lutte contre injections SQL) renvoyant du JSON.
- **Client Lourd JavaFX (`src/main/`)** : Interface graphique consommant l'API (parse le JSON et peuple une `TableView`).

###  Preuves des Challenges de Cybersécurité (Mission 6)
1. **La Faille IDOR démontrée** : Nous avons forcé le client JavaFX à requêter `idExpert=2` (Sherlock Holmes) à la place de l'expert connecté (Lupin), prouvant la nécessité d'implémenter des Jetons (Tokens de Session).
2. **Audit de sécurité fonctionnel** : Toute exécution de `DELETE FROM missionexpertise` est interceptée silencieusement par le Trigger SQL qui inscrit l'auteur et la date dans `AUDIT_SUPPRESSION`.
3. **Logique métier "Absences"** : Ajout d'un système visuel permettant aux experts de signaler instantanément l'indisponibilité d'un client. Changement dynamique du modèle (`Mission.boolean indisponible`) avec application au layout visuel en temps-réel (RowFactory en rouge).
