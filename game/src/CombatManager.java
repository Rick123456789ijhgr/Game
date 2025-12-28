import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class CombatManager {
    public final List<Enemy> enemies = new ArrayList<>();
    public boolean inCombat = false;
    public boolean playerTurn = false;
    public int comboCount = 0; // Combo counter
    private final GameLogger logger;
    private final Player player;
    private final Runnable updateUICallback;
    private final BiConsumer<Boolean, Runnable> attackAnimationHandler;
    private final EnemyAnimationHandler enemyAnimationHandler;

    private int pendingAnimations = 0;
    private boolean endTurnPending = false;

    public interface EnemyAnimationHandler {
        void animate(Enemy enemy, Runnable onHit, Runnable onFinish);
    }

    public CombatManager(Player player, GameLogger logger, Runnable updateUICallback,
            BiConsumer<Boolean, Runnable> attackAnimationHandler,
            EnemyAnimationHandler enemyAnimationHandler) {
        this.player = player;
        this.logger = logger;
        this.updateUICallback = updateUICallback;
        this.attackAnimationHandler = attackAnimationHandler;
        this.enemyAnimationHandler = enemyAnimationHandler;
    }

    public void initializeCombatEnemies(NodeType type) {
        enemies.clear();
        if (type == NodeType.ENEMY) {
            enemies.add(new Enemy(GameConstants.ENEMY_SMALL_HP, "小怪", GameConstants.ENEMY_SMALL_DMG));
        } else if (type == NodeType.ELITE) {
            enemies.add(new Enemy(GameConstants.ENEMY_ELITE_HP, "精英怪", GameConstants.ENEMY_ELITE_DMG));
        } else if (type == NodeType.BOSS) {
            enemies.add(new Enemy(GameConstants.ENEMY_BOSS_HP, "最終首領", GameConstants.ENEMY_BOSS_DMG));
        }
    }

    public void startCombat() {
        inCombat = true;
        logger.log("--- 戰鬥開始 ---");
        startPlayerTurn();
    }

    // ... initializeCombatEnemies, startCombat ...

    public void startPlayerTurn() {
        playerTurn = true;
        player.energy = player.maxEnergy;
        player.block = 0;
        comboCount = 0;
        pendingAnimations = 0;
        endTurnPending = false;

        player.discardPile.addAll(player.hand);
        player.hand.clear();

        drawCards(5);

        logger.log("--- 你的回合 (費用: " + player.energy + ") ---");
        updateUICallback.run();
    }

    public void drawCards(int amount) {
        for (int i = 0; i < amount; i++) {
            if (player.deck.isEmpty()) {
                if (player.discardPile.isEmpty())
                    break;

                player.deck.addAll(player.discardPile);
                player.discardPile.clear();
                Collections.shuffle(player.deck);
                logger.log("牌組重洗。");
            }

            Card drawnCard = player.deck.remove(0);
            player.hand.add(drawnCard);
        }
    }

    public void playCard(Card card, Enemy target) {
        if (!playerTurn || target == null || target.health <= 0)
            return;

        if (player.energy >= card.energyCost) {
            player.energy -= card.energyCost;

            if (card.name.equals("能量爆發")) {
                player.energy += GameConstants.ENERGY_BURST_AMOUNT;
                logger.log("打出 [能量爆發]，獲得 " + GameConstants.ENERGY_BURST_AMOUNT + " 點額外費用。");
            }

            Runnable effectLogic = () -> {
                executeCardEffect(card, target);
                enemies.removeIf(e -> e.health <= 0);
                updateUICallback.run();
                if (enemies.isEmpty()) {
                    endCombat(true);
                }

                if (card.type == CardType.ATTACK) {
                    pendingAnimations--;
                }
                checkEndTurnCondition();
            };

            if (card.type == CardType.ATTACK) {
                comboCount++;
                boolean isStrong = card.value >= GameConstants.ATTACK_EFFECT_THRESHOLD;
                if (attackAnimationHandler != null) {
                    pendingAnimations++;
                    attackAnimationHandler.accept(isStrong, effectLogic);
                } else {
                    effectLogic.run();
                }
            } else {
                effectLogic.run();
            }

            player.hand.remove(card);
            player.discardPile.add(card);

            logger.log("打出 [" + card.name + "]，剩餘費用: " + player.energy);
            updateUICallback.run();

            // DO NOT check auto-end here. It is handled in checkEndTurnCondition called by
            // effectLogic (or immediately for skills).

        } else {
            logger.log("費用不足！");
        }
    }

    private void checkEndTurnCondition() {
        if (pendingAnimations > 0)
            return;

        if (endTurnPending || (player.energy == 0 && !canPlayAnyCard())) {
            if (!endTurnPending) {
                logger.log("費用耗盡，自動結束回合。");
            }
            realEndPlayerTurn();
        }
    }

    private boolean canPlayAnyCard() {
        return player.hand.stream().anyMatch(card -> player.energy >= card.energyCost);
    }

    private void executeCardEffect(Card card, Enemy target) {
        switch (card.type) {
            case ATTACK:
                int damage = card.value;
                int actualDamage = damage;

                if (card.name.equals("狂暴")) {
                    player.health -= GameConstants.RAMPAGE_SELF_DAMAGE;
                    logger.log("因 [狂暴] 副作用，失去 " + GameConstants.RAMPAGE_SELF_DAMAGE + " 點生命。");
                    if (player.health <= 0) {
                        endCombat(false);
                        return;
                    }
                }

                if (target.block > 0) {
                    if (target.block >= damage) {
                        target.block -= damage;
                        actualDamage = 0;
                    } else {
                        actualDamage = damage - target.block;
                        target.block = 0;
                    }
                }
                target.health -= actualDamage;
                if (actualDamage > 0)
                    logger.log("對 " + target.name + " 造成 " + actualDamage + " 點傷害。");
                else
                    logger.log("攻擊被 " + target.name + " 的防禦抵擋。");
                break;
            case SKILL:
                if (card.name.equals("防禦") || card.name.equals("壁壘")) {
                    player.block += card.value;
                    logger.log("獲得 " + card.value + " 點防禦。");
                } else if (card.name.equals("治療術")) {
                    player.health = Math.min(player.maxHealth, player.health + card.value);
                    logger.log("恢復 " + card.value + " 點生命。");
                }
                break;
        }
    }

    public void endPlayerTurn() {
        if (!inCombat || !playerTurn)
            return;

        if (pendingAnimations > 0) {
            endTurnPending = true;
            logger.log("請稍候，等待攻擊動畫結束...");
            return;
        }

        realEndPlayerTurn();
    }

    private void realEndPlayerTurn() {
        if (!inCombat || !playerTurn)
            return;
        playerTurn = false;
        logger.log("--- 你的回合結束 ---");

        player.discardPile.addAll(player.hand);
        player.hand.clear();

        updateEnemyTurn();
    }

    private void updateEnemyTurn() {
        logger.log("--- 敵人回合開始 ---");
        processEnemyAction(0);
    }

    private void processEnemyAction(int index) {
        if (index >= enemies.size() || !inCombat) {
            // All enemies done or combat ended
            if (inCombat) {
                startPlayerTurn();
            }
            return;
        }

        Enemy enemy = enemies.get(index);
        if (enemy.health <= 0) {
            processEnemyAction(index + 1); // Skip dead enemies
            return;
        }

        // Define logic to happen ON HIT (damage application)
        Runnable onHitLogic = () -> {
            int damage = enemy.baseDamage;
            int actualDamage = damage;

            if (player.block > 0) {
                if (player.block >= damage) {
                    player.block -= damage;
                    actualDamage = 0;
                } else {
                    actualDamage = damage - player.block;
                    player.block = 0;
                }
            }

            player.health -= actualDamage;
            if (actualDamage > 0)
                logger.log(enemy.name + " 攻擊了玩家，造成 " + actualDamage + " 點傷害！");
            else
                logger.log(enemy.name + " 的攻擊被防禦抵擋。");

            updateUICallback.run(); // Update UI to show damage
        };

        // Define logic to happen ON FINISH (next enemy)
        Runnable onFinishLogic = () -> {
            if (player.health <= 0) {
                endCombat(false);
            } else {
                processEnemyAction(index + 1);
            }
        };

        // Trigger animation
        if (enemyAnimationHandler != null) {
            enemyAnimationHandler.animate(enemy, onHitLogic, onFinishLogic);
        } else {
            // No animation fallback
            onHitLogic.run();
            onFinishLogic.run();
        }
    }

    public void endCombat(boolean victory) {
        inCombat = false;
        playerTurn = false;

        player.deck.addAll(player.discardPile);
        player.discardPile.clear();
        player.deck.addAll(player.hand);
        player.hand.clear();
        Collections.shuffle(player.deck);

        if (victory) {
            logger.log("🎉 戰鬥勝利！請在地圖上選擇下一個節點。");
        } else {
            logger.log("💀 你被擊敗了... 遊戲結束。");
        }
        updateUICallback.run();
    }
}
