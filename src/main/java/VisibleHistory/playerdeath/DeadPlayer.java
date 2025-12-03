package VisibleHistory.playerdeath;

import VisibleHistory.modcore.MyModConfig;
import VisibleHistory.modcore.visibleHistory;
import VisibleHistory.utils.Hpr;
import VisibleHistory.utils.Invoker;
import VisibleHistory.utils.CorpsePositionManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import com.megacrit.cardcrawl.vfx.combat.DamageNumberEffect;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import java.util.Collections;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.TinyChest;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.screens.runHistory.TinyCard;
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
import com.megacrit.cardcrawl.cards.red.Defend_Red;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.cards.red.FlameBarrier;
import com.megacrit.cardcrawl.cards.green.Strike_Green;
import com.megacrit.cardcrawl.cards.green.Defend_Green;
import com.megacrit.cardcrawl.cards.green.Survivor;
import com.megacrit.cardcrawl.cards.green.Neutralize;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.cards.blue.Zap;
import com.megacrit.cardcrawl.cards.blue.Aggregate;

import com.megacrit.cardcrawl.cards.purple.Defend_Watcher;
import com.megacrit.cardcrawl.cards.purple.Eruption;
import com.megacrit.cardcrawl.cards.purple.Worship;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static VisibleHistory.modcore.MyModConfig.toumingdu;
import static VisibleHistory.modcore.MyModConfig.showCards;
import static VisibleHistory.modcore.MyModConfig.showRelics;
import static VisibleHistory.modcore.MyModConfig.ctrlClickTransform;

