# 🚗 ÉTUDE DE CAS RESTILOC
**Date :** 23/03/2026

## 📋 Mission 1 – Gestion des prestations de remise en état (PREE)

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

## 📅 Mission 2 – Gestion des rendez-vous

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

## 📱 Mission 3 – Application mobile pour les experts

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

## 🔒 Mission 4 – Cybersécurité et conformité RGPD

### 4.1.1 Analyse des risques (DICP)
* **Risque 1 (Confidentialité) :** Vol ou fuite des données personnelles des clients ou des experts (noms, adresses, mots de passe).
  * **Mesure :** Chiffrement des données en base et utilisation du protocole HTTPS pour les échanges.
* **Risque 2 (Intégrité) :** Modification frauduleuse des frais de remise en état par un expert ou un pirate.
  * **Mesure :** Mise en place d'un système de droits stricts (contrôle d'accès) et de triggers d'audit pour tracer toutes les modifications.

### 4.1.2 Critère DICP de la photo PREE
C’est le critère de **Preuve** parce que la photo fige l'état réel du véhicule. En cas de contestation du client sur les frais facturés, la photo prouve que le dommage existait réellement au moment de l'expertise.

### 4.2.1 Chiffrement vs Hachage
* Le **chiffrement** est réversible (avec une clé de déchiffrement, on retrouve le mot de passe en clair).
* Le **hachage** est irréversible. C'est préférable car même si un pirate vole la base de données, il ne pourra pas relire les mots de passe des experts.
* **Algorithme recommandé par l’ANSSI :** SHA-256 (ou plus récent comme Argon2).

### 4.3.1 Faille d'injection SQL
* **Ligne concernée :** `$sql .= " WHERE idExpert = ".$id;`
* **Explication :** La variable `$id` vient directement de l'URL (`$_GET['idExpert']`) sans être vérifiée. Un pirate peut écrire `?idExpert=1 OR 1=1`. La requête devient alors toujours vraie et le pirate peut récupérer les missions de tous les experts.
* **Correction avec PDO (requête préparée) :**
```php
$sql .= " WHERE idExpert = :id AND dateMission = '".DATE("Y-m-d")."' ORDER BY heureDebut ASC";
$stmt = $bdd->prepare($sql);
$stmt->execute(['id' => $id]);
$resultat = $stmt->fetchAll(PDO::FETCH_ASSOC);
```

### 4.3.2 Faille de contrôle d'accès (IDOR)
L'IDOR (Insecure Direct Object Reference) se produit quand un système ne vérifie pas si l'utilisateur a le droit d'accéder à l'objet qu'il demande. 
Un pirate peut simplement modifier le chiffre dans l'URL pour mettre `?idExpert=3` ou `?idExpert=1`. Comme le serveur ne vérifie pas l'identité réelle de celui qui fait la demande, l'attaquant pourra espionner l'emploi du temps et les missions de tous ses collègues.

### 4.4.1 Trigger d'audit de suppression
```sql
CREATE TRIGGER trig_audit_delete_mission
ON MissionExpertise
AFTER DELETE
AS
BEGIN
    INSERT INTO AUDIT_SUPPRESSION (idMissionSupprimee, dateSuppression)
    SELECT idMission, GETDATE()
    FROM deleted;
END;
```
