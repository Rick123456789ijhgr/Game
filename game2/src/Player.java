import java.util.ArrayList;
import java.util.List;

public class Player {
    public int health;
    public int maxHealth;
    public int energy;
    public int maxEnergy = GameConstants.PLAYER_MAX_ENERGY;
    public int block = 0;
    public List<Card> deck;
    public List<Card> hand;
    public List<Card> discardPile;
    public int gold = 0;

    public Player(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.deck = new ArrayList<>();
        this.hand = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.gold = GameConstants.PLAYER_INITIAL_GOLD; // 給予初始金幣
    }
}
