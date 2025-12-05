/*     */ package com.megacrit.cardcrawl.integrations;
/*     */ 
/*     */ import com.badlogic.gdx.Gdx;
/*     */ import com.badlogic.gdx.Net;
/*     */ import com.badlogic.gdx.net.HttpRequestBuilder;
/*     */ import com.google.gson.Gson;
/*     */ import com.megacrit.cardcrawl.characters.AbstractPlayer;
/*     */ import com.megacrit.cardcrawl.core.CardCrawlGame;
/*     */ import java.io.Reader;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.Paths;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.logging.log4j.LogManager;
/*     */ import org.apache.logging.log4j.Logger;
/*     */ 
/*     */ 
/*     */ public class SteelSeries
/*     */ {
/*  23 */   private final Logger logger = LogManager.getLogger(SteelSeries.class.getName());
/*  24 */   private final String gameName = "SLAY_THE_SPIRE";
/*     */   public Boolean isEnabled;
/*     */   private String url;
/*  27 */   private long timeAtLastHealthcheck = 0L;
/*     */   
/*     */   public SteelSeries() {
/*  30 */     String program_data = System.getenv("PROGRAMDATA");
/*  31 */     Path winPath = Paths.get(program_data + "/SteelSeries/SteelSeries Engine 3/coreProps.json", new String[0]);
/*  32 */     Path macPath = Paths.get("/Library/Application Support/SteelSeries Engine 3/coreProps.json", new String[0]);
/*  33 */     Boolean winExists = Boolean.valueOf(Files.exists(winPath, new java.nio.file.LinkOption[0]));
/*  34 */     Boolean macExists = Boolean.valueOf(Files.exists(macPath, new java.nio.file.LinkOption[0]));
/*  35 */     this.isEnabled = Boolean.valueOf((winExists.booleanValue() || macExists.booleanValue()));
/*  36 */     this.logger.info("enabled=" + this.isEnabled);
/*  37 */     if (!this.isEnabled.booleanValue())
/*     */       return; 
/*  39 */     String _url = winExists.booleanValue() ? getUrl(winPath) : getUrl(macPath);
/*  40 */     if (_url != null) {
/*  41 */       this.url = "http://" + _url;
/*     */     } else {
/*  43 */       this.logger.info("ERROR: url is null!");
/*     */     } 
/*  45 */     register();
/*     */   }
/*     */   
/*     */   private String getUrl(Path path) {
/*  49 */     Gson gson = new Gson();
/*     */     try {
/*  51 */       Reader reader = Files.newBufferedReader(path);
/*  52 */       Map<?, ?> map = (Map<?, ?>)gson.fromJson(reader, Map.class);
/*  53 */       reader.close();
/*  54 */       return (String)map.get("address");
/*  55 */     } catch (Exception e) {
/*  56 */       e.printStackTrace();
/*  57 */       this.isEnabled = Boolean.valueOf(false);
/*     */       
/*  59 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   public void update() {
/*  64 */     if (System.currentTimeMillis() - this.timeAtLastHealthcheck > 14000L) {
/*  65 */       doHealthCheck();
/*  66 */       this.timeAtLastHealthcheck = System.currentTimeMillis();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void doHealthCheck() {
/*  75 */     if (!this.isEnabled.booleanValue())
/*     */       return; 
/*  77 */     Map<String, Object> data = new HashMap<>();
/*  78 */     data.put("game", "SLAY_THE_SPIRE");
/*  79 */     sendPost(this.url + "/game_heartbeat", data, new Net.HttpResponseListener()
/*     */         {
/*     */           public void handleHttpResponse(Net.HttpResponse httpResponse) {}
/*     */ 
/*     */           
/*     */           public void failed(Throwable t) {
/*  85 */             SteelSeries.this.logger.info("Healthcheck failed.");
/*  86 */             SteelSeries.this.isEnabled = Boolean.valueOf(false);
/*     */           }
/*     */ 
/*     */           
/*     */           public void cancelled() {}
/*     */         });
/*     */   }
/*     */   
/*     */   private void register() {
/*  95 */     if (!this.isEnabled.booleanValue())
/*     */       return; 
/*  97 */     Map<String, Object> data = new HashMap<>();
/*  98 */     data.put("game", "SLAY_THE_SPIRE");
/*  99 */     data.put("game_display_name", "Slay the Spire");
/* 100 */     data.put("developer", "MEGACRIT");
/* 101 */     sendPost(this.url + "/game_metadata", data, new Net.HttpResponseListener()
/*     */         {
/*     */           public void handleHttpResponse(Net.HttpResponse httpResponse) {
/* 104 */             SteelSeries.this.create_event_handler();
/*     */           }
/*     */ 
/*     */           
/*     */           public void failed(Throwable t) {
/* 109 */             SteelSeries.this.logger.info("Steel Series service not running.");
/* 110 */             SteelSeries.this.isEnabled = Boolean.valueOf(false);
/*     */           }
/*     */ 
/*     */           
/*     */           public void cancelled() {
/* 115 */             SteelSeries.this.logger.info("http request cancelled.");
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private Map<String, Object> create_event_map(AbstractPlayer.PlayerClass character, Map<String, Integer> color) {
/* 121 */     Map<String, Object> c1 = new HashMap<>();
/* 122 */     c1.put("red", Integer.valueOf(0));
/* 123 */     c1.put("green", Integer.valueOf(0));
/* 124 */     c1.put("blue", Integer.valueOf(0));
/* 125 */     Map<String, Object> gradient = new HashMap<>();
/* 126 */     gradient.put("zero", c1);
/* 127 */     gradient.put("hundred", color);
/* 128 */     Map<String, Object> colorConfig = new HashMap<>();
/* 129 */     colorConfig.put("gradient", gradient);
/*     */     
/* 131 */     Map<String, Object> keyboardHandler = new HashMap<>();
/* 132 */     keyboardHandler.put("device-type", "keyboard");
/* 133 */     keyboardHandler.put("zone", "all");
/* 134 */     keyboardHandler.put("color", colorConfig);
/* 135 */     keyboardHandler.put("mode", "percent");
/*     */     
/* 137 */     Map<String, Object> mouseHandler = new HashMap<>();
/* 138 */     mouseHandler.put("device-type", "mouse");
/* 139 */     mouseHandler.put("zone", "all");
/* 140 */     mouseHandler.put("color", colorConfig);
/* 141 */     mouseHandler.put("mode", "percent");
/*     */     
/* 143 */     List<Map<String, Object>> handlers = new ArrayList<>();
/* 144 */     handlers.add(keyboardHandler);
/* 145 */     handlers.add(mouseHandler);
/* 146 */     Map<String, Object> data = new HashMap<>();
/* 147 */     data.put("game", "SLAY_THE_SPIRE");
/* 148 */     data.put("event", character.toString());
/* 149 */     data.put("min_value", Integer.valueOf(0));
/* 150 */     data.put("max_value", Integer.valueOf(100));
/* 151 */     data.put("icon_id", Integer.valueOf(0));
/* 152 */     data.put("handlers", handlers);
/* 153 */     return data;
/*     */   }
/*     */   
/*     */   private Net.HttpResponseListener newEventHandlerListener() {
/* 157 */     return new Net.HttpResponseListener()
/*     */       {
/*     */         public void handleHttpResponse(Net.HttpResponse httpResponse) {}
/*     */ 
/*     */         
/*     */         public void failed(Throwable t) {
/* 163 */           SteelSeries.this.logger.info("Steel Series service not running.");
/* 164 */           SteelSeries.this.isEnabled = Boolean.valueOf(false);
/*     */         }
/*     */ 
/*     */         
/*     */         public void cancelled() {
/* 169 */           SteelSeries.this.logger.info("http request cancelled.");
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private void create_event_handler() {
/* 175 */     if (!this.isEnabled.booleanValue()) {
/*     */       return;
/*     */     }
/*     */     
/* 179 */     Map<String, Integer> ironclad_color = new HashMap<>();
/* 180 */     ironclad_color.put("red", Integer.valueOf(255));
/* 181 */     ironclad_color.put("green", Integer.valueOf(0));
/* 182 */     ironclad_color.put("blue", Integer.valueOf(0));
/* 183 */     Map<String, Object> eventMap = create_event_map(AbstractPlayer.PlayerClass.IRONCLAD, ironclad_color);
/* 184 */     sendPost(this.url + "/bind_game_event", eventMap, newEventHandlerListener());
/*     */     
/* 186 */     Map<String, Integer> silent_color = new HashMap<>();
/* 187 */     silent_color.put("red", Integer.valueOf(0));
/* 188 */     silent_color.put("green", Integer.valueOf(255));
/* 189 */     silent_color.put("blue", Integer.valueOf(0));
/* 190 */     eventMap = create_event_map(AbstractPlayer.PlayerClass.THE_SILENT, silent_color);
/* 191 */     sendPost(this.url + "/bind_game_event", eventMap, newEventHandlerListener());
/*     */     
/* 193 */     Map<String, Integer> defect_color = new HashMap<>();
/* 194 */     defect_color.put("red", Integer.valueOf(0));
/* 195 */     defect_color.put("green", Integer.valueOf(0));
/* 196 */     defect_color.put("blue", Integer.valueOf(255));
/* 197 */     eventMap = create_event_map(AbstractPlayer.PlayerClass.DEFECT, defect_color);
/* 198 */     sendPost(this.url + "/bind_game_event", eventMap, newEventHandlerListener());
/*     */     
/* 200 */     Map<String, Integer> watcher_color = new HashMap<>();
/* 201 */     watcher_color.put("red", Integer.valueOf(148));
/* 202 */     watcher_color.put("green", Integer.valueOf(0));
/* 203 */     watcher_color.put("blue", Integer.valueOf(211));
/* 204 */     eventMap = create_event_map(AbstractPlayer.PlayerClass.WATCHER, watcher_color);
/* 205 */     sendPost(this.url + "/bind_game_event", eventMap, newEventHandlerListener());
/*     */   }
/*     */   
/*     */   public void event_character_chosen(AbstractPlayer.PlayerClass character) {
/* 209 */     if (!this.isEnabled.booleanValue())
/*     */       return; 
/* 211 */     Map<String, Object> value = new HashMap<>();
/* 212 */     value.put("value", Integer.valueOf(100));
/* 213 */     Map<String, Object> data = new HashMap<>();
/* 214 */     data.put("game", "SLAY_THE_SPIRE");
/* 215 */     data.put("event", character.toString());
/* 216 */     data.put("data", value);
/* 217 */     sendPost(this.url + "/game_event", data, new Net.HttpResponseListener()
/*     */         {
/*     */           public void handleHttpResponse(Net.HttpResponse httpResponse) {}
/*     */ 
/*     */           
/*     */           public void failed(Throwable t) {
/* 223 */             SteelSeries.this.logger.info("Steel Series service not running.");
/* 224 */             SteelSeries.this.isEnabled = Boolean.valueOf(false);
/*     */           }
/*     */ 
/*     */           
/*     */           public void cancelled() {
/* 229 */             SteelSeries.this.logger.info("http request cancelled.");
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private void sendPost(String url, Map<String, Object> data, Net.HttpResponseListener listener) {
/* 235 */     Gson gson = new Gson();
/* 236 */     String content = gson.toJson(data);
/* 237 */     HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 242 */     Net.HttpRequest httpRequest = requestBuilder.newRequest().method("POST").url(url).header("Content-Type", "application/json").header("Accept", "application/json").header("User-Agent", "sts/" + CardCrawlGame.TRUE_VERSION_NUM).timeout(1).build();
/* 243 */     httpRequest.setContent(content);
/* 244 */     Gdx.net.sendHttpRequest(httpRequest, listener);
/*     */   }
/*     */ }


/* Location:              C:\Users\gaoming\Desktop\杀戮尖塔 mod\desktop-1.0.jar!\com\megacrit\cardcrawl\integrations\SteelSeries.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */