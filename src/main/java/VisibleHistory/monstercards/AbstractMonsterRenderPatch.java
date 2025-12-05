package VisibleHistory.monstercards;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * AbstractMonster的渲染Patch类
 * 用于在怪物render方法中渲染头顶卡牌
 */
@SpirePatch(
    clz = AbstractMonster.class,
    method = "render"
)
class AbstractMonsterRenderPatch {

    @SpirePostfixPatch
    public static void postfix(AbstractMonster __instance, SpriteBatch sb) {
        // 在怪物render方法结束后渲染怪物的头顶卡牌
        MonsterCardManager.getInstance().render(sb);
    }
}