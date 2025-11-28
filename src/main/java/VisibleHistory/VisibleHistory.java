package VisibleHistory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class VisibleHistory {
    public static final String MOD_ID = "VisibleHistory";
    public static final String SHOULDER_IMGS_PATH = "img/char/";
    public static final String CHAR_IMG_PATH = SHOULDER_IMGS_PATH + "mainChar/";
    public static final String POWER_IMG_PATH = "img/powers/";
    public static final String RELIC_IMG_PATH = "img/relics/";
    public static final String CARD_IMG_PATH = "img/cards/";
    public static final String ENERGY_ORB_FILE = "img/char/shoulder/orb.png";
    public static final String MOD_ASSETS_PATH = "visibleHistoryResources";
    public static final String BADGE_IMG_PATH = MOD_ASSETS_PATH + "/badge.png";

    public static String makeCardPath(String filename) {
        return MOD_ASSETS_PATH + "/" + CARD_IMG_PATH + filename;
    }

    public static String makePowerPath(String filename) {
        return MOD_ASSETS_PATH + "/" + POWER_IMG_PATH + filename;
    }

    public static String makeRelicPath(String filename) {
        return MOD_ASSETS_PATH + "/" + RELIC_IMG_PATH + filename;
    }
}