package VisibleHistory.playerdeath;

import VisibleHistory.modcore.visibleHistory;
import VisibleHistory.utils.Hpr;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.TinyChest;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.TinyCard;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.actions.utility.UnlimboAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javafx.scene.shape.Circle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static VisibleHistory.modcore.MyModConfig.toumingdu;
import static VisibleHistory.modcore.MyModConfig.showCards;
import static VisibleHistory.modcore.MyModConfig.showRelics;
import static VisibleHistory.modcore.MyModConfig.ctrlClickTransform;

import static VisibleHistory.modcore.visibleHistory.testTexture;
import static com.megacrit.cardcrawl.helpers.ImageMaster.CAMPFIRE_SMITH_BUTTON;

public class DeadPlayer {
    public static ArrayList<DeadPlayer>  deadPlayers=new ArrayList<>();

    // 缓存最后hover的尸体，避免每帧重复计算
    private static DeadPlayer cachedLastHoveredPlayer = null;
    private static boolean hoverCacheDirty = true;

    // 双击检测相关变量
    private static DeadPlayer lastClickedPlayer = null;
    private static long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_WINDOW = 500; // 双击时间窗口（毫秒）

    public float x;
    public float y;
    public Texture img;
    public boolean flipHorizontal=false;
    public boolean flipVertical=false;
    public Hitbox hb;
    private static final float RELIC_SPACE;

    // 新增拖动相关属性
    public boolean isDragging = false;
    public boolean isDraggingRequested = false;
    public boolean showHistory = false;  // 是否显示历史记录
    public boolean isRevived = false;    // 是否已复活（显示活着的玩家而不是尸体）
    List<String> relics;
    public static final String[] TEXT;
    String runDate = "";
    List<TinyCard> cards=new ArrayList<>();
    private static final String RARITY_LABEL_STARTER;
    private static final String RARITY_LABEL_COMMON;
    private static final String RARITY_LABEL_UNCOMMON;
    private static final String RARITY_LABEL_RARE;
    private static final String RARITY_LABEL_SPECIAL;
    private static final String RARITY_LABEL_BOSS;
    private static final String RARITY_LABEL_SHOP;
    private static final String RARITY_LABEL_UNKNOWN;
    private static final String RARITY_LABEL_CURSE;
    AbstractPlayer deadPlayerChosen;
    private RunData runData; // 延迟加载的Run数据
    private boolean cardsLoaded = false; // 卡组是否已加载
    private boolean dataLoaded = false; // 通用数据是否已加载(用于遗物显示)

    // 新增：复活后牌堆和显示系统
    private ArrayList<AbstractCard> revivedDeck = null;  // 复活后的牌堆
    private ArrayList<AbstractRelic> revivedRelics = null; // 复活后的遗物
    private AbstractCard displayedCard = null;            // 当前显示在头顶的卡牌
    private float cardDisplayTimer = 0.0f;                // 卡牌显示计时器
    private static final float CARD_DISPLAY_DURATION = 3.0f; // 卡牌显示持续时间
    public DeadPlayer(float x, float y, Texture img, RunData runs) throws ParseException {
        this.x=x;
        this.y=y;
        this.img=img;
        this.hb=new Hitbox(300,150);
        this.hb.x=this.x+100;
        this.hb.y=this.y;
        relics=runs.relics;

        // 延迟初始化 - 只在需要时才加载详细数据
        this.runData = runs;

        // 简单的日期处理，避免重复创建格式化器
        try {
            Date date = Metrics.timestampFormatter.parse(runs.local_time);
            // 使用简单的数字格式，避免复杂的本地化逻辑
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            runDate = dateFormat.format(date);
        } catch (Exception e) {
            runDate = runs.local_time.substring(0, Math.min(16, runs.local_time.length()));
        }
    }

    /**
     * 延迟加载卡组和玩家动画（仅在首次需要时加载）
     */
    private void lazyLoadDataIfNeeded() {
        if (!dataLoaded && runData != null) {
            try {
                // 根据配置决定是否加载卡组数据
                if (showCards) {
                    reloadCards(runData);
                    cardsLoaded = true;
                }

                // 延迟加载玩家动画
                try {
                    if (deadPlayerChosen == null && runData.character_chosen != null) {
                        AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.valueOf(runData.character_chosen);
                        deadPlayerChosen = CardCrawlGame.characterManager.getCharacter(playerClass).newInstance();
                        deadPlayerChosen.movePosition(this.x + Settings.WIDTH / 12, this.y);
                    }
                } catch (Exception e) {
                    Hpr.info("初始化deadPlayerChosen失败: " + e.getMessage());
                    deadPlayerChosen = null;
                }

                // 标记为已加载
                dataLoaded = true;
            } catch (Exception e) {
                Hpr.info("延迟加载数据失败: " + e.getMessage());
                dataLoaded = true; // 即使失败也标记为已加载，避免重复尝试
            }
        }
    }

    public void update() {
        if (AbstractDungeon.screen!= AbstractDungeon.CurrentScreen.NONE){
            return;
        }
        // 更新hitbox检测
        this.hb.update();

        // 更新hitbox位置
        this.hb.x = this.x;
        this.hb.y = this.y;

        // 检查鼠标输入
        checkMouseInput();

        // 处理拖动逻辑
        if (isDragging) {
            handleDragging();
        }

        // 处理复活玩家的动画更新
        if (isRevived && deadPlayerChosen != null) {
            this.deadPlayerChosen.update();
            // 让尸体位置跟随玩家动画位置
            if (showHistory && false) { // 可选：让复活玩家也跟随动画
                this.x = this.deadPlayerChosen.drawX;
                this.y = this.deadPlayerChosen.drawY;
            } else {
                // 否则让玩家位置跟随尸体位置
                deadPlayerChosen.movePosition(this.x + Settings.WIDTH / 12, this.y);
            }
        }

        // 更新头顶显示的卡牌
        updateCardDisplay();
/*
        // 处理动画显示
        if (showHistory && xianshidonghua) {
            this.deadPlayerChosen.update();
            this.x = this.deadPlayerChosen.drawX;
            this.y = this.deadPlayerChosen.drawY;
        }*/
    }

    /**
     * 检查鼠标输入（左键拖动、右键+悬停显示历史、Ctrl+双击转换）
     */
    private void checkMouseInput() {
        // 左键拖动检测
        if (InputHelper.justClickedLeft && this.hb.hovered && !isDragging) {
            // 检查Ctrl+双击
            checkCtrlDoubleClick();

            // 如果不是Ctrl+双击，则进行拖动
            if (!isCtrlDoubleClick()) {
                isDragging = true;
                isDraggingRequested = true;
            }
        }

        // 右键显示历史记录检测 - 只有右键按住且悬停时才显示
        if (InputHelper.isMouseDown_R && this.hb.hovered && !isDragging) {
            showHistory = true;
        } else {
            showHistory = false;
        }

        // 松开鼠标时停止拖动
        if (InputHelper.justReleasedClickLeft && isDragging) {
            isDragging = false;
            InputHelper.justReleasedClickLeft = false;
        }
    }

    /**
     * 处理拖动逻辑
     */
    private void handleDragging() {
        // 获取鼠标位置（考虑缩放）
        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;

        // 更新尸体位置到鼠标位置
        this.x = mouseX - this.hb.width / 2.0f;
        this.y = mouseY - this.hb.height / 2.0f;

        // 限制尸体在屏幕范围内
        this.x = Math.max(0, Math.min(this.x, Settings.WIDTH - this.hb.width));
        this.y = Math.max(0, Math.min(this.y, Settings.HEIGHT - this.hb.height));
    }

    private static boolean ctrlDoubleClickTriggered = false;

    /**
     * 检查当前是否是Ctrl+双击
     */
    private boolean isCtrlDoubleClick() {
        return ctrlDoubleClickTriggered;
    }

    /**
     * 检查并处理Ctrl+双击逻辑
     */
    private void checkCtrlDoubleClick() {
        ctrlDoubleClickTriggered = false;

        // 检测Ctrl键是否被按下
        boolean ctrlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                              Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        // 只有在Ctrl键按下时才检测双击
        if (!ctrlPressed) {
            // 如果没有按Ctrl，只记录第一次点击但不触发双击
            long currentTime = System.currentTimeMillis();
            lastClickedPlayer = this;
            lastClickTime = currentTime;
            return;
        }

        // Ctrl已按下，检查是否是同一个尸体的双击
        long currentTime = System.currentTimeMillis();
        if (this == lastClickedPlayer && (currentTime - lastClickTime) < DOUBLE_CLICK_TIME_WINDOW) {
            // 这是Ctrl+双击，触发转换功能
            ctrlDoubleClickTriggered = true;

            // 检查配置选项是否启用
            if (ctrlClickTransform) {
                performTransformation();
            } else {
                Hpr.info("Ctrl+双击转换功能已在设置中禁用");
            }

            // 重置点击记录
            lastClickedPlayer = null;
            lastClickTime = 0;
        } else {
            // 第一次点击，更新记录
            lastClickedPlayer = this;
            lastClickTime = currentTime;
        }
    }

