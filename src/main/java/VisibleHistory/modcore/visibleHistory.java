package VisibleHistory.modcore;


import VisibleHistory.cards.CorpseRevival;
import VisibleHistory.playerdeath.DeadPlayer;
import VisibleHistory.relics.Huixiang;
import VisibleHistory.utils.Hpr;
import VisibleHistory.utils.Summary;
import VisibleHistory.utils.CorpsePositionManager;
import basemod.*;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    // 新增：尸体堆叠管理系统
    private static HashMap<String, Integer> characterStackCount = new HashMap<>(); // 角色 -> 堆叠数量
    private static HashMap<String, Float> characterAssignedX = new HashMap<>(); // 角色 -> 分配的X坐标

    // 防重复调用标记
    private static boolean battleStartProcessed = false;

    // 预分配的固定X坐标位置（5个角色位置）
    private static final float[] PRESET_X_POSITIONS = {
        200.0f,   // 位置1
        400.0f,   // 位置2
        600.0f,   // 位置3
        800.0f,   // 位置4
        1000.0f   // 位置5
    };

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
        /*
        // 防止重复调用
        if (battleStartProcessed) {
            Hpr.info("战斗开始事件已被处理，跳过重复调用");
            return;
        }
        battleStartProcessed = true;
*/
        // 根据配置选择排序模式
        updateSortModeFromConfig();

        // 添加调试信息
        Hpr.info("战斗开始，配置: 排序模式=" + (MyModConfig.sortByTime ? "按时间" : "按角色分组") +
                  ", 堆叠模式=" + (MyModConfig.stackByCharacter ? "开启" : "关闭"));

        if (Summary.sortMode == Summary.SortMode.BY_TIME) {
            generateDeadPlayerByTime();
        } else {
            generateDeadPlayer(); // 原有的按角色分组逻辑
        }
    }

    // 新增：从配置更新排序模式
    private void updateSortModeFromConfig() {
        // 从布尔配置确定排序模式
        Summary.SortMode newMode = MyModConfig.sortByTime
            ? Summary.SortMode.BY_TIME
            : Summary.SortMode.BY_CHARACTER;

        Summary.setSortMode(newMode);

        // 打印配置信息用于调试
        Hpr.info("配置状态 - 排序模式: " + (MyModConfig.sortByTime ? "按时间" : "按角色分组") +
                  ", 堆叠模式: " + (MyModConfig.stackByCharacter ? "开启" : "关闭"));
    }

    // 新增：尸体堆叠位置管理
    private void initCharacterStacking() {
        characterStackCount.clear(); // 重置堆叠计数
        characterAssignedX.clear(); // 重置分配的X坐标
        Hpr.info("初始化尸体堆叠系统，堆叠模式: " + (MyModConfig.stackByCharacter ? "开启" : "关闭"));
    }

    private float[] getStackedPosition(String characterName) {
        if (!MyModConfig.stackByCharacter) {
            // 不使用堆叠，返回随机位置
            return new float[]{Hpr.getRandomPositionX(), Hpr.getRandomPositionY()};
        }

        // 获取该角色已分配的X坐标，如果没有则分配一个
        Float assignedX = characterAssignedX.get(characterName);
        if (assignedX == null) {

            int positionIndex = Math.abs(characterName.hashCode() % 20);
            assignedX = (float) (Settings.WIDTH/20*positionIndex);
            characterAssignedX.put(characterName, assignedX);
            Hpr.info("为角色 " + characterName + " 分配X坐标: " + assignedX);
        }

        // 获取当前堆叠数量
        int stackCount = characterStackCount.getOrDefault(characterName, 0);

        // 计算Y位置（堆叠间距 80 像素，不使用Settings.scale避免0值）
        float stackOffset = 80.0f;
        float baseY = 300.0f;
        float currentY = baseY + (stackCount * stackOffset);

        // 增加该角色的堆叠计数
        characterStackCount.put(characterName, stackCount + 1);

        Hpr.info("角色 " + characterName + " 堆叠位置: X=" + assignedX + ", Y=" + currentY + " (堆叠层数: " + (stackCount + 1) + ")");

        return new float[]{assignedX, currentY};
    }

    public void generateDeadPlayerByTime() {
        DeadPlayer.deadPlayers.clear();
        // 重置hover缓存
        DeadPlayer.invalidateHoverCache();

        // 初始化堆叠系统
        initCharacterStacking();

        // 获取当前怪物对应的「角色-失败记录」映射（和原逻辑一样）
        Map<String, Summary.FailureRecord> monsterFailureRecords = Summary.getCharacterFailureRecords(lastCombatMetricKey);

        if (monsterFailureRecords == null || monsterFailureRecords.isEmpty()) {
            Hpr.info("当前怪物 " + lastCombatMetricKey + " 没有击败任何角色");
            return;
        }

        // 收集当前怪物的所有失败RunData并按时间排序
        List<RunData> currentMonsterRuns = new ArrayList<>();
        for (Summary.FailureRecord record : monsterFailureRecords.values()) {
            currentMonsterRuns.addAll(record.runList);
        }

        // 按时间戳降序排序（最新的在前）
        currentMonsterRuns = currentMonsterRuns.stream()
                .sorted((run1, run2) -> {
                    try {
                        long time1 = Long.parseLong(run1.timestamp);
                        long time2 = Long.parseLong(run2.timestamp);
                        return Long.compare(time2, time1); // 降序：最新的在前
                    } catch (NumberFormatException e) {
                        // 时间戳解析失败时，按文件名字母顺序
                        return run2.timestamp.compareTo(run1.timestamp);
                    }
                })
                .collect(Collectors.toList());

        Hpr.info("当前怪物 " + lastCombatMetricKey + " 击败了 " + currentMonsterRuns.size() + " 个角色（按时间排序）");

        // 按时间顺序生成尸体，最多生成配置的尸体数量
        int maxCorpse = MyModConfig.DeadPlayerMax;
        int created = 0;

        for (RunData runData : currentMonsterRuns) {
            if (created >= maxCorpse) break;
            if (DeadPlayer.deadPlayers.size() >= maxCorpse) break;

            try {
                // 解析角色类、获取角色尸体图片
                AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.valueOf(runData.character_chosen);
                AbstractPlayer player = CardCrawlGame.characterManager.getCharacter(playerClass);
                Texture corpseImg = player.corpseImg;

                // 使用堆叠系统获取位置
                float[] position = getStackedPosition(runData.character_chosen);

                // 创建 DeadPlayer（使用堆叠位置）
                DeadPlayer deadPlayer = new DeadPlayer(
                        position[0],  // X坐标（可能堆叠）
                        position[1],  // Y坐标（递增）
                        corpseImg,
                        runData
                );

                DeadPlayer.deadPlayers.add(deadPlayer);
                created++;

            } catch (IllegalArgumentException e) {
                Hpr.info("无效角色类：" + runData.character_chosen + "，跳过该角色的尸体生成");
            } catch (Exception e) {
                Hpr.info("创建尸体时出错：" + e.getMessage() + "，跳过该角色");
            }
        }

        Hpr.info("当前怪物按时间排序生成了 " + created + " 个尸体，当前尸体总数：" + DeadPlayer.deadPlayers.size());
    }

    public void generateDeadPlayer(){
        DeadPlayer.deadPlayers.clear();
        // 重置hover缓存
        DeadPlayer.invalidateHoverCache();

        // 初始化堆叠系统
        initCharacterStacking();

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
                        // 使用堆叠系统获取位置
                        float[] position = getStackedPosition(character);

                        // 4. 传入堆叠位置、尸体图片、对应 RunData 创建 DeadPlayer
                        DeadPlayer deadPlayer = new DeadPlayer(
                                position[0],  // X坐标（可能堆叠）
                                position[1],  // Y坐标（递增）
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
                } catch (Exception e) {
                    Hpr.info("处理角色时出错：" + e.getMessage() + "，跳过该角色");
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

    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {
        DeadPlayer.deadPlayers.clear();
        // 战斗结束，重置位置管理器为下次战斗做准备
        CorpsePositionManager.getInstance().reset();
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
        // 检测T键切换尸体显示
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            MyModConfig.showCorpses = !MyModConfig.showCorpses;
            Hpr.info("尸体显示已" + (MyModConfig.showCorpses ? "开启" : "关闭"));
        }

        if (CardCrawlGame.isInARun()) {
            // 只有在显示尸体时才更新和渲染
            if (MyModConfig.showCorpses) {
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
}