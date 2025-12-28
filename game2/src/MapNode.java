import java.util.ArrayList;
import java.util.List;

public class MapNode {
    public int x;
    public int y;
    public NodeType type;
    public List<MapNode> nextNodes = new ArrayList<>();
    public boolean visited = false;

    public MapNode(int x, int y, NodeType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
