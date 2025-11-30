package VisibleHistory.utils;

import VisibleHistory.utils.Hpr;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.screens.stats.RunData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Summary {
    // 存储所有有效对局数据
    public static ArrayList<RunData> runs = new ArrayList<>();
    // Gson解析工具
    private static final Gson gson = new Gson();

    // 排序选项
    public enum SortMode {
        BY_CHARACTER,  // 按角色分组（当前默认行为）
        BY_TIME        // 按时间排序
    }

    // 当前排序模式（默认按角色分组）
    public static SortMode sortMode = SortMode.BY_CHARACTER;

    // 原有核心结构：怪物 -> (角色 -> 被击败次数)（保留，方便快速查询次数）
    public static Map<String, Map<String, Integer>> monsterDefeatStats = new HashMap<>();

    // 新增：按时间排序的失败对局列表（用于按时间排序模式）
    public static List<RunData> sortedFailedRuns = new ArrayList<>();

    // 新增核心结构：怪物 -> (角色 -> 失败记录（次数+对应RunData列表）)（用于关联对局数据）
    public static Map<String, Map<String, FailureRecord>> monsterDefeatDetails = new HashMap<>();

    // 辅助类：存储某怪物-角色组合的失败次数和对应RunData列表（静态内部类，方便关联）
    public static class FailureRecord {
        public int count; // 失败次数（与monsterDefeatStats同步）
        public List<RunData> runList; // 该组合下的所有失败对局原始数据

        public FailureRecord() {
            this.count = 0;
            this.runList = new ArrayList<>();
        }

        // 新增失败对局时同步更新次数和列表
        public void addRun(RunData run) {
            this.runList.add(run);
            this.count = this.runList.size();
        }
    }

    // 入口方法：加载数据并统计
    public static void load() {
        loadRunData();               // 加载本地对局数据
        calculateMonsterDefeatStats();  // 按怪物为键统计（次数+RunData关联）
        sortFailedRuns();           // 按时间排序失败对局
    }

    // 加载本地存储的对局数据（保留你已加的runs.clear()）
    private static void loadRunData() {
        runs.clear(); // 清空原有数据，避免重复加载
        FileHandle[] folders = Gdx.files.local("runs").list();
        for (FileHandle folder : folders) {
            for (FileHandle file : folder.list()) {
                try {
                    RunData data = gson.fromJson(file.readString(), RunData.class);
                    if (data == null) continue;

                    // 处理时间戳兼容问题
                    if (data.timestamp == null) {
                        data.timestamp = file.nameWithoutExtension();
                        try {
                            long days = Long.parseLong(data.timestamp);
                            data.timestamp = Long.toString(days * 86400L);
                        } catch (NumberFormatException e) {
                            Hpr.info("跳过无效时间戳文件: " + file.path());
                            continue;
                        }
                    }

                    // 验证角色合法性
                    try {
                        AbstractPlayer.PlayerClass.valueOf(data.character_chosen);
                        runs.add(data);
                    } catch (NullPointerException | IllegalArgumentException e) {
                        Hpr.info("跳过无效角色数据: " + data.character_chosen + " (" + file.path() + ")");
                    }
                } catch (JsonSyntaxException e) {
                    Hpr.info("解析文件失败: " + file.path() + " - " + e.getMessage());
                }
            }
        }
    }

    // 统计失败对局：保留原有次数统计，新增RunData关联（保留你已加的monsterDefeatStats.clear()）
    private static void calculateMonsterDefeatStats() {
        monsterDefeatStats.clear(); // 清空原有统计，避免重复
        monsterDefeatDetails.clear(); // 清空RunData关联，避免重复

        // 筛选所有失败的对局
        List<RunData> failedRuns = runs.stream()
                .filter(run -> !run.victory)
                .collect(Collectors.toList());

        // 遍历失败对局，同时更新次数统计和RunData关联
        for (RunData run : failedRuns) {
            String monster = Optional.ofNullable(run.killed_by).orElse("未知怪物"); // 外层键：怪物
            String character = run.character_chosen; // 内层键：角色

            // ---------------------- 原有逻辑：更新次数统计 ----------------------
            monsterDefeatStats.putIfAbsent(monster, new HashMap<>());
            Map<String, Integer> characterCounts = monsterDefeatStats.get(monster);
            characterCounts.put(character, characterCounts.getOrDefault(character, 0) + 1);

            // ---------------------- 新增逻辑：关联RunData ----------------------
            monsterDefeatDetails.putIfAbsent(monster, new HashMap<>());
            Map<String, FailureRecord> characterRecords = monsterDefeatDetails.get(monster);
            // 初始化该角色的失败记录（不存在则创建）
            characterRecords.putIfAbsent(character, new FailureRecord());
            FailureRecord failureRecord = characterRecords.get(character);
            // 关联当前对局的RunData
            failureRecord.addRun(run);
        }

        // 打印排序后的统计结果（保留原有打印，新增RunData数量提示）
        printSortedMonsterDefeatStats();
    }

    // 新增：按时间排序失败对局
    private static void sortFailedRuns() {
        sortedFailedRuns.clear();

        // 筛选所有失败的对局
        List<RunData> failedRuns = runs.stream()
                .filter(run -> !run.victory)
                .collect(Collectors.toList());

        // 按时间戳降序排序（最新的在前）
        sortedFailedRuns = failedRuns.stream()
                .sorted((run1, run2) -> {
                    try {
                        long time1 = Long.parseLong(run1.timestamp);
                        long time2 = Long.parseLong(run2.timestamp);
                        return Long.compare(time2, time1); // 降序：最新的在前
                    } catch (NumberFormatException e) {
                        // 时间戳解析失败时，按文件名字母顺序
                        return run2.timestamp.compareTo(run1.timestamp);
                    }
                })
                .collect(Collectors.toList());

        Hpr.info("按时间排序失败对局完成，共 " + sortedFailedRuns.size() + " 条记录");
    }

    // 新增：获取按时间排序的尸体数据（用于 DeadPlayer 生成）
    /**
     * 获取排序后的尸体数据
     * @return 按当前排序模式返回的尸体数据列表
     */
    public static List<RunData> getSortedCorpseData() {
        if (sortMode == SortMode.BY_TIME) {
            return sortedFailedRuns;
        } else {
            // 按角色分组模式：使用原有逻辑（怪物分组 -> 角色分组）
            return getAllFailedRunsFlat();
        }
    }

    // 辅助方法：将怪物分组的数据平铺为列表（按角色分组模式使用）
    private static List<RunData> getAllFailedRunsFlat() {
        List<RunData> allRuns = new ArrayList<>();
        for (Map<String, FailureRecord> characterRecords : monsterDefeatDetails.values()) {
            for (FailureRecord record : characterRecords.values()) {
                allRuns.addAll(record.runList);
            }
        }
        return allRuns;
    }

    // 排序并打印统计结果（保留你的排序逻辑，补充RunData数量提示）
    private static void printSortedMonsterDefeatStats() {
        Hpr.info("===== 怪物击败角色统计（按怪物分组） =====");

        // 外层：按怪物名称字母顺序排序
        monsterDefeatStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(monsterEntry -> {
                    String monster = monsterEntry.getKey();
                    Hpr.info("怪物: " + monster);

                    // 内层：按角色被击败次数降序排序
                    monsterEntry.getValue().entrySet().stream()
                            .sorted((char1, char2) -> {
                                int countCompare = Integer.compare(char2.getValue(), char1.getValue());
                                if (countCompare != 0) {
                                    return countCompare;
                                }
                                return char1.getKey().compareTo(char2.getKey());
                            })
                            .forEach(charEntry -> {
                                String character = charEntry.getKey();
                                int count = charEntry.getValue();
                                // 补充显示该组合下的RunData数量（方便确认数据关联）
                                int runDataCount = monsterDefeatDetails.get(monster).get(character).runList.size();
                                Hpr.info("  角色[" + character + "]被击败: " + count + "次（关联" + runDataCount + "个对局数据）");
                            });
                    Hpr.info(""); // 怪物间空行分隔
                });
    }

    // ------------------- 新增：便捷查询工具方法（生成尸体时直接用） -------------------
    /**
     * 快速查询：某怪物击败某角色的所有失败对局RunData
     * @param monster 怪物名称（如"史莱姆"）
     * @param character 角色名称（如"IRONCLAD"）
     * @return 失败对局的RunData列表（无结果返回空列表，避免空指针）
     */
    public static List<RunData> getFailedRuns(String monster, String character) {
        return Optional.ofNullable(monsterDefeatDetails.get(monster))
                .map(records -> records.get(character))
                .map(record -> record.runList)
                .orElse(new ArrayList<>());
    }

    /**
     * 快速查询：某怪物击败的所有角色及对应失败记录（含RunData）
     * @param monster 怪物名称
     * @return 角色→失败记录的映射（无结果返回空Map）
     */
    public static Map<String, FailureRecord> getCharacterFailureRecords(String monster) {
        return monsterDefeatDetails.getOrDefault(monster, new HashMap<>());
    }

    // 新增：切换排序模式
    /**
     * 切换排序模式并重新加载数据
     * @param newMode 新的排序模式
     */
    public static void setSortMode(SortMode newMode) {
        if (sortMode != newMode) {
            sortMode = newMode;
            Hpr.info("排序模式已切换为: " + (newMode == SortMode.BY_TIME ? "按时间排序" : "按角色分组"));
        }
    }
}