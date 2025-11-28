# Slay the Spire API Documentation
*A comprehensive AI-friendly guide to the Slay the Spire game framework*

## Overview
This document provides detailed documentation for Slay the Spire mod development using Java 8 and BaseMod. The API is organized into logical systems for easy reference.

## MCP Tools Usage Guide
*AI-friendly toolkit for analyzing Slay the Spire JAR files*

### Prerequisites
- **MCP Server Required**: `bfHaz-Y7LcmAWkjy1mpdr` MCP server must be installed
- **Game Location**: `C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire`
- **Main JAR**: `desktop-1.0.jar` (or `desktop-1.0-patched.jar` if modded)

### Essential MCP Commands

#### 1. Dependency Scanning
```json
{
  "tool": "mcp__bfHaz-Y7LcmAWkjy1mpdr__scan_dependencies",
  "parameters": {
    "projectPath": "C:\\Program Files (x86)\\Steam\\steamapps\\common\\SlayTheSpire",
    "forceRefresh": true
  }
}
```
**Purpose**: Creates complete class-to-JAR mapping index
**Output**: `.mcp-class-index.json` with 1000+ classes indexed
**Usage**: Run once per session, or when needing fresh data

#### 2. Class Analysis
```json
{
  "tool": "mcp__bfHaz-Y7LcmAWkjy1mpdr__analyze_class",
  "parameters": {
    "className": "com.megacrit.cardcrawl.cards.AbstractCard",
    "projectPath": "C:\\Program Files (x86)\\Steam\\steamapps\\common\\SlayTheSpire"
  }
}
```
**Purpose**: Analyzes specific class structure, methods, and properties
**Output**: Method signatures, inheritance, interface implementations
**Tips**:
- Use full class names with package
- For inner classes: `ClassName$InnerClass`
- Common classes listed below in quick reference

#### 3. Class Decompilation
```json
{
  "tool": "mcp__bfHaz-Y7LcmAWkjy1mpdr__decompile_class",
  "parameters": {
    "className": "com.megacrit.cardcrawl.cards.red.Strike_Red",
    "projectPath": "C:\\Program Files (x86)\\Steam\\steamapps\\common\\SlayTheSpire",
    "useCache": true
  }
}
```
**Purpose**: Returns full source code for a class
**Output**: Complete Java source code
**Usage**: When detailed implementation logic is needed

### Important Class Names (Copy-Paste Ready)

#### Core Game Classes
- `com.megacrit.cardcrawl.core.CardCrawlGame`
- `com.megacrit.cardcrawl.dungeons.AbstractDungeon`
- `com.megacrit.cardcrawl.characters.AbstractPlayer`
- `com.megacrit.cardcrawl.rooms.AbstractRoom`

#### Card System
- `com.megacrit.cardcrawl.cards.AbstractCard`
- `com.megacrit.cardcrawl.cards.CardGroup`
- `com.megacrit.cardcrawl.cards.red.Strike_Red`
- `com.megacrit.cardcrawl.cards.green.Defend_Green`

#### Monster System
- `com.megacrit.cardcrawl.monsters.AbstractMonster`
- `com.megacrit.cardcrawl.monsters.MonsterGroup`
- `com.megacrit.cardcrawl.monsters.exordium.Cultist`
- `com.megacrit.cardcrawl.monsters.exordium.JawWorm`

#### Relic System
- `com.megacrit.cardcrawl.relics.AbstractRelic`
- `com.megacrit.cardcrawl.relics.CrackedCore`
- `com.megacrit.cardcrawl.relics.BurningBlood`

#### Power System
- `com.megacrit.cardcrawl.powers.AbstractPower`
- `com.megacrit.cardcrawl.powers.StrengthPower`
- `com.megacrit.cardcrawl.powers.WeakPower`

#### Action System
- `com.megacrit.cardcrawl.actions.AbstractGameAction`
- `com.megacrit.cardcrawl.actions.common.DamageAction`
- `com.megacrit.cardcrawl.actions.common.DrawCardAction`

### Troubleshooting Common Issues

#### "未找到类对应的JAR包" (Class not found)
**Solution**:
1. Run dependency scan first: `mcp__bfHaz-Y7LcmAWkjy1mpdr__scan_dependencies`
2. Verify class name spelling (case-sensitive)
3. Check if using correct project path

#### Best Practices
1. **Always scan dependencies before analyzing** - The MCP tools need the index
2. **Use full class names** - Include package names for accuracy
3. **Cache is enabled by default** - Use `useCache: false` only if needed
4. **Start with core classes** - Understand base classes before specific implementations
5. **Check inheritance hierarchy** - Many classes extend common base classes

### Analysis Workflow
1. **Initial Scan**: `scan_dependencies` to build index
2. **Core Analysis**: Analyze `AbstractCard`, `AbstractMonster`, `AbstractRelic`
3. **Specific Examples**: Analyze concrete implementations (e.g., `Strike_Red`)
4. **Deep Dive**: Use `decompile_class` for implementation details

---

## Status Legend
- 📋 Todo: Not yet documented
- 🔄 In Progress: Currently being documented
- ✅ Complete: Fully documented

---

# 🃏 Card System
**Status: ✅ Complete**

## AbstractCard
*Base class for all cards in the game*

### Core Properties
- `name`: String - Card name
- `cost`: int - Energy cost to play
- `cardType`: CardType - ATTACK, SKILL, POWER, STATUS, CURSE
- `cardRarity`: CardRarity - BASIC, SPECIAL, COMMON, UNCOMMON, RARE, CURSE
- `cardTarget`: CardTarget - ENEMY, ALL_ENEMY, SELF, ALL, NONE
- `damage`: int - Base damage value
- `block`: int - Base block amount
- `magicNumber`: int - Special effect parameter
- `description`: String - Card description text

### Key Methods
```java
// Abstract methods (must implement)
public abstract void use(AbstractPlayer p, AbstractMonster m)
public abstract void upgrade()
public abstract AbstractCard makeCopy()

// Card lifecycle
public void triggerWhenDrawn()           // Called when card is drawn
public void triggerOnExhaust()           // Called when card is exhausted
public void triggerOnEndOfPlayerTurn()   // Called at end of turn
public void triggerOnManualDiscard()     // Called when manually discarded
public void onPlayCard(AbstractCard c, AbstractMonster m)  // When this card is played

// Power and damage calculation
public void applyPowers()                // Apply current powers to card
public void calculateCardDamage(AbstractMonster mo)   // Calculate damage
public void clearPowers()                // Remove power effects

// Cost management
public void modifyCostForCombat(int amt) // Temporary cost change
public void setCostForTurn(int cost)     // Set cost for this turn
public void resetAttributes()            // Reset to base values

// Usability checks
public boolean cardPlayable(AbstractMonster m)
public boolean hasEnoughEnergy()
public boolean canUse(AbstractPlayer p, AbstractMonster m)

// Visual effects
public void flash()
public void superFlash()
public void beginGlowing()
public void stopGlowing()
```

### Usage Notes
- All custom cards must extend AbstractCard
- Call `initializeDescription()` after modifying card text
- Use `makeSameInstanceOf()` for temporary card copies
- Use `makeStatEquivalentCopy()` for permanent copies

## CardGroup
*Container for collections of cards (hand, deck, discard, etc.)*

### Group Types
- `HAND`: Player's current hand
- `DRAW_DECK`: Player's draw deck
- `DISCARD_PILE`: Discarded cards
- `EXHAUST_PILE`: Exhausted cards
- `MASTER_DECK`: Complete deck library

### Key Methods
```java
// Card manipulation
public void addToTop(AbstractCard c)           // Add to top
public void addToBottom(AbstractCard c)        // Add to bottom
public void addToRandomSpot(AbstractCard c)    // Add randomly
public void removeCard(AbstractCard c)         // Remove specific card

// Card access
public AbstractCard getTopCard()               // Get top card
public AbstractCard getRandomCard()            // Get random card
public AbstractCard getHoveredCard()           // Get hovered card

// Utility methods
public void shuffle()                          // Shuffle the group
public int size()                              // Get group size
public boolean isEmpty()                       // Check if empty
public boolean contains(AbstractCard c)        // Check containment

// Filtering and sorting
public CardGroup getSkills()                   // Get skill cards only
public CardGroup getAttacks()                  // Get attack cards only
public CardGroup getPowers()                   // Get power cards only
public void sortByCost(boolean reverse)        // Sort by energy cost
public void sortByRarity(boolean reverse)      // Sort by rarity

// Special operations
public void applyPowers()                      // Apply powers to all cards
public void glowCheck()                        // Update glow states
public void moveToDiscardPile(AbstractCard c)  // Move card to discard
public void moveToExhaustPile(AbstractCard c)  // Move card to exhaust
public void moveToHand(AbstractCard c)         // Move card to hand
```

