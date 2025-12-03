package xyz.iwolfking.unobtainium.fixes;


import com.mojang.blaze3d.systems.RenderSystem;

public class TransmogRenderUtils {
    private static int renderCount = 0;
    private static int inventoryCount = 0;
    private static int inventoryExcludedCount = 0;

    public static void enterRenderClass() {
        renderCount++;
    }

    public static void exitRenderClass() {
        renderCount--;
    }

    public static void enterInventoryClass() {
        inventoryCount++;
    }

    public static void exitInventoryClass() {
        inventoryCount--;
    }

    public static void enterInventoryExcludedClass() {
        inventoryExcludedCount++;
    }

    public static void exitInventoryExcludedClass() {
        inventoryExcludedCount--;
    }

    public static boolean isCalledForRendering() {
        return RenderSystem.isOnRenderThread() && renderCount > 0;
    }

    public static boolean isCalledForInventory() {
        return RenderSystem.isOnRenderThread() && inventoryCount > 0 && inventoryExcludedCount == 0;
    }

}