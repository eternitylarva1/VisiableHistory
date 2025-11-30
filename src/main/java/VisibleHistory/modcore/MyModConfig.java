package VisibleHistory.modcore;


import VisibleHistory.helpers.ModHelper;
import basemod.EasyConfigPanel;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import static java.lang.reflect.Array.setBoolean;

public class MyModConfig extends EasyConfigPanel {
    public MyModConfig() {
        super(visibleHistory.MyModID, ModHelper.makePath(MyModConfig.class.getSimpleName()));
        setNumberRange("DeadPlayerMax",0,99);
        setNumberRange("toumingdu",0,1);
        // 排序模式配置通过修改配置文件来设置，暂时不显示在界面中

    }

    public static int DeadPlayerMax =25;

    public static float toumingdu=0.35f;

    public static boolean showCards = true;

    public static boolean showRelics = true;

    public static boolean ctrlClickTransform = true;

    // 新增：排序模式配置（false=按角色分组，true=按时间排序）
    public static boolean sortByTime = false;

    // 新增：尸体堆叠显示配置（true=同角色尸体堆叠，false=随机位置）
    public static boolean stackByCharacter = false;

    // 新增：尸体显示开关（true=显示尸体，false=隐藏尸体）
    public static boolean showCorpses = true;

   // public static boolean xianshidonghua=false;
}
