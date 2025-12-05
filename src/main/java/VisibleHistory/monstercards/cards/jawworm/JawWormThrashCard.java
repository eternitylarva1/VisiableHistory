package VisibleHistory.monstercards.cards.jawworm;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

public class JawWormThrashCard extends AbstractMonsterCard {
    public static final String ID = "JawWormThrash";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/JawWorm.png";

    public JawWormThrashCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.SKILL, CardTarget.ENEMY);

        this.baseDamage = 7;
        this.baseBlock = 5;

        this.damage = this.baseDamage;
        this.block = this.baseBlock;

        this.tags.add(CardTags.STRIKE);
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            // 攻击目标
            addToBot(new DamageAction(target, new DamageInfo(source, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.BLUNT_LIGHT));

            // 给自己增加格挡
            addToBot(new GainBlockAction(source, source, this.block));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(2);
            this.upgradeBlock(2);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new JawWormThrashCard();
    }
}