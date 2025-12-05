package VisibleHistory.monstercards;

import VisibleHistory.utils.Hpr;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.combat.DamageNumberEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MonsterCardPlayer - 改进版怪物出牌系统
 * 基于用户自定义卡牌配置，让怪物像原版一样行动
 */
public class MonsterCardPlayer {

    // 关联的怪物
    public AbstractMonster monster;

    // 卡牌配置管理器
    private MonsterCardConfig cardConfig;

    // 独立牌库系统
    private CardGroup monsterDrawPile;                     // 怪物抽牌堆
    private CardGroup monsterDiscardPile;                  // 怪物弃牌堆

    // 当前显示的卡牌
    private AbstractCard displayedCard = null;            // 当前显示在头顶的卡牌
    private float cardDisplayTimer = 0.0f;                // 卡牌显示计时器

    // 出牌控制
    private boolean enabled = false;                       // 是否启用出牌系统
    private float turnTimer = 0.0f;                       // 回合计时器
    private static final float TURN_INTERVAL = 4.0f;      // 每4秒出一张牌（更慢，更像原版）

    // 卡牌显示位置
    private static final float CARD_DISPLAY_HEIGHT = 80.0f * Settings.scale;
    private static final float CARD_HOVER_SCALE = 0.6f;
    private static final float CARD_NORMAL_SCALE = 0.4f;

    public MonsterCardPlayer(AbstractMonster monster) {
        this.monster = monster;
        this.cardConfig = MonsterCardConfig.getInstance();
        initializeCardSystem();
    }

    /**
     * 初始化怪物的牌库系统（基于用户配置）
     */
    private void initializeCardSystem() {
        // 初始化牌库
        monsterDrawPile = new CardGroup(CardGroup.CardGroupType.DRAW_PILE);
        monsterDiscardPile = new CardGroup(CardGroup.CardGroupType.DISCARD_PILE);

        // 从配置中获取该怪物的卡牌
        ArrayList<AbstractCard> monsterCards = getMonsterCustomCards();
        if (monsterCards.isEmpty()) {
            // 如果没有自定义配置，使用默认卡牌
            monsterCards = getDefaultMonsterCards();
        }

        // 洗牌并初始化抽牌堆
        shuffleIntoDrawPile(monsterCards);

        Hpr.info("怪物CardPlayer初始化完成: " + monster.name + " ，牌库包含 " + monsterDrawPile.size() + " 张卡牌");
    }

    /**
     * 获取怪物的自定义卡牌配置
     */
    private ArrayList<AbstractCard> getMonsterCustomCards() {
        ArrayList<AbstractCard> cards = new ArrayList<>();

        String monsterId = monster.id;
        if (cardConfig.hasCustomConfig(monsterId)) {
            List<AbstractCard> configCards = cardConfig.getMonsterCardConfig(monsterId);
            cards.addAll(configCards);
            Hpr.info("为怪物 " + monster.name + " 加载了 " + cards.size() + " 张自定义卡牌");
        }

        return cards;
    }