### Usage Examples
```java
// Draw cards from deck
for (int i = 0; i < 5; i++) {
    if (!AbstractDungeon.player.drawPile.isEmpty()) {
        AbstractCard card = AbstractDungeon.player.drawPile.getTopCard();
        AbstractDungeon.player.drawPile.removeCard(card);
        AbstractDungeon.player.hand.addToTop(card);
    }
}

// Create filtered group of attack cards
CardGroup attacks = AbstractDungeon.player.hand.getAttacks();

// Shuffle discard pile back into deck
AbstractDungeon.player.discardPile.shuffle();
while (!AbstractDungeon.player.discardPile.isEmpty()) {
    AbstractCard c = AbstractDungeon.player.discardPile.getTopCard();
    AbstractDungeon.player.discardPile.moveToDeck(c, false);
}
```

## CardType, CardRarity, CardTarget Enums
*Enumerations for card classification*

### CardType
- `ATTACK`: Cards that deal damage
- `SKILL`: Cards with various effects (block, draw cards, etc.)
- `POWER`: Cards that grant persistent powers
- `STATUS`: Unwanted status cards
- `CURSE`: Negative curse cards

### CardRarity
- `BASIC`: Starter cards (Strike, Defend)
- `SPECIAL`: Special reward cards
- `COMMON`: Common cards from card rewards
- `UNCOMMON`: Uncommon reward cards
- `RARE`: Rare reward cards
- `CURSE`: Curse cards

### CardTarget
- `ENEMY`: Single enemy target
- `ALL_ENEMY`: All enemies
- `SELF`: Self-targeted effects
- `ALL`: All targets (self + enemies)
- `NONE`: No specific target required

---

# 📋 Core Game Framework
**Status: 🔄 In Progress**

## CardCrawlGame
*Game entry point and main controller*

### Key Methods
```java
// Game state access
public static CardCrawlGame()
public void menuState()
public void create()
public void render()

// Save system
public void loadPlayerSave()
public void savePlayerSave()

// Asset management
public void updateFont()
public void refresh()
```

### Static Access
- `CardCrawlGame.blendColor`: Color manipulation
- `CardCrawlGame.sound`: Sound system access
- `CardCrawlGame.effectList`: Visual effects list
- `CardCrawlGame.playerName`: Current player name

---

# 📋 Dungeon & Room System
**Status: 🔄 In Progress**

## AbstractDungeon
*Central game state manager*

### Key Methods
```java
// Room and level access
public static AbstractRoom getCurrRoom()
public static ArrayList<MapRoomNode> map
public static int floorNum

// Player management
public static AbstractPlayer player
public static MonsterGroup monsters

// Combat state
public static boolean isLoadingScreen
public static boolean isScreenUp
public static void closeCurrentScreen()

// Card and reward management
public static CardRewardScreen cardRewardScreen
public static com.megacrit.cardcrawl.rewards.RewardItem getRewardItem()

// Dungeon generation
public static void generateDungeon()
public static void nextRoomTransition(com.megacrit.cardcrawl.map.MapEdge node)
```

### Usage Notes
- Always null-check `getCurrRoom()` before access
- Most game state should be accessed through this singleton

## AbstractRoom
*Base class for all room types*

### Room Types
- `MonsterRoom`: Combat encounters
- `RestRoom`: Campfire rest sites
- `TreasureRoom`: Treasure chests
- `ShopRoom`: Merchant encounters
- `EventRoom`: Random events
- `BossRoom`: Boss encounters

### Key Properties
- `phase`: RoomPhase (COMBAT, EVENT, INCOMPLETE)
- `monsters`: MonsterGroup for combat rooms
- ` reward`: Rewards for completing room

---

# 📋 Player System
**Status: 🔄 In Progress**

## AbstractPlayer
*Base class for all player characters*

### Key Methods
```java
// Combat actions
public void draw(int numCards)
public void applyStartOfTurnRelics()
public void applyStartOfTurnCards()
public void applyStartOfTurnPowers()

// Energy management
public void gainEnergy(int amount)
public void useEnergy(int amount)
public boolean hasRelic(String relicName)

// Damage and health
public void damage(DamageInfo info)
public void heal(int amount, boolean showEffect)

// Deck management
public void masterDeckAddTopCard(AbstractCard card)
public void moveCardToDeck(AbstractCard card, boolean randomSpot)

// Utility
public void addToHand(AbstractCard card)
public void addToTopOfDeck(AbstractCard card)
```

