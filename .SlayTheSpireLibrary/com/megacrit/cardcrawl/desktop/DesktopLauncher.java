/*     */ package com.megacrit.cardcrawl.desktop;
/*     */ 
/*     */ import com.badlogic.gdx.ApplicationListener;
/*     */ import com.badlogic.gdx.Files;
/*     */ import com.badlogic.gdx.Gdx;
/*     */ import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
/*     */ import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
/*     */ import com.badlogic.gdx.utils.GdxNativesLoader;
/*     */ import com.badlogic.gdx.utils.SharedLibraryLoader;
/*     */ import com.megacrit.cardcrawl.core.CardCrawlGame;
/*     */ import com.megacrit.cardcrawl.core.DisplayConfig;
/*     */ import com.megacrit.cardcrawl.core.ExceptionHandler;
/*     */ import com.megacrit.cardcrawl.core.SystemStats;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.nio.charset.Charset;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.Paths;
/*     */ import java.nio.file.attribute.FileAttribute;
/*     */ import java.util.Locale;
/*     */ import org.apache.logging.log4j.LogManager;
/*     */ import org.apache.logging.log4j.Logger;
/*     */ 
/*     */ public class DesktopLauncher
/*     */ {
/*     */   static {
/*  28 */     System.setProperty("log4j.configurationFile", "log4j2.xml");
/*  29 */     OverrideLibraryLoadingFix();
/*     */   }
/*  31 */   private static final Logger logger = LogManager.getLogger(DesktopLauncher.class.getName());
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void OverrideLibraryLoadingFix() {
/*  37 */     if (!System.getProperty("os.name").toLowerCase().contains("windows"))
/*  38 */       return;  Path normalPath = Paths.get(System.getProperty("java.io.tmpdir"), new String[] { "libgdx", System.getProperty("user.name") });
/*     */     
/*  40 */     if (Is7bitAscii(normalPath.toAbsolutePath().toString()))
/*  41 */       return;  System.out.println("Detected invalid path: " + normalPath);
/*     */ 
/*     */     
/*  44 */     GdxNativesLoader.disableNativesLoading = true;
/*  45 */     Path lib = Paths.get(System.getProperty("user.dir"), new String[] { "lib" }).toAbsolutePath();
/*  46 */     System.out.println("Loading libs extracted to custom path: " + lib);
/*     */ 
/*     */     
/*  49 */     System.setProperty("org.lwjgl.librarypath", lib.toString());
/*  50 */     System.setProperty("com.codedisaster.steamworks.SharedLibraryExtractPath", lib.toString());
/*  51 */     System.setProperty("com.codedisaster.steamworks.SDKLibraryPath", lib.toString());
/*     */ 
/*     */     
/*     */     try {
/*  55 */       Files.createDirectories(lib, (FileAttribute<?>[])new FileAttribute[0]);
/*     */ 
/*     */       
/*  58 */       SharedLibraryLoader loader = new SharedLibraryLoader();
/*     */       
/*  60 */       loader.extractFileTo(SharedLibraryLoader.is64Bit ? "gdx64.dll" : "gdx.dll", new File(lib.toString()));
/*  61 */       loader.extractFileTo(SharedLibraryLoader.is64Bit ? "lwjgl64.dll" : "lwjgl.dll", new File(lib.toString()));
/*  62 */       if (!LwjglApplicationConfiguration.disableAudio)
/*  63 */         loader.extractFileTo(SharedLibraryLoader.is64Bit ? "OpenAL64.dll" : "OpenAL32.dll", new File(lib.toString())); 
/*  64 */     } catch (IOException e) {
/*  65 */       logger.info("Exception occurred while initializing application!");
/*  66 */       ExceptionHandler.handleException(e, logger);
/*  67 */       Gdx.app.exit();
/*     */     } 
/*     */ 
/*     */     
/*  71 */     System.load(Paths.get(lib.toString(), new String[] { "gdx64.dll" }).toAbsolutePath().toString());
/*     */   }
/*     */   
/*     */   public static boolean Is7bitAscii(String str) {
/*  75 */     for (char c : str.toCharArray()) {
/*  76 */       if (c > '') return false; 
/*     */     } 
/*  78 */     return true;
/*     */   }
/*     */   
/*     */   public static void main(String[] arg) {
/*  82 */     logger.info("time: " + System.currentTimeMillis());
/*  83 */     logger.info("version: " + CardCrawlGame.TRUE_VERSION_NUM);
/*  84 */     logger.info("libgdx:  1.9.5");
/*  85 */     logger.info("default_locale: " + Locale.getDefault());
/*  86 */     logger.info("default_charset: " + Charset.defaultCharset());
/*  87 */     logger.info("default_encoding: " + System.getProperty("file.encoding"));
/*  88 */     logger.info("java_version: " + System.getProperty("java.version"));
/*  89 */     logger.info("os_arch: " + System.getProperty("os.arch"));
/*  90 */     logger.info("os_name: " + System.getProperty("os.name"));
/*  91 */     logger.info("os_version: " + System.getProperty("os.version"));
/*  92 */     SystemStats.logMemoryStats();
/*  93 */     SystemStats.logDiskStats();
/*     */ 
/*     */     
/*     */     try {
/*  97 */       LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
/*  98 */       config.setDisplayModeCallback = new STSDisplayModeCallback();
/*     */       
/* 100 */       config.addIcon("images/ui/icon.png", Files.FileType.Internal);
/* 101 */       config.resizable = false;
/* 102 */       config.title = "Slay the Spire";
/* 103 */       loadSettings(config);
/* 104 */       logger.info("Launching application...");
/* 105 */       new LwjglApplication((ApplicationListener)new CardCrawlGame(config.preferencesDirectory), config);
/* 106 */     } catch (Exception e) {
/* 107 */       logger.info("Exception occurred while initializing application!");
/* 108 */       ExceptionHandler.handleException(e, logger);
/* 109 */       Gdx.app.exit();
/*     */     } 
/*     */   }
/*     */   
/*     */   private static void loadSettings(LwjglApplicationConfiguration config) {
/* 114 */     DisplayConfig displayConf = DisplayConfig.readConfig();
/*     */ 
/*     */     
/* 117 */     if (displayConf.getWidth() < 800 || displayConf.getHeight() < 450) {
/* 118 */       logger.info("[ERROR] Display Config set lower than minimum allowed, resetting config.");
/* 119 */       config.width = 1280;
/* 120 */       config.height = 720;
/* 121 */       DisplayConfig.writeDisplayConfigFile(1280, 720, displayConf
/*     */ 
/*     */           
/* 124 */           .getMaxFPS(), displayConf
/* 125 */           .getIsFullscreen(), displayConf
/* 126 */           .getWFS(), displayConf
/* 127 */           .getIsVsync());
/*     */     } else {
/* 129 */       config.height = displayConf.getHeight();
/* 130 */       config.width = displayConf.getWidth();
/*     */     } 
/*     */     
/* 133 */     config.foregroundFPS = displayConf.getMaxFPS();
/* 134 */     config.backgroundFPS = config.foregroundFPS;
/*     */     
/* 136 */     if (displayConf.getIsFullscreen()) {
/* 137 */       logger.info("[FULLSCREEN_MODE]");
/* 138 */       System.setProperty("org.lwjgl.opengl.Display.enableOSXFullscreenModeAPI", "true");
/* 139 */       System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
/* 140 */       config.fullscreen = true;
/* 141 */       config.height = displayConf.getHeight();
/* 142 */       config.width = displayConf.getWidth();
/* 143 */       logger.info("Running the game in: " + config.width + " x " + config.height);
/*     */     } else {
/* 145 */       config.fullscreen = false;
/*     */ 
/*     */       
/* 148 */       if (displayConf.getWFS() && config.width == (LwjglApplicationConfiguration.getDesktopDisplayMode()).width && config.height == 
/* 149 */         (LwjglApplicationConfiguration.getDesktopDisplayMode()).height) {
/* 150 */         logger.info("[BORDERLESS_FULLSCREEN_MODE]");
/* 151 */         System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
/* 152 */         config.width = (LwjglApplicationConfiguration.getDesktopDisplayMode()).width;
/* 153 */         config.height = (LwjglApplicationConfiguration.getDesktopDisplayMode()).height;
/* 154 */         logger.info("Running the game in: " + config.width + " x " + config.height);
/*     */       } else {
/* 156 */         logger.info("[WINDOWED_MODE]");
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 163 */     if (config.fullscreen && (displayConf.getWidth() > (LwjglApplicationConfiguration.getDesktopDisplayMode()).width || displayConf
/* 164 */       .getHeight() > (LwjglApplicationConfiguration.getDesktopDisplayMode()).height)) {
/* 165 */       logger.info("[ERROR] Monitor resolution is lower than config, resetting config.");
/* 166 */       config.width = (LwjglApplicationConfiguration.getDesktopDisplayMode()).width;
/* 167 */       config.height = (LwjglApplicationConfiguration.getDesktopDisplayMode()).height;
/* 168 */       DisplayConfig.writeDisplayConfigFile(config.width, config.height, displayConf
/*     */ 
/*     */           
/* 171 */           .getMaxFPS(), false, false, displayConf
/*     */ 
/*     */           
/* 174 */           .getIsVsync());
/*     */     } 
/*     */ 
/*     */     
/* 178 */     config.vSyncEnabled = displayConf.getIsVsync();
/* 179 */     logger.info("Settings successfully loaded");
/*     */   }
/*     */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\desktop\DesktopLauncher.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */