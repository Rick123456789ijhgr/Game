import javax.swing.JOptionPane;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.awt.Component;

public class EventManager {
    private final GameLogger logger;
    private final Random random = new Random();
    private final List<Card> rareCards = List.of(
            new Card("狂暴",
                    "造成 " + GameConstants.RAMPAGE_DAMAGE + " 點傷害，但本回合額外承受 " + GameConstants.RAMPAGE_SELF_DAMAGE
                            + " 點傷害",
                    GameConstants.RAMPAGE_COST, GameConstants.RAMPAGE_DAMAGE, CardType.ATTACK),
            new Card("壁壘", "獲得 " + GameConstants.BARRIER_BLOCK + " 點防禦", GameConstants.BARRIER_COST,
                    GameConstants.BARRIER_BLOCK, CardType.SKILL),
            new Card("能量爆發", "獲得 " + GameConstants.ENERGY_BURST_AMOUNT + " 點額外能量", GameConstants.ENERGY_BURST_COST, 0,
                    CardType.SKILL));

    public EventManager(GameLogger logger) {
        this.logger = logger;
    }

    public void handleShopEvent(Component parentComponent, Player player, Runnable updateUI) {
        logger.log("歡迎來到商店！");
        String[] options = { "購買卡牌 (" + GameConstants.SHOP_CARD_PRICE + " 金)",
                "移除卡牌 (" + GameConstants.SHOP_REMOVE_CARD_PRICE + " 金)", "離開" };

        while (true) {
            int choice = JOptionPane.showOptionDialog(parentComponent,
                    "請選擇服務。您有 " + player.gold + " 金。",
                    "商店", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[2]);

            updateUI.run();

            if (choice == 0) { // 購買卡牌
                if (player.gold >= GameConstants.SHOP_CARD_PRICE) {
                    Card newCard = rareCards.get(random.nextInt(rareCards.size()));
                    player.deck.add(newCard);
                    player.gold -= GameConstants.SHOP_CARD_PRICE;
                    logger.log("成功購買 [" + newCard.name + "]，加入牌組。金幣 -" + GameConstants.SHOP_CARD_PRICE);
                } else {
                    logger.log("金幣不足，無法購買卡牌！");
                }
            } else if (choice == 1) { // 移除卡牌
                if (player.gold >= GameConstants.SHOP_REMOVE_CARD_PRICE) {
                    if (player.deck.size() > GameConstants.SHOP_DECK_MIN_SIZE_FOR_REMOVAL) {
                        String cardList = player.deck.stream().map(c -> c.name).collect(Collectors.joining(", "));
                        String cardName = JOptionPane.showInputDialog(parentComponent,
                                "您目前擁有的卡牌:\n" + cardList + "\n\n請輸入要移除的卡牌名稱:",
                                "移除卡牌", JOptionPane.PLAIN_MESSAGE);

                        if (cardName != null) {
                            Card cardToRemove = player.deck.stream()
                                    .filter(c -> c.name.equals(cardName)).findFirst().orElse(null);

                            if (cardToRemove != null) {
                                player.deck.remove(cardToRemove);
                                player.gold -= GameConstants.SHOP_REMOVE_CARD_PRICE;
                                logger.log("成功移除一張 [" + cardName + "]。金幣 -" + GameConstants.SHOP_REMOVE_CARD_PRICE);
                            } else {
                                logger.log("牌組中沒有找到該卡牌。");
                            }
                        }
                    } else {
                        logger.log("牌組卡牌太少，無法移除。");
                    }
                } else {
                    logger.log("金幣不足，無法移除卡牌！");
                }
            } else { // 離開或取消
                logger.log("離開商店。");
                break;
            }
        }
    }

    public void handleRestEvent(Component parentComponent, Player player) {
        logger.log("來到休息點，可以選擇休息或鍛造。");
        String[] options = { "休息 (HP +" + GameConstants.REST_HEAL_AMOUNT + ")", "鍛造卡牌 (升級打擊)", "離開" };
        int choice = JOptionPane.showOptionDialog(parentComponent,
                "請選擇一個行動", "休息點", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: // 休息
                int healAmount = GameConstants.REST_HEAL_AMOUNT;
                player.health = Math.min(player.maxHealth, player.health + healAmount);
                logger.log("休息。恢復 " + healAmount + " 點生命。");
                break;
            case 1: // 鍛造 (升級卡牌)
                Card strike = player.deck.stream()
                        .filter(c -> c.name.equals("打擊")).findFirst().orElse(null);

                if (strike != null) {
                    strike.name = "重打+";
                    strike.value = GameConstants.STRIKE_UPGRADED_DAMAGE;
                    logger.log("成功鍛造！一張 [打擊] 升級為 [重打+] (傷害 " + GameConstants.STRIKE_UPGRADED_DAMAGE + ")。");
                } else {
                    logger.log("牌組中沒有 [打擊] 卡牌可以升級。");
                }
                break;
            case 2: // 離開
            default:
                logger.log("離開休息點。");
                break;
        }
    }

    public void handleTreasureEvent(Player player) {
        logger.log("發現一個寶箱！獲得金幣並抽一張稀有卡。");
        int goldReward = random.nextInt(30) + 20; // 20-49 金幣
        player.gold += goldReward;

        Card newCard = rareCards.get(random.nextInt(rareCards.size()));
        player.deck.add(newCard);

        logger.log("獲得 " + goldReward + " 金幣。");
        logger.log("獲得稀有卡: [" + newCard.name + "]，已加入牌組。");
    }
}
