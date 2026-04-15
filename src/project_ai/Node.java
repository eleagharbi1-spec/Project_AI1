//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project_ai;

public class Node {
    public int row;
    public int col;
    public Node parent;

    public Node(int row, int col, Node parent) {
        this.row = row;
        this.col = col;
        this.parent = parent;
    }

    public String toString() {
        return "Node(" + this.row + ", " + this.col + ")";
    }
}
