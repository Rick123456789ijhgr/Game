import java.util.ArrayList;
import java.util.List;

public class MapManager {
    public List<List<MapNode>> gameMap = new ArrayList<>();
    public MapNode currentNode;
    private final GameLogger logger;

    public MapManager(GameLogger logger) {
        this.logger = logger;
    }

    public void initializeMap() {
        gameMap.clear();

        // 0. START
        MapNode start = new MapNode(3, 0, NodeType.START);
        gameMap.add(List.of(start));

        // 1. E/S
        MapNode n1a = new MapNode(2, 1, NodeType.ENEMY);
        MapNode n1b = new MapNode(4, 1, NodeType.SHOP);
        start.nextNodes.add(n1a);
        start.nextNodes.add(n1b);
        gameMap.add(List.of(n1a, n1b));

        // 2. E/T
        MapNode n2a = new MapNode(1, 2, NodeType.ENEMY);
        MapNode n2b = new MapNode(3, 2, NodeType.TREASURE);
        n1a.nextNodes.add(n2a);
        n1a.nextNodes.add(n2b);
        n1b.nextNodes.add(n2b);
        gameMap.add(List.of(n2a, n2b));

        // 3. E/ELITE
        MapNode n3a = new MapNode(2, 3, NodeType.ELITE);
        MapNode n3b = new MapNode(4, 3, NodeType.ENEMY);
        n2a.nextNodes.add(n3a);
        n2b.nextNodes.add(n3a);
        n2b.nextNodes.add(n3b);
        gameMap.add(List.of(n3a, n3b));

        // 4. REST/ENEMY
        MapNode n4a = new MapNode(3, 4, NodeType.REST);
        n3a.nextNodes.add(n4a);
        n3b.nextNodes.add(n4a);
        gameMap.add(List.of(n4a));

        // 5. SHOP/ENEMY
        MapNode n5a = new MapNode(2, 5, NodeType.SHOP);
        MapNode n5b = new MapNode(4, 5, NodeType.ENEMY);
        n4a.nextNodes.add(n5a);
        n4a.nextNodes.add(n5b);
        gameMap.add(List.of(n5a, n5b));

        // 6. TREASURE
        MapNode n6 = new MapNode(3, 6, NodeType.TREASURE);
        n5a.nextNodes.add(n6);
        n5b.nextNodes.add(n6);
        gameMap.add(List.of(n6));

        // 7. BOSS
        MapNode boss = new MapNode(3, GameConstants.MAP_FLOORS - 1, NodeType.BOSS);
        n6.nextNodes.add(boss);
        gameMap.add(List.of(boss));

        currentNode = start;
        currentNode.visited = true;
    }

    public MapNode handleMapClick(int clickX, int clickY, int width, int height) {
        int floorSpacing = height / GameConstants.MAP_FLOORS;
        int widthCenter = width / 2;

        if (currentNode.y + 1 >= GameConstants.MAP_FLOORS)
            return null;

        for (MapNode nextNode : gameMap.get(currentNode.y + 1)) {
            if (!currentNode.nextNodes.contains(nextNode))
                continue;

            int nodeY = nextNode.y * floorSpacing + floorSpacing / 2;
            int nodeX = widthCenter + (nextNode.x - 3) * (width / 8);

            if (clickX >= nodeX && clickX <= nodeX + GameConstants.MAP_NODE_SIZE &&
                    clickY >= nodeY && clickY <= nodeY + GameConstants.MAP_NODE_SIZE) {

                currentNode = nextNode;
                currentNode.visited = true;
                logger.log("進入節點: " + currentNode.type.name());
                return currentNode;
            }
        }
        logger.log("請點擊一個已連線的下一層節點。");
        return null;
    }
}