### Player Classes
- `Ironclad`: Red character (strength, combat)
- `Silent`: Green character (poison, shivs)
- `Defect`: Blue character (orbs, lightning)
- `Watcher`: Purple character (stances, divine cards)

---

# 📋 System Architecture
**Status: 🔄 In Progress**

## GameFlowManager
*Controls game state transitions*

## SaveFile
*Save/load system management*

## Settings
*Game configuration and preferences*

---

# 👹 Monster System
**Status: ✅ Complete**

## AbstractMonster
*Base class for all game enemies and monsters*

### Core Properties
- `name`: String - Monster name
- `currentHealth`: int - Current hit points
- `maxHealth`: int - Maximum hit points
- `currentBlock`: int - Current block amount
- `intent`: Intent - Next action type indicator
- `moveIndex`: byte - Current move selection
- `nextMove`: byte - Next upcoming move
- `intentDamage`: int - Damage value for next intent
- `isEscaping`: boolean - Whether monster is escaping
- `isDying`: boolean - Whether monster is dying
- `halfDead`: boolean - Special state (Louse, etc.)
- `gold`: int - Gold reward when defeated

### Key Methods
```java
// Abstract methods (must implement)
public abstract void takeTurn()

// Intent system
public void setMove(byte moveName, Intent intent, int baseDamage, int multiplier, boolean isMultiDamage)
public void setMove(String moveName, byte moveName, Intent intent)
public void createIntent()                    // Initialize intent display
public void rollMove()                        // Randomly select next move

// Combat lifecycle
public void takeTurn()                        // Execute planned turn
public void usePreBattleAction()              // Setup at combat start
public void damage(DamageInfo info)           // Apply damage
public void heal(int amount)                  # Restore health
public void die(boolean triggerRelics)        // Handle death
public void escape()                          // Flee from combat

// State management
public void applyPowers()                     // Apply current powers
public void changeState(String stateName)     // Change internal state
public void addToBot(AbstractGameAction action) // Queue action at end
public void addToTop(AbstractGameAction action) // Queue action immediately

// Utility methods
public int getIntentDmg()                     // Get intended damage
public void flashIntent()                     // Flash intent indicator
public void renderTip(SpriteBatch sb)         // Show tooltip on hover
```

## Intent System
*Visual and logical system showing what monsters will do*

### Common Intent Types
- `ATTACK`: Monster will damage player
- `ATTACK_BUFF`: Attack + self-buff
- `ATTACK_DEBUFF`: Attack + player debuff
- `ATTACK_DEFEND`: Attack + gain block
- `BUFF`: Apply positive effect to self or ally
- `DEBUFF`: Apply negative effect to player
- `DEFEND`: Gain block
- `DEFEND_DEBUFF`: Block + player debuff
- `MAGIC`: Special non-damaging effect
- `STUN`: Skip turn (stunned state)
- `ESCAPE`: Attempt to flee combat
- `UNKNOWN`: "?" placeholder for first turn

### Intent Visualization
- Icons appear above monster heads
- Damage values shown next to attack icons
- Multi-hit attacks show "X#" indicator
- Color coding for effect types

## MonsterGroup
*Container for managing all monsters in combat*

### Key Methods
```java
// Monster management
public void addMonster(AbstractMonster m)                 // Add monster
public void addSpawnedMonster(AbstractMonster m)          // Add mid-combat spawn
public boolean areMonstersDead()                          // Check all dead
public boolean areMonstersBasicallyDead()                 // Check dead/dying

// Intent and turn management
public void applyPreTurnLogic()                           // Pre-turn effects
public void showIntent()                                  // Update all intents
public void applyEndOfTurnPowers()                        // End-of-turn effects

// Utility methods
public AbstractMonster getRandomMonster()                 // Get random monster
public AbstractMonster getRandomMonster(boolean aliveOnly) // With filters
public AbstractMonster getMonster(String name)            // Get by name
public ArrayList<String> getMonsterNames()                // Get all names
public boolean haveMonstersEscaped()                      // Check escapes
```

## Monster Categories and Examples

