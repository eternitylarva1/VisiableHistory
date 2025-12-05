package VisibleHistory.monstercards.cards.jawworm;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

public class JawWormBellowCard extends AbstractMonsterCard {
    public static final String ID = "JawWormBellow";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/JawWorm.png";

    private int strengthAmount;
    private int blockAmount;

    public JawWormBellowCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.POWER, CardTarget.SELF);

        // 根据难度调整数值
        this.strengthAmount = 3;
        this.blockAmount = 6;

        if (AbstractDungeon.ascensionLevel >= 17) {
            this.strengthAmount = 5;
            this.blockAmount = 9;
        } else if (AbstractDungeon.ascensionLevel >= 2) {
            this.strengthAmount = 4;
            this.blockAmount = 6;
        }

        this.baseMagicNumber = this.strengthAmount;
        this.magicNumber = this.strengthAmount;
        this.baseBlock = this.blockAmount;
        this.block = this.blockAmount;
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            // 给自己增加力量
            addToBot(new ApplyPowerAction(target, source,
                (AbstractPower) new StrengthPower(target, this.strengthAmount), this.strengthAmount));

            // 给自己增加格挡
            addToBot(new GainBlockAction(target, source, this.blockAmount));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(1);
            this.upgradeBlock(2);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new JawWormBellowCard();
    }
}