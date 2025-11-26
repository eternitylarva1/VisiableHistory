package VisibleHistory.modcore;


import VisibleHistory.helpers.ModHelper;
import basemod.EasyConfigPanel;
import com.megacrit.cardcrawl.relics.AbstractRelic;

public class MyModConfig extends EasyConfigPanel {
    public MyModConfig() {
        super(visibleHistory.MyModID, ModHelper.makePath(MyModConfig.class.getSimpleName()));
        setNumberRange("DeadPlayerMax",0,99);
        setNumberRange("toumingdu",0,1);
    }

    public static int DeadPlayerMax =25;

    public static float toumingdu=0.35f;
    public static boolean xianshidonghua=false;
}
