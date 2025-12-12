package org.novagladecode.livesplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.novagladecode.livesplugin.LivePlugin;
import org.novagladecode.livesplugin.commands.EndMaceCommand;
import org.novagladecode.livesplugin.commands.NetherMaceCommand;
import org.novagladecode.livesplugin.commands.WardenMaceCommand;

public class MaceListener implements Listener {

    private final LivePlugin plugin;
    private final WardenMaceCommand wardenCmd;
    private final NetherMaceCommand netherCmd;
    private final EndMaceCommand endCmd;

    public MaceListener(LivePlugin plugin, WardenMaceCommand wardenCmd, NetherMaceCommand netherCmd,
            EndMaceCommand endCmd) {
        this.plugin = plugin;
        this.wardenCmd = wardenCmd;
        this.netherCmd = netherCmd;
        this.endCmd = endCmd;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        // Check if player has Mace Action Mode enabled
        if (!plugin.isMaceInteractMode(p.getUniqueId())) {
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.MACE || !item.hasItemMeta()) {
            return;
        }

        String name = item.getItemMeta().getDisplayName();
        if (name == null)
            return;

        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

            // Determine Ability
            // Shift + Right Click = Ability 2
            // Right Click = Ability 1
            boolean isShift = p.isSneaking();

            if (name.equals("§3Warden Mace")) {
                e.setCancelled(true); // Prevent normal interact if needed (e.g. placing blocks?)
                if (isShift) {
                    wardenCmd.useAbility2(p);
                } else {
                    wardenCmd.useAbility1(p);
                }
            } else if (name.equals("§cNether Mace")) {
                e.setCancelled(true);
                if (isShift) {
                    netherCmd.useAbility2(p);
                } else {
                    netherCmd.useAbility1(p);
                }
            } else if (name.equals("§5End Mace")) {
                e.setCancelled(true);
                if (isShift) {
                    endCmd.useAbility2(p);
                } else {
                    endCmd.useAbility1(p);
                }
            }
        }
    }
}
