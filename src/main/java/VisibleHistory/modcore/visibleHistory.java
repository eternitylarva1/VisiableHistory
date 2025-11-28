package VisibleHistory.modcore;


import VisibleHistory.cards.CorpseRevival;
import VisibleHistory.playerdeath.DeadPlayer;
import VisibleHistory.relics.Huixiang;
import VisibleHistory.utils.Hpr;
import VisibleHistory.utils.Summary;
import basemod.*;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.CharacterManager;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.Keyword;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.stats.RunData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


import static VisibleHistory.utils.Summary.monsterDefeatStats;
import static com.megacrit.cardcrawl.core.Settings.language;
import static com.megacrit.cardcrawl.core.Settings.seed;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.lastCombatMetricKey;
import static com.megacrit.cardcrawl.helpers.ImageMaster.CAMPFIRE_SMITH_BUTTON;


@SpireInitializer
public class visibleHistory implements PostUpdateSubscriber,PostRenderSubscriber,StartActSubscriber,PostDungeonInitializeSubscriber,PostInitializeSubscriber,EditKeywordsSubscriber,OnStartBattleSubscriber, PostBattleSubscriber , EditStringsSubscriber, EditRelicsSubscriber,EditCardsSubscriber,OnPlayerTurnStartSubscriber { // 实现接口
    public visibleHistory() {
        BaseMod.subscribe(this); // 告诉basemod你要订阅事件
    }
    public static int turn=0;
    public static final String MyModID = "visibleHistory";
    ModPanel settingsPanel = new ModPanel();
    public static SpireConfig config;
    public static boolean hasselected=false;
    public static boolean isfakefire;
    public static HashMap<Integer,Boolean> firemap=new HashMap<>();

    public static void initialize() throws IOException {

        new visibleHistory();


    }

    // 当basemod开始注册mod卡牌时，便会调用这个函数

    @Override
    public void receiveStartAct() {

    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new Huixiang(), RelicType.SHARED);
    }

    @Override
    public void receiveEditCards() {
        BaseMod.addCard(new CorpseRevival());
    }

    @Override
    public void receiveEditStrings() {
        String lang;
        if (language == Settings.GameLanguage.ZHS) {
            lang = "ZHS"; // 如果语言设置为简体中文，则加载ZHS文件夹的资源
        } else {
            lang = "ENG"; // 如果没有相应语言的版本，默认加载英语
        }
    BaseMod.loadCustomStringsFile(RelicStrings.class, "visibleHistoryResources/localization/" + lang + "/relics.json");
        BaseMod.loadCustomStringsFile(PowerStrings.class, "visibleHistoryResources/localization/" + lang + "/powers.json");
        BaseMod.loadCustomStringsFile(CardStrings.class, "visibleHistoryResources/localization/" + lang + "/cards.json");
        BaseMod.loadCustomStringsFile(UIStrings.class, "visibleHistoryResources/localization/" + lang + "/ui.json");

    }
    public static float getYPos(float y) {
        return Settings.HEIGHT/(2160/y);
    }
    public static float getXPos(float x) {
        return Settings.WIDTH/(3840/x);
    }
    @Override
    public void receivePostInitialize() {
        Summary.load();
        testTexture=new Texture("visibleHistoryResources/images/relics/img.png");

        BaseMod.registerModBadge(ImageMaster.loadImage("visibleHistoryResources/images/relics/img.png"),MyModID,"Dieyou", "在战斗开始时，每在当前怪物组合手上死过一次，在场上添加一具尸体，可以鼠标放上去查看具体的历史记录", new MyModConfig());

    }



    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        generateDeadPlayer();
    }
    public void generateDeadPlayer(){
        DeadPlayer.deadPlayers.clear();
        // 重置hover缓存
        DeadPlayer.invalidateHoverCache();

// 1. 获取当前怪物对应的「角色-失败记录」映射（替换原有的次数映射）
        Map<String, Summary.FailureRecord> monsterFailureRecords = Summary.getCharacterFailureRecords(lastCombatMetricKey);

        if (monsterFailureRecords != null && !monsterFailureRecords.isEmpty()) {
            monsterFailureRecords.forEach((character, failureRecord) -> {
                try {

                    // 2. 解析角色类、获取角色尸体图片（保留原有逻辑）
                    AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.valueOf(character);
                    AbstractPlayer player = CardCrawlGame.characterManager.getCharacter(playerClass);
                    Texture corpseImg = player.corpseImg;

                    // 3. 遍历该角色被当前怪物击败的所有 RunData（一个 RunData 对应一个 DeadPlayer）
                    for (RunData runData : failureRecord.runList) {
                        // 4. 传入随机位置、尸体图片、对应 RunData 创建 DeadPlayer
                        DeadPlayer deadPlayer = new DeadPlayer(
                                Hpr.getRandomPositionX(),
                                Hpr.getRandomPositionY(),
                                corpseImg,
                                runData  // 新增：传入当前失败对局的 RunData
                        );
                        if(DeadPlayer.deadPlayers.size()<=MyModConfig.DeadPlayerMax) {
                            DeadPlayer.deadPlayers.add(deadPlayer);
                        }
                    }

                    // 5. 保留原有打印逻辑（次数 = RunData 列表大小，结果和之前一致）
                    String monsterName = MonsterHelper.getEncounterName(lastCombatMetricKey);
                    int defeatCount = failureRecord.count;
                    System.out.println(character + " 被 " + monsterName + " 击败 " + defeatCount + " 次");
                } catch (IllegalArgumentException e) {
                    // 异常处理：避免无效角色类导致崩溃（保留容错性）
                    Hpr.info("无效角色类：" + character + "，跳过该角色的尸体生成");
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
   public static void initializeHashmap(){
        if (AbstractDungeon.player==null|| !CardCrawlGame.isInARun()){
            return;
        }
       com.megacrit.cardcrawl.random.Random rng=new com.megacrit.cardcrawl.random.Random(seed);

        for(int i=0;i<1000;i++){
            boolean istrue;
            istrue=rng.randomBoolean(0.7f);
            firemap.put(i,istrue);

        }
   }
    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();
        String lang = "ENG";
        if (language == Settings.GameLanguage.ZHS) {
            lang = "ZHS";
        }

        String json = Gdx.files.internal("visibleHistoryResources/localization/" + lang + "/keywords.json")
                .readString(String.valueOf(StandardCharsets.UTF_8));
        Keyword[] keywords = gson.fromJson(json, Keyword[].class);

    }

    @Override
    public void receiveOnPlayerTurnStart() {
        // 触发所有复活尸体的抽牌阶段
        if (CardCrawlGame.isInARun()) {
            DeadPlayer.triggerRevivedDrawPhase();
        }
    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        DeadPlayer.deadPlayers.clear();
    }


    @Override
    public void receivePostDungeonInitialize() {
        Summary.load();
    }
    public static Texture testTexture;;
    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {


    }

    @Override
    public void receivePostUpdate() {
        if (CardCrawlGame.isInARun()) {
            // 处理尸体移除（在更新前执行，避免并发修改异常）
            DeadPlayer.processRemovals();

            // 标记hover缓存需要更新（每帧更新一次以确保鼠标移动时能正确响应）
            DeadPlayer.invalidateHoverCache();
            // 处理全局鼠标输入（拖动）
            DeadPlayer.handleGlobalMouseInput();

            for (DeadPlayer deadPlayer : DeadPlayer.deadPlayers) {
                deadPlayer.update();
            }
        }
    }
}