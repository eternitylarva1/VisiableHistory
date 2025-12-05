/*    */ package com.megacrit.cardcrawl.trials;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class AnyColorDraftTrial
/*    */   extends AbstractTrial
/*    */ {
/*    */   public boolean keepsStarterCards() {
/* 17 */     return false;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public ArrayList<String> dailyModIDs() {
/* 27 */     ArrayList<String> retVal = new ArrayList<>();
/* 28 */     retVal.add("Diverse");
/* 29 */     retVal.add("Draft");
/* 30 */     return retVal;
/*    */   }
/*    */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\trials\AnyColorDraftTrial.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */