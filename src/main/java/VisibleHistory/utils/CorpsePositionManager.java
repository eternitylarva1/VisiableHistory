package VisibleHistory.utils;

import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import VisibleHistory.playerdeath.DeadPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 尸体位置分配管理器
 * 智能分配尸体位置，避免重叠，充分利用左侧和右侧空间
 */
public class CorpsePositionManager {
    private static CorpsePositionManager instance;

    // 位置配置
    private static final float CORPSE_WIDTH = 120.0f * Settings.scale;
    private static final float CORPSE_HEIGHT = 150.0f * Settings.scale;
    private static final float MIN_SPACING_X = 20.0f * Settings.scale;
    private static final float MIN_SPACING_Y = 15.0f * Settings.scale;

    // 区域配置
    private static final float LEFT_AREA_X = -300.0f * Settings.scale;  // 玩家左侧区域
    private static final float RIGHT_AREA_X = 150.0f * Settings.scale;  // 玩家右侧区域
    private static final float GROUND_Y = -80.0f * Settings.scale;      // 地面基准高度
    private static final float MAX_ROWS = 3;                             // 最多3层

    // 已分配的位置映射（尸体ID -> 位置）
    private Map<String, Vector2> allocatedPositions;

    // 位置使用状态（检查某位置是否被占用）
    private ArrayList<Vector2> usedPositions;

    // 当前分配的起始位置（为了美观，从左到右分配）
    private Vector2 currentAllocationStart;

    private CorpsePositionManager() {
        reset();
    }

    public static CorpsePositionManager getInstance() {
        if (instance == null) {
            instance = new CorpsePositionManager();
        }
        return instance;
    }

    /**
     * 重置位置管理器（新战斗开始时调用）
     */
    public void reset() {
        allocatedPositions = new HashMap<>();
        usedPositions = new ArrayList<>();
        currentAllocationStart = new Vector2(LEFT_AREA_X, GROUND_Y);
    }

    /**
     * 为尸体分配一个位置
     */
    public Vector2 allocatePosition(String corpseId) {
        if (corpseId == null) {
            corpseId = String.valueOf(System.currentTimeMillis());
        }

        // 如果已经分配过位置，返回原位置
        if (allocatedPositions.containsKey(corpseId)) {
            return allocatedPositions.get(corpseId);
        }

        // 计算新位置
        Vector2 newPosition = findNextAvailablePosition();

        // 保存位置
        allocatedPositions.put(corpseId, newPosition);
        usedPositions.add(newPosition.cpy());

        return newPosition;
    }

    /**
     * 找到下一个可用位置
     */
    private Vector2 findNextAvailablePosition() {
        Vector2 position;
        int attempts = 0;
        final int maxAttempts = 50; // 最多尝试50次

        do {
            // 在有效范围内随机分配位置：保留屏幕边缘1/10的空间
            float minX = Settings.WIDTH * 0.1f;  // 左边1/10
            float maxX = Settings.WIDTH * 0.9f - CORPSE_WIDTH;  // 右边1/10，减去尸体宽度
            float minY = Settings.HEIGHT * 0.1f;  // 上边1/10
            float maxY = Settings.HEIGHT * 0.9f - CORPSE_HEIGHT;  // 下边1/10，减去尸体高度

            float x = minX + AbstractDungeon.cardRandomRng.random(maxX - minX);
            float y = minY + AbstractDungeon.cardRandomRng.random(maxY - minY);
            position = new Vector2(x, y);
            attempts++;
        } while (isPositionOccupied(position) && attempts < maxAttempts);

        return position;
    }

    /**
     * 在指定X坐标区域找位置
     */
    private Vector2 findPositionInArea(float areaStartX) {
        for (int row = 0; row < MAX_ROWS; row++) {
            float y = GROUND_Y + row * (CORPSE_HEIGHT + MIN_SPACING_Y);

            for (int col = 0; col < 10; col++) {  // 最多10列
                float x = areaStartX + col * (CORPSE_WIDTH + MIN_SPACING_X);
                Vector2 testPos = new Vector2(x, y);

                if (!isPositionOccupied(testPos)) {
                    return testPos;
                }
            }
        }
        return null;
    }

    /**
     * 创建新行的位置
     */
    private Vector2 createNewRowPosition() {
        // 如果当前行是最后一行，回到左侧开始
        int currentRow = (int)((currentAllocationStart.y - GROUND_Y) / (CORPSE_HEIGHT + MIN_SPACING_Y));
        int nextRow = (currentRow + 1) % (int)MAX_ROWS;

        float newY = GROUND_Y + nextRow * (CORPSE_HEIGHT + MIN_SPACING_Y);
        currentAllocationStart.set(LEFT_AREA_X, newY);

        return new Vector2(LEFT_AREA_X, newY);
    }

    /**
     * 检查位置是否被占用
     */
    private boolean isPositionOccupied(Vector2 position) {
        for (Vector2 usedPos : usedPositions) {
            if (usedPos.dst(position) < MIN_SPACING_X) {
                return true;
            }
        }
        return false;
    }

    /**
     * 释放尸体位置（尸体消失时调用）
     */
    public void releasePosition(String corpseId) {
        if (allocatedPositions.containsKey(corpseId)) {
            Vector2 position = allocatedPositions.get(corpseId);
            usedPositions.removeIf(pos -> pos.dst(position) < 1.0f);
            allocatedPositions.remove(corpseId);
        }
    }

    /**
     * 获取已分配的位置
     */
    public Vector2 getPosition(String corpseId) {
        return allocatedPositions.get(corpseId);
    }

    /**
     * 检查位置管理器是否已重置
     */
    public boolean isEmpty() {
        return allocatedPositions.isEmpty();
    }

    /**
     * 获取当前已使用的尸体数量
     */
    public int getUsedCount() {
        return allocatedPositions.size();
    }

    /**
     * 手动刷新所有位置（房间转换时调用）
     */
    public void refreshPositions() {
        // 记录当前尸体数量
        int corpseCount = allocatedPositions.size();

        if (corpseCount > 0) {
            // 保存所有尸体ID
            ArrayList<String> corpseIds = new ArrayList<>(allocatedPositions.keySet());

            // 重置管理器
            reset();

            // 重新分配位置（从左到右重新排列）
            Vector2[] newPositions = new Vector2[corpseIds.size()];
            for (int i = 0; i < corpseIds.size(); i++) {
                String id = corpseIds.get(i);
                Vector2 newPos = allocatePosition(id);
                newPositions[i] = newPos;
            }

            // 更新真实尸体的位置
            updateDeadPlayerPositions(corpseIds, newPositions);
        }
    }

    /**
     * 更新真实尸体的位置
     */
    private void updateDeadPlayerPositions(ArrayList<String> corpseIds, Vector2[] newPositions) {
        for (int i = 0; i < corpseIds.size(); i++) {
            String id = corpseIds.get(i);
            Vector2 newPos = newPositions[i];

            // 找到对应的尸体并更新位置
            for (DeadPlayer corpse : DeadPlayer.deadPlayers) {
                if (id.equals(corpse.getCorpseId())) {
                    corpse.setPosition(newPos.x, newPos.y);
                    break;
                }
            }
        }
    }
}