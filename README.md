# Demo_Java_Jeu_Sodoku

Application de **Sudoku en Java Swing**, utilisant le thème **Nimbus**, compatible avec **Java 8** et **NetBeans 8.0.2**.

Debut
<img width="1034" height="842" alt="Capture d&#39;écran 2026-11-17 172316" src="https://github.com/user-attachments/assets/9ce82e71-5a48-4182-af4b-3172cea2bfda" />

Fin
<img width="1034" height="842" alt="Capture d&#39;écran 2026-11-17 172402" src="https://github.com/user-attachments/assets/e73e8870-d1b7-4ce4-b6cb-281d62aece55" />


Le projet utilise **Ant**, sans Maven ni bibliothèque externe.

## Fonctionnalités

- Choix de la taille : **4×4, 6×6, 9×9, 12×12 et 16×16**.
- Trois niveaux : **Facile, Moyen et Difficile**.
- Génération de grilles à **solution unique**.
- Protection des valeurs de départ.
- Mise en évidence de la ligne, de la colonne et du bloc sélectionnés.
- Détection des répétitions en rouge.
- Vérification des réponses.
- Ajout d’un indice.
- Annulation de la dernière saisie.
- Effacement d’une case.
- Recommencement de la grille.
- Affichage de la solution.
- Chronomètre et journal de la partie.

## Prérequis

- **JDK 8** pour compiler le projet.
- **NetBeans 8.0.2**, ou **Apache Ant** pour construire en ligne de commande.
- **Java 8 ou supérieur** pour exécuter le fichier JAR.

## Installation dans NetBeans

1. Téléchargez ou clonez le dépôt.
2. Lancez **NetBeans 8.0.2**.
3. Sélectionnez **Fichier → Ouvrir un projet**.
4. Ouvrez le dossier `Demo_Java_Jeu_Sodoku`.
5. Vérifiez que le projet utilise un **JDK 8**.
6. Appuyez sur **F6** pour lancer l’application.

Classe principale :

```java
demo_java_jeu_sodoku.Demo_Java_Jeu_Sodoku
```

## Exécution du JAR

```bash
java -jar dist/Demo_Java_Jeu_Sodoku.jar
```

## Règles du jeu

Complétez la grille avec les nombres de **1 à N**, où **N** correspond à la taille choisie.

Chaque nombre doit apparaître une seule fois dans :

- Chaque ligne.
- Chaque colonne.
- Chaque bloc.

| Taille | Dimensions des blocs | Valeurs autorisées |
| --- | --- | --- |
| 4×4 | 2 lignes × 2 colonnes | 1 à 4 |
| 6×6 | 2 lignes × 3 colonnes | 1 à 6 |
| 9×9 | 3 lignes × 3 colonnes | 1 à 9 |
| 12×12 | 3 lignes × 4 colonnes | 1 à 12 |
| 16×16 | 4 lignes × 4 colonnes | 1 à 16 |

## Utilisation

1. Choisissez la **taille** et la **difficulté**.
2. Cliquez sur **Nouvelle partie**.
3. Sélectionnez une case à compléter.
4. Saisissez une valeur au clavier ou utilisez le pavé jaune.
5. Utilisez **Vérifier la grille** pour contrôler vos réponses.

Les valeurs de départ sont affichées en **bleu** et ne sont pas modifiables.

Une case rouge indique une répétition ou une réponse incorrecte signalée après vérification.

> Le changement de taille ou de difficulté s’applique lors du lancement d’une nouvelle partie.

### Commandes

| Commande | Action |
| --- | --- |
| Flèches directionnelles | Se déplacer dans la grille |
| Retour arrière / Suppr | Effacer dans la case sélectionnée |
| Ctrl+Z | Annuler la dernière saisie |
| Vérifier la grille | Signaler les réponses incorrectes |
| Donner un indice | Compléter ou corriger une case |
| Effacer la case | Vider la case sélectionnée |
| Recommencer | Revenir à la grille de départ |
| Afficher la solution | Révéler la solution et terminer la partie |


### Interface graphique

`Demo_Java_Jeu_Sodoku.java` gère la fenêtre Swing, le thème Nimbus, les commandes, le chronomètre et les interactions avec la grille.

### Logique du jeu

`SudokuModel.java` gère la génération des grilles, les règles de placement et le contrôle de l’unicité des solutions.

L’interface est écrite directement en Java, sans fichier `.form` du concepteur visuel NetBeans.

## Génération et difficulté

Le générateur part d’une grille complète valide, puis retire des valeurs en conservant une solution unique.

| Difficulté | Proportion de cases retirées visée |
| --- | --- |
| Facile | 40 % |
| Moyen | 52 % |
| Difficile | 62 % |

Le retrait peut s’arrêter avant la cible pour préserver l’unicité ou limiter le temps de calcul à environ cinq secondes.

Ces niveaux correspondent à la densité de la grille ; ils ne constituent pas une évaluation des techniques humaines nécessaires à sa résolution.

## Vérifications réalisées

- Compilation avec le **JDK 8**.
- Construction du JAR avec **Ant**.
- Vérification de **45 grilles** couvrant les cinq tailles et les trois difficultés.
- Contrôle de la validité des solutions et de leur unicité.
- Tests Swing : saisie, vérification, indice, annulation, effacement, victoire, recommencement et passage en 16×16.

## Limites

- Aucune sauvegarde des parties sur disque.
- Fermer l’application fait perdre la partie courante.
- Une nouvelle partie remplace la précédente.
- Aucun accès réseau nécessaire.