### Act 1: Exordium Monsters
**Basic Enemies:**
- `JawWorm`: "Bellow" (buff) → "Thrash" (attack defend) → "Chomp" (damage)
- `Cultist`: "Incantation" (weak) → "Dark Strike" (attack)
- `LouseBlue/LouseRed`: "Bite" (damage) + "Spit Web" (weak)
- `SlimeBlue/SlimeGreen`: "Tackle" (damage) + "Lick" (weak/debuff)

**Elite Enemies:**
- `GremlinNob`: Rush, Bash, Anger pattern
- `Lagavulin`: Start stunned, then alternating attacks
- `GremlinFat/Wiz/etc.` - Gremlin gang combinations

**Boss:**
- `TheGuardian`: Defensive mode + offensive mode phases

### Act 2: City Monsters
**Basic Enemies:**
- `BlueSlaver`: "Stab" (damage) + "Rake" (bleed)
- `RedSlaver`: "Stab" + "Entangle"
- `Rodent`: "Bite" + multiple bites at low health
- `Sentry": "Beam" + hardware damage
- `Byrd`: "Swoop" + "Peck" multi-hit

**Elites:**
- `ColosseumMaster`: Spike damage patterns
- `Champ`: Defensive tank with punishing damage

**Boss:**
- `Collector`: Collects heads, summons minions

### Act 3: The Temple
**Basic Enemies:**
- `Spiker`: Sets damage spikes
- `Thief`: Steals gold, high damage
- `Mugger`: Damage + gold steal
- `SnakePlant`: Poison damage + heal over time

**Elites:**
- `BookStabber`: Alternating damage/debuff
- `GiantHead`: Heavy damage at low health

**Boss:**
- `Donu and Deca`: Dual boss with synergy mechanics

### Act 4: The Beyond
**Enemies:**
- `Snecko`: Randomizes costs + multi-attack
- `SpireGrowth`: Progressive damage scaling
- `Transient`: Heavy single damage + debuff

**Boss:**
- `Awakened One`: Two-phase difficulty scaling
- `TimeEater`: Timed turn cycles

### Special Event Monsters
- `Hexaghost`: Act 1 boss (test event)
- `SlimeBoss`: Alternative Act 1 boss
- `Reptomancer`: Summoner boss with lizard adds

## Monster Behavior Patterns

### Common AI Patterns
1. **Damage Escalation**: Stronger moves at low health
2. **Defensive Cycling**: Block followed by strong attacks
3. **Progressive Powers**: Buffs that stack over time
4. **Rhythm Patterns**: Fixed move sequences
5. **Adaptive AI**: Changes based on player state

### Special Mechanics
- **Spawning**: Some monsters summon allies (Collector, Reptomancer)
- **Synergy**: Multi-enemy coordination (Gremlin Gang, Donu/Deca)
- **Transformations**: State changes (The Guardian phases)
- **Escape Mechanics**: Some monsters can flee (Threat System)
- **Status Interactions**: Exploit specific player conditions

### Difficulty Scaling
- **Ascension Effects**: Enhanced monster stats/behaviors
- **Act-based Scaling**: Higher health/damage in later acts
- **Elite Buffs**: Elites receive flat stat increases
- **Boss Strength**: Major health/damage pools

---

# 🏺 Relic System
**Status: ✅ Complete**

## AbstractRelic
*Base class for all relics and artifacts*

### Core Properties
- `name`: String - Relic name
- `tier`: RelicTier - COMMON, UNCOMMON, RARE, SPECIAL, BOSS, STARTER
- `img`: Texture - Relic image texture
- `largeImg`: Texture - Large version for tooltip
- `outlineImg`: Texture - Outline for special effects
- `flipped`: boolean - Whether image is horizontally flipped
- `counter`: int - Optional counter for tracking uses/duration
- `usedUp`: boolean - Whether relic is consumed after use
- `pulse`: boolean - Whether relic is pulsing (active effect)

### Key Methods
```java
// Abstract methods (must implement)
public abstract AbstractRelic makeCopy()

// Lifecycle hooks
public void onEquip()                          // When first equipped
public void onUnequip()                        // When removed
public void instantObtain(AbstractPlayer p, int slot, boolean callOnEquip)
public void obtain()                           // Standard obtain sequence

// Combat phases
public void atPreBattle()                      // Before combat starts
public void atBattleStart()                    // When combat begins
public void atBattleStartPreDraw()             // Before initial draw
public void atTurnStart()                      // At start of turn
public void atTurnStartPostDraw()              // After turn start draw
public void onPlayerEndTurn()                  // At end of player turn
public void onVictory()                        // When combat is won

// Card interactions
public void onPlayCard(AbstractCard card, AbstractMonster m)
public void onUseCard(AbstractCard card, UseCardAction action)
public void onCardDraw(AbstractCard card)     // When card is drawn
public void onObtainCard(AbstractCard card)   // When card obtained
public void onExhaust(AbstractCard card)       // When card exhausted
public void onManualDiscard()                  // When manually discarded

// Combat interactions
public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target)
public int onAttacked(DamageInfo info, int damageAmount)
public int onAttackedToChangeDamage(DamageInfo info, int damageAmount)
public int onAttackToChangeDamage(DamageInfo info, int damageAmount)
public int onPlayerGainBlock(int blockAmount)
public int onPlayerHeal(int healAmount)

// Economic interactions
public void onGainGold()                       // When gold gained
public void onLoseGold()                       // When gold lost
public void onSpendGold()                      // When gold spent
public int getPrice()                          // Get sell price

// Room transitions
public void onEnterRoom(AbstractRoom room)     // When entering new room
public void justEnteredRoom(AbstractRoom room) // Immediately after enter
public void onEnterRestRoom()                  // When entering rest room
public void onRest()                           // When resting at campfire

// Orb interactions
public void onEvokeOrb(AbstractOrb orb)        // When orb is evoked

// Energy and powers
public void onEnergyRecharge()                 // When energy recharges
public void onChangeStance(AbstractStance newStance, AbstractStance prevStance)

// Health interactions
public void onLoseHp(int amount)               // When HP lost
public void onNotBloodied()                    // When below 50% HP ends
public void onBloodied()                       // When HP drops below 50%
public void onBlockBroken(AbstractCreature target) // When block broken

// Monster interactions
public void onSpawnMonster(AbstractMonster m)  // When monster spawns
public void onMonsterDeath(AbstractMonster m)  // When monster dies

// Campfire interactions
public void addCampfireOption(ArrayList<AbstractCampfireOption> options)
public boolean canUseCampfireOption(AbstractCampfireOption option)
public void onRest()                           // When using rest option
public void onSmith()                          // When using smith option
public void onRitual()                         // When using ritual option
public void onMeditate()                       // When using meditate option

// Chest and rewards
public void onChestOpen(boolean bossChest)     // When chest opened
public void onChestOpenAfter(boolean bossChest) // After chest loot

// Deck modifications
public void onMasterDeckChange()               // When deck composition changes
public void onShuffle()                        // When deck is shuffled
public void onPreviewObtainCard(AbstractCard card) // Preview during reward

// Card reward modifications
public int changeNumberOfCardsInReward(int numCards)
public int changeRareCardRewardChance(int chance)
public int changeUncommonCardRewardChance(int chance)

// Damage modifications
public float atDamageModify(float damage, AbstractCard card)

// Utility methods
public void setCounter(int counter)            // Set counter value
public void usedUp()                           // Mark as consumed
public void spawn(float x, float y)            // Spawn at location
public int getColumn()                         // Get grid column position

// Visual effects
public void flash()                            // Flash effect
public void beginPulse()                       // Start pulsing
public void beginLongPulse()                   // Start long pulse
public void stopPulse()                        // Stop pulsing
public void playLandingSFX()                   // Play landing sound

