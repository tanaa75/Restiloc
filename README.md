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

---

## 📝 Questions de Synthèse & Évaluation

**1. Quelle modélisation de base de données avez-vous choisie pour différencier les prestations "Peinture" et "Pièce" ?**
> J'ai choisi l'héritage de données, avec une table mère "PREE" contenant les informations globales, et deux tables filles "PREE_Peinture" et "PREE_Piece" qui héritent de la clé primaire de PREE.

**2. Justifiez l’utilisation d’une classe abstraite pour la classe Expertise dans le diagramme de classes.**
> Une classe abstraite permet d'avoir le code en commun (lieu, date...) pour ne pas se répéter. De plus, une "Expertise" générique n'existe pas dans Restiloc, ça doit toujours être obligatoirement soit un RDV Client, soit un Pool Garage (les classes filles).

**3. Expliquez l’architecture applicative permettant l’échange de données entre le SGBD, le serveur PHP et le client JavaFX.**
> Le client JavaFX envoie directement des requêtes SQL au serveur PHP via une URL. Le serveur PHP traduit ça en JSON pour la base de données, puis renvoie le résultat au JavaFX pour l'afficher dans le tableau.

**4. Pourquoi la présence d’un identifiant en clair dans l’URL d’une API constitue-t-elle une vulnérabilité de type IDOR ?**
> C'est de l'IDOR car n'importe qui peut changer l'identifiant dans son navigateur (ex: changer `?idExpert=1` en `idExpert=2`) pour voir les données d'un autre utilisateur, vu que le serveur ne vérifie pas l'identité avec un token de session.

**5. En quoi l’utilisation de HTTPS est-elle indispensable même si l’application nécessite une authentification ?**
> Même avec un mot de passe, si on utilise HTTP simple, les données circulent en clair sur le réseau wifi. Un pirate sur le même réseau peut alors sniffer et lire les identifiants envoyés. Le HTTPS, lui, chiffre la connexion.

**6. Expliquez la différence entre une donnée à caractère personnel et une donnée sensible selon les critères du RGPD.**
> Une donnée personnelle permet d'identifier quelqu'un (un nom, un email, un matricule). Une donnée sensible est beaucoup plus confidentielle (religion, santé, opinion politique...) et son traitement est en principe totalement interdit sans très fortes garanties.

**7. Pourquoi le hachage des mots de passe est-il techniquement préférable à leur chiffrement ?**
> Parce que le chiffrement peut toujours être inversé si on a la clé. Le hachage va détruire mathématiquement l'information de départ : on ne peut jamais retrouver le mot de passe en clair, même si la base entière est piratée.

**8. Quel critère du DICP est principalement visé par l’archivage obligatoire d’une photo pour chaque prestation ?**
> C'est le critère de **Preuve**. La photo sert de preuve irréfutable pour certifier que le dommage au véhicule était existant au moment de l'expertise, ce qui protège le garage contre les contestations du client.

**9. Expliquez le fonctionnement du déclencheur `trig_audit_delete_mission` pour assurer la traçabilité des suppressions.**
> Ce trigger se déclenche JUSTE AVANT que la ligne soit supprimée de la BDD. Il prend l'identifiant de la mission concernée et la date du jour, pour les copier dans la table `AUDIT_SUPPRESSION` afin qu'on garde une trace avant de perdre les données.

**10. Comment la classe `RestiClient` transforme-t-elle un flux JSON reçu par l’API en une liste d’objets `Mission` ?**
> La classe fait une requête HTTP, récupère la réponse sous forme de texte brut, puis crée un `JSONArray` pour faire une boucle. À chaque tour de boucle, on injecte les champs du JSON dans un  `new Mission(...)` qu'on ajoute à une `ArrayList` finale.