    /**
     * 执行卡组和遗物转换功能
     */
    private void performTransformation() {
        if (AbstractDungeon.player == null || runData == null) {
            return;
        }

        try {
            // 1. 转换卡组
            transformPlayerDeck();

            // 2. 转换遗物
            transformPlayerRelics();

            // 3. 移除当前尸体
            removeThisCorpse();

            Hpr.info("成功转换卡组和遗物，并移除了尸体");
        } catch (Exception e) {
            Hpr.info("转换过程中出现错误: " + e.getMessage());
        }
    }

    /**
     * 转换玩家卡组到尸体的卡组
     */
    private void transformPlayerDeck() {
        if (runData.master_deck == null || runData.master_deck.isEmpty()) {
            return;
        }

        // 清空当前卡组
        AbstractDungeon.player.masterDeck.clear();

        // 根据尸体的卡组数据添加卡牌
        for (String cardID : runData.master_deck) {
            AbstractCard card = cardForName(runData, cardID);
            if (card != null) {
                AbstractDungeon.player.masterDeck.addToBottom(card);
            }
        }

        // 重新洗牌，将卡组分配到各个区域
        redistributeCards();

        Hpr.info("已将玩家卡组转换为尸体的卡组，包含 " + runData.master_deck.size() + " 张卡牌");
    }

    /**
     * 重新分配卡牌到各个卡组区域（抽牌堆、手牌、弃牌堆）
     */
    private void redistributeCards() {
        // 清空所有战斗中的卡牌堆
        AbstractDungeon.player.drawPile.clear();
        AbstractDungeon.player.hand.clear();
        AbstractDungeon.player.discardPile.clear();

        // 将主卡组的所有卡牌移到抽牌堆
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            AbstractDungeon.player.drawPile.addToBottom(card.makeStatEquivalentCopy());
        }

