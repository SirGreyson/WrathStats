/*
 * Copyright (c) ReasonDev 2014.
 * All Rights Reserved
 */

package co.reasondev.wrath;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StatTrack extends JavaPlugin {

    private Random random = new Random();
    private Map<Material, HashMap<String, Integer>> loreMap = new HashMap<Material, HashMap<String, Integer>>();

    public void onEnable() {
        saveDefaultConfig();
        loadRandomLores();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("has been enabled");
    }

    public void onDisable() {
        getLogger().info("has been disabled");
    }

    private void loadRandomLores() {
        for (String type : getConfig().getConfigurationSection("RANDOM_LORES").getKeys(false)) {
            Material material = Material.getMaterial(type);
            if (material == null) {
                getLogger().severe("Error! Invalid Material: " + type);
                continue;
            }
            loreMap.put(material, new HashMap<String, Integer>());
            for (String lore : getConfig().getStringList("RANDOM_LORES." + type)) {
                loreMap.get(material).put(ChatColor.translateAlternateColorCodes('&', lore.split("=")[0]), Integer.parseInt(lore.split("=")[1]));
            }
        }
    }

    public boolean canAddLore(ItemStack itemStack) {
        return loreMap.containsKey(itemStack.getType());
    }

    public String getRandomLore(Material type) {
        int total = 0;
        for (int i : loreMap.get(type).values()) total += i;
        int chance = random.nextInt(total);
        total = 0;
        for (String s : loreMap.get(type).keySet()) {
            total += loreMap.get(type).get(s);
            if (chance <= total) return s;
        }
        return null;
    }

    public boolean isStatTracker(ItemStack itemStack) {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore() &&
                loreMap.containsKey(itemStack.getType()) && loreMap.get(itemStack.getType()).containsKey(itemStack.getItemMeta().getLore().get(0));
    }
}
