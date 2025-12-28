public class Enemy {
    public int health;
    public int maxHealth;
    public String name;
    public int baseDamage;
    public int block = 0;

    public Enemy(int health, String name, int baseDamage) {
        this.maxHealth = health;
        this.health = health;
        this.name = name;
        this.baseDamage = baseDamage;
    }
}
