//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package EchoForm.powers;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Reptomancer;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;
import com.megacrit.cardcrawl.monsters.exordium.GremlinNob;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class EchoPower extends AbstractPower {
    public static final String POWER_ID = "Echo Form1";
    private static final PowerStrings powerStrings;
    public static final String NAME;
    public static final String[] DESCRIPTIONS;
    private int cardsDoubledThisTurn = 0;

    public EchoPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = "Echo Form1";
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        this.loadRegion("echo");
    }

    public void updateDescription() {
        if (this.amount == 1) {
            this.description = DESCRIPTIONS[0];
        } else {
            this.description = DESCRIPTIONS[1] + this.amount + DESCRIPTIONS[2];
        }

    }

    public void duringTurn() {
        this.cardsDoubledThisTurn = 0;
        for (int i=0;i<this.amount;i++){
            if (this.owner instanceof AbstractMonster ){
                if (this.owner instanceof GremlinLeader||this.owner instanceof Reptomancer)
                {
                    if (((AbstractMonster) this.owner).intent== AbstractMonster.Intent.UNKNOWN) {
                        continue;
                    }
                }                ((AbstractMonster) this.owner).takeTurn();
            }
        }

    }

    public void onUseCard(AbstractCard card, UseCardAction action) {

    }

    static {
        powerStrings = CardCrawlGame.languagePack.getPowerStrings("Echo Form1");
        NAME = powerStrings.NAME;
        DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    }
}
