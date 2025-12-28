public class Card {
    public String name;
    public String description;
    public int energyCost;
    public int value;
    public CardType type;

    public Card(String name, String description, int cost, int value, CardType type) {
        this.name = name;
        this.description = description;
        this.energyCost = cost;
        this.value = value;
        this.type = type;
    }
}
