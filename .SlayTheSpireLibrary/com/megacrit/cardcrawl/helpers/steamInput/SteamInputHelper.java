/*    */ package com.megacrit.cardcrawl.helpers.steamInput;
/*    */ 
/*    */ import com.badlogic.gdx.Gdx;
/*    */ import com.badlogic.gdx.controllers.Controller;
/*    */ import com.badlogic.gdx.utils.Array;
/*    */ import com.codedisaster.steamworks.SteamAPI;
/*    */ import com.codedisaster.steamworks.SteamController;
/*    */ import com.codedisaster.steamworks.SteamControllerActionSetHandle;
/*    */ import com.codedisaster.steamworks.SteamControllerHandle;
/*    */ import com.megacrit.cardcrawl.core.CardCrawlGame;
/*    */ import com.megacrit.cardcrawl.core.Settings;
/*    */ import com.megacrit.cardcrawl.helpers.Hitbox;
/*    */ import com.megacrit.cardcrawl.helpers.ImageMaster;
/*    */ import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
/*    */ import java.util.ArrayList;
/*    */ import org.apache.logging.log4j.LogManager;
/*    */ import org.apache.logging.log4j.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SteamInputHelper
/*    */ {
/* 23 */   private static final Logger logger = LogManager.getLogger(SteamInputHelper.class.getName());
/* 24 */   public static Array<Controller> controllers = null;
/* 25 */   public static ArrayList<SteamInputAction> actions = new ArrayList<>();
/* 26 */   public static CInputHelper.ControllerModel model = null;
/*    */   
/*    */   public static boolean alive = false;
/*    */   
/*    */   public static SteamController controller;
/*    */   public static SteamControllerHandle[] controllerHandles;
/*    */   public static SteamControllerHandle handle;
/* 33 */   public static int numControllers = 0;
/*    */ 
/*    */   
/*    */   public static SteamControllerActionSetHandle setHandle;
/*    */ 
/*    */   
/*    */   public SteamInputHelper() {
/* 40 */     if (!SteamAPI.isSteamRunning()) {
/* 41 */       logger.info("Steam isn't running? SteamInput is disabled.");
/*    */       
/*    */       return;
/*    */     } 
/*    */     
/* 46 */     controller = new SteamController();
/* 47 */     alive = controller.init();
/*    */     
/* 49 */     if (!alive) {
/* 50 */       logger.info("Steam controller failed to initialize.");
/*    */       
/*    */       return;
/*    */     } 
/* 54 */     controllerHandles = new SteamControllerHandle[16];
/*    */     
/* 56 */     logger.info("Starting input detection...");
/* 57 */     Thread controllerDetectThread = new Thread(new SteamInputDetect());
/* 58 */     CardCrawlGame.sInputDetectThread = controllerDetectThread;
/* 59 */     controllerDetectThread.setName("InputDetect");
/* 60 */     controllerDetectThread.start();
/*    */     
/* 62 */     model = CInputHelper.ControllerModel.XBOX_ONE;
/* 63 */     ImageMaster.loadControllerImages(CInputHelper.ControllerModel.XBOX_ONE);
/*    */   }
/*    */   
/*    */   public static void initActions(SteamControllerHandle controllerHandle) {
/* 67 */     handle = controllerHandle;
/* 68 */     SteamInputActionSet.load(controller);
/*    */     
/* 70 */     for (SteamInputAction a : actions) {
/* 71 */       a.init(controller, handle);
/*    */     }
/*    */   }
/*    */   
/*    */   public static void updateFirst() {
/* 76 */     controller.runFrame();
/*    */     
/* 78 */     for (SteamInputAction a : actions) {
/* 79 */       a.update();
/*    */     }
/*    */   }
/*    */   
/*    */   public static void setCursor(Hitbox hb) {
/* 84 */     if (hb != null) {
/* 85 */       Gdx.input.setCursorPosition((int)hb.cX, Settings.HEIGHT - (int)hb.cY);
/*    */     }
/*    */   }
/*    */   
/*    */   public static boolean isJustPressed(int keycode) {
/* 90 */     return false;
/*    */   }
/*    */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\helpers\steamInput\SteamInputHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */