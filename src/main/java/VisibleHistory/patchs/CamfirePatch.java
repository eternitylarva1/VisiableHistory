package VisibleHistory.patchs;


import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;


public class CamfirePatch {
    @SpirePatch(clz = AbstractDungeon.class, method = "generateMap")
    public static class ModifyRewardScreenStuff {
        @SpireInsertPatch(
                loc=645
        )
        public static void patch() {
       }
    }
}