        // 抽取初始手牌
        int handSize = Math.min(5, AbstractDungeon.player.drawPile.size());
        for (int i = 0; i < handSize; i++) {
            AbstractDungeon.actionManager.addToBottom(new DrawCardAction(1));

        }
    }

    /**
     * 转换玩家遗物到尸体的遗物
     */
    private void transformPlayerRelics() {
        if (relics == null || relics.isEmpty()) {
            return;
        }

        // 先移除所有当前遗物（触发onUnequip）
        for (AbstractRelic relic : new ArrayList<>(AbstractDungeon.player.relics)) {
            AbstractDungeon.player.loseRelic(relic.relicId);
        }
int i=0;
        // 添加尸体的遗物
        for (String relicID : relics) {
            try {
                AbstractRelic relic = RelicLibrary.getRelic(relicID).makeCopy();
                if (relic != null) {
                    relic.isSeen = true;
                 relic. instantObtain(AbstractDungeon.player, i,true);
                 i++;
                }
            } catch (Exception e) {
                Hpr.info("无法添加遗物 " + relicID + ": " + e.getMessage());
            }
        }

        Hpr.info("已将玩家遗物转换为尸体的遗物，包含 " + relics.size() + " 个遗物");
    }

    /**
     * 移除当前尸体（仅当前战斗）
     */
    public void removeThisCorpse() {
        // 使用安全的方式移除尸体，避免ConcurrentModificationException
        markForRemoval(this);
        Hpr.info("标记尸体移除，当前剩余尸体数量: " + deadPlayers.size());
    }

    // 需要移除的尸体列表
    private static ArrayList<DeadPlayer> corpsesToRemove = new ArrayList<>();

    /**
     * 安全标记尸体需要在下一帧移除
     */
    private static void markForRemoval(DeadPlayer corpse) {
        corpsesToRemove.add(corpse);
    }

    /**
     * 处理需要移除的尸体（在每帧更新前调用）
     */
    public static void processRemovals() {
        if (!corpsesToRemove.isEmpty()) {
            deadPlayers.removeAll(corpsesToRemove);
            invalidateHoverCache();
            int removedCount = corpsesToRemove.size();
            corpsesToRemove.clear();
            Hpr.info("安全移除了 " + removedCount + " 个尸体，剩余尸体数量: " + deadPlayers.size());
        }
    }

    /**
     * 复活这具尸体 - 让尸体开始渲染活着的玩家，并初始化牌堆和遗物
     */
    public void reviveCorpse() {
        if (!isRevived) {
            isRevived = true;
            // 确保玩家数据已加载
            lazyLoadDataIfNeeded();
            initializeRevivedPlayer();
            Hpr.info("尸体已被复活，现在将显示活着的玩家并初始化牌堆和遗物");
        }
    }

    /**
     * 初始化复活玩家的牌堆和遗物系统
     */
    private void initializeRevivedPlayer() {
        // 初始化牌堆
        if (revivedDeck == null) {
            revivedDeck = createRevivedDeck();
        }

        Hpr.info("复活玩家牌堆大小: " + (revivedDeck != null ? revivedDeck.size() : 0));
    }

    /**
     * 创建复活后的牌堆（基于历史卡组数据）
     */
    private ArrayList<AbstractCard> createRevivedDeck() {
        ArrayList<AbstractCard> deck = new ArrayList<>();
        try {
            if (runData != null && runData.master_deck != null) {
                for (Object cardObj : runData.master_deck) {
                    // 这里需要根据实际的TinyCard结构来处理
                    if (cardObj instanceof String) {
                        String cardId = (String) cardObj;
                        try {
                            AbstractCard card = CardLibrary.getCard(cardId);
                            if (card != null) {
                                deck.add(card.makeCopy());
                            }
                        } catch (Exception e) {
                            Hpr.info("无法创建卡牌: " + cardId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Hpr.info("创建牌堆时出错: " + e.getMessage());
        }
        return deck;
    }

    /**
     * 创建复活后的遗物列表
     */
    private ArrayList<AbstractRelic> createRevivedRelics() {
        ArrayList<AbstractRelic> relicsList = new ArrayList<>();
        try {
            if (relics != null) {
                for (String relicId : relics) {
                    try {
                        AbstractRelic relic = RelicLibrary.getRelic(relicId);
                        if (relic != null) {
                            relicsList.add(relic.makeCopy());
                        }
                    } catch (Exception e) {
                        Hpr.info("无法创建遗物: " + relicId);
                    }
                }
            }
        } catch (Exception e) {
            Hpr.info("创建遗物列表时出错: " + e.getMessage());
        }
        return relicsList;
    }

    /**
     * 回合开始时抽一张牌并显示在头顶
     */
    public void drawCardAtTurnStart() {
        if (!isRevived || revivedDeck == null || revivedDeck.isEmpty()) {
            return;
        }

        // 从牌堆顶部抽一张牌
        AbstractCard drawnCard = revivedDeck.get(0);
        revivedDeck.remove(0);

        // 设置显示的卡牌
        displayedCard = drawnCard.makeCopy();

        // 调整卡牌大小 - 使用较小的缩放比例
        displayedCard.drawScale = 0.4f;  // 从0.8f改为0.4f，使卡牌更小
        displayedCard.targetDrawScale = 0.4f;
float cardDrawX1=this.deadPlayerChosen.drawX-this.deadPlayerChosen.hb_w/2;
        float cardDrawY1=this.deadPlayerChosen.drawY+this.deadPlayerChosen.hb_h;
        // 计算卡牌位置 - 使用与updateCardDisplay相同的逻辑
        float cardDrawX = this.x + (this.hb.width / 2.0f) - (AbstractCard.IMG_WIDTH * displayedCard.drawScale / 2.0f);
        float cardDrawY = this.y + this.hb.height + 30.0f * Settings.scale;

        displayedCard.current_x = cardDrawX;
        displayedCard.current_y = cardDrawY;
        displayedCard.target_x = cardDrawX;
        displayedCard.target_y = cardDrawY;

        // 设置卡牌的hitbox位置
        displayedCard.hb.move(cardDrawX, cardDrawY);

        Hpr.info(getCharacterName() + " 回合开始抽牌: " + displayedCard.name);
    }

    /**
     * 更新卡牌显示状态
     */
    private void updateCardDisplay() {
        if (displayedCard != null) {
            // 持续更新卡牌动画和位置




            float cardDrawY =this.deadPlayerChosen.drawY+this.deadPlayerChosen.hb_h;

            // 处理卡牌悬停放大效果
            if (displayedCard.hb.hovered) {
                // 鼠标悬停时放大
                float targetScale = 0.6f; // 悬停时的放大目标
                if (displayedCard.drawScale < targetScale) {
                    displayedCard.drawScale += Gdx.graphics.getDeltaTime() * 2.0f; // 平滑放大
                    displayedCard.drawScale = Math.min(displayedCard.drawScale, targetScale);
                    displayedCard.targetDrawScale = targetScale;
                }
            } else {
                // 鼠标未悬停时恢复到正常大小
                float normalScale = 0.4f; // 正常大小
                if (displayedCard.drawScale > normalScale) {
                    displayedCard.drawScale -= Gdx.graphics.getDeltaTime() * 2.0f; // 平滑缩小
                    displayedCard.drawScale = Math.max(displayedCard.drawScale, normalScale);
                    displayedCard.targetDrawScale = normalScale;
                }
            }

            // 重新计算卡牌位置（因为scale可能变化了）
          float  cardDrawX = this.deadPlayerChosen.drawX-this.deadPlayerChosen.hb_w/2;

            // 设置卡牌位置
            displayedCard.current_x = cardDrawX;
            displayedCard.current_y = cardDrawY;
            displayedCard.target_x = cardDrawX;
            displayedCard.target_y = cardDrawY;

            // 让卡牌跟随尸体移动
            if (isDragging) {
                displayedCard.hb.move(cardDrawX, cardDrawY);
            }
            displayedCard.update();
        }
    }

    /**
     * 在游戏回合开始时触发所有复活尸体的抽牌
     */
    public static void triggerRevivedDrawPhase() {
        for (DeadPlayer corpse : deadPlayers) {
            if (corpse.isRevived && corpse.revivedDeck != null && !corpse.revivedDeck.isEmpty()) {
                corpse.drawCardAtTurnStart();
                // 正确使用卡牌：通过动作管理器执行而不是直接调用use()
                if (corpse.displayedCard != null) {
                    corpse.executeRevivedCardAction(corpse.displayedCard);
                }
            }
        }
        Hpr.info("触发复活尸体抽牌阶段");
    }

    /**
     * 执行复活尸体的卡牌动作
     */
    private void executeRevivedCardAction(AbstractCard card) {
        if (card == null || deadPlayerChosen == null) {
            return;
        }

        try {
            // 选择目标怪物
            AbstractMonster targetMonster = null;
            if (card.target == AbstractCard.CardTarget.ENEMY) {
                targetMonster = AbstractDungeon.getRandomMonster();
            }

            // 设置卡牌为尸体使用，确保尸体作为卡牌使用者
            card.current_x = card.target_x = 0.0f;
            card.current_y = card.target_y = 0.0f;

            // 让复活尸体作为卡牌使用者使用卡牌
            if (card.target == AbstractCard.CardTarget.ENEMY && targetMonster != null) {
                card.calculateCardDamage(targetMonster);
                card.use(deadPlayerChosen, targetMonster);
            } else if (card.target != AbstractCard.CardTarget.ENEMY) {
                card.use(deadPlayerChosen, null);
            }

            Hpr.info("复活尸体 " + getCharacterName() + " 执行卡牌: " + card.name);
        } catch (Exception e) {
            Hpr.info("执行复活尸体卡牌时出错: " + e.getMessage());
            // 降级方案：直接使用卡牌
            card.use(deadPlayerChosen, AbstractDungeon.getRandomMonster());
        }
    }

    /**
     * 获取角色名称
     */
    public String getCharacterName() {
        if (runData != null && runData.character_chosen != null) {
            return runData.character_chosen;
        }
        return "未知角色";
    }

    /**
     * 静态方法：处理全局鼠标点击事件
     */
    public static void handleGlobalMouseInput() {
        for (DeadPlayer deadPlayer : DeadPlayer.deadPlayers) {
            if (deadPlayer.isDraggingRequested) {
                deadPlayer.isDraggingRequested = false;
                return; // 只处理第一个请求拖动的尸体
            }
        }
    }
    private float screenPos(float val) {
        return val * Settings.scale;
    }

    private float screenPosX(float val) {
        return val * Settings.xScale;
    }

    private float screenPosY(float val) {
        return val * Settings.yScale;
    }
    private void renderSubHeadingWithMessage(SpriteBatch sb, String main, String description, float x, float y) {
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, main, x, y, Settings.GOLD_COLOR);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.cardDescFont_N, description, x + FontHelper.getSmartWidth(FontHelper.buttonLabelFont, main, 99999.0F, 0.0F), y - 4.0F * Settings.scale, Settings.CREAM_COLOR);
    }
    private void renderPlayer(SpriteBatch sb) {
        deadPlayerChosen.render(sb);
    }
    private float renderRelics(SpriteBatch sb, float x, float y) {
        String mainText = String.format(TEXT[21], TEXT[10], this.relics.size());
        this.renderSubHeadingWithMessage(sb, mainText,runDate, x, y);
        int col = 0;
        int row = 0;
        float relicStartX = x + this.screenPosX(30.0F) + RELIC_SPACE / 2.0F;
        float relicStartY = y - RELIC_SPACE - this.screenPosY(10.0F);

        // 确保y坐标在屏幕范围内
        float adjustedY = adjustYPositionForScreenBoundary(relicStartY);

        for(String rs : this.relics) {
            if (col == 15) {
                col = 0;
                ++row;
            }
            AbstractRelic r= RelicLibrary.getRelic(rs).makeCopy();
            r.isSeen=true;
            r.currentX = relicStartX + RELIC_SPACE * (float)col;
            r.currentY = adjustedY - RELIC_SPACE * (float)row;
            r.hb.move(r.currentX, r.currentY);
            r.render(sb, false, Settings.TWO_THIRDS_TRANSPARENT_BLACK_COLOR);
            ++col;
        }

        return adjustedY - RELIC_SPACE * (float)row;
    }
    public void render(SpriteBatch sb) {
        this.hb.render(sb);

        // 鼠标悬停效果：尸体变亮 + 鼠标变成放大镜
        boolean isHovered = this.hb.hovered;

        if (isHovered) {
            CardCrawlGame.cursor.changeType(GameCursor.CursorType.INSPECT);
        }

        // 获取当前所有hover尸体中最后生成的那一个
        DeadPlayer lastHoveredPlayer = getLastHoveredPlayer();

        // 判断是否应该显示详细信息（右键按住 + 当前是最后hover的尸体）
        boolean shouldShowHistory = showHistory && (lastHoveredPlayer == this);

        // 如果需要显示详细数据，触发延迟加载
        if (shouldShowHistory || (showHistory && isHovered)) {
            lazyLoadDataIfNeeded();
        }

        // 渲染尸体
        // 如果已复活，显示活着的玩家而不是尸体
        if (isRevived && deadPlayerChosen != null) {
            // 复活状态：显示活着的玩家
            sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            // 更新玩家动画
            deadPlayerChosen.update();

            // 渲染活着的玩家
            renderPlayer(sb);
        } else if (!isHovered) {
            // 默认状态：半透明尸体
            sb.setColor(1.0f, 1.0f, 1.0f, toumingdu);
            sb.draw(this.img, this.x, this.y);
        } else if (isHovered && !shouldShowHistory) {
            // 鼠标悬停状态：变亮但不显示详细信息
            sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            sb.draw(this.img, this.x, this.y);
        } else {
            // 显示详细信息状态（右键按住 + 是最后hover的尸体）
            if (false && deadPlayerChosen != null) {
                sb.setColor(1.0f, 1.0f, 1.0f, toumingdu);
                renderPlayer(sb);
                sb.setColor(Color.WHITE);
            } else {
                sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                sb.draw(this.img, this.x, this.y);
            }

            // 确保数据已加载后再显示历史记录
            if (dataLoaded) {
                // 调整y位置以确保历史记录始终在屏幕内
                float adjustedY = adjustYPositionForScreenBoundary(this.y);

                // 根据配置决定是否显示遗物
                if (showRelics) {
                    this.renderRelics(sb, this.x, adjustedY);
                }

                // 根据配置决定是否显示卡组
                if (showCards && cardsLoaded) {
                    this.renderDeck(sb, this.x, adjustedY);
                }
            }
        }

        sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);

//TODO 1.优化交互（右键拖动交互） 2.增加设置界面+显示上限√（1.显示尸体数量 2.透明度 3.位置）(done)
        // 交互方式已改为：右键按住+悬停显示历史记录，左键拖动尸体

        // FontHelper.renderFont(sb,FontHelper.largeCardFont,"测试位置",this.x,this.y, Color.WHITE);

        // 渲染头顶显示的卡牌（如果是复活状态且有卡牌要显示）
        if (displayedCard != null) {
            displayedCard.render(sb);
        }
    }
    private static final float HIDE_X = -800.0F * Settings.xScale;
    private float SHOW_X = 300.0F * Settings.xScale;
    private float screenX = SHOW_X;
    private float targetX = SHOW_X;

    private static final AbstractCard.CardRarity[] orderedRarity= new AbstractCard.CardRarity[]{AbstractCard.CardRarity.SPECIAL, AbstractCard.CardRarity.RARE, AbstractCard.CardRarity.UNCOMMON, AbstractCard.CardRarity.COMMON, AbstractCard.CardRarity.BASIC, AbstractCard.CardRarity.CURSE};
    ;

    private void renderDeck(SpriteBatch sb, float x, float y) {

        this.screenX = MathHelper.uiLerpSnap(this.screenX, this.targetX);

        layoutTinyCards((ArrayList<TinyCard>) cards, this.screenX + screenPosX(90.0F), y-100);
        int cardCount = 0;
        for (TinyCard card : cards) {
            card.render(sb);
            cardCount += card.count;
        }
        String      LABEL_WITH_COUNT_IN_PARENS = TEXT[21];
        String mainText = String.format(LABEL_WITH_COUNT_IN_PARENS, new Object[] { TEXT[9], Integer.valueOf(cardCount) });
        renderSubHeadingWithMessage(sb, mainText, "", x, y-100);
    }

    /**
     * 调整y坐标以确保历史记录始终在屏幕内
     * 如果位置超出屏幕上半部分，则向下移动半个屏幕的距离
     * @param y 原始y坐标
     * @return 调整后的y坐标
     */
    private float adjustYPositionForScreenBoundary(float y) {
        // 如果y坐标在屏幕的上半部分（前1/3），则将其向下移动半个屏幕的高度
        if (y < Settings.HEIGHT / 3) {
            return y + Settings.HEIGHT / 2;
        }
        // 如果调整后的位置超出了屏幕下半部分，则将其限制在屏幕内
        else if (y > Settings.HEIGHT * 2 / 3) {
            return Settings.HEIGHT * 2 / 3;
        }
        // 否则保持原位置
        return y;
    }

    private void reloadCards(RunData runData) {
        Hashtable<String, AbstractCard> rawNameToCards = new Hashtable();
        Hashtable<AbstractCard, Integer> cardCounts = new Hashtable();
        Hashtable<AbstractCard.CardRarity, Integer> cardRarityCounts = new Hashtable();
        CardGroup sortedMasterDeck = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);

        for(String cardID : runData.master_deck) {
            AbstractCard card;
            if (rawNameToCards.containsKey(cardID)) {
                card = (AbstractCard)rawNameToCards.get(cardID);
            } else {
                card = this.cardForName(runData, cardID);
            }

            if (card != null) {
                int value = cardCounts.containsKey(card) ? (Integer)cardCounts.get(card) + 1 : 1;
                cardCounts.put(card, value);
                rawNameToCards.put(cardID, card);
                int rarityCount = cardRarityCounts.containsKey(card.rarity) ? (Integer)cardRarityCounts.get(card.rarity) + 1 : 1;
                cardRarityCounts.put(card.rarity, rarityCount);
            }
        }

        sortedMasterDeck.clear();

        for(AbstractCard card : rawNameToCards.values()) {
            sortedMasterDeck.addToTop(card);
        }

        sortedMasterDeck.sortAlphabetically(true);
        sortedMasterDeck.sortByRarityPlusStatusCardType(false);
        sortedMasterDeck = sortedMasterDeck.getGroupedByColor();
        this.cards.clear();

        for(AbstractCard card : sortedMasterDeck.group) {
            this.cards.add(new TinyCard(card, (Integer)cardCounts.get(card)));
        }

        StringBuilder bldr = new StringBuilder();

        for(AbstractCard.CardRarity rarity :orderedRarity ) {
            if (cardRarityCounts.containsKey(rarity)) {
                if (bldr.length() > 0) {
                    bldr.append(", ");
                }

                bldr.append(String.format( TEXT[20], cardRarityCounts.get(rarity), this.rarityLabel(rarity)));
            }
        }

        this.cardCountByRarityString = bldr.toString();
    }
    private String cardCountByRarityString;
    private String rarityLabel(AbstractCard.CardRarity rarity) {
        switch (rarity) {
            case BASIC:
                return RARITY_LABEL_STARTER;
            case SPECIAL:
                return RARITY_LABEL_SPECIAL;
            case COMMON:
                return RARITY_LABEL_COMMON;
            case UNCOMMON:
                return RARITY_LABEL_UNCOMMON;
            case RARE:
                return RARITY_LABEL_RARE;
            case CURSE:
                return RARITY_LABEL_CURSE;
            default:
                return RARITY_LABEL_UNKNOWN;
        }
    }

    private AbstractCard cardForName(RunData runData, String cardID) {
        String libraryLookupName = cardID;
        if (cardID.endsWith("+")) {
            libraryLookupName = cardID.substring(0, cardID.length() - 1);
        }

        if (libraryLookupName.equals("Defend") || libraryLookupName.equals("Strike")) {
            libraryLookupName = libraryLookupName + this.baseCardSuffixForCharacter(runData.character_chosen);
        }

        AbstractCard card = CardLibrary.getCard(libraryLookupName);
        int upgrades = 0;
        if (card != null) {
            if (cardID.endsWith("+")) {
                upgrades = 1;
            }
        } else if (libraryLookupName.contains("+")) {
            String[] split = libraryLookupName.split("\\+", -1);
            libraryLookupName = split[0];
            upgrades = Integer.parseInt(split[1]);
            card = CardLibrary.getCard(libraryLookupName);
        }

        if (card == null) {

            return null;
        } else {
            card = card.makeCopy();

            for(int i = 0; i < upgrades; ++i) {
                card.upgrade();
            }

            return card;
        }
    }
    public String baseCardSuffixForCharacter(String character) {
        switch (AbstractPlayer.PlayerClass.valueOf(character)) {
            case IRONCLAD:
                return "_R";
            case THE_SILENT:
                return "_G";
            case DEFECT:
                return "_B";
            case WATCHER:
                return "_W";
            default:
                return "";
        }
    }
    private void layoutTinyCards(ArrayList<TinyCard> cards, float x, float y) {
        float originX = x + screenPosX(60.0F);
        float originY = y - screenPosY(64.0F);
        float rowHeight = screenPosY(48.0F);
        float columnWidth = screenPosX(340.0F);
        int row = 0, column = 0;
        TinyCard.desiredColumns = (cards.size() <= 36) ? 3 : 4;
        int cardsPerColumn = cards.size() / TinyCard.desiredColumns;
        int remainderCards = cards.size() - cardsPerColumn * TinyCard.desiredColumns;
        int[] columnSizes = new int[TinyCard.desiredColumns];
        Arrays.fill(columnSizes, cardsPerColumn);
        for (int i = 0; i < remainderCards; i++)
            columnSizes[i % TinyCard.desiredColumns] = columnSizes[i % TinyCard.desiredColumns] + 1;
        for (TinyCard card : cards) {
            if (row >= columnSizes[column]) {
                row = 0;
                column++;
            }
            float cardY = originY - row * rowHeight;
            card.hb.move(originX + column * columnWidth + card.hb.width / 2.0F, cardY);
            if (card.col == -1) {
                card.col = column;
                card.row = row;
            }
            row++;
        }
    }

    /**
     * 获取最后生成的DeadPlayer（列表中最后一个元素）
     */
    public static DeadPlayer getLastDeadPlayer() {
        if (deadPlayers != null && !deadPlayers.isEmpty()) {
            return deadPlayers.get(deadPlayers.size() - 1);
        }
        return null;
    }

    /**
     * 获取当前所有被hover的尸体中最后生成的那一个
     */
    public static DeadPlayer getLastHoveredPlayer() {
        // 如果缓存失效，重新计算
        if (hoverCacheDirty || cachedLastHoveredPlayer == null) {
            updateLastHoveredCache();
        }
        return cachedLastHoveredPlayer;
    }

    /**
     * 更新最后hover尸体缓存
     */
    private static void updateLastHoveredCache() {
        cachedLastHoveredPlayer = null;
        if (deadPlayers != null) {
            for (int i = deadPlayers.size() - 1; i >= 0; i--) {
                // 从后往前遍历，找到第一个hover的尸体就是最后生成的
                DeadPlayer player = deadPlayers.get(i);
                if (player.hb.hovered) {
                    cachedLastHoveredPlayer = player;
                    break;
                }
            }
        }
        hoverCacheDirty = false;
    }

    /**
     * 标记hover缓存需要更新
     */
    public static void invalidateHoverCache() {
        hoverCacheDirty = true;
    }

    static {
        RELIC_SPACE = 64.0F * Settings.scale;
       ;
        TEXT =  CardCrawlGame.languagePack.getUIString("RunHistoryScreen").TEXT;
        RARITY_LABEL_STARTER = TEXT[11];
        RARITY_LABEL_COMMON = TEXT[12];
        RARITY_LABEL_UNCOMMON = TEXT[13];
        RARITY_LABEL_RARE = TEXT[14];
        RARITY_LABEL_SPECIAL = TEXT[15];
        RARITY_LABEL_CURSE = TEXT[16];
        RARITY_LABEL_BOSS = TEXT[17];
        RARITY_LABEL_SHOP = TEXT[18];
        RARITY_LABEL_UNKNOWN = TEXT[19];
    }
}