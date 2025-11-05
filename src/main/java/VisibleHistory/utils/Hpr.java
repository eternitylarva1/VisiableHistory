package VisibleHistory.utils;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.GainStrengthPower;
import com.megacrit.cardcrawl.powers.LoseDexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import java.util.ArrayList;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hpr {
  private static final Logger logger = LogManager.getLogger(Hpr.class);
  
  public static void movePosition(AbstractMonster m, float x, float y) {
    m.drawX = x;
    m.drawY = y;
    m.dialogX = m.drawX + 0.0F * Settings.scale;
    m.dialogY = m.drawY + 170.0F * Settings.scale;
    m.animX = 0.0F;
    m.animY = 0.0F;
    m.updateAnimations();
  }
  
  public static AbstractMonster getRandomMonsterSafe() {
    AbstractMonster m = AbstractDungeon.getRandomMonster();
    if (m != null && !m.isDeadOrEscaped() && !m.isDead)
      return m; 
    return null;
  }
  
  public static boolean isInBattle() {
    return (CardCrawlGame.dungeon != null && AbstractDungeon.currMapNode != null && 
      (AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT);
  }
  
  public static ArrayList<AbstractMonster> monsters() {
    return (AbstractDungeon.getMonsters()).monsters;
  }
  
  public static boolean isAlive(AbstractCreature c) {
    return (c != null && !c.isDeadOrEscaped() && !c.isDead);
  }

  public static float getRandomPositionX() {

    return AbstractDungeon.cardRandomRng.random(Settings.WIDTH/2);
  }
  public static float getRandomPositionY() {

    return AbstractDungeon.cardRandomRng.random(Settings.HEIGHT);
  }
  public static int aliveMonstersAmount() {
    int i = 0;
    for (AbstractMonster m : monsters()) {
      if (isAlive((AbstractCreature)m))
        i++; 
    } 
    return i;
  }
  
  public static void addToBot(AbstractGameAction action) {
    AbstractDungeon.actionManager.addToBottom(action);
  }
  
  public static void addToTop(AbstractGameAction action) {
    AbstractDungeon.actionManager.addToTop(action);
  }
  
  public static void addToBotAbstract(final VoidSupplier func) {
    AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
          public void update() {
            func.get();
            this.isDone = true;
          }
        });
  }
  
  public static void addToTopAbstract(final VoidSupplier func) {
    AbstractDungeon.actionManager.addToTop(new AbstractGameAction() {
          public void update() {
            func.get();
            this.isDone = true;
          }
        });
  }
  
  public static void addEffect(AbstractGameEffect effect) {
    AbstractDungeon.effectList.add(effect);
  }
  
  public static void GainRelic(AbstractRelic r) {
    AbstractDungeon.player.relics.add(r);
    r.onEquip();
    AbstractDungeon.player.reorganizeRelics();
  }
  
  public static void info(String s) {
    logger.info(s);
  }
  
  public static AbstractCard makeStatEquivalentCopy(AbstractCard c) {
    AbstractCard card = c.makeStatEquivalentCopy();
    card.retain = c.retain;
    card.selfRetain = c.selfRetain;
    card.purgeOnUse = c.purgeOnUse;
    card.isEthereal = c.isEthereal;
    card.exhaust = c.exhaust;
    card.glowColor = c.glowColor;
    card.rawDescription = c.rawDescription;
    card.cardsToPreview = c.cardsToPreview;
    card.initializeDescription();
    return card;
  }
  
  public static void foreachCardNotExhausted(Function<AbstractCard, Boolean> func) {
    for (AbstractCard c : AbstractDungeon.player.drawPile.group) {
      if (((Boolean)func.apply(c)).booleanValue())
        return; 
    } 
    for (AbstractCard c : AbstractDungeon.player.hand.group) {
      if (((Boolean)func.apply(c)).booleanValue())
        return; 
    } 
    for (AbstractCard c : AbstractDungeon.player.discardPile.group) {
      if (((Boolean)func.apply(c)).booleanValue())
        return; 
    } 
  }
  
  public static void foreachCardNotExhaustedNotHand(Function<AbstractCard, Boolean> func) {
    for (AbstractCard c : AbstractDungeon.player.drawPile.group) {
      if (((Boolean)func.apply(c)).booleanValue())
        return; 
    } 
    for (AbstractCard c : AbstractDungeon.player.discardPile.group) {
      if (((Boolean)func.apply(c)).booleanValue())
        return; 
    } 
  }
  
  public static void foreachPowerHeroAndMonstersHave(Function<AbstractPower, Boolean> func) {
    for (AbstractPower p : AbstractDungeon.player.powers) {
      if (((Boolean)func.apply(p)).booleanValue())
        return; 
    } 
    for (AbstractMonster m : monsters()) {
      if (isAlive((AbstractCreature)m))
        for (AbstractPower p : m.powers) {
          if (((Boolean)func.apply(p)).booleanValue())
            return; 
        }  
    } 
  }
  
  public static void foreachAliveMonster(Function<AbstractMonster, Boolean> func) {
    for (AbstractMonster m : monsters()) {
      if (isAlive((AbstractCreature)m) && (
        (Boolean)func.apply(m)).booleanValue())
        return; 
    } 
  }
  
  public static void tempLoseStrength(AbstractCreature mo, AbstractCreature p, int amt) {
    addToBot((AbstractGameAction)new ApplyPowerAction(mo, p, (AbstractPower)new StrengthPower(mo, -amt), -amt, true, AbstractGameAction.AttackEffect.NONE));
    if (!mo.hasPower("Artifact"))
      addToBot((AbstractGameAction)new ApplyPowerAction(mo, p, (AbstractPower)new GainStrengthPower(mo, amt), amt, true, AbstractGameAction.AttackEffect.NONE)); 
  }
  
  public static void tempGainDex(AbstractCreature target, AbstractCreature source, int amt) {
    addToBot((AbstractGameAction)new ApplyPowerAction(target, source, (AbstractPower)new DexterityPower(source, amt)));
    addToBot((AbstractGameAction)new ApplyPowerAction(target, source, (AbstractPower)new LoseDexterityPower(source, amt)));
  }
  
  public static interface VoidSupplier {
    void get();
  }
}
