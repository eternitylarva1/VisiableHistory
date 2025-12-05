/*    */ package com.megacrit.cardcrawl.relics;
/*    */ 
/*    */ public class RunicPyramid extends AbstractRelic {
/*    */   public static final String ID = "Runic Pyramid";
/*    */   
/*    */   public RunicPyramid() {
/*  7 */     super("Runic Pyramid", "runicPyramid.png", AbstractRelic.RelicTier.BOSS, AbstractRelic.LandingSound.FLAT);
/*    */   }
/*    */ 
/*    */   
/*    */   public String getUpdatedDescription() {
/* 12 */     return this.DESCRIPTIONS[0];
/*    */   }
/*    */ 
/*    */   
/*    */   public AbstractRelic makeCopy() {
/* 17 */     return new RunicPyramid();
/*    */   }
/*    */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\relics\RunicPyramid.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */