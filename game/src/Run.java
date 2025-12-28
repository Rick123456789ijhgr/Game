import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class Run extends JFrame implements GameLogger {

    // --------------------------------------------------
    // 遊戲參數 & 常數
    // --------------------------------------------------
    private static final int WINDOW_WIDTH = GameConstants.WINDOW_WIDTH;
    private static final int WINDOW_HEIGHT = GameConstants.WINDOW_HEIGHT;
    private static final int HAND_PANEL_HEIGHT = GameConstants.HAND_PANEL_HEIGHT;
    private static final int SIDE_PANEL_WIDTH = GameConstants.SIDE_PANEL_WIDTH;

    // --------------------------------------------------
    // 核心數據模型 (Model)
    // --------------------------------------------------
    // (已移至獨立檔案: Card.java, CardType.java, MapNode.java, NodeType.java, Enemy.java,
    // Player.java)

    // --------------------------------------------------
    // 遊戲狀態 (State) & 管理器 (Managers)
    // --------------------------------------------------
    private Player player;
    private MapManager mapManager;
    private CombatManager combatManager;
    private EventManager eventManager;

    // --------------------------------------------------
    // UI 元件 (View)
    // --------------------------------------------------
    private JTextArea logArea;
    private GamePanel gamePanel;
    private JLabel playerHpLabel;
    private JLabel energyLabel;
    private JLabel pileStatusLabel;
    private JPanel handPanel;
    private JButton endTurnButton;

    // --------------------------------------------------
    // 構造函數與初始化
    // --------------------------------------------------

    public Run() {
        // 初始化所有 final/core UI 變數
        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);
        logArea.setFont(GameConstants.UI_FONT.deriveFont(12f));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);

        playerHpLabel = new JLabel();
        energyLabel = new JLabel();
        pileStatusLabel = new JLabel();

        endTurnButton = new JButton("結束回合");
        endTurnButton.setFont(GameConstants.UI_FONT.deriveFont(java.awt.Font.BOLD, 18));
        endTurnButton.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH - 20, 50));
        endTurnButton.addActionListener(e -> {
            if (combatManager.inCombat && combatManager.playerTurn) {
                combatManager.endPlayerTurn();
                updateUIStatus();
                updateHandPanel();
            }
        });

        // 設置遊戲數據與管理器
        player = new Player(GameConstants.PLAYER_MAX_HEALTH);
        mapManager = new MapManager(this);
        combatManager = new CombatManager(player, this, this::onCombatUpdate,
                (isStrong, callback) -> {
                    gamePanel.spawnAttackEffect(isStrong, callback);
                },
                (enemy, onHit, onFinish) -> {
                    gamePanel.playEnemyAttackAnimation(onHit, onFinish);
                });
        eventManager = new EventManager(this);

        initializeCards();
        mapManager.initializeMap();

        // 設置主視窗和佈局
        setTitle("Sts Roguelike Demo (Map & Combat)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 遊戲面板 (CENTER)
        gamePanel = new GamePanel(player, mapManager, combatManager);
        gamePanel.setPreferredSize(new Dimension(WINDOW_WIDTH - SIDE_PANEL_WIDTH, WINDOW_HEIGHT - HAND_PANEL_HEIGHT));
        gamePanel.addMouseListener(new MapMouseListener()); // 地圖導航監聽
        add(gamePanel, BorderLayout.CENTER);

        // 底部手牌/行動面板 (SOUTH)
        handPanel = new JPanel();
        handPanel.setPreferredSize(new Dimension(WINDOW_WIDTH - SIDE_PANEL_WIDTH, HAND_PANEL_HEIGHT));
        handPanel.setBackground(new Color(20, 20, 40));
        add(handPanel, BorderLayout.SOUTH);

        // 側邊 UI 面板 (EAST)
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);

        // 遊戲定時器 (修復 Timer 歧義錯誤：使用 javax.swing.Timer)
        // Update game loop to tick logic
        new javax.swing.Timer(16, e -> {
            gamePanel.updateLogic();
            gamePanel.repaint();
        }).start();

        // 初始狀態：在地圖模式
        endTurnButton.setVisible(false); // 隱藏戰鬥按鈕
        logMessage("遊戲開始! 您有 " + GameConstants.PLAYER_INITIAL_GOLD + " 金幣。請點擊 [START] 節點開始旅程...");
        updateUIStatus();

        pack();
        setResizable(false);
        setVisible(true);
    }

    // --------------------------------------------------
    // Logger 實作
    // --------------------------------------------------
    @Override
    public void log(String message) {
        logMessage(message);
    }

    private void logMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void onCombatUpdate() {
        endTurnButton.setVisible(combatManager.inCombat);
        endTurnButton.setEnabled(combatManager.playerTurn);
        updateUIStatus();
        updateHandPanel();
        gamePanel.repaint();
    }

    private void initializeCards() {
        for (int i = 0; i < 8; i++) {
            player.deck.add(new Card("打擊", "造成 " + GameConstants.STRIKE_DAMAGE + " 點傷害", GameConstants.STRIKE_COST,
                    GameConstants.STRIKE_DAMAGE, CardType.ATTACK));
        }
        for (int i = 0; i < 4; i++) {
            player.deck.add(new Card("防禦", "獲得 " + GameConstants.DEFEND_BLOCK + " 點防禦", GameConstants.DEFEND_COST,
                    GameConstants.DEFEND_BLOCK, CardType.SKILL));
        }
        player.deck.add(new Card("重擊", "造成 " + GameConstants.HEAVY_STRIKE_DAMAGE + " 點傷害",
                GameConstants.HEAVY_STRIKE_COST, GameConstants.HEAVY_STRIKE_DAMAGE, CardType.ATTACK));
        player.deck.add(new Card("治療術", "恢復 " + GameConstants.HEAL_AMOUNT + " 點生命", GameConstants.HEAL_COST,
                GameConstants.HEAL_AMOUNT, CardType.SKILL));

        Collections.shuffle(player.deck);
    }

    // --- 地圖與導航邏輯 ---

    private class MapMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!combatManager.inCombat && !combatManager.playerTurn) {
                MapNode clickedNode = mapManager.handleMapClick(e.getX(), e.getY(), gamePanel.getWidth(),
                        gamePanel.getHeight());
                if (clickedNode != null) {
                    processNodeEvent(clickedNode);
                }
            }
        }
    }

    private void processNodeEvent(MapNode node) {
        if (node.type == NodeType.ENEMY || node.type == NodeType.ELITE || node.type == NodeType.BOSS) {
            combatManager.initializeCombatEnemies(node.type);
            combatManager.startCombat();
            onCombatUpdate(); // refresh UI for combat start
        } else if (node.type == NodeType.TREASURE) {
            eventManager.handleTreasureEvent(player);
        } else if (node.type == NodeType.REST) {
            eventManager.handleRestEvent(this, player);
        } else if (node.type == NodeType.SHOP) {
            eventManager.handleShopEvent(this, player, this::updateUIStatus);
        }
        updateUIStatus();
        gamePanel.repaint();
    }

    // --------------------------------------------------
    // UI/View 邏輯 (Panel 和 繪圖)
    // --------------------------------------------------

    private void updateUIStatus() {
        playerHpLabel.setText("HP: " + player.health + " / " + player.maxHealth
                + (player.block > 0 ? " (防禦: " + player.block + ")" : ""));
        energyLabel.setText("費用: " + player.energy + " / " + player.maxEnergy);
        pileStatusLabel.setText("金幣: " + player.gold + " | 牌組: " + player.deck.size() + " 張 | 棄牌堆: "
                + player.discardPile.size() + " 張");
    }

    // ... (UI 輔助方法: createSidePanel, createCardButton, updateHandPanel)
    // 為了縮減篇幅，部分方法保留在主類中，因為它們直接操作 Swing 元件

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, WINDOW_HEIGHT));
        sidePanel.setLayout(new BorderLayout());
        sidePanel.setBackground(Color.BLACK);

        // --- 頂部資訊 (HP, 費用, 牌堆狀態) ---
        JPanel topInfo = new JPanel();
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
        topInfo.setBackground(Color.DARK_GRAY.darker());
        topInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (JLabel label : new JLabel[] { playerHpLabel, energyLabel, pileStatusLabel }) {
            label.setForeground(Color.WHITE);
            label.setFont(GameConstants.UI_FONT);
            topInfo.add(label);
            topInfo.add(Box.createVerticalStrut(5));
        }

        sidePanel.add(topInfo, BorderLayout.NORTH);

        // --- 中間日誌 ---
        JScrollPane scrollPane = new JScrollPane(
                logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "戰鬥日誌", 0, 0,
                GameConstants.UI_FONT.deriveFont(12f), Color.BLACK));

        sidePanel.add(scrollPane, BorderLayout.CENTER);

        // --- 底部按鈕 (結束回合) ---
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setBackground(Color.DARK_GRAY);
        bottomButtonPanel.add(endTurnButton);

        sidePanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        return sidePanel;
    }

    private JButton createCardButton(Card card) {
        String description;
        if (card.name.equals("重打+")) {
            description = "傷害: " + card.value;
        } else if (card.name.equals("治療術")) {
            description = "治療: " + card.value;
        } else if (card.type == CardType.ATTACK) {
            description = "傷害: " + card.value;
        } else {
            description = "防禦: " + card.value;
        }

        JButton button = new JButton(
                "<html><center><b>" + card.name + "</b> [" + card.energyCost + "]<br>" +
                        description + "</center></html>");
        button.setToolTipText(card.description);

        Color bgColor = card.type == CardType.ATTACK ? new Color(200, 50, 50) : new Color(50, 50, 200);
        if (card.name.endsWith("+"))
            bgColor = new Color(255, 165, 0);

        boolean canPlay = player.energy >= card.energyCost && combatManager.playerTurn && combatManager.inCombat;

        button.setBackground(canPlay ? bgColor : bgColor.darker().darker());
        button.setForeground(Color.WHITE);
        button.setFont(GameConstants.UI_FONT.deriveFont(12f)); // Set font for card button
        button.setPreferredSize(new Dimension(120, 150));
        button.setEnabled(canPlay);

        button.addActionListener(e -> {
            Enemy target = combatManager.enemies.isEmpty() ? null : combatManager.enemies.get(0);
            combatManager.playCard(card, target);
        });
        return button;
    }

    private void updateHandPanel() {
        handPanel.removeAll();
        handPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        List<Card> sortedHand = player.hand.stream()
                .sorted(Comparator.comparing(card -> card.type.toString()))
                .collect(Collectors.toList());

        if (combatManager.inCombat) {
            for (Card card : sortedHand) {
                JButton cardButton = createCardButton(card);
                handPanel.add(cardButton);
            }
        }

        handPanel.revalidate();
        handPanel.repaint();
    }

    // --------------------------------------------------
    // 主函數
    // --------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Run::new);
    }
}