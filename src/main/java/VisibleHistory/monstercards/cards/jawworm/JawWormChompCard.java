package VisibleHistory.monstercards.cards.jawworm;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

public class JawWormChompCard extends AbstractMonsterCard {
    public static final String ID = "JawWormChomp";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/JawWorm.png";

    public JawWormChompCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.ATTACK, CardTarget.ENEMY);

        // 根据难度调整伤害
        this.baseDamage = 11;
        if (AbstractDungeon.ascensionLevel >= 2) {
            this.baseDamage = 12;
        }

        this.damage = this.baseDamage;
        this.tags.add(CardTags.STRIKE);
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            addToBot(new DamageAction(target, new DamageInfo(source, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.NONE));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(3);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new JawWormChompCard();
    }
}