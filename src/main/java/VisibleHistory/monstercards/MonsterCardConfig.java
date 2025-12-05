package VisibleHistory.monstercards;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import VisibleHistory.monstercards.cards.cultist.CultistDarkStrikeCard;
import VisibleHistory.monstercards.cards.cultist.CultistIncantationCard;
import VisibleHistory.monstercards.cards.jawworm.JawWormBellowCard;
import VisibleHistory.monstercards.cards.jawworm.JawWormChompCard;
import VisibleHistory.monstercards.cards.jawworm.JawWormThrashCard;
import VisibleHistory.monstercards.cards.louse.LouseBiteCard;
import VisibleHistory.monstercards.cards.louse.LouseShellCard;
import VisibleHistory.monstercards.cards.slaver.SlaverRakeCard;
import VisibleHistory.monstercards.cards.slaver.SlaverStabCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 怪物卡牌配置管理器
 * 为每个怪物提供基于其真实行为的卡牌
 */
public class MonsterCardConfig {

    // 单例模式
    private static MonsterCardConfig instance = null;

    // 怪物ID -> 卡牌配置列表的映射
    private Map<String, List<AbstractCard>> monsterCardConfigs;

    private MonsterCardConfig() {
        monsterCardConfigs = new HashMap<>();
        initializeMonsterSpecificCards();
    }

    public static MonsterCardConfig getInstance() {
        if (instance == null) {
            instance = new MonsterCardConfig();
        }
        return instance;
    }

    /**
     * 初始化每个怪物的特定卡牌配置
     * 基于原版游戏的takeTurn()行为
     */
    private void initializeMonsterSpecificCards() {
        // Louse Normal (虫子) - ID: FuzzyLouseNormal
        List<AbstractCard> louseCards = new ArrayList<>();
        louseCards.add(new LouseBiteCard());     // 咬击：攻击 6伤害
        louseCards.add(new LouseShellCard());    // 外壳：+3力量
        monsterCardConfigs.put("FuzzyLouseNormal", louseCards);

        // Louse Defensive (防御型虫子) - 类似的配置
        List<AbstractCard> louseDefCards = new ArrayList<>();
        louseDefCards.add(new LouseBiteCard());     // 咬击：攻击 6伤害
        louseDefCards.add(new LouseShellCard());    // 外壳：+3力量
        monsterCardConfigs.put("FuzzyLouseDefensive", louseDefCards);

        // Jaw Worm (下颚蠕虫) - ID: JawWorm
        List<AbstractCard> jawWormCards = new ArrayList<>();
        jawWormCards.add(new JawWormChompCard());    // 咀嚼：攻击 11伤害
        jawWormCards.add(new JawWormBellowCard());   // 咆哮：+3力量+6格挡
        jawWormCards.add(new JawWormThrashCard());   // 乱击：攻击7+5格挡
        monsterCardConfigs.put("JawWorm", jawWormCards);

        // Cultist (邪教徒) - ID: Cultist
        List<AbstractCard> cultistCards = new ArrayList<>();
        cultistCards.add(new CultistDarkStrikeCard());  // 黑暗打击：攻击 6伤害
        cultistCards.add(new CultistIncantationCard()); // 咒语：+3仪式力量
        monsterCardConfigs.put("Cultist", cultistCards);

        // Slaver Blue (蓝奴隶主) - ID: SlaverBlue
        List<AbstractCard> slaverBlueCards = new ArrayList<>();
        slaverBlueCards.add(new SlaverStabCard());   // 刺击：攻击 12伤害
        slaverBlueCards.add(new SlaverRakeCard());   // 抓挠：攻击7+虚弱1
        monsterCardConfigs.put("SlaverBlue", slaverBlueCards);

        // Slaver Red (红奴隶主) - 类似蓝奴隶主
        List<AbstractCard> slaverRedCards = new ArrayList<>();
        slaverRedCards.add(new SlaverStabCard());   // 刺击：攻击 12伤害
        slaverRedCards.add(new SlaverRakeCard());   // 抓挠：攻击7+虚弱1
        monsterCardConfigs.put("SlaverRed", slaverRedCards);
    }

    /**
     * 获取指定怪物的卡牌配置
     * 如果没有找到特定配置，返回null
     */
    public List<AbstractCard> getMonsterCardConfig(String monsterId) {
        List<AbstractCard> config = monsterCardConfigs.get(monsterId);
        if (config != null) {
            return new ArrayList<>(config);
        }
        // 如果没有找到特定配置，返回null（表示没有卡牌配置）
        return null;
    }

    /**
     * 检查是否有怪物特定的配置
     */
    public boolean hasCustomConfig(String monsterId) {
        return monsterCardConfigs.containsKey(monsterId);
    }

    /**
     * 获取所有配置的怪物ID列表
     */
    public List<String> getConfiguredMonsterIds() {
        return new ArrayList<>(monsterCardConfigs.keySet());
    }

    /**
     * 清空所有配置（用于重置）
     */
    public void clearAllConfigs() {
        monsterCardConfigs.clear();
    }

    /**
     * 为指定怪物添加自定义卡牌配置
     */
    public void addCustomConfig(String monsterId, List<AbstractCard> cards) {
        if (monsterId != null && cards != null) {
            monsterCardConfigs.put(monsterId, new ArrayList<>(cards));
        }
    }

    /**
     * 移除指定怪物的配置
     */
    public void removeConfig(String monsterId) {
        monsterCardConfigs.remove(monsterId);
    }
}