package EchoForm.relics;

import EchoForm.powers.EchoPower;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;


public class Huixiang extends CustomRelic {
    public static final String ID = Huixiang.class.getSimpleName();
    public static final String IMG = "echoFormResources/images/relics/echoform.png";


    public Huixiang() {
        super(ID, new Texture(Gdx.files.internal(IMG)), RelicTier.SPECIAL, LandingSound.CLINK);
    }

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public void atBattleStart() {
        super.atBattleStart();
        for (AbstractMonster monster : AbstractDungeon.getCurrRoom().monsters.monsters) {
            this.addToBot(new ApplyPowerAction(monster,monster,new EchoPower(monster,1)));
        }
        addToBot(new ApplyPowerAction(AbstractDungeon.player,AbstractDungeon.player,new com.megacrit.cardcrawl.powers.EchoPower(AbstractDungeon.player,2)));
    }

}
