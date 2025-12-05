package VisibleHistory.monstercards;

import VisibleHistory.utils.Hpr;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.HashMap;
import java.util.Map;

/**
 * MonsterCardManager - 管理所有怪物的出牌系统
 * 为每个怪物分配对应的MonsterCardPlayer
 */
public class MonsterCardManager {

    // 静态实例（单例模式）
    private static MonsterCardManager instance = null;

    // 怪物ID -> MonsterCardPlayer的映射
    private Map<String, MonsterCardPlayer> monsterCardPlayers;

    // 已启用的怪物列表（用于全局更新）
    private Map<String, AbstractMonster> enabledMonsters;

    private MonsterCardManager() {
        monsterCardPlayers = new HashMap<>();
        enabledMonsters = new HashMap<>();
    }

    /**
     * 获取单例实例
     */
    public static MonsterCardManager getInstance() {
        if (instance == null) {
            instance = new MonsterCardManager();
        }
        return instance;
    }

    /**
     * 为怪物启用出牌系统
     */
    public void enableMonster(AbstractMonster monster) {
        if (monster == null) {
            return;
        }

        String monsterId = getMonsterId(monster);

        // 检查是否已经有对应的CardPlayer
        MonsterCardPlayer cardPlayer = monsterCardPlayers.get(monsterId);
        if (cardPlayer == null) {
            // 创建新的CardPlayer
            cardPlayer = new MonsterCardPlayer(monster);

            // 获取怪物的卡牌配置
            MonsterCardConfig config = MonsterCardConfig.getInstance();
            java.util.List<com.megacrit.cardcrawl.cards.AbstractCard> monsterCards = config.getMonsterCardConfig(monster.id);

            if (monsterCards != null && !monsterCards.isEmpty()) {
                // 为CardPlayer设置怪物的特定卡牌
                for (com.megacrit.cardcrawl.cards.AbstractCard card : monsterCards) {
                    if (card != null) {
                        cardPlayer.addCardToDrawPile(card);
                    }
                }
                Hpr.info("为怪物 " + monster.name + " 配置了 " + monsterCards.size() + " 张卡牌: " + monster.id);
            } else {
                Hpr.info("怪物 " + monster.name + " 没有特定的卡牌配置: " + monster.id);
            }

            monsterCardPlayers.put(monsterId, cardPlayer);
            Hpr.info("为怪物 " + monster.name + " 创建了CardPlayer");
        }

        // 启用出牌系统
        cardPlayer.enable();
        enabledMonsters.put(monsterId, monster);

        Hpr.info("已启用怪物 " + monster.name + " 的出牌系统");
    }

    /**
     * 禁用怪物的出牌系统
     */
    public void disableMonster(AbstractMonster monster) {
        if (monster == null) {
            return;
        }

        String monsterId = getMonsterId(monster);
        MonsterCardPlayer cardPlayer = monsterCardPlayers.get(monsterId);

        if (cardPlayer != null) {
            cardPlayer.disable();
        }

        enabledMonsters.remove(monsterId);

        Hpr.info("已禁用怪物 " + monster.name + " 的出牌系统");
    }

    /**
     * 更新所有启用的怪物的出牌逻辑
     */
    public void update() {
        // 更新所有启用的怪物
        for (Map.Entry<String, AbstractMonster> entry : enabledMonsters.entrySet()) {
            String monsterId = entry.getKey();
            AbstractMonster monster = entry.getValue();

            // 检查怪物是否还有效
            if (isMonsterValid(monster)) {
                MonsterCardPlayer cardPlayer = monsterCardPlayers.get(monsterId);
                if (cardPlayer != null) {
                    cardPlayer.update();
                }
            } else {
                // 怪物已无效，清理
                disableMonster(monster);
                cleanupInvalidMonster(monsterId);
            }
        }
    }

