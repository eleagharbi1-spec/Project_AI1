//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project_ai;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GridCanvas extends Canvas {
    static final int CELL = 65;
    static final int M = 5;
    static final Color C_BG = Color.rgb(10, 12, 20);
    static final Color C_WALL = Color.rgb(20, 22, 35);
    static final Color C_WALL_EDG = Color.rgb(35, 38, 58);
    static final Color C_FREE = Color.rgb(235, 238, 248);
    static final Color C_START = Color.rgb(48, 213, 160);
    static final Color C_EXIT = Color.rgb(255, 90, 90);
    static final Color C_VISITED = Color.rgb(82, 175, 255, 0.55);
    static final Color C_PATH = Color.rgb(255, 210, 60);
    static final Color C_GRID = Color.rgb(28, 33, 58, 0.22);
    static final Color C_WHITE_22 = Color.rgb(255, 255, 255, 0.086);
    static final Color C_SHADOW = Color.rgb(0, 0, 0, 0.31);
    Set<String> visitedSet = new HashSet();
    Set<String> pathSet = new HashSet();
    BuildingMap map;
    boolean editMode = false;
    char paintMode = '#';
    int hoverR = -1;
    int hoverC = -1;
    Runnable onCellChanged;

    GridCanvas() {
        super((double)0.0F, (double)0.0F);
        this.setStyle("-fx-background-color: transparent;");
        this.setOnMouseMoved(this::handleHover);
        this.setOnMousePressed(this::handleClick);
        this.setOnMouseDragged(this::handleClick);
        this.setOnMouseExited((e) -> {
            this.hoverR = this.hoverC = -1;
            this.paint();
        });
    }

    void setMap(BuildingMap m) {
        this.map = m;
        this.setWidth((double)(m.cols * 65));
        this.setHeight((double)(m.rows * 65));
        this.paint();
    }

    void reset() {
        this.visitedSet.clear();
        this.pathSet.clear();
    }

    private void handleHover(MouseEvent e) {
        this.hoverC = (int)(e.getX() / (double)65.0F);
        this.hoverR = (int)(e.getY() / (double)65.0F);
        this.paint();
    }

    private void handleClick(MouseEvent e) {
        if (this.editMode && this.map != null) {
            int c = (int)(e.getX() / (double)65.0F);
            int r = (int)(e.getY() / (double)65.0F);
            if (this.map.inBounds(r, c)) {
                char newVal = this.paintMode;
                this.map.setCell(r, c, newVal);
                this.paint();
            }
        }
    }

    public void paint() {
        if (this.map != null) {
            GraphicsContext g = this.getGraphicsContext2D();
            double w = this.getWidth();
            double h = this.getHeight();
            g.setFill(C_BG);
            g.fillRect((double)0.0F, (double)0.0F, w, h);

            for(int r = 0; r < this.map.rows; ++r) {
                for(int c = 0; c < this.map.cols; ++c) {
                    this.drawCell(g, r, c);
                }
            }

            g.setStroke(C_GRID);
            g.setLineWidth((double)0.5F);

            for(int r = 0; r <= this.map.rows; ++r) {
                g.strokeLine((double)0.0F, (double)(r * 65), (double)(this.map.cols * 65), (double)(r * 65));
            }

            for(int c = 0; c <= this.map.cols; ++c) {
                g.strokeLine((double)(c * 65), (double)0.0F, (double)(c * 65), (double)(this.map.rows * 65));
            }

        }
    }

    private void drawCell(GraphicsContext g, int r, int c) {
        double x = (double)(c * 65);
        double y = (double)(r * 65);
        String key = r + "," + c;
        char cell = this.map.getCell(r, c);
        if (cell == '#') {
            this.drawWall(g, x, y);
        } else {
            Color fill;
            if (cell == 'S') {
                fill = C_START;
            } else if (cell == 'E') {
                fill = C_EXIT;
            } else if (this.pathSet.contains(key)) {
                fill = C_PATH;
            } else if (this.visitedSet.contains(key)) {
                fill = C_VISITED;
            } else {
                fill = C_FREE;
            }

            double rx = x + (double)5.0F;
            double ry = y + (double)5.0F;
            double rw = (double)55.0F;
            double rh = (double)55.0F;
            double arc = (double)10.0F;
            g.setFill(fill);
            g.fillRoundRect(rx, ry, rw, rh, arc, arc);
            g.setFill(C_WHITE_22);
            g.fillRoundRect(rx, ry, rw, rh / (double)3.0F, arc, arc);
            double bw;
            Color bc;
            if (cell != 'S' && cell != 'E') {
                if (this.pathSet.contains(key)) {
                    bc = C_PATH.darker();
                    bw = (double)1.5F;
                } else {
                    bc = Color.rgb(180, 190, 220, 0.14);
                    bw = 0.8;
                }
            } else {
                bc = fill.brighter();
                bw = (double)2.0F;
            }

            g.setStroke(bc);
            g.setLineWidth(bw);
            g.strokeRoundRect(rx, ry, rw, rh, arc, arc);
            if (cell != 'S' && cell != 'E') {
                if (this.pathSet.contains(key)) {
                    this.drawCentered(g, "★", x, y, (double)65.0F, Font.font("SansSerif", FontWeight.BOLD, (double)16.0F), Color.rgb(80, 60, 0));
                } else if (this.visitedSet.contains(key)) {
                    this.drawCentered(g, "·", x, y, (double)65.0F, Font.font("SansSerif", FontWeight.BOLD, (double)20.0F), Color.rgb(60, 90, 180));
                }
            } else {
                this.drawCentered(g, String.valueOf(cell), x, y, (double)65.0F, Font.font("Monospaced", FontWeight.BOLD, (double)18.0F), Color.WHITE);
            }

            if (this.editMode && r == this.hoverR && c == this.hoverC) {
                g.setFill(Color.rgb(255, 255, 255, 0.157));
                g.fillRoundRect(rx, ry, rw, rh, arc, arc);
                g.setStroke(Color.rgb(255, 255, 255, 0.353));
                g.setLineWidth((double)1.5F);
                g.strokeRoundRect(rx, ry, rw, rh, arc, arc);
            }

        }
    }

    private void drawWall(GraphicsContext g, double x, double y) {
        g.setFill(C_WALL);
        g.fillRect(x, y, (double)65.0F, (double)65.0F);
        g.save();
        g.beginPath();
        g.rect(x, y, (double)65.0F, (double)65.0F);
        g.clip();
        g.setStroke(Color.rgb(28, 31, 50));
        g.setLineWidth((double)0.5F);

        for(int i = -65; i < 130; i += 12) {
            g.strokeLine(x + (double)i, y, x + (double)i + (double)65.0F, y + (double)65.0F);
        }

        g.restore();
        g.setStroke(C_WALL_EDG);
        g.setLineWidth((double)1.0F);
        g.strokeRect(x, y, (double)65.0F, (double)65.0F);
    }

    private void drawCentered(GraphicsContext g, String txt, double x, double y, double size, Font font, Color color) {
        g.setFont(font);
        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(C_SHADOW);
        g.fillText(txt, x + size / (double)2.0F + (double)1.0F, y + size * 0.68 + (double)1.0F);
        g.setFill(color);
        g.fillText(txt, x + size / (double)2.0F, y + size * 0.68);
    }
}
