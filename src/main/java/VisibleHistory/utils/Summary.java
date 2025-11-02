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

    // 核心数据结构：怪物 -> (角色 -> 被该怪物击败的次数)
    // 例如："史莱姆" -> {"铁clad": 5, "静默猎手": 3}
    public static Map<String, Map<String, Integer>> monsterDefeatStats = new HashMap<>();

    // 入口方法：加载数据并统计
    public static void load() {
        loadRunData();               // 加载本地对局数据
        calculateMonsterDefeatStats();  // 按怪物为键统计失败次数
    }

    // 加载本地存储的对局数据（逻辑不变）
    private static void loadRunData() {
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

    // 统计失败对局：以怪物为键，记录每个角色被该怪物击败的次数
    private static void calculateMonsterDefeatStats() {
        // 筛选所有失败的对局
        List<RunData> failedRuns = runs.stream()
                .filter(run -> !run.victory)
                .collect(Collectors.toList());

        // 遍历失败对局，按「怪物→角色」层级累计次数
        for (RunData run : failedRuns) {
            String monster = Optional.ofNullable(run.killed_by).orElse("未知怪物"); // 外层键：怪物
            String character = run.character_chosen; // 内层键：角色

            // 1. 初始化怪物对应的Map（如果不存在）
            monsterDefeatStats.putIfAbsent(monster, new HashMap<>());
            Map<String, Integer> characterCounts = monsterDefeatStats.get(monster);

            // 2. 累加该角色被当前怪物击败的次数
            characterCounts.put(character, characterCounts.getOrDefault(character, 0) + 1);
        }

        // 打印排序后的统计结果（按怪物分组，方便查询）
        printSortedMonsterDefeatStats();
    }

    // 排序并打印统计结果（按怪物分组，每个怪物下的角色按死亡次数降序）
    private static void printSortedMonsterDefeatStats() {
        Hpr.info("===== 怪物击败角色统计（按怪物分组） =====");

        // 外层：按怪物名称字母顺序排序（方便按怪物名查询）
        monsterDefeatStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(monsterEntry -> {
                    String monster = monsterEntry.getKey();
                    Hpr.info("怪物: " + monster);

                    // 内层：按角色被该怪物击败的次数降序排序（次数相同则按角色名排序）
                    monsterEntry.getValue().entrySet().stream()
                            .sorted((char1, char2) -> {
                                int countCompare = Integer.compare(char2.getValue(), char1.getValue());
                                if (countCompare != 0) {
                                    return countCompare;
                                }
                                return char1.getKey().compareTo(char2.getKey());
                            })
                            .forEach(charEntry -> {
                                Hpr.info("  角色[" + charEntry.getKey() + "]被击败: " + charEntry.getValue() + "次");
                            });
                    Hpr.info(""); // 怪物间空行分隔，增强可读性
                });
    }
}