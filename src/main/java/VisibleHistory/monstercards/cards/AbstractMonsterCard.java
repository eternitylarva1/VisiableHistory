package VisibleHistory.monstercards.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * AbstractMonsterCard - 所有怪物卡牌的基类
 * 继承自AbstractCard，添加了怪物特定的功能
 */
public abstract class AbstractMonsterCard extends AbstractCard {

    protected CardStrings cardStrings;

    public AbstractMonsterCard(String id, String name, String img, int cost, String rawDescription,
                             CardType type, CardTarget target) {
        super(id, name, img, cost, rawDescription, type, CardColor.CURSE, CardRarity.SPECIAL, target);

        // 设置默认属性
        this.target = target;
        this.isCostModified = false;
        this.isCostModifiedForTurn = false;
        this.isDamageModified = false;
        this.isBlockModified = false;
        this.isMagicNumberModified = false;
        this.freeToPlayOnce = false;

        // 怪物卡牌的默认颜色
        if (this.color == null) {
            this.color = CardColor.CURSE;
        }
    }

    /**
     * 怪物卡牌专用的use方法
     * source: 怪物（卡牌的拥有者）
     * target: 攻击目标（通常是玩家）
     */
    public abstract void use(AbstractCreature source, AbstractCreature target);

    /**
     * 怪物卡牌的基础use实现（重写父类方法）
     */
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 怪物卡牌不使用玩家的use方法，而是使用上面的重载方法
        // 这个方法留空以避免混淆
    }

    /**
     * 辅助方法：创建伤害行动
     */
    protected void addToBot(AbstractGameAction action) {
        com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager.addToBottom(action);
    }

    /**
     * 辅助方法：创建攻击行动
     */
    protected void performAttack(AbstractCreature source, AbstractCreature target, int damage,
                                AbstractGameAction.AttackEffect effect) {
        if (target != null) {
            DamageInfo damageInfo = new DamageInfo(source, damage, damageTypeForTurn);
            addToBot(new DamageAction(target, damageInfo, effect));
        }
    }

    /**
     * 辅助方法：创建攻击行动（使用预设效果）
     */
    protected void performAttack(AbstractCreature source, AbstractCreature target, int damage) {
        performAttack(source, target, damage, AbstractGameAction.AttackEffect.NONE);
    }

    /**
     * 获取当前难度下的伤害值
     */
    protected int getCurrentDamage() {
        return this.damage;
    }

    /**
     * 获取当前难度下的格挡值
     */
    protected int getCurrentBlock() {
        return this.block;
    }

    /**
     * 获取当前难度下的魔法值
     */
    protected int getCurrentMagicNumber() {
        return this.magicNumber;
    }

    /**
     * 检查是否为高难度模式
     */
    protected boolean isHighDifficulty() {
        return com.megacrit.cardcrawl.dungeons.AbstractDungeon.ascensionLevel >= 17;
    }

    /**
     * 检查是否为中等难度模式
     */
    protected boolean isMediumDifficulty() {
        return com.megacrit.cardcrawl.dungeons.AbstractDungeon.ascensionLevel >= 2;
    }

    /**
     * 更新卡牌状态（调用此方法来同步难度相关数值）
     */
    public void updateValues() {
        // 子类可以重写此方法来更新动态数值
    }

    /**
     * 克隆卡牌（用于游戏内复制）
     */
    @Override
    public abstract AbstractMonsterCard makeCopy();

    /**
     * 检查卡牌是否可升级
     */
    @Override
    public boolean canUpgrade() {
        return false; // 默认怪物卡牌不能升级，子类可以重写
    }

    /**
     * 升级卡牌（子类重写以实现具体升级效果）
     */
    @Override
    public void upgrade() {
        // 默认实现为空，子类重写以实现升级效果
    }
}