    /**
     * 获取默认怪物卡牌（如果用户没有配置）
     */
    private ArrayList<AbstractCard> getDefaultMonsterCards() {
        ArrayList<AbstractCard> cards = new ArrayList<>();

        String monsterId = monster.id;

        // 根据怪物类型生成不同的默认卡牌组合
        if (monsterId.contains("Louse")) {
            // 虫子：咬击 + 防御
            cards.add(createMonsterCard("STRIKE_R", 6, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("DEFEND_R", 5, AbstractCard.CardType.SKILL));
        } else if (monsterId.contains("JawWorm")) {
            // 下颚蠕虫：咀嚼 + 咆哮 + 践踏
            cards.add(createMonsterCard("STRIKE_R", 11, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("RAGE", 3, AbstractCard.CardType.POWER));
            cards.add(createMonsterCard("DEFEND_R", 7, AbstractCard.CardType.SKILL));
        } else if (monsterId.contains("Slaver")) {
            // 奴隶主：多种攻击
            cards.add(createMonsterCard("BASH", 7, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("STRIKE_R", 6, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("DEFEND_R", 5, AbstractCard.CardType.SKILL));
        } else {
            // 默认配置
            cards.add(createMonsterCard("STRIKE_R", 6, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("BASH", 7, AbstractCard.CardType.ATTACK));
            cards.add(createMonsterCard("DEFEND_R", 5, AbstractCard.CardType.SKILL));
            cards.add(createMonsterCard("RAGE", 2, AbstractCard.CardType.POWER));
        }

        Hpr.info("为怪物 " + monster.name + " 生成了 " + cards.size() + " 张默认卡牌");
        return cards;
    }

    /**
     * 创建怪物卡牌（使用游戏原版卡牌）
     */
    private AbstractCard createMonsterCard(String cardId, int amount, AbstractCard.CardType type) {
        try {
            AbstractCard baseCard = CardLibrary.getCard(cardId);
            if (baseCard != null) {
                AbstractCard monsterCard = baseCard.makeCopy();

                // 设置为无色，怪物无能量限制
                monsterCard.color = AbstractCard.CardColor.COLORLESS;
                monsterCard.freeToPlayOnce = true;
                monsterCard.cost = 0;
                monsterCard.costForTurn = 0;

                // 根据卡牌类型设置数值
                if (type == AbstractCard.CardType.ATTACK) {
                    // 攻击卡牌设置伤害
                    monsterCard.baseDamage = amount;
                    monsterCard.damage = amount;
                } else if (type == AbstractCard.CardType.SKILL) {
                    // 技能卡牌设置格挡
                    monsterCard.block = amount;
                    monsterCard.baseBlock = amount;
                } else if (type == AbstractCard.CardType.POWER) {
                    // 能力卡牌（这里暂时不处理具体的power效果）
                }

                // 移除卡牌原有的限制条件
                monsterCard.isInAutoplay = true;

                return monsterCard;
            }
        } catch (Exception e) {
            Hpr.info("创建怪物卡牌时出错: " + cardId + " - " + e.getMessage());
        }

        return null;
    }

    /**
     * 洗牌到抽牌堆
     */
    private void shuffleIntoDrawPile(ArrayList<AbstractCard> cards) {
        monsterDrawPile.clear();

        if (cards == null || cards.isEmpty()) {
            Hpr.info("警告：怪物 " + monster.name + " 没有可用的卡牌");
            return;
        }

        // 添加卡牌到抽牌堆
        for (AbstractCard card : cards) {
            if (card != null) {
                monsterDrawPile.addToBottom(card);
            }
        }

        // 洗牌
        ArrayList<AbstractCard> tempList = new ArrayList<>();
        tempList.addAll(monsterDrawPile.group);
        Collections.shuffle(tempList, AbstractDungeon.cardRandomRng.random);
        monsterDrawPile.group = tempList;

        Hpr.info("怪物 " + monster.name + " 洗牌完成，抽牌堆包含 " + monsterDrawPile.size() + " 张卡牌");
    }

    /**
     * 启用出牌系统
     */
    public void enable() {
        if (!enabled) {
            enabled = true;
            turnTimer = 0.0f;
            Hpr.info("怪物 " + monster.name + " 出牌系统已启用");
        }
    }

    /**
     * 禁用出牌系统
     */
    public void disable() {
        if (enabled) {
            enabled = false;
            displayedCard = null;
            cardDisplayTimer = 0.0f;
            Hpr.info("怪物 " + monster.name + " 出牌系统已禁用");
        }
    }

    /**
     * 更新出牌逻辑
     */
    public void update() {
        if (!enabled || monster == null || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.NONE) {
            return;
        }

        // 更新回合计时器
        turnTimer += Gdx.graphics.getDeltaTime();

        // 检查是否该出牌
        if (turnTimer >= TURN_INTERVAL) {
            playNextCard();
            turnTimer = 0.0f;
        }

        // 更新卡牌显示计时器
        if (cardDisplayTimer > 0.0f) {
            cardDisplayTimer -= Gdx.graphics.getDeltaTime();
            if (cardDisplayTimer <= 0.0f) {
                displayedCard = null;
            }
        }

        // 更新显示卡牌的位置
        updateCardDisplay();
    }

    /**
     * 出下一张牌（使用游戏原生Action系统）
     */
    private void playNextCard() {
        if (monsterDrawPile == null || monsterDrawPile.isEmpty()) {
            // 尝试从弃牌堆重新洗牌
            if (monsterDiscardPile != null && !monsterDiscardPile.isEmpty()) {
                shuffleDiscardToDraw();
            } else {
                Hpr.info("怪物 " + monster.name + " 牌库已空，无法出牌");
                return;
            }
        }

        // 从抽牌堆抽一张牌
        AbstractCard drawnCard = monsterDrawPile.getTopCard();
        if (drawnCard != null) {
            // 移除顶牌
            monsterDrawPile.removeTopCard();

            // 复制一张用于显示
            displayedCard = drawnCard.makeStatEquivalentCopy();

            // 设置显示计时器
            cardDisplayTimer = 3.0f; // 显示3秒

            // 执行卡牌效果（使用游戏原生Action系统）
            executeMonsterCard(drawnCard);

            Hpr.info("怪物 " + monster.name + " 打出了: " + drawnCard.name);
        }
    }

    /**
     * 执行怪物的卡牌效果（使用游戏原生Action系统，模仿原版怪物）
     */
    private void executeMonsterCard(AbstractCard card) {
        if (card == null || monster == null) {
            return;
        }

        try {
            // 安全检查
            if (AbstractDungeon.getCurrRoom() == null ||
                AbstractDungeon.getCurrRoom().monsters == null ||
                AbstractDungeon.getCurrRoom().monsters.monsters.isEmpty()) {
                Hpr.info("怪物 " + monster.name + " 当前房间没有有效目标，跳过卡牌执行");
                return;
            }

            AbstractPlayer targetPlayer = AbstractDungeon.player;
            if (targetPlayer == null) {
                Hpr.info("怪物 " + monster.name + " 无法找到有效目标，跳过卡牌执行");
                return;
            }

            // 根据卡牌类型执行不同的Action（模仿原版怪物的takeTurn方法）
            if (card.type == AbstractCard.CardType.ATTACK) {
                // 攻击卡牌 - 造成伤害
                executeAttackCard(card, targetPlayer);
            } else if (card.type == AbstractCard.CardType.SKILL) {
                // 技能卡牌 - 获得格挡或其他效果
                executeSkillCard(card);
            } else if (card.type == AbstractCard.CardType.POWER) {
                // 能力卡牌 - 获得永久效果
                executePowerCard(card);
            }

            // 将用过的卡牌加入弃牌堆
            monsterDiscardPile.addToBottom(card.makeStatEquivalentCopy());

        } catch (Exception e) {
            Hpr.info("怪物 " + monster.name + " 执行卡牌时出错: " + e.getMessage());
        }
    }

    /**
     * 执行攻击卡牌（模仿原版怪物的DamageAction）
     */
    private void executeAttackCard(AbstractCard card, AbstractPlayer target) {
        if (card.damage > 0) {
            // 创建伤害信息（使用怪物的伤害信息系统）
            DamageInfo damageInfo = new DamageInfo(monster, card.damage, DamageInfo.DamageType.NORMAL);

            // 添加伤害Action（与原版怪物相同）
            AbstractDungeon.actionManager.addToBottom(
                new DamageAction(target, damageInfo, AbstractGameAction.AttackEffect.BLUNT_LIGHT)
            );
        }

        Hpr.info("怪物 " + monster.name + " 使用攻击卡牌 " + card.name + " 造成 " + card.damage + " 点伤害");
    }

    /**
     * 执行技能卡牌（模仿原版怪物的GainBlockAction等）
     */
    private void executeSkillCard(AbstractCard card) {
        if (card.block > 0) {
            // 获得格挡
            AbstractDungeon.actionManager.addToBottom(
                new GainBlockAction(monster, monster, card.block)
            );
        }

        Hpr.info("怪物 " + monster.name + " 使用技能卡牌 " + card.name + " 获得 " + card.block + " 点格挡");
    }

    /**
     * 执行能力卡牌（模仿原版怪物的ApplyPowerAction等）
     */
    private void executePowerCard(AbstractCard card) {
        // 这里可以扩展为应用各种power
        // 目前先处理力量提升
        if (card.name.contains("Rage") || card.name.contains("愤怒")) {
            AbstractDungeon.actionManager.addToBottom(
                new com.megacrit.cardcrawl.actions.common.ApplyPowerAction(
                    monster, monster,
                    new com.megacrit.cardcrawl.powers.StrengthPower(monster, 2), 2
                )
            );
        }

        Hpr.info("怪物 " + monster.name + " 使用能力卡牌 " + card.name);
    }

    /**
     * 从弃牌堆重新洗牌到抽牌堆
     */
    private void shuffleDiscardToDraw() {
        if (monsterDiscardPile == null || monsterDiscardPile.isEmpty()) {
            return;
        }

        int discardCount = monsterDiscardPile.size();

        monsterDrawPile.clear();
        ArrayList<AbstractCard> tempList = new ArrayList<>();
        tempList.addAll(monsterDiscardPile.group);
        Collections.shuffle(tempList, AbstractDungeon.cardRandomRng.random);
        monsterDrawPile.group = tempList;

        monsterDiscardPile.clear();

        Hpr.info("怪物 " + monster.name + " 从弃牌堆重新洗牌，将 " + discardCount + " 张牌重新加入抽牌堆");
    }

    /**
     * 更新卡牌显示
     */
    private void updateCardDisplay() {
        if (displayedCard != null && monster != null) {
            // 设置卡牌位置为怪物头顶
            float cardX = monster.drawX;
            float cardY = monster.drawY + monster.hb_h + CARD_DISPLAY_HEIGHT;

            displayedCard.current_x = cardX;
            displayedCard.current_y = cardY;
            displayedCard.target_x = cardX;
            displayedCard.target_y = cardY;

            // 根据鼠标hover调整缩放
            boolean isHovered = isCardHovered(displayedCard, cardX, cardY);
            float targetScale = isHovered ? CARD_HOVER_SCALE : CARD_NORMAL_SCALE;

            displayedCard.drawScale = targetScale;
            displayedCard.targetDrawScale = targetScale;

            // 更新卡牌状态
            displayedCard.update();
        }
    }

    /**
     * 手动检测卡牌是否被hover
     */
    private boolean isCardHovered(AbstractCard card, float cardX, float cardY) {
        if (card == null) {
            return false;
        }

        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;

        float cardWidth = card.hb.width;
        float cardHeight = card.hb.height;
        float cardLeft = cardX - cardWidth / 2.0f;
        float cardRight = cardX + cardWidth / 2.0f;
        float cardTop = cardY + cardHeight / 2.0f;
        float cardBottom = cardY - cardHeight / 2.0f;

        return mouseX >= cardLeft && mouseX <= cardRight &&
               mouseY >= cardBottom && mouseY <= cardTop;
    }

    /**
     * 渲染头顶卡牌
     */
    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch sb) {
        if (displayedCard != null) {
            displayedCard.render(sb);
        }
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 获取抽牌堆大小
     */
    public int getDrawPileSize() {
        return monsterDrawPile != null ? monsterDrawPile.size() : 0;
    }

    /**
     * 获取弃牌堆大小
     */
    public int getDiscardPileSize() {
        return monsterDiscardPile != null ? monsterDiscardPile.size() : 0;
    }

    /**
     * 手动添加卡牌到抽牌堆（用于测试）
     */
    public void addCardToDrawPile(AbstractCard card) {
        if (card != null && monsterDrawPile != null) {
            AbstractCard cardCopy = card.makeStatEquivalentCopy();
            cardCopy.color = AbstractCard.CardColor.COLORLESS;
            cardCopy.freeToPlayOnce = true;
            cardCopy.cost = 0;
            cardCopy.costForTurn = 0;
            monsterDrawPile.addToBottom(cardCopy);

            Hpr.info("为怪物 " + monster.name + " 添加了卡牌到抽牌堆: " + cardCopy.name);
        }
    }

    /**
     * 重置系统（用于重置战斗状态）
     */
    public void reset() {
        disable();
        if (monsterDrawPile != null) {
            monsterDrawPile.clear();
        }
        if (monsterDiscardPile != null) {
            monsterDiscardPile.clear();
        }

        Hpr.info("怪物 " + monster.name + " CardPlayer系统已重置");
    }
}