// Rendering methods
public void render(SpriteBatch sb)             // Main render
public void renderCounter(SpriteBatch sb, boolean inTopPanel)
public void renderTip(SpriteBatch sb)          // Tooltip rendering
public void renderBossTip(SpriteBatch sb)      // Boss relic tooltip
public void renderInTopPanel(SpriteBatch sb)   // In-relic bar rendering
```

## Relic Tiers and Distribution

### Tier Classifications
- **STARTER**: Beginning relics, 1 per character
  - Example: `CrackedCore` (1 energy), `BurningBlood` (heal after combat)

- **COMMON**: Frequent drops, most plentiful
  - Examples: `Anchor`, `BagOfMarbles`, `BloodVial`

- **UNCOMMON**: Less common, more powerful effects
  - Examples: `Akabeko`, `Calipers`, `ChampionBelt`

- **RARE**: Powerful but uncommon drops
  - Examples: `AncientTeaSet`, `ChemicalX`, `ChampionBelt`

- **BOSS**: Guaranteedd boss rewards (1 per boss)
  - Examples: `Astrolabe`, `BottledFlame`, `Ectoplasm`

- **SPECIAL**: Unique event/unlockable relics
  - Examples: `BlueCandle`, `CurseKey`, `GoldenIdol`

### Relic Pool Management
- `commonRelicPool`: Available common relics
- `uncommonRelicPool`: Available uncommon relics
- `rareRelicPool`: Available rare relics
- `bossRelicPool`: Available boss relics
- `shopRelics`: Current shop inventory

## Relic Trigger Categories

### Combat Triggers
- **Start of Combat**: Setup effects (energy, card draw)
- **Turn Start**: Ongoing effects (passive bonuses)
- **Turn End**: Cleanup/accumulation effects
- **Victory**: Post-combat effects (healing, transforms)

### Card Interaction Triggers
- **On Play**: Modify played cards (damage, cost)
- **On Draw**: Effects when cards are drawn
- **On Exhaust**: Trigger when cards exhausted
- **Cost Modification**: Alter energy costs

### Damage/Health Triggers
- **On Attack**: Modify outgoing damage
- **When Attacked**: Modify incoming damage
- **On Heal**: Modify healing amounts
- **HP Thresholds**: Bloodied/not-bloodied states

### Economic Triggers
- **Gold Events**: Gaining, losing, spending gold
- **Shop Interactions**: Price modifications
- **Reward Modifications**: Card and relic rewards

### Utility Triggers
- **Room Transitions**: Entry/exit effects
- **Campfire Options**: Additional resting choices
- **Chest Opening**: Special chest interactions

### Special Category Relics

#### Card Transformation Relics
- `BottledFlame`: Convert skill to attack
- `BottledLightning`: Convert power to attack
- `BottledTornado`: Convert attack to skill

#### Evolvable Relics
- `Omamori`: Gain curse relic slots, then transform
- `WhiteBeast`: Transform on boss relic acquisition
- `FrozenCore`: Transform on condition

#### Conditional Relics
- `MarkOfPain`: Remove damage-dealing powers
- `WingBoots`: Escaping penalty prevention
- `VelvetChoker`: First transform per combat

## Notable Relic Examples

### Combat Staples
- `Pendant`: Extra energy on even-numbered turns
- `RingOfTheSnake`: Card draw each turn
- `BagOfPreparation`: Start with 2 additional cards

### Synergy Engines
- `FrozenEye`: Draw-cost reduction
- `PrayerWheel`: Orb channeling effects
- `Bullfight`: Bonus damage from block

### Special Mechanics
- `OddlySmoothStone`: Turn-based energy boost
- `Toolbox`: Card transformation options
- `Sozu`: Removes potions for energy

## Modding Integration

### Custom Relic Creation
```java
public class CustomRelic extends AbstractRelic {
    public CustomRelic() {
        super("CustomRelic", "custom_relic.png", RelicTier.COMMON, LandingSound.MAGICAL);
    }

    @Override
    public void atBattleStart() {
        // Setup logic
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        // Card interaction logic
    }

    @Override
    public AbstractRelic makeCopy() {
        return new CustomRelic();
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0]; // From localization
    }
}
```

### Registration with BaseMod
```java
BaseMod.registerRelic(new CustomRelic(), false); // false = not boss relic
```

---

# 📋 Power System
**Status: 📋 Todo**

## AbstractPower
*Base class for all status effects and powers*

### Components to Document
- Power application and removal
- Turn-based timing effects
- Stacking and duration management
- Power icon visualization
- Combat vs non-combat powers

---

# 📋 Other Game Systems
**Status: 📋 Todo**

## Orbs System (Defect)
*Orb management and channeling*

## Potion System
*Consumable items management*

## Map System
*Dungeon navigation and progress*

## Event System
*Random encounters and choices*

## Achievement System
*Progress tracking and unlocks*

## Tip System
*Helpful hints and tutorials*

## Helper Classes
*Common utilities and helpers*

---

## Development Notes

### Important Conventions
- Use proper null checking for game state access
- Follow established naming patterns for custom content
- Implement proper cleanup for custom effects
- Use the provided localization system for text

### Common Patterns
- Static factory methods for content creation
- Abstract base classes for extensibility
- Observer pattern for game events
- Singleton pattern for global state

### Modding Integration Points
- `BaseMod.subscribe()` for event handling
- `SpireInsertPatch` and `SpirePatch` for game modification
- Custom content registration methods
- Localization key management