import static VisibleHistory.modcore.visibleHistory.MyModID;
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
    public boolean isResurrecting = false;  // 是否正在复活过程中（复活前状态）
    List<String> relics;
    public static final String[] TEXT;
    String runDate = "";
    List<TinyCard> cards = new ArrayList<>(); // 恢复使用正确的TinyCard
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

    private AbstractCard displayedCard = null;            // 当前显示在头顶的卡牌
    private float cardDisplayTimer = 0.0f;                // 卡牌显示计时器

    // 复活尸体的独立牌库系统
    private CardGroup CorpseMasterDeck;                   // 复活尸体的主牌库
    private CardGroup CorpseDrawPile;                     // 复活尸体的抽牌堆
    private CardGroup CorpseDiscardPile;                  // 复活尸体的弃牌堆

    // 新增：自动移动相关属性
    private boolean autoMoveToPlayer = true;           // 是否自动移动到玩家身边
    private boolean hasMovedToPlayer = false;           // 是否已经移动到玩家身边（避免重复移动）
    private static final float AUTO_MOVE_SPEED = 50.0f; // 自动移动速度（改为瞬移）
    private static final float COLLISION_BUFFER = 5.0f;   // 碰撞缓冲距离

    // 新增：血条相关属性
    private float maxHealth = 100.0f;                  // 最大血量 (可根据角色调整) - 用于初始化
    private float currentHealth = 100.0f;               // 当前血量 - 用于初始化
    private String corpseId;                           // 尸体唯一标识（用于位置管理器）
    public DeadPlayer(float x, float y, Texture img, RunData runs) throws ParseException {
        // 生成唯一尸体ID
        this.corpseId = generateCorpseId(runs);

        // 使用位置管理器分配位置
        CorpsePositionManager positionManager = CorpsePositionManager.getInstance();
        if (!MyModConfig.stackByCharacter) {
            com.badlogic.gdx.math.Vector2 allocatedPosition = positionManager.allocatePosition(this.corpseId);
            this.x = allocatedPosition.x;
            this.y = allocatedPosition.y;
        }else {
            this.x=x;
            this.y=y;
        }
        this.img=img;
        this.hb=new Hitbox(300,150);
        this.hb.x=this.x;
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
     * 生成唯一的尸体ID（基于角色和时间）
     */
    private String generateCorpseId(RunData runData) {
        String character = runData.character_chosen != null ? runData.character_chosen : "UNKNOWN";
        String time = runData.local_time != null ? runData.local_time : String.valueOf(System.currentTimeMillis());
        // 取更完整的时间戳并添加额外标识符确保唯一性
        String shortTime = time.length() > 15 ? time.substring(0, 15) : time;
        // 使用hashCode提供额外唯一性
        int hashSuffix = System.identityHashCode(runData);
        return character + "_" + shortTime + "_" + Math.abs(hashSuffix);
    }

    /**
     * 获取尸体ID
     */
    public String getCorpseId() {
        return this.corpseId;
    }

    /**
     * 设置尸体位置（用于位置管理器更新）
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.hb.x = this.x + 100;
        this.hb.y = this.y;

        // 更新复活角色的位置
        if (deadPlayerChosen != null) {
            deadPlayerChosen.drawX = this.x + 50;
            deadPlayerChosen.drawY = this.y + 30;
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

        // 更新hitbox位置 - 如果自动移动，让hitbox跟随尸体位置
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

            // 瞬移到玩家附近位置（只执行一次）
            autoMoveToPlayerPosition();
        }

        // 简化的卡牌显示逻辑
        updateCardDisplay();

        // 更新卡牌显示计时器
        if (cardDisplayTimer > 0.0f) {
            cardDisplayTimer -= Gdx.graphics.getDeltaTime();
            // 计时器结束时清理显示的卡牌
            if (cardDisplayTimer <= 0.0f) {
                displayedCard = null;
            }
        }

        // 注意：测试快捷键已移除 - 根据rules.md，不允许随意添加快捷键
        // 如需测试功能，请使用调试模式或在开发时临时添加，发布前移除
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
        // 左键点击检测（用于拖动，忽略卡牌点击因为现在是自动出牌）
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

            // 拖动结束，不做位置重置，让用户自由拖动
            // 复活后的位置管理已完成，用户可以随意拖动
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

        // 同步复活玩家的动画位置（如果已经复活）
        if (isRevived && deadPlayerChosen != null) {
            deadPlayerChosen.movePosition(this.x + Settings.WIDTH / 12, this.y);
        }
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
                AbstractRelic relic = RelicLibrary.getRelic(relicID);
                if (relic != null) {
                    relic.isSeen = true;
                 relic.instantObtain(AbstractDungeon.player, i, true);
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
            isResurrecting = true;  // 开始复活过程
            isRevived = true;
            hasMovedToPlayer = false; // 重置移动状态，复活后可以移动到玩家附近
            // 确保玩家数据已加载
            lazyLoadDataIfNeeded();
            initializeRevivedPlayer();
            // 开始第一个回合，自动出牌
            drawCardAtTurnStart();
            isResurrecting = false; // 复活过程完成
            Hpr.info("尸体已被复活，现在将显示活着的玩家并初始化牌堆和遗物");
        }
    }

    /**
     * 初始化复活玩家的系统（简化版）
     */
    private void initializeRevivedPlayer() {
        // 简化的初始化：只保留基本的显示和功能
        initializeHealthBar();

        // 显示血条 - 使用AbstractCreature内置系统
        if (deadPlayerChosen != null) {
            deadPlayerChosen.showHealthBar();
        }

        Hpr.info("复活玩家初始化完成，血量: " + currentHealth + "/" + maxHealth);
    }

    /**
     * 回合开始时随机选择一张简单的卡牌来显示和执行
     */
    public void drawCardAtTurnStart() {
        if (!isRevived || deadPlayerChosen == null) {
            return;
        }

        // 检查抽牌堆是否为空
        if (CorpseDrawPile == null || CorpseDrawPile.isEmpty()) {
            Hpr.info(getCharacterName() + " 抽牌堆为空，尝试从弃牌堆重新洗牌");

            // 优先从弃牌堆重新洗牌
            if (CorpseDiscardPile != null && !CorpseDiscardPile.isEmpty()) {
                CorpseDrawPile.clear();

                // 记录弃牌堆数量
                int discardCount = CorpseDiscardPile.size();

                // 将弃牌堆的所有牌移到抽牌堆
                ArrayList<AbstractCard> tempList = new ArrayList<>();
                tempList.addAll(CorpseDiscardPile.group);
                Collections.shuffle(tempList, AbstractDungeon.cardRandomRng.random);
                CorpseDrawPile.group = tempList;

                // 清空弃牌堆
                CorpseDiscardPile.clear();

                Hpr.info(getCharacterName() + " 从弃牌堆重新洗牌，将 " + discardCount + " 张牌重新加入抽牌堆");
            } else {
                Hpr.info(getCharacterName() + " 弃牌堆也为空，无法抽牌，所有卡牌都已使用完毕");
                return;
            }
        }

        // 从抽牌堆抽一张牌
        AbstractCard drawnCard = CorpseDrawPile.getTopCard();
        if (drawnCard != null) {
            // 移除顶牌（从抽牌堆中取出）
            CorpseDrawPile.removeTopCard();

            // 复制一张卡牌用于显示
            displayedCard = drawnCard.makeStatEquivalentCopy();

            // 设置卡牌位置为使用者头顶
            float cardDrawX = this.deadPlayerChosen.drawX;
            float cardDrawY = this.deadPlayerChosen.drawY + this.deadPlayerChosen.hb_h + 50.0f * Settings.scale;
            displayedCard.current_x = cardDrawX;
            displayedCard.current_y = cardDrawY;
            displayedCard.target_x = cardDrawX;
            displayedCard.target_y = cardDrawY;
            displayedCard.drawScale = 0.5f;
            displayedCard.targetDrawScale = 0.5f;

            // 设置显示计时器（3秒后消失）
            cardDisplayTimer = 3.0f;

            // 打印调试信息
            Hpr.info(getCharacterName() + " 从抽牌堆抽出了: [" + drawnCard.cardID + "] " + drawnCard.name);
            Hpr.info(getCharacterName() + " 抽牌堆剩余: " + (CorpseDrawPile.size() - 1) + " 张牌");

            // 打印当前抽牌堆内容（调试用）
            Hpr.info(getCharacterName() + " 当前抽牌堆内容:");
            for (int i = CorpseDrawPile.size() - 1; i >= Math.max(0, CorpseDrawPile.size() - 5); i--) {
                AbstractCard card = CorpseDrawPile.group.get(i);
                Hpr.info("  [" + i + "] [" + card.cardID + "] " + card.name);
            }
            if (CorpseDrawPile.size() > 5) {
                Hpr.info("  ... 还有 " + (CorpseDrawPile.size() - 5) + " 张牌");
            }
        } else {
            Hpr.info(getCharacterName() + " 抽牌失败：无法获取卡牌");
        }

        // 立即执行这张卡牌的效果
        if (displayedCard != null) {
            executeRevivedCardAction(displayedCard, drawnCard.makeStatEquivalentCopy());
        }

        Hpr.info(getCharacterName() + " 回合开始随机出牌: " + displayedCard.name);
    }

    /**
     * 获取简单的基础卡牌选项（根据角色类型）
     */
    private ArrayList<AbstractCard> getSimpleCardOptions() {
        ArrayList<AbstractCard> cards = new ArrayList<>();

        if (runData != null && runData.character_chosen != null) {
            try {
                switch (runData.character_chosen) {
                    case "IRONCLAD":
                        cards.add(new com.megacrit.cardcrawl.cards.red.Strike_Red());
                        cards.add(new com.megacrit.cardcrawl.cards.red.Defend_Red());
                        cards.add(new com.megacrit.cardcrawl.cards.red.Bash());
                        cards.add(new com.megacrit.cardcrawl.cards.red.FlameBarrier());
                        break;
                    case "THE_SILENT":
                        cards.add(new com.megacrit.cardcrawl.cards.green.Strike_Green());
                        cards.add(new com.megacrit.cardcrawl.cards.green.Defend_Green());
                        cards.add(new com.megacrit.cardcrawl.cards.green.Neutralize());
                        cards.add(new com.megacrit.cardcrawl.cards.green.Survivor());
                        break;
                    case "DEFECT":
                        cards.add(new com.megacrit.cardcrawl.cards.blue.Strike_Blue());
                        cards.add(new com.megacrit.cardcrawl.cards.blue.Defend_Blue());
                        cards.add(new com.megacrit.cardcrawl.cards.blue.Zap());
                        cards.add(new com.megacrit.cardcrawl.cards.blue.Aggregate());
                        break;
                    case "WATCHER":
                        cards.add(new com.megacrit.cardcrawl.cards.purple.Defend_Watcher());
                        cards.add(new com.megacrit.cardcrawl.cards.purple.Eruption());
                        cards.add(new com.megacrit.cardcrawl.cards.purple.Worship());
                        break;
                    default:
                        // 默认混合牌组
                        cards.add(new com.megacrit.cardcrawl.cards.red.Strike_Red());
                        cards.add(new com.megacrit.cardcrawl.cards.green.Defend_Green());
                        cards.add(new com.megacrit.cardcrawl.cards.blue.Zap());
                        break;
                }
            } catch (Exception e) {
                Hpr.info("创建卡牌选项时出错: " + e.getMessage());
                // 添加一些通用卡牌作为备选
                cards.add(new com.megacrit.cardcrawl.cards.red.Strike_Red());
                cards.add(new com.megacrit.cardcrawl.cards.red.Defend_Red());
            }
        }

        return cards;
    }

    /**
     * 初始化血条系统（根据角色设置不同的血量）
     */
    private void initializeHealthBar() {
        if (runData != null && runData.character_chosen != null && deadPlayerChosen != null) {
            switch (runData.character_chosen) {
                case "IRONCLAD":
                    maxHealth = 75.0f;
                    currentHealth = 75.0f;
                    break;
                case "THE_SILENT":
                    maxHealth = 70.0f;
                    currentHealth = 70.0f;
                    break;
                case "DEFECT":
                    maxHealth = 75.0f;
                    currentHealth = 75.0f;
                    break;
                case "WATCHER":
                    maxHealth = 70.0f;
                    currentHealth = 70.0f;
                    break;
                default:
                    maxHealth = 80.0f;
                    currentHealth = 80.0f;
                    break;
            }

            // 直接设置AbstractCreature的血量
            deadPlayerChosen.maxHealth = Math.round(maxHealth);
            deadPlayerChosen.currentHealth = Math.round(currentHealth);
            deadPlayerChosen.currentBlock = 0;
        }
    }

    /**
     * 自动移动到玩家身边的位置（使用碰撞箱检测，瞬移）
     */
    private void autoMoveToPlayerPosition() {
        if (AbstractDungeon.player == null || hasMovedToPlayer) {
            return;
        }

        // 获取玩家的位置和尺寸
        float playerX = AbstractDungeon.player.drawX;
        float playerY = AbstractDungeon.player.drawY;
        float playerHbWidth = AbstractDungeon.player.hb_w;

        // 获取自己的碰撞箱宽度
        float myHbWidth = this.hb.width;

        // 计算当前复活的玩家数量（包括自己）
        int revivedCount = 0;
        int myIndex = 0;
        for (int i = 0; i < DeadPlayer.deadPlayers.size(); i++) {
            DeadPlayer dp = DeadPlayer.deadPlayers.get(i);
            if (dp.isRevived) {
                if (dp == this) {
                    myIndex = revivedCount;
                }
                revivedCount++;
            }
        }

        // 根据复活玩家的序号分配不同位置
        // 基础位置：玩家左侧
        float targetY = playerY;
        float targetX;

        if (revivedCount == 1) {
            // 只有1个复活玩家，直接使用你原来的逻辑
            targetX = playerX - (playerHbWidth + myHbWidth) / 2.0f;
        } else {
            // 多个复活玩家，水平排列
            float spacing = 50.0f * Settings.scale; // 复活玩家之间的间隔
            float totalWidth = (revivedCount - 1) * spacing;

            // 计算起始位置（最左边的复活玩家）
            float leftMostX = playerX - playerHbWidth - myHbWidth - totalWidth;

            // 根据序号计算自己的位置
            targetX = leftMostX + myIndex * spacing;
        }

        // 瞬移到目标位置
        this.x = targetX;
        this.y = targetY;

        // 同步hitbox位置
        this.hb.x = this.x;
        this.hb.y = this.y;

        // 更新玩家动画位置
        if (deadPlayerChosen != null) {
            deadPlayerChosen.movePosition(this.x + Settings.WIDTH / 12, this.y);
        }

        // 标记已经移动过，不再重复移动
        hasMovedToPlayer = true;

        Hpr.info(getCharacterName() + " 已瞬移到玩家附近位置 (序号: " + myIndex + "/" + revivedCount + ")");
    }

    /**
     * 切换自动移动到玩家身边
     */
    public void toggleAutoMoveToPlayer() {
        autoMoveToPlayer = !autoMoveToPlayer;
        if (autoMoveToPlayer) {
            hasMovedToPlayer = false; // 重新启用时重置状态，允许再次移动
        }
        Hpr.info(getCharacterName() + " 自动移动到玩家身边: " + (autoMoveToPlayer ? "开启" : "关闭"));
    }

    /**
     * 手动触发移动到玩家身边（用于测试）
     */
    public void moveToPlayerSide() {
        hasMovedToPlayer = false; // 重置状态
        autoMoveToPlayerPosition();
    }

    /**
     * 处理受到的伤害（用于血条更新）
     */
    public void takeDamage(float damage) {
        if (!isRevived || deadPlayerChosen == null) {
            return;
        }

        // 使用AbstractCreature的DamageInfo系统进行伤害
        DamageInfo damageInfo = new DamageInfo(null, Math.round(damage), DamageInfo.DamageType.NORMAL);
        deadPlayerChosen.damage(damageInfo);

        Hpr.info(getCharacterName() + " 受到伤害: " + damage + ", 剩余血量: " + deadPlayerChosen.currentHealth + "/" + deadPlayerChosen.maxHealth);
    }

    /**
     * 添加格挡（使用AbstractCreature内置系统）
     */
    public void addBlock(int amount) {
        if (isRevived && deadPlayerChosen != null) {
            deadPlayerChosen.addBlock(amount);
            Hpr.info(getCharacterName() + " 获得格挡: +" + amount + ", 当前格挡: " + deadPlayerChosen.currentBlock);
        }
    }

    /**
     * 简化的卡牌显示更新（移除复杂的hover效果）
     */
    private void updateCardDisplay() {
        if (displayedCard != null && deadPlayerChosen != null) {
            // 简单设置卡牌位置为使用者头顶
            float cardDrawX = this.deadPlayerChosen.drawX;
            float cardDrawY = this.deadPlayerChosen.drawY + this.deadPlayerChosen.hb_h + 50.0f * Settings.scale;

            displayedCard.current_x = cardDrawX;
            displayedCard.current_y = cardDrawY;
            displayedCard.target_x = cardDrawX;
            displayedCard.target_y = cardDrawY;

            // 简单更新卡牌状态
            displayedCard.update();
        }
    }

    /**
     * 在游戏回合开始时触发所有复活角色的随机出牌
     */
    public static void triggerRevivedDrawPhase() {
        for (DeadPlayer corpse : deadPlayers) {
            if (corpse.isRevived) {
                corpse.drawCardAtTurnStart();
                Hpr.info("复活尸体 " + corpse.getCharacterName() + " 随机出手牌");
            }
        }
        Hpr.info("触发复活尸体随机出牌阶段");
    }

    /**
     * 执行复活尸体的卡牌动作（简化版 - 使用游戏原生方法）
     */
    private void executeRevivedCardAction(AbstractCard displayCard, AbstractCard originalCard) {
        if (displayCard == null || originalCard == null || deadPlayerChosen == null) {
            return;
        }

        // 打印出牌前的调试信息
        Hpr.info(getCharacterName() + " 准备使用卡牌: [" + displayCard.cardID + "] " + displayCard.name);
        Hpr.info(getCharacterName() + " 出牌前抽牌堆状态: " + (CorpseDrawPile != null ? CorpseDrawPile.size() + " 张牌" : "抽牌堆为空"));

        try {
            // 安全检查：确保当前房间有怪物（针对攻击卡牌）
            if (displayCard.target == AbstractCard.CardTarget.ENEMY &&
                (AbstractDungeon.getCurrRoom() == null ||
                 AbstractDungeon.getCurrRoom().monsters == null ||
                 AbstractDungeon.getCurrRoom().monsters.monsters.isEmpty())) {
                Hpr.info(getCharacterName() + " 当前房间没有怪物，跳过攻击卡牌执行");
                // 直接清理显示的卡牌
                displayedCard = null;
                return;
            }

            // 选择目标怪物
            AbstractMonster targetMonster = null;
            if (displayCard.target == AbstractCard.CardTarget.ENEMY) {
                targetMonster = AbstractDungeon.getRandomMonster();
                if (targetMonster == null) {
                    Hpr.info(getCharacterName() + " 无法找到有效的目标怪物，跳过卡牌执行");
                    // 直接清理显示的卡牌
                    displayedCard = null;
                    return;
                }
            }

            // ***在使用卡牌前先计算伤害值***
            // 应用所有能力效果到卡牌数值
            displayCard.applyPowers();

            // 计算卡牌对目标的伤害（如果有目标）
            if (displayCard.target == AbstractCard.CardTarget.ENEMY && targetMonster != null) {
                displayCard.calculateCardDamage(targetMonster);
            } else if (displayCard.target == AbstractCard.CardTarget.ALL_ENEMY) {
                // 对于群体攻击，使用第一个怪物计算伤害
                if (AbstractDungeon.getCurrRoom() != null &&
                    AbstractDungeon.getCurrRoom().monsters != null &&
                    !AbstractDungeon.getCurrRoom().monsters.monsters.isEmpty()) {
                    AbstractMonster firstMonster = AbstractDungeon.getCurrRoom().monsters.monsters.get(0);
                    displayCard.calculateCardDamage(firstMonster);
                }
            } else {
                // 对于非攻击卡牌，也需要调用applyPowers来更新数值
                displayCard.calculateDamageDisplay(null);
            }

            // ***直接使用游戏原生的card.use()方法***
            if (displayCard.target == AbstractCard.CardTarget.ENEMY && targetMonster != null) {
                // 单体攻击卡牌 - 使用复活角色作为使用者
                displayCard.use(deadPlayerChosen, targetMonster);
            } else if (displayCard.target == AbstractCard.CardTarget.ALL_ENEMY) {
                // 群体攻击卡牌 - 使用复活角色作为使用者
                displayCard.use(deadPlayerChosen, null);
            } else {
                // 非攻击卡牌或技能卡牌
                displayCard.use(deadPlayerChosen, null);
            }

            // 设置卡牌显示时间（简单显示即可）
            cardDisplayTimer = 1.0f; // 显示1秒

            // 更新显示卡牌的位置为使用者上方
            if (displayedCard != null) {
                float cardDrawX = this.deadPlayerChosen.drawX;
                float cardDrawY = this.deadPlayerChosen.drawY + this.deadPlayerChosen.hb_h + 30.0f * Settings.scale;
                displayedCard.current_x = cardDrawX;
                displayedCard.current_y = cardDrawY;
                displayedCard.target_x = cardDrawX;
                displayedCard.target_y = cardDrawY;
            }

            Hpr.info("复活尸体 " + getCharacterName() + " 执行了卡牌: " + displayCard.name +
                    (targetMonster != null ? " (目标: " + targetMonster.name + ")" : "") +
                    " - 已使用原生card.use()方法");

            // 打印出牌后的调试信息
            Hpr.info(getCharacterName() + " 出牌后抽牌堆状态: " + (CorpseDrawPile != null ? CorpseDrawPile.size() + " 张牌" : "抽牌堆为空"));

            // 将用过的卡牌加入弃牌堆
            if (CorpseDiscardPile != null && originalCard != null) {
                CorpseDiscardPile.addToBottom(originalCard);
                Hpr.info(getCharacterName() + " 已使用的卡牌 [" + originalCard.cardID + "] 进入弃牌堆，弃牌堆现在有 " + CorpseDiscardPile.size() + " 张牌");
            }
        } catch (Exception e) {
            Hpr.info("执行复活尸体卡牌时出错: " + e.getMessage());
            // 降级方案：清理显示的卡牌，不影响游戏继续
            displayedCard = null;
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
            AbstractRelic r = RelicLibrary.getRelic(rs);
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

            // 新增：在复活状态下仍然显示历史记录（右键时）
            if (shouldShowHistory && dataLoaded) {
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

        // 渲染头顶显示的抽牌堆（根据复活状态选择渲染方式）
        if (isResurrecting && !cards.isEmpty()) {
            // 复活前：渲染TinyCard（小卡牌预览），固定小尺寸
            renderTinyCards(sb);
        } else if (isRevived && deadPlayerChosen != null && CorpseDrawPile != null && !CorpseDrawPile.isEmpty()) {
            // 复活后：渲染AbstractCard，支持hover缩放
            renderDrawPile(sb);
        } else if (displayedCard != null) {
            // 兼容旧逻辑：如果没有抽牌堆但有单张显示卡牌
            displayedCard.render(sb);
        }

        // 使用AbstractCreature内置的血条系统（如果是复活状态）
        if (isRevived && deadPlayerChosen != null) {
            Invoker.invoke(deadPlayerChosen,"updateHealth");

            deadPlayerChosen.renderHealth(sb);  // 渲染血条
        }
    }
    private static final float HIDE_X = -800.0F * Settings.xScale;
    private float SHOW_X = 300.0F * Settings.xScale;
    private float screenX = SHOW_X;
    private float targetX = SHOW_X;
    
    private static final AbstractCard.CardRarity[] orderedRarity= new AbstractCard.CardRarity[]{AbstractCard.CardRarity.SPECIAL, AbstractCard.CardRarity.RARE, AbstractCard.CardRarity.UNCOMMON, AbstractCard.CardRarity.COMMON, AbstractCard.CardRarity.BASIC, AbstractCard.CardRarity.CURSE};

    /**
     * 手动检测卡牌是否被hover
     */
    private boolean isCardHovered(AbstractCard card, float cardX, float cardY) {
        if (card == null) {
            return false;
        }

        // 手动检测鼠标是否在卡牌范围内
        float mouseX = InputHelper.mX;
        float mouseY = InputHelper.mY;

        // 卡牌的渲染边界（使用卡牌的hitbox尺寸）
        float cardWidth = card.hb.width;
        float cardHeight = card.hb.height;
        float cardLeft = cardX - cardWidth / 2.0f;
        float cardRight = cardX + cardWidth / 2.0f;
        float cardTop = cardY + cardHeight / 2.0f;
        float cardBottom = cardY - cardHeight / 2.0f;

        // 检查鼠标是否在卡牌区域内
        return mouseX >= cardLeft && mouseX <= cardRight &&
               mouseY >= cardBottom && mouseY <= cardTop;
    }

    /**
     * 渲染复活前的TinyCard（小卡牌预览）
     */
    private void renderTinyCards(SpriteBatch sb) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        // 简化渲染：直接渲染前几张TinyCard，让它自己处理位置
        int maxDisplay = Math.min(5, cards.size());
        for (int i = 0; i < maxDisplay; i++) {
            TinyCard tinyCard = cards.get(i);
            if (tinyCard != null) {
                // TinyCard自动处理渲染位置和大小
                tinyCard.render(sb);
            }
        }
    }

    /**
     * 渲染复活尸体的抽牌堆
     */
    private void renderDrawPile(SpriteBatch sb) {
        if (CorpseDrawPile == null || CorpseDrawPile.isEmpty() || deadPlayerChosen == null) {
            return;
        }

        // 计算抽牌堆位置的基准点（复活玩家头顶）
        float baseX = this.deadPlayerChosen.drawX;
        float baseY = this.deadPlayerChosen.drawY + this.deadPlayerChosen.hb_h + 50.0f * Settings.scale;

        // 最多显示5张牌（顶部5张）
        int maxDisplay = Math.min(5, CorpseDrawPile.size());

        // 卡牌错开的间距
        float cardOffset = 15.0f * Settings.scale;
        float scaleOffset = 0.05f; // 每张牌的缩放递减

        // 从底牌开始渲染，这样顶牌会最后渲染在最前面
        for (int i = CorpseDrawPile.size() - maxDisplay, displayCount = maxDisplay - 1; i < CorpseDrawPile.size(); i++, displayCount--) {
            AbstractCard card = CorpseDrawPile.group.get(i);

            if (card != null) {
                // 计算每张牌的位置（错开显示）
                float cardX = baseX + displayCount * cardOffset;
                float cardY = baseY + displayCount * cardOffset * 0.5f;

                // 更新卡牌位置
                card.current_x = cardX;
                card.current_y = cardY;
                card.target_x = cardX;
                card.target_y = cardY;

                // 手动检测当前卡牌是否被hover
                boolean isHovered = isCardHovered(card, cardX, cardY);

                // 为非hover状态的卡牌设置默认缩放
                if (!isHovered) {
                    float cardScale = 0.4f - displayCount * scaleOffset; // 从0.4缩小到0.2
                    card.drawScale = cardScale;
                    card.targetDrawScale = cardScale;
                } else {
                    // hover状态的卡牌设置更大的缩放
                    float hoverScale = 0.6f - displayCount * scaleOffset * 0.5f; // hover时更大一些
                    card.drawScale = hoverScale;
                    card.targetDrawScale = hoverScale;
                }

                // 更新卡牌状态（不使用游戏内置的hover系统）
                card.update();

                // 渲染卡牌
                card.render(sb);
            }
        }
    }

    private void renderDeck(SpriteBatch sb, float x, float y) {
        int cardCount = 0;
        for (TinyCard tinyCard : cards) {
            tinyCard.render(sb);
            cardCount += tinyCard.count;
        }
        this.layoutTinyCards((ArrayList<TinyCard>) cards, this.screenX + this.screenPosX(90.0F), y-100);
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

        // 为复活尸体初始化牌库
        CorpseMasterDeck = new CardGroup(CardGroup.CardGroupType.MASTER_DECK);
        CorpseDrawPile = new CardGroup(CardGroup.CardGroupType.DRAW_PILE);
        CorpseDiscardPile = new CardGroup(CardGroup.CardGroupType.DISCARD_PILE);

        // 将卡牌添加到复活尸体的主牌库
        for(AbstractCard card : sortedMasterDeck.group) {
            Integer count = cardCounts.get(card);
            for(int i = 0; i < count; ++i) {
                CorpseMasterDeck.addToBottom(card.makeStatEquivalentCopy());
            }
        }

        // 根据牌库大小决定初始化抽牌堆时的手牌数量
        int handSize = Math.min(5, CorpseMasterDeck.size());
        Hpr.info("复活尸体 [" + getCharacterName() + "] 牌库初始化完成，主牌库包含 " + CorpseMasterDeck.size() + " 张卡牌，初始手牌数量: " + handSize);

        // 洗牌并初始化抽牌堆
        CorpseDrawPile.clear();
        ArrayList<AbstractCard> tempList = new ArrayList<>();
        tempList.addAll(CorpseMasterDeck.group);
        Collections.shuffle(tempList, AbstractDungeon.cardRandomRng.random);
        CorpseDrawPile.group = tempList;

        // 确保抽牌堆不会超过主牌库大小
        handSize = Math.min(handSize, CorpseDrawPile.size());

        Hpr.info("复活尸体 [" + getCharacterName() + "] 抽牌堆初始化完成，包含 " + CorpseDrawPile.size() + " 张卡牌");

        // 同时更新TinyCard显示系统（用于鼠标悬停显示卡组信息）
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
        for (TinyCard tinyCard : cards) {
            if (row >= columnSizes[column]) {
                row = 0;
                column++;
            }
            float cardY = originY - row * rowHeight;
            tinyCard.hb.move(originX + column * columnWidth + tinyCard.hb.width / 2.0F, cardY);
            if (tinyCard.col == -1) {
                tinyCard.col = column;
                tinyCard.row = row;
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