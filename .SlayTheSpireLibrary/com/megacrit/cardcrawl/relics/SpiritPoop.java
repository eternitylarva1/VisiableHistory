/*    */ package com.megacrit.cardcrawl.relics;
/*    */ 
/*    */ public class SpiritPoop extends AbstractRelic {
/*    */   public static final String ID = "Spirit Poop";
/*    */   
/*    */   public SpiritPoop() {
/*  7 */     super("Spirit Poop", "spiritPoop.png", AbstractRelic.RelicTier.SPECIAL, AbstractRelic.LandingSound.MAGICAL);
/*    */   }
/*    */ 
/*    */   
/*    */   public String getUpdatedDescription() {
/* 12 */     return this.DESCRIPTIONS[0];
/*    */   }
/*    */ 
/*    */   
/*    */   public AbstractRelic makeCopy() {
/* 17 */     return new SpiritPoop();
/*    */   }
/*    */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\relics\SpiritPoop.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */