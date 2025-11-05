package VisibleHistory.patchs;


import VisibleHistory.playerdeath.DeadPlayer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.RestRoom;


public class CamfirePatch {
    @SpirePatch(
            clz = AbstractPlayer.class,
            method = "render"
    )
    public static class RenderPatch {
        public RenderPatch() {
        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPlayer _instance, SpriteBatch sb) {
            if (!(AbstractDungeon.getCurrRoom() instanceof RestRoom)) {
                for (DeadPlayer deadPlayer : DeadPlayer.deadPlayers) {
                    Color oldcolor=sb.getColor();
                   sb.setColor(Color.WHITE);
                    deadPlayer.render(sb);
                    sb.setColor(oldcolor);
                }
            }
        }
    }
}
