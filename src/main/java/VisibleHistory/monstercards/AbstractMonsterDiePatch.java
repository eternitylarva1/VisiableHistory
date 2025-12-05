package VisibleHistory.monstercards;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * 怪物死亡时的Patch类
 * 用于在怪物死亡时清理出牌系统
 */
@SpirePatch(
    clz = AbstractMonster.class,
    method = "die"
)
class AbstractMonsterDiePatch {

    @SpirePrefixPatch
    public static void prefix(AbstractMonster __instance) {
        // 在怪物die方法开始时禁用其出牌系统
        MonsterCardManager.getInstance().disableMonster(__instance);
    }
}