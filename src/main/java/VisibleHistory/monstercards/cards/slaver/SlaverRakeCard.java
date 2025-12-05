package VisibleHistory.monstercards.cards.slaver;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.WeakPower;

public class SlaverRakeCard extends AbstractMonsterCard {
    public static final String ID = "SlaverRake";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/SlaverBlue.png";

    private int weakAmount;

    public SlaverRakeCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.ATTACK, CardTarget.ENEMY);

        // 根据难度调整数值
        this.baseDamage = 7;
        this.weakAmount = 1;

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.baseDamage = 8;
        }
        if (AbstractDungeon.ascensionLevel >= 17) {
            this.weakAmount = 2;
        }

        this.damage = this.baseDamage;
        this.baseMagicNumber = this.weakAmount;
        this.magicNumber = this.weakAmount;

        this.tags.add(CardTags.STRIKE);
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            // 攻击目标
            addToBot(new DamageAction(target, new DamageInfo(source, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.NONE));

            // 给目标施加虚弱
            addToBot(new ApplyPowerAction(target, source,
                (AbstractPower) new WeakPower(target, this.magicNumber, true), this.magicNumber));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(2);
            this.upgradeMagicNumber(1);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new SlaverRakeCard();
    }
}