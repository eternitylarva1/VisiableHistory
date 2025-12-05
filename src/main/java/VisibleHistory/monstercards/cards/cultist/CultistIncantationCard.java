package VisibleHistory.monstercards.cards.cultist;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.RitualPower;

public class CultistIncantationCard extends AbstractMonsterCard {
    public static final String ID = "CultistIncantation";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/Cultist.png";

    private int ritualAmount;

    public CultistIncantationCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.POWER, CardTarget.SELF);

        // 根据难度调整仪式力量数量
        this.ritualAmount = 3;
        if (AbstractDungeon.ascensionLevel >= 2) {
            this.ritualAmount = 4;
        }
        if (AbstractDungeon.ascensionLevel >= 17) {
            this.ritualAmount = 4; // 高难度时是+1
        }

        this.baseMagicNumber = this.ritualAmount;
        this.magicNumber = this.ritualAmount;
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            addToBot(new ApplyPowerAction(target, source,
                (AbstractPower) new RitualPower(target, this.ritualAmount, false), this.ritualAmount));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(1);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new CultistIncantationCard();
    }
}