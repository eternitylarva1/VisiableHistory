package VisibleHistory.monstercards.cards.louse;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

public class LouseShellCard extends AbstractMonsterCard {
    public static final String ID = "LouseShell";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/FuzzyLouseNormal.png";

    public LouseShellCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.POWER, CardTarget.SELF);

        // 根据难度调整力量加成
        this.baseMagicNumber = 3;
        this.magicNumber = this.baseMagicNumber;
        if (AbstractDungeon.ascensionLevel >= 17) {
            this.baseMagicNumber = 4;
            this.magicNumber = 4;
        }
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            addToBot(new ApplyPowerAction(target, source,
                (AbstractPower) new StrengthPower(target, this.magicNumber), this.magicNumber));
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
        return new LouseShellCard();
    }
}