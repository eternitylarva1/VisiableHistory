package VisibleHistory.monstercards.cards.louse;

import VisibleHistory.monstercards.cards.AbstractMonsterCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;

public class LouseBiteCard extends AbstractMonsterCard {
    public static final String ID = "LouseBite";
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;
    private static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final String IMG_PATH = "VisibleHistoryResources/images/creatures/FuzzyLouseNormal.png";

    public LouseBiteCard() {
        super(ID, NAME, IMG_PATH, -1, DESCRIPTION, CardType.ATTACK, CardTarget.ENEMY);

        // 根据难度调整伤害
        this.baseDamage = 6;
        if (AbstractDungeon.ascensionLevel >= 2) {
            this.baseDamage = 7;
        } else {
            this.baseDamage = 6;
        }

        this.damage = this.baseDamage;
        this.tags.add(CardTags.STRIKE);
    }

    @Override
    public void use(AbstractCreature source, AbstractCreature target) {
        if (target != null) {
            addToBot(new DamageAction(target, new DamageInfo(source, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.BLUNT_LIGHT));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(2);
        }
    }

    @Override
    public AbstractMonsterCard makeCopy() {
        return new LouseBiteCard();
    }
}