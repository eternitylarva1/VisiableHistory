package VisibleHistory.modcore;


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
import com.megacrit.cardcrawl.localization.Keyword;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


import static VisibleHistory.utils.Summary.monsterDefeatStats;
import static com.megacrit.cardcrawl.core.Settings.language;
import static com.megacrit.cardcrawl.core.Settings.seed;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.lastCombatMetricKey;
import static com.megacrit.cardcrawl.helpers.ImageMaster.CAMPFIRE_SMITH_BUTTON;


@SpireInitializer
public class visibleHistory implements PostUpdateSubscriber,PostRenderSubscriber,StartActSubscriber,PostDungeonInitializeSubscriber,PostInitializeSubscriber,EditKeywordsSubscriber,OnStartBattleSubscriber, PostBattleSubscriber , EditStringsSubscriber, EditRelicsSubscriber,OnPlayerTurnStartSubscriber { // 实现接口
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
    public void receiveEditStrings() {
        String lang;
        if (language == Settings.GameLanguage.ZHS) {
            lang = "ZHS"; // 如果语言设置为简体中文，则加载ZHS文件夹的资源
        } else {
            lang = "ENG"; // 如果没有相应语言的版本，默认加载英语
        }
    BaseMod.loadCustomStringsFile(RelicStrings.class, "visibleHistoryResources/localization/" + lang + "/relics.json");
        BaseMod.loadCustomStringsFile(PowerStrings.class, "visibleHistoryResources/localization/" + lang + "/powers.json");

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
    }



    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        DeadPlayer.deadPlayers.clear();
        Map<String, Integer> monsterKills = monsterDefeatStats.get(lastCombatMetricKey);
        if (monsterKills != null) {
            monsterKills.forEach((character, count) -> {
                AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.valueOf(character);
                AbstractPlayer player= CardCrawlGame.characterManager.getCharacter(playerClass);

                for (int i=0;i<count;i++){
                    DeadPlayer.deadPlayers.add(new DeadPlayer(Hpr.getRandomPositionX(),Hpr.getRandomPositionY(), player.corpseImg));
                }
                System.out.println(character + MonsterHelper.getEncounterName(lastCombatMetricKey) +"击败"+ count + "次");
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


    }

    @Override
    public void receivePostBattle(AbstractRoom abstractRoom) {

    }


    @Override
    public void receivePostDungeonInitialize() {
        Summary.load();
    }
    Texture testTexture;;
    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        spriteBatch.draw(testTexture, InputHelper.mX,InputHelper.mY);
        if (!CardCrawlGame.isInARun()){
            return;
        }
        if (AbstractDungeon.player!=null){

        }
    }

    @Override
    public void receivePostUpdate() {
        for (DeadPlayer deadPlayer : DeadPlayer.deadPlayers) {
         deadPlayer.update();
        }
    }
}