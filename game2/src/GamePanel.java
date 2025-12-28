import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.FontMetrics;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;

public class GamePanel extends JPanel {
    private final Player player;
    private final MapManager mapManager;
    private final CombatManager combatManager;
    private Image playerImage;
    private Image enemyImage;
    private Image bossImage;
    private Image attack0Image;
    private Image attack1Image;
    private final java.util.List<Projectile> projectiles = new java.util.ArrayList<>();
    private int shakeDuration = 0;
    private int shakeIntensity = 0;

    public enum GameState {
        PLAYING, VICTORY, GAME_OVER
    }

    private GameState gameState = GameState.PLAYING;

    // Enemy Animation State
    private float enemyOffsetX = 0;

    private enum EnemyAnimState {
        IDLE, FORWARD, BACK
    }

    private EnemyAnimState enemyAnimState = EnemyAnimState.IDLE;
    private Runnable enemyOnHit;
    private Runnable enemyOnFinish;

    public GamePanel(Player player, MapManager mapManager, CombatManager combatManager) {
        this.player = player;
        this.mapManager = mapManager;
        this.combatManager = combatManager;

        try {
            // 使用 ClassLoader 載入資源 (適用於 bin 目錄在 classpath 的情況)
            java.net.URL playerUrl = getClass().getClassLoader().getResource(GameConstants.RES_PLAYER_IMAGE);
            java.net.URL enemyUrl = getClass().getClassLoader().getResource(GameConstants.RES_ENEMY_IMAGE);
            java.net.URL bossUrl = getClass().getClassLoader().getResource(GameConstants.RES_BOSS_IMAGE);
            java.net.URL attack0Url = getClass().getClassLoader().getResource(GameConstants.RES_ATTACK0_IMAGE);
            java.net.URL attack1Url = getClass().getClassLoader().getResource(GameConstants.RES_ATTACK1_IMAGE);

            if (playerUrl != null)
                playerImage = ImageIO.read(playerUrl);
            else
                System.err.println("Could not find player image at: " + GameConstants.RES_PLAYER_IMAGE);

            if (enemyUrl != null)
                enemyImage = ImageIO.read(enemyUrl);
            else
                System.err.println("Could not find enemy image at: " + GameConstants.RES_ENEMY_IMAGE);

            if (bossUrl != null)
                bossImage = ImageIO.read(bossUrl);
            else
                System.err.println("Could not find boss image at: " + GameConstants.RES_BOSS_IMAGE);

            if (attack0Url != null)
                attack0Image = ImageIO.read(attack0Url);
            else
                System.err.println("Could not find attack0 image at: " + GameConstants.RES_ATTACK0_IMAGE);

            if (attack1Url != null)
                attack1Image = ImageIO.read(attack1Url);
            else
                System.err.println("Could not find attack1 image at: " + GameConstants.RES_ATTACK1_IMAGE);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (shakeDuration > 0) {
            int dx = (int) (Math.random() * shakeIntensity * 2 - shakeIntensity);
            int dy = (int) (Math.random() * shakeIntensity * 2 - shakeIntensity);
            g2d.translate(dx, dy);
        }

        if (combatManager.inCombat)
            drawCombat(g2d);
        else if (combatManager.inCombat)
            drawCombat(g2d);
        else
            drawMap(g2d);

        if (gameState == GameState.GAME_OVER) {
            drawGameOver(g2d);
        } else if (gameState == GameState.VICTORY) {
            drawVictory(g2d);
        }
    }

    public void showGameOver() {
        this.gameState = GameState.GAME_OVER;
        repaint();
    }

    public void showVictory() {
        this.gameState = GameState.VICTORY;
        repaint();
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        String text = "GAME OVER";
        g2d.setFont(GameConstants.UI_FONT.deriveFont(java.awt.Font.BOLD, 80f));
        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int x = (getWidth() - textW) / 2;
        int y = (getHeight() + textH) / 2;

        g2d.setColor(Color.RED);
        g2d.drawString(text, x, y);
    }

    private void drawVictory(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        String text = "VICTORY";
        g2d.setFont(GameConstants.UI_FONT.deriveFont(java.awt.Font.BOLD, 80f));
        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getAscent();

        int x = (getWidth() - textW) / 2;
        int y = (getHeight() + textH) / 2;

        g2d.setColor(Color.YELLOW);
        g2d.drawString(text, x, y);
    }

    public void startShake(int duration, int intensity) {
        this.shakeDuration = duration;
        this.shakeIntensity = intensity;
    }

    public void updateLogic() {
        projectiles.removeIf(Projectile::update);
        if (shakeDuration > 0) {
            shakeDuration--;
        }

        // Enemy Animation Logic
        if (enemyAnimState == EnemyAnimState.FORWARD) {
            enemyOffsetX -= 25.0f; // Move left fast
            if (enemyOffsetX <= -300) { // Target reached (approx player pos)
                enemyAnimState = EnemyAnimState.BACK;
                if (enemyOnHit != null) {
                    startShake(10, 5); // Shake on hit
                    enemyOnHit.run();
                }
            }
        } else if (enemyAnimState == EnemyAnimState.BACK) {
            enemyOffsetX += 10.0f; // Move right slow
            if (enemyOffsetX >= 0) {
                enemyOffsetX = 0;
                enemyAnimState = EnemyAnimState.IDLE;
                if (enemyOnFinish != null)
                    enemyOnFinish.run();
            }
        }
    }

    public void playEnemyAttackAnimation(Runnable onHit, Runnable onFinish) {
        this.enemyOnHit = onHit;
        this.enemyOnFinish = onFinish;
        this.enemyAnimState = EnemyAnimState.FORWARD;
        this.enemyOffsetX = 0;
    }

    private void drawMap(Graphics2D g2d) {
        g2d.setColor(GameConstants.BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int floorSpacing = getHeight() / GameConstants.MAP_FLOORS;
        int widthCenter = getWidth() / 2;

        for (List<MapNode> floor : mapManager.gameMap) {
            for (MapNode node : floor) {
                int nodeY = node.y * floorSpacing + floorSpacing / 2;
                int nodeX = widthCenter + (node.x - 3) * (getWidth() / 8);

                // 1. 繪製連線
                for (MapNode next : node.nextNodes) {
                    int nextY = next.y * floorSpacing + floorSpacing / 2;
                    int nextX = widthCenter + (next.x - 3) * (getWidth() / 8);
                    g2d.setColor(node.visited ? new Color(100, 100, 100) : new Color(50, 50, 50));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(nodeX + GameConstants.MAP_NODE_SIZE / 2, nodeY + GameConstants.MAP_NODE_SIZE / 2,
                            nextX + GameConstants.MAP_NODE_SIZE / 2, nextY + GameConstants.MAP_NODE_SIZE / 2);
                }

                // 2. 繪製節點
                Color color = getNodeColor(node.type);
                boolean isNextMove = mapManager.currentNode.nextNodes.contains(node);

                if (node == mapManager.currentNode) {
                    color = Color.YELLOW;
                } else if (isNextMove) {
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(nodeX - 2, nodeY - 2, GameConstants.MAP_NODE_SIZE + 4,
                            GameConstants.MAP_NODE_SIZE + 4);
                }

                g2d.setColor(color);
                g2d.fillOval(nodeX, nodeY, GameConstants.MAP_NODE_SIZE, GameConstants.MAP_NODE_SIZE);
                g2d.setColor(Color.BLACK);
                g2d.setFont(GameConstants.UI_FONT.deriveFont(10f));
                String label = node.type.name().substring(0, Math.min(node.type.name().length(), 4));
                g2d.drawString(label, nodeX + 5, nodeY + GameConstants.MAP_NODE_SIZE / 2 + 5);
            }
        }
    }

    private Color getNodeColor(NodeType type) {
        return switch (type) {
            case START -> Color.GREEN;
            case ENEMY -> Color.RED;
            case ELITE -> Color.ORANGE;
            case SHOP -> Color.BLUE;
            case TREASURE -> Color.YELLOW;
            case REST -> Color.CYAN;
            case BOSS -> Color.MAGENTA;
        };
    }

    private void drawCombat(Graphics2D g2d) {
        // 1. 背景 (Unified)
        g2d.setColor(GameConstants.BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 2. 地板 (Floor)
        int floorY = getHeight() * 3 / 4;
        g2d.setColor(new Color(50, 50, 70));
        g2d.fillRect(0, floorY, getWidth(), getHeight() - floorY);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, floorY, getWidth(), floorY);

        int playerX = getWidth() / 4;

        // 繪製玩家圖片 (Align bottom to floorY)
        if (playerImage != null) {
            g2d.drawImage(playerImage, playerX - GameConstants.PLAYER_IMAGE_WIDTH / 2,
                    floorY - GameConstants.PLAYER_IMAGE_HEIGHT,
                    GameConstants.PLAYER_IMAGE_WIDTH, GameConstants.PLAYER_IMAGE_HEIGHT, null);
        } else {
            g2d.setColor(Color.GREEN);
            g2d.fillRect(playerX - 30, floorY - 60, 60, 60);
        }

        // Player Labels (On floor)
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameConstants.UI_FONT);
        g2d.drawString("玩家", playerX - 15, floorY + 25);

        if (player.block > 0) {
            g2d.setColor(Color.CYAN);
            g2d.fillOval(playerX + GameConstants.PLAYER_IMAGE_WIDTH / 2 + 10,
                    floorY - GameConstants.PLAYER_IMAGE_HEIGHT / 2, 30, 30);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(player.block), playerX + GameConstants.PLAYER_IMAGE_WIDTH / 2 + 20,
                    floorY - GameConstants.PLAYER_IMAGE_HEIGHT / 2 + 20);
        }

        Enemy enemy = combatManager.enemies.isEmpty() ? null : combatManager.enemies.get(0);
        if (enemy != null) {
            int enemyDisplayX = getWidth() * 3 / 4 + (int) enemyOffsetX;

            // 判斷是否為 Boss
            boolean isBoss = enemy.name.equals("最終首領") || enemy.maxHealth >= GameConstants.ENEMY_BOSS_HP;
            Image targetImage = isBoss ? bossImage : enemyImage;
            int width = isBoss ? GameConstants.BOSS_IMAGE_WIDTH : GameConstants.ENEMY_IMAGE_WIDTH;
            int height = isBoss ? GameConstants.BOSS_IMAGE_HEIGHT : GameConstants.ENEMY_IMAGE_HEIGHT;

            if (targetImage != null) {
                g2d.drawImage(targetImage, enemyDisplayX - width / 2, floorY - height, width, height, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(enemyDisplayX - 30, floorY - 60, 60, 60);
            }

            // Enemy Labels (On floor)
            g2d.setColor(Color.WHITE);
            g2d.setFont(GameConstants.UI_FONT);
            g2d.drawString(enemy.name, enemyDisplayX - 25, floorY + 25);
            g2d.drawString("HP: " + enemy.health + "/" + enemy.maxHealth, enemyDisplayX - 35, floorY + 45);

            // HP Bar (Above Enemy)
            g2d.setColor(Color.RED);
            g2d.fillRect(enemyDisplayX - 30, floorY - height - 20, 60, 5);
            g2d.setColor(Color.YELLOW);
            int hpWidth = (int) ((double) enemy.health / enemy.maxHealth * 60);
            g2d.fillRect(enemyDisplayX - 30, floorY - height - 20, hpWidth, 5);

            // Attack Intent
            g2d.setColor(Color.ORANGE);
            g2d.setFont(GameConstants.UI_FONT.deriveFont(java.awt.Font.BOLD, 18f));
            g2d.drawString("攻擊 (" + enemy.baseDamage + ")", enemyDisplayX - 30, floorY - height - 30);
        }

        // 3. Combo Hit Display
        if (combatManager.comboCount > 1) {
            String comboText = combatManager.comboCount + " HITS!";
            g2d.setFont(GameConstants.UI_FONT.deriveFont(java.awt.Font.BOLD | java.awt.Font.ITALIC, 48f));

            int textX = getWidth() / 2 - 100;
            int textY = getHeight() / 2 - 100;

            // Shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString(comboText, textX + 4, textY + 4);

            // Main Text (Gradient-like effect using 2 colors)
            g2d.setColor(Color.YELLOW);
            g2d.drawString(comboText, textX, textY);

            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawString(comboText, textX, textY); // Re-draw for thickness/style
        }

        // 4. Projectiles
        for (Projectile p : projectiles) {
            p.draw(g2d);
        }
    }

    public void spawnAttackEffect(boolean isStrong, Runnable onHitCallback) {
        int floorY = getHeight() * 3 / 4;
        int playerX = getWidth() / 4;
        int enemyX = getWidth() * 3 / 4;

        double startX, startY;
        double targetX, targetY;
        double speed = 20.0; // Speed of projectile
        Image image;
        int pWidth, pHeight;

        if (isStrong) {
            // Strong Attack: Run along floor
            // Image drawn centered at (x, y), so y + height/2 = floorY => y = floorY -
            // height/2
            pWidth = GameConstants.ATTACK1_IMAGE_WIDTH;
            pHeight = GameConstants.ATTACK1_IMAGE_HEIGHT;

            startX = playerX;
            startY = floorY - pHeight / 2.0;
            targetX = enemyX;
            targetY = floorY - pHeight / 2.0;
            image = attack1Image;
        } else {
            // Weak Attack: Fly from center
            pWidth = GameConstants.ATTACK0_IMAGE_WIDTH;
            pHeight = GameConstants.ATTACK0_IMAGE_HEIGHT;

            startX = playerX;
            startY = floorY - GameConstants.PLAYER_IMAGE_HEIGHT / 2.0; // Center body
            targetX = enemyX;
            targetY = floorY - GameConstants.ENEMY_IMAGE_HEIGHT / 2.0; // Center enemy
            image = attack0Image;
        }

        projectiles.add(new Projectile(startX, startY, targetX, targetY, speed, image, pWidth, pHeight, () -> {
            startShake(10, 5); // Shake for 10 ticks with intensity 5
            if (onHitCallback != null)
                onHitCallback.run();
        }));
    }
}
