import java.awt.Graphics2D;
import java.awt.Image;

public class Projectile {
    private double x, y;
    private final double targetX, targetY;
    private final double speed;
    private final Image image;
    private final int width;
    private final int height;
    private final Runnable onHitCallback;
    private boolean hasHit = false;

    public Projectile(double startX, double startY, double targetX, double targetY, double speed, Image image,
            int width, int height, Runnable onHitCallback) {
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.speed = speed;
        this.image = image;
        this.width = width;
        this.height = height;
        this.onHitCallback = onHitCallback;
    }

    public boolean update() {
        if (hasHit)
            return true;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            // Reached target
            x = targetX;
            y = targetY;
            hasHit = true;
            if (onHitCallback != null) {
                onHitCallback.run();
            }
            return true; // Remove projectile
        }

        // Move towards target
        double moveX = (dx / distance) * speed;
        double moveY = (dy / distance) * speed;

        x += moveX;
        y += moveY;

        return false; // Keep projectile
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, (int) x - width / 2, (int) y - height / 2, width, height, null);
        }
    }
}
