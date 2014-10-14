/*
 * Copyright (c) ReasonDev 2014.
 * All Rights Reserved
 */

package co.reasondev.wrath;

import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    private StatTrack plugin;
    private List<String> loreFormat;

    public PlayerListener(StatTrack plugin) {
        this.plugin = plugin;
        this.loreFormat = plugin.getConfig().getStringList("LORE_FORMAT");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(CraftItemEvent e) {
        if (e.isCancelled() || !plugin.canAddLore(e.getRecipe().getResult()) || !e.getWhoClicked().hasPermission("wrathstattrack.create")) {
            return;
        }
        ItemMeta meta = e.getRecipe().getResult().getItemMeta();
        List<String> loreList = new ArrayList<String>();
        for (String s : loreFormat) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', s.
                    replace("%RANDOM_LORE%", plugin.getRandomLore(e.getRecipe().getResult().getType())).
                    replace("%CREATOR%", e.getWhoClicked().getName()).
                    replace("%PVP_KILLS%", "0").
                    replace("%PVE_KILLS%", "0")));
        }
        meta.setLore(loreList);
        e.getCurrentItem().setItemMeta(meta);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null || !plugin.isStatTracker(e.getEntity().getKiller().getItemInHand())) {
            return;
        }
        Player killer = e.getEntity().getKiller();
        ItemStack stackTracker = killer.getItemInHand();
        ItemMeta meta = stackTracker.getItemMeta();
        List<String> lore = meta.getLore();
        for (int i = 0; i < loreFormat.size(); i++) {
            if (loreFormat.get(i).contains(e.getEntity() instanceof Player ? "%PVP_KILLS%" : "%PVE_KILLS%")) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', loreFormat.get(i).
                        replace(e.getEntity() instanceof Player ? "%PVP_KILLS%" : "%PVE_KILLS%", String.valueOf(Integer.parseInt(lore.get(i).split(": ")[1]) + 1))));
                meta.setLore(lore);
                stackTracker.setItemMeta(meta);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() == null) {
            e.setDeathMessage(null);
            return;
        }
        Player killer = e.getEntity().getKiller();
        ItemStack weapon = killer.getItemInHand();
        if (plugin.canAddLore(weapon)) {
            e.setDeathMessage(null);
            sendJSONDeathMessage(e.getEntity().getName(), killer.getName(), weapon);
        } else {
            e.setDeathMessage(ChatColor.RED + e.getEntity().getName() + ChatColor.YELLOW + " was killed by " + ChatColor.RED + killer.getName());
        }
    }

    private String getItemName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        StringBuilder sb = new StringBuilder("");
        String[] args = itemStack.getType().toString().toLowerCase().split("_");
        for (int i = 0; i < args.length; i++)
            sb.append((i == 0 ? "" : " ") + args[i].substring(0, 1).toUpperCase() + args[i].substring(1));
        return ChatColor.AQUA + sb.toString().replace("_", " ");
    }

    private void sendJSONDeathMessage(String player, String killer, ItemStack weapon) {
        new FancyMessage(killer).color(ChatColor.RED).
                then(weapon.getType() == Material.BOW ? " shot " : " slayed ").color(ChatColor.YELLOW).
                then(player).color(ChatColor.RED).
                then(" using ").color(ChatColor.YELLOW).
                then("[").color(ChatColor.AQUA).
                then(getItemName(weapon)).itemTooltip(weapon).
                then("]").color(ChatColor.AQUA).send(plugin.getServer().getOnlinePlayers());
    }
}
