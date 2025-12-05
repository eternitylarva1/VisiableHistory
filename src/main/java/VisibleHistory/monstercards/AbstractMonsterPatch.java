package VisibleHistory.monstercards;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * AbstractMonster的更新Patch类
 * 用于在怪物的update方法中注入我们的出牌系统
 */
@SpirePatch(
    clz = AbstractMonster.class,
    method = "update"
)
public class AbstractMonsterPatch {

    @SpirePostfixPatch
    public static void postfix(AbstractMonster __instance) {
        // 在怪物update方法结束后更新怪物的出牌系统
        MonsterCardManager.getInstance().update();
    }
}