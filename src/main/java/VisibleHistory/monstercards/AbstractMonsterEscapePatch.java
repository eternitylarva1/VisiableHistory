package VisibleHistory.monstercards;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/**
 * 怪物逃跑时的Patch类
 * 用于在怪物逃跑时清理出牌系统
 */
@SpirePatch(
    clz = AbstractMonster.class,
    method = "escape"
)
class AbstractMonsterEscapePatch {

    @SpirePrefixPatch
    public static void prefix(AbstractMonster __instance) {
        // 在怪物escape方法开始时禁用其出牌系统
        MonsterCardManager.getInstance().disableMonster(__instance);
    }
}