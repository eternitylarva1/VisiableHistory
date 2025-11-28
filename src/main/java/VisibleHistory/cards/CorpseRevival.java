package VisibleHistory.cards;

import VisibleHistory.playerdeath.DeadPlayer;
import VisibleHistory.utils.Hpr;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.green.GrandFinale;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.vfx.combat.GrandFinalEffect;

import java.util.ArrayList;
import java.util.List;

public class CorpseRevival extends CustomCard {
    public static final String ID = "VisibleHistory:CorpseRevival";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    public static final String NAME = cardStrings.NAME;
    public static final String DESCRIPTION = cardStrings.DESCRIPTION;
    public static final String IMG_PATH = "visibleHistoryResources/img/cards/CorpseRevival.png";

    private static final AbstractCard.CardType TYPE = AbstractCard.CardType.SKILL;
    private static final AbstractCard.CardColor COLOR = AbstractCard.CardColor.COLORLESS;
    private static final AbstractCard.CardRarity RARITY = AbstractCard.CardRarity.SPECIAL;
    private static final AbstractCard.CardTarget TARGET = AbstractCard.CardTarget.NONE;

    private static final int COST = 1;

    public CorpseRevival() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, COLOR, RARITY, TARGET);

        this.exhaust = true; // 使用后消耗
        this.isEthereal = true; // 虚无卡，回合结束时自动消耗
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 获取所有可复活的尸体
        List<DeadPlayer> availableCorpses = new ArrayList<>();
        for (DeadPlayer corpse : DeadPlayer.deadPlayers) {
            if (!corpse.isRevived) {  // 只选择未复活的尸体
                availableCorpses.add(corpse);
            }
        }

        if (!availableCorpses.isEmpty()) {
            // 随机选择一具尸体
            int randomIndex =AbstractDungeon.cardRandomRng.random(availableCorpses.size() - 1);
            DeadPlayer selectedCorpse = availableCorpses.get(randomIndex);

            // 复活这具尸体（让尸体开始渲染活着的玩家）
            selectedCorpse.reviveCorpse();

            // 显示复活效果
            AbstractDungeon.effectList.add(new SpeechBubble(p.dialogX, p.dialogY, 2.0F,
                "复活了 " + selectedCorpse.getCharacterName() + "！", true));

            Hpr.info("成功复活了尸体: " + selectedCorpse.getCharacterName());
            this.addToBot(new VFXAction(new GrandFinalEffect(), 0.7F));
        } else {
            // 没有可复活的尸体
            AbstractDungeon.effectList.add(new SpeechBubble(p.dialogX, p.dialogY, 2.0F,
                "没有可复活的尸体...", true));
            Hpr.info("没有可复活的尸体");
        }
    }

    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeBaseCost(0); // 升级后消耗变为0
            rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
}