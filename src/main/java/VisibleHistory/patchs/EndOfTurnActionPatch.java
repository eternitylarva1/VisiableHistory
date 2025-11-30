package VisibleHistory.patchs;

import VisibleHistory.playerdeath.DeadPlayer;
import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.unique.RetainCardsAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.TimeWarpPower;

@SpirePatch(
        cls = "com.megacrit.cardcrawl.actions.GameActionManager",
        method = "callEndOfTurnActions"
)
public class EndOfTurnActionPatch {
    public EndOfTurnActionPatch() {
    }

    public static void Postfix(GameActionManager callEndOfTurnActions) {
        BaseMod.logger.info("----------- DeadPlayer Before Attacking --------------");
        if (CardCrawlGame.isInARun()) {
            DeadPlayer.triggerRevivedDrawPhase();
        }
    }

}