    /**
     * 渲染所有启用的怪物的头顶卡牌
     */
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch sb) {
        for (Map.Entry<String, AbstractMonster> entry : enabledMonsters.entrySet()) {
            String monsterId = entry.getKey();
            AbstractMonster monster = entry.getValue();

            if (isMonsterValid(monster)) {
                MonsterCardPlayer cardPlayer = monsterCardPlayers.get(monsterId);
                if (cardPlayer != null && cardPlayer.isEnabled()) {
                    cardPlayer.render(sb);
                }
            }
        }
    }

    /**
     * 清理无效的怪物
     */
    private void cleanupInvalidMonster(String monsterId) {
        MonsterCardPlayer cardPlayer = monsterCardPlayers.get(monsterId);
        if (cardPlayer != null) {
            cardPlayer.reset();
        }
        monsterCardPlayers.remove(monsterId);

        Hpr.info("清理了无效怪物的CardPlayer: " + monsterId);
    }

    /**
     * 检查怪物是否有效
     */
    private boolean isMonsterValid(AbstractMonster monster) {
        return monster != null &&
               !monster.isDying &&
               !monster.isEscaping &&
               monster.currentHealth > 0 &&
               monster.hb != null;
    }

    /**
     * 获取怪物的唯一标识符
     */
    private String getMonsterId(AbstractMonster monster) {
        if (monster == null) {
            return "unknown";
        }

        // 使用怪物的ID + hashCode生成唯一标识
        String baseId = monster.id != null ? monster.id : "unknown";
        int hashSuffix = System.identityHashCode(monster);
        return baseId + "_" + Math.abs(hashSuffix);
    }

    /**
     * 启用当前房间的所有怪物
     */
    public void enableRoomMonsters(java.util.ArrayList<AbstractMonster> monsters) {
        if (monsters == null || monsters.isEmpty()) {
            return;
        }

        int enabledCount = 0;
        for (AbstractMonster monster : monsters) {
            if (isMonsterValid(monster)) {
                enableMonster(monster);
                enabledCount++;
            }
        }

        Hpr.info("为当前房间启用了 " + enabledCount + " 个怪物的出牌系统");
    }

    /**
     * 禁用当前房间的所有怪物
     */
    public void disableRoomMonsters() {
        // 复制一份列表避免并发修改
        java.util.ArrayList<AbstractMonster> currentMonsters = new java.util.ArrayList<>(enabledMonsters.values());

        for (AbstractMonster monster : currentMonsters) {
            disableMonster(monster);
        }

        Hpr.info("已禁用当前房间所有怪物的出牌系统");
    }

    /**
     * 获取指定怪物的CardPlayer
     */
    public MonsterCardPlayer getCardPlayer(AbstractMonster monster) {
        if (monster == null) {
            return null;
        }

        String monsterId = getMonsterId(monster);
        return monsterCardPlayers.get(monsterId);
    }

    /**
     * 为指定怪物手动添加卡牌（用于测试）
     */
    public void addCardToMonster(AbstractMonster monster, com.megacrit.cardcrawl.cards.AbstractCard card) {
        MonsterCardPlayer cardPlayer = getCardPlayer(monster);
        if (cardPlayer != null) {
            cardPlayer.addCardToDrawPile(card);
        } else {
            Hpr.info("无法为未启用的怪物 " + monster.name + " 添加卡牌");
        }
    }

    /**
     * 获取当前启用的怪物数量
     */
    public int getEnabledCount() {
        return enabledMonsters.size();
    }

    /**
     * 获取所有启用的怪物信息
     */
    public String getEnabledMonstersInfo() {
        StringBuilder info = new StringBuilder();
        info.append("当前启用的怪物出牌系统 (").append(getEnabledCount()).append("): ");

        for (Map.Entry<String, AbstractMonster> entry : enabledMonsters.entrySet()) {
            AbstractMonster monster = entry.getValue();
            MonsterCardPlayer cardPlayer = monsterCardPlayers.get(entry.getKey());

            info.append("\n  - ").append(monster.name)
                 .append(" (抽牌: ").append(cardPlayer != null ? cardPlayer.getDrawPileSize() : 0)
                 .append(", 弃牌: ").append(cardPlayer != null ? cardPlayer.getDiscardPileSize() : 0)
                 .append(")");
        }

        return info.toString();
    }

    /**
     * 重置所有系统（用于战斗重置）
     */
    public void resetAll() {
        // 禁用所有怪物
        disableRoomMonsters();

        // 清空所有映射
        monsterCardPlayers.clear();
        enabledMonsters.clear();

        Hpr.info("MonsterCardManager系统已完全重置");
    }
}