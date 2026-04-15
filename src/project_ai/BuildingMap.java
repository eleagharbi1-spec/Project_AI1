package project_ai;
/**
 * Représente la carte du bâtiment (grille 2D).
 */
public class BuildingMap {

    public static final char WALL  = '#';
    public static final char FREE  = '.';
    public static final char START = 'S';
    public static final char EXIT  = 'E';

    public int     rows, cols;
    public char[][] grid;
    // Positions de départ et de sortie, initialisées à des valeurs invalides parce qu'on les détecte dans la carte
    public int[] startPos = {-1, -1};// Initialisé à une position invalide
    public int[] exitPos  = {-1, -1};// Initialisé à une position invalide

    public BuildingMap(char[][] source) {
        this.rows = source.length;// Nombre de lignes
        this.cols = source[0].length;// Nombre de colonnes (en supposant que toutes les lignes ont la même longueur)
        this.grid = new char[rows][cols];// Création de la grille
        for (int r = 0; r < rows; r++) {// Copie de chaque ligne de la source dans la grille
            this.grid[r] = source[r].clone();// Clone pour éviter les références partagées car les tableaux sont des objets en Java
        }
        detect();// Détection des positions de départ et de sortie dans la grille
    }

    private void detect() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == START) startPos = new int[]{r, c};
                if (grid[r][c] == EXIT)  exitPos  = new int[]{r, c};
            }
        }
    }

    public char getCell(int r, int c) { return grid[r][c]; }// Retourne le caractère à la position (r, c) de la grille

    public void setCell(int r, int c, char val) { grid[r][c] = val; }// Modifie le caractère à la position (r, c) de la grille

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;// Vérifie si la position (r, c) est à l'intérieur des limites de la grille
    }
}