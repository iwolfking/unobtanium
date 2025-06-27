package xyz.iwolfking.unobtainium.fixes;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class FixIronFurnacesLag { // backported caches from iron furnaces 4.1.6
    public static final Map<Item, Integer> SMOKING_BURNS = new HashMap<>();
    public static final Map<Item, Boolean> HAS_RECIPE = new HashMap<>();
    public static final Map<Item, Boolean> HAS_RECIPE_SMOKING = new HashMap<>();
    public static final Map<Item, Boolean> HAS_RECIPE_BLASTING = new HashMap<>();
}
