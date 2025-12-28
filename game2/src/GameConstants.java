import java.awt.Color;

public class GameConstants {
    public static final Color BACKGROUND_COLOR = new Color(20, 20, 40);
    // Player
    public static final int PLAYER_MAX_HEALTH = 80;
    public static final int PLAYER_MAX_ENERGY = 3;
    public static final int PLAYER_INITIAL_GOLD = 100;

    // Map
    public static final int MAP_FLOORS = 8;
    public static final int MAP_NODE_SIZE = 40;

    // Window
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;
    public static final int HAND_PANEL_HEIGHT = 180;
    public static final int SIDE_PANEL_WIDTH = 250;

    // Cards - Strike
    public static final int STRIKE_COST = 1;
    public static final int STRIKE_DAMAGE = 6;
    public static final int STRIKE_UPGRADED_DAMAGE = 9;

    // Cards - Defend
    public static final int DEFEND_COST = 1;
    public static final int DEFEND_BLOCK = 5;

    // Cards - Heavy Strike
    public static final int HEAVY_STRIKE_COST = 2;
    public static final int HEAVY_STRIKE_DAMAGE = 15;

    // Cards - Heal
    public static final int HEAL_COST = 1;
    public static final int HEAL_AMOUNT = 8;

    // Cards - Rampage
    public static final int RAMPAGE_COST = 2;
    public static final int RAMPAGE_DAMAGE = 20;
    public static final int RAMPAGE_SELF_DAMAGE = 3;

    // Cards - Barrier
    public static final int BARRIER_COST = 2;
    public static final int BARRIER_BLOCK = 12;

    // Cards - Energy Burst
    public static final int ENERGY_BURST_COST = 0;
    public static final int ENERGY_BURST_AMOUNT = 2;

    // Shop
    public static final int SHOP_CARD_PRICE = 50;
    public static final int SHOP_REMOVE_CARD_PRICE = 75;
    public static final int SHOP_DECK_MIN_SIZE_FOR_REMOVAL = 12;

    // Rest
    public static final int REST_HEAL_AMOUNT = 15;

    // Enemies
    public static final int ENEMY_SMALL_HP = 20;
    public static final int ENEMY_SMALL_DMG = 4;

    public static final int ENEMY_ELITE_HP = 50;
    public static final int ENEMY_ELITE_DMG = 8;

    public static final int ENEMY_BOSS_HP = 120;
    public static final int ENEMY_BOSS_DMG = 12;

    // Resources
    public static final String RES_PLAYER_IMAGE = "img/player.png";
    public static final String RES_ENEMY_IMAGE = "img/enemy.png";
    public static final String RES_BOSS_IMAGE = "img/boss.png";
    public static final String RES_ATTACK0_IMAGE = "img/attack0.png";
    public static final String RES_ATTACK1_IMAGE = "img/attack1.png";

    // Attack Threshold
    public static final int ATTACK_EFFECT_THRESHOLD = 15;

    // Image Sizes
    public static final int PLAYER_IMAGE_WIDTH = 120;
    public static final int PLAYER_IMAGE_HEIGHT = 202;

    public static final int ENEMY_IMAGE_WIDTH = 200;
    public static final int ENEMY_IMAGE_HEIGHT = 90;

    public static final int BOSS_IMAGE_WIDTH = 170;
    public static final int BOSS_IMAGE_HEIGHT = 320;

    // Attack Effect Sizes
    public static final int ATTACK0_IMAGE_WIDTH = 115;
    public static final int ATTACK0_IMAGE_HEIGHT = 25;
    public static final int ATTACK1_IMAGE_WIDTH = 120;
    public static final int ATTACK1_IMAGE_HEIGHT = 215;

    // Card Images
    public static final String RES_CARD_ATTACK_6 = "img/card/attack6.png";
    public static final String RES_CARD_DEFENSE_5 = "img/card/defense5.png";
    public static final String RES_CARD_ATTACK_15 = "img/card/attack15.png";
    public static final String RES_CARD_HEAL = "img/card/heal.png";
    public static final String RES_CARD_ATTACK_20 = "img/card/attack20.png";
    public static final String RES_CARD_DEFENSE_12 = "img/card/defense12.png";
    public static final String RES_CARD_BUFF = "img/card/buff.png";
    public static final String RES_CARD_ATTACK_9 = "img/card/attack9.png";

    // Font
    public static final java.awt.Font UI_FONT = new java.awt.Font("Microsoft JhengHei", java.awt.Font.BOLD, 14);
}
