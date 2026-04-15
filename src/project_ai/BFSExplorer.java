package project_ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BFSExplorer {
    public List<List<int[]>> allVisitedOrders = new ArrayList<>();
    public List<List<int[]>> allPathOrders    = new ArrayList<>();

    // NOUVEAU : profondeur max atteinte par chaque agent
    public List<Integer> allMaxDepths = new ArrayList<>();

    // NOUVEAU : log texte pour chaque agent (comme les print() Python)
    public List<List<String>> allLogs = new ArrayList<>();

    private static final int[][] DIRS = {{-1,0},{1,0},{0,-1},{0,1}};

    public boolean explore(BuildingMap map) {
        this.allVisitedOrders.clear();
        this.allPathOrders.clear();
        this.allMaxDepths.clear();
        this.allLogs.clear();

        List<int[]> starts = new ArrayList<>();
        for (int r = 0; r < map.rows; r++)
            for (int c = 0; c < map.cols; c++)
                if (map.getCell(r, c) == 'S')
                    starts.add(new int[]{r, c});

        if (starts.isEmpty()) return false;

        boolean foundAtLeastOne = false;

        for (int[] sPos : starts) {
            Queue<int[]>      queue    = new LinkedList<>();
            Map<String,int[]> parent   = new HashMap<>();
            Map<String,Integer> depth  = new HashMap<>();   // NOUVEAU
            List<int[]>  visited       = new ArrayList<>();
            List<String> logs          = new ArrayList<>(); // NOUVEAU
            boolean[][] seen           = new boolean[map.rows][map.cols];

            String startKey = sPos[0] + "," + sPos[1];
            queue.add(sPos);
            seen[sPos[0]][sPos[1]] = true;
            parent.put(startKey, null);
            depth.put(startKey, 0);   // NOUVEAU

            int[] exitFound = null;
            int   maxDepth  = 0;

            while (!queue.isEmpty()) {
                int[] cur      = queue.poll();
                String curKey  = cur[0] + "," + cur[1];
                int    curDepth = depth.get(curKey);  // NOUVEAU

                visited.add(cur);

                // Log comme Python : "Noeud exploré : (r,c) | profondeur = d"
                logs.add("Noeud exploré : (" + cur[0] + "," + cur[1]
                        + ") | profondeur = " + curDepth);

                maxDepth = Math.max(maxDepth, curDepth);

                if (map.getCell(cur[0], cur[1]) == 'E') {
                    exitFound = cur;
                    break;
                }

                for (int[] d : DIRS) {
                    int nr = cur[0] + d[0];
                    int nc = cur[1] + d[1];
                    if (map.inBounds(nr, nc) && !seen[nr][nc]
                            && map.getCell(nr, nc) != '#') {
                        seen[nr][nc] = true;
                        String nKey = nr + "," + nc;
                        parent.put(nKey, cur);
                        depth.put(nKey, curDepth + 1);  // NOUVEAU
                        queue.add(new int[]{nr, nc});
                    }
                }
            }

            this.allVisitedOrders.add(visited);
            this.allMaxDepths.add(maxDepth);          // NOUVEAU
            this.allLogs.add(logs);                   // NOUVEAU

            if (exitFound != null) {
                foundAtLeastOne = true;
                List<int[]> path = new ArrayList<>();
                for (int[] t = exitFound; t != null; t = parent.get(t[0]+","+t[1]))
                    path.add(0, t);
                this.allPathOrders.add(path);
            }
        }

        return foundAtLeastOne;
    }
}