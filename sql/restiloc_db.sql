-- ============================================================
--  RESTILOC - Script complet de création de la base de données
--  Missions 3 & 4 du TP BTS SIO
-- ============================================================

-- -------------------------------------------------------
-- ETAPE 1 : Création des tables
-- -------------------------------------------------------

-- 1. Table des experts (mdp_hash pour le RGPD)
CREATE TABLE IF NOT EXISTS Expert (
    idExpert  INT          PRIMARY KEY AUTO_INCREMENT,
    nom       VARCHAR(50),
    prenom    VARCHAR(50),
    email     VARCHAR(100),
    login     VARCHAR(50),
    mdp_hash  VARCHAR(255) -- Stockage sécurisé : jamais en clair !
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Table des garages
CREATE TABLE IF NOT EXISTS Garage (
    idGarage INT          PRIMARY KEY AUTO_INCREMENT,
    nom      VARCHAR(100),
    ville    VARCHAR(50),
    tel      VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Table des missions d'expertise
CREATE TABLE IF NOT EXISTS MissionExpertise (
    idMission      INT         PRIMARY KEY AUTO_INCREMENT,
    dateMission    DATE,
    heureDebut     TIME,
    idGarage       INT,
    idExpert       INT,
    immatriculation VARCHAR(15),
    CONSTRAINT fk_garage FOREIGN KEY (idGarage) REFERENCES Garage(idGarage),
    CONSTRAINT fk_expert FOREIGN KEY (idExpert) REFERENCES Expert(idExpert)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Table d'audit pour la Mission 4 (Trigger de suppression)
CREATE TABLE IF NOT EXISTS AUDIT_SUPPRESSION (
    idAudit           INT      PRIMARY KEY AUTO_INCREMENT,
    idMissionSupprimee INT,
    dateSuppression   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- ETAPE 1 (suite) : Données de test initiales
-- -------------------------------------------------------

-- Expert Lupin (mdp par défaut : "Resti2026!")
INSERT INTO Expert (nom, prenom, email, login, mdp_hash) VALUES
('Lupin', 'Arsène', 'a.lupin@restiloc.fr', 'alupin',
 '$2y$10$e0MYzXyjpJS7Pd0RVvHwHeFOnNVatB.9.2DkXzZ.v7Xv6YmN9Z9y6');

-- Un premier garage
INSERT INTO Garage (nom, ville, tel) VALUES
("Garage de l'Orb", 'Béziers', '0467000000');

-- -------------------------------------------------------
-- ETAPE 3 : Déclencheur (Trigger) d'audit des suppressions
-- -------------------------------------------------------

DELIMITER //

CREATE TRIGGER IF NOT EXISTS trig_audit_delete_mission
AFTER DELETE ON MissionExpertise
FOR EACH ROW
BEGIN
    INSERT INTO AUDIT_SUPPRESSION (idMissionSupprimee, dateSuppression)
    VALUES (OLD.idMission, NOW());
END //

DELIMITER ;

-- -------------------------------------------------------
-- ETAPE 5 : Jeu d'essai étendu (populate)
-- -------------------------------------------------------

-- Garages supplémentaires
INSERT INTO Garage (nom, ville, tel) VALUES
('Garage des Cimes',    'Montpellier',      '0467112233'),
('Auto Citronelle',     'Lavérune',         '0467445566'),
('Espace Carrosserie',  'Castelnau-le-Lez', '0467778899');

-- Experts supplémentaires (même mdp haché : "Resti2026!")
INSERT INTO Expert (nom, prenom, email, login, mdp_hash) VALUES
('Holmes', 'Sherlock', 's.holmes@restiloc.fr', 'sholmes',
 '$2y$10$e0MYzXyjpJS7Pd0RVvHwHeFOnNVatB.9.2DkXzZ.v7Xv6YmN9Z9y6'),
('Poirot', 'Hercule',  'h.poirot@restiloc.fr', 'hpoirot',
 '$2y$10$e0MYzXyjpJS7Pd0RVvHwHeFOnNVatB.9.2DkXzZ.v7Xv6YmN9Z9y6'),
('Maigret','Jules',    'j.maigret@restiloc.fr','jmaigret',
 '$2y$10$e0MYzXyjpJS7Pd0RVvHwHeFOnNVatB.9.2DkXzZ.v7Xv6YmN9Z9y6');

-- Missions du jour (apparaîtront dans l'API)
INSERT INTO MissionExpertise (dateMission, heureDebut, idGarage, idExpert, immatriculation) VALUES
(CURDATE(), '08:30:00', 1, 1, 'AA-229-XY'),  -- Lupin au Garage de l'Orb
(CURDATE(), '10:45:00', 2, 1, 'BC-548-ZX'),  -- Lupin à Montpellier
(CURDATE(), '14:00:00', 3, 2, 'AB-346-TU'),  -- Holmes à Lavérune
(CURDATE(), '16:15:00', 4, 2, 'DE-998-RS'),  -- Holmes à Castelnau
(CURDATE(), '09:00:00', 2, 3, 'FG-112-HI');  -- Poirot à Montpellier

-- Missions de demain (pour tester le filtrage par date)
INSERT INTO MissionExpertise (dateMission, heureDebut, idGarage, idExpert, immatriculation) VALUES
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 1, 1, 'ZZ-999-ZZ'),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00', 4, 3, 'XY-001-AB');

-- Missions passées (pour l'historique)
INSERT INTO MissionExpertise (dateMission, heureDebut, idGarage, idExpert, immatriculation) VALUES
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '15:00:00', 2, 1, 'OLD-123-OK');
