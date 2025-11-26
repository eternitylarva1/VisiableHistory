package VisibleHistory.playerdeath;

import VisibleHistory.modcore.visibleHistory;
import VisibleHistory.utils.Hpr;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.TinyChest;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.TinyCard;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javafx.scene.shape.Circle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static VisibleHistory.modcore.MyModConfig.toumingdu;
import static VisibleHistory.modcore.MyModConfig.xianshidonghua;
import static VisibleHistory.modcore.visibleHistory.testTexture;
import static com.megacrit.cardcrawl.helpers.ImageMaster.CAMPFIRE_SMITH_BUTTON;

public class DeadPlayer {
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
    public DeadPlayer(float x, float y, Texture img, RunData runs) throws ParseException {
        this.x=x;
        this.y=y;
        this.img=img;
        this.hb=new Hitbox(300,150);
        this.hb.x=this.x+100;
        this.hb.y=this.y;
        relics=runs.relics;
        SimpleDateFormat dateFormat;
        if (Settings.language == Settings.GameLanguage.JPN) {
            // 日语使用游戏语言包中的格式（TEXT[34]来自UI语言包“RunHistoryScreen”）
            dateFormat = new SimpleDateFormat(TEXT[34], Locale.JAPAN);
        } else {
            // 其他语言使用默认格式（TEXT[34]例如“yyyy-MM-dd HH:mm”）
            dateFormat = new SimpleDateFormat(TEXT[34]);
        }
        Date date = Metrics.timestampFormatter.parse(runs.local_time);
        runDate = dateFormat.format(date);
        reloadCards( runs);

        // 初始化deadPlayerChosen对象用于动画显示
        try {
            AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.valueOf(runs.character_chosen);
            deadPlayerChosen = CardCrawlGame.characterManager.getCharacter(playerClass).newInstance();
            deadPlayerChosen.movePosition(this.x + Settings.WIDTH / 12, this.y);
        } catch (Exception e) {
            Hpr.info("初始化deadPlayerChosen失败: " + e.getMessage());
        }
    }
    public void update() {
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

        // 处理动画显示
        if (showHistory && xianshidonghua) {
            this.deadPlayerChosen.update();
            this.x = this.deadPlayerChosen.drawX;
            this.y = this.deadPlayerChosen.drawY;
        }
    }

    /**
     * 检查鼠标输入（左键拖动、右键显示历史）
     */
    private void checkMouseInput() {
        // 左键拖动检测
        if (InputHelper.justClickedLeft && this.hb.hovered && !isDragging) {
            isDragging = true;
            isDraggingRequested = true;
            // 不重置InputHelper.justClickedLeft，让全局处理
        }

        // 右键显示历史记录检测
        if (InputHelper.justClickedRight && this.hb.hovered && !isDragging) {
            showHistory = !showHistory; // 切换历史记录显示状态
            InputHelper.justClickedRight = false; // 防止与其他右键冲突
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

        for(String rs : this.relics) {
            if (col == 15) {
                col = 0;
                ++row;
            }
            AbstractRelic r= RelicLibrary.getRelic(rs).makeCopy();
            r.isSeen=true;
            r.currentX = relicStartX + RELIC_SPACE * (float)col;
            r.currentY = relicStartY - RELIC_SPACE * (float)row;
            r.hb.move(r.currentX, r.currentY);
            r.render(sb, false, Settings.TWO_THIRDS_TRANSPARENT_BLACK_COLOR);
            ++col;
        }

        return relicStartY - RELIC_SPACE * (float)row;
    }
    public void render(SpriteBatch sb) {
        this.hb.render(sb);

        // 鼠标悬停效果：尸体变亮 + 鼠标变成放大镜
        boolean isHovered = this.hb.hovered;

        if (isHovered) {
            CardCrawlGame.cursor.changeType(GameCursor.CursorType.INSPECT);
        }

        // 判断是否显示详细信息（右键点击后）
        boolean shouldShowDetails = showHistory;

        // 渲染尸体
        if (!isHovered && !shouldShowDetails) {
            // 默认状态：半透明
            sb.setColor(1.0f, 1.0f, 1.0f, toumingdu);
            sb.draw(this.img, this.x, this.y);
        } else if (isHovered && !shouldShowDetails) {
            // 鼠标悬停状态：变亮但不显示详细信息
            sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            sb.draw(this.img, this.x, this.y);
        } else {
            // 显示详细信息状态（右键点击后）
            if (xianshidonghua) {
                sb.setColor(1.0f, 1.0f, 1.0f, toumingdu);
                renderPlayer(sb);
                sb.setColor(Color.WHITE);
            } else {
                sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                sb.draw(this.img, this.x, this.y);
            }

            float rendery = this.y;
            if (this.y < Settings.WIDTH / 3) {
                rendery += Settings.WIDTH / 3;
            }
            if (rendery >= Settings.WIDTH * 5 / 6) {
                rendery = Settings.WIDTH * 5 / 6;
            }
            this.renderRelics(sb, this.x, rendery);
            this.renderDeck(sb, this.x, rendery);
        }

        sb.setColor(1.0f, 1.0f, 1.0f, 1.0f);

//TODO 1.优化交互（右键拖动交互） 2.增加设置界面+显示上限√（1.显示尸体数量 2.透明度 3.位置）(---)

        // FontHelper.renderFont(sb,FontHelper.largeCardFont,"测试位置",this.x,this.y, Color.WHITE);
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
        DeadPlayer lastHovered = null;
        if (deadPlayers != null) {
            for (DeadPlayer player : deadPlayers) {
                // 检查这个尸体是否被鼠标接触
                if (player.hb.hovered) {
                    // 如果还没有找到hover的尸体，或者这个尸体比之前找到的更后生成
                    if (lastHovered == null ||
                        deadPlayers.indexOf(player) > deadPlayers.indexOf(lastHovered)) {
                        lastHovered = player;
                    }
                }
            }
        }
        return lastHovered;
    }

    public static ArrayList<DeadPlayer> deadPlayers=new ArrayList<>();
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
