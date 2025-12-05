package VisibleHistory.monstercards;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;

/**
 * 战斗开始时的Patch类
 * 用于在怪物组初始化时为所有怪物启用出牌系统
 */
@SpirePatch(
    clz = MonsterGroup.class,
    method = "init"
)
class BattleStartPatch {

    @SpirePostfixPatch
    public static void postfix(MonsterGroup __instance) {
        // 在怪物组初始化完成后为所有怪物启用出牌系统
        MonsterCardManager manager = MonsterCardManager.getInstance();

        // 重置之前的状态（确保新战斗开始时干净）
        manager.resetAll();

        // 获取当前怪物组的怪物列表
        if (__instance != null && __instance.monsters != null) {

            // 为当前怪物组的所有怪物启用出牌系统
            manager.enableRoomMonsters(__instance.monsters);

            VisibleHistory.utils.Hpr.info("怪物组初始化，为 " + __instance.monsters.size() + " 个怪物启用了出牌系统");
        }
    }
}