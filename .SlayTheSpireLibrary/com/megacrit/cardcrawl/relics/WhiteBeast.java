/*    */ package com.megacrit.cardcrawl.relics;
/*    */ 
/*    */ public class WhiteBeast extends AbstractRelic {
/*    */   public static final String ID = "White Beast Statue";
/*    */   
/*    */   public WhiteBeast() {
/*  7 */     super("White Beast Statue", "whiteBeast.png", AbstractRelic.RelicTier.UNCOMMON, AbstractRelic.LandingSound.SOLID);
/*    */   }
/*    */ 
/*    */   
/*    */   public String getUpdatedDescription() {
/* 12 */     return this.DESCRIPTIONS[0];
/*    */   }
/*    */ 
/*    */   
/*    */   public AbstractRelic makeCopy() {
/* 17 */     return new WhiteBeast();
/*    */   }
/*    */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\relics\WhiteBeast.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */