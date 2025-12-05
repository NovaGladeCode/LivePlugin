package org.novagladecode.livesplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.livesplugin.data.PlayerDataManager;
import org.novagladecode.livesplugin.gui.UnbanGUI;
import org.novagladecode.livesplugin.logic.EffectManager;
import org.novagladecode.livesplugin.logic.ItemManager;

import java.util.HashMap;
import java.util.UUID;

public class GameListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;
    private final EffectManager effectManager;
    private final UnbanGUI unbanGUI;

    // Warden Mace sonic boom cooldown tracking
    private final HashMap<UUID, Long> sonicBoomCooldown = new HashMap<>();
    private final HashMap<UUID, Double> playerFallStart = new HashMap<>();

    public GameListener(JavaPlugin plugin, PlayerDataManager dataManager, ItemManager itemManager,
            EffectManager effectManager, UnbanGUI unbanGUI) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.itemManager = itemManager;
        this.effectManager = effectManager;
        this.unbanGUI = unbanGUI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        dataManager.initializePlayer(p.getUniqueId());

        // Check if banned
        if (dataManager.isBanned(p.getUniqueId())) {
            p.kickPlayer("§cYou are banned! Someone must craft an Unban Token to revive you.");
        } else {
            int level = dataManager.getLevel(p.getUniqueId());
            effectManager.applyEffects(p, level);

            // Scan inventory for banned items and process valid items
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                boolean foundBanned = false;
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        if (isBannedItem(item)) {
                            item.setAmount(0);
                            foundBanned = true;
                        } else {
                            processItem(item);
                        }
                    }
                }

                // Check armor slots
                for (ItemStack armor : p.getInventory().getArmorContents()) {
                    if (armor != null && armor.getType() != Material.AIR) {
                        if (isBannedItem(armor)) {
                            armor.setAmount(0);
                            foundBanned = true;
                        } else {
                            processItem(armor);
                        }
                    }
                }

                if (foundBanned) {
                    p.sendMessage("§cNetherite armor and weapons have been removed from your inventory!");
                }
            }, 20L); // Wait 1 second for player to fully load
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        UUID victimUUID = victim.getUniqueId();
        int level = dataManager.getLevel(victimUUID);

        // Victim loses one level on death (if they have any)
        if (level > 0) {
            level--;
            dataManager.setLevel(victimUUID, level);
        }

        final int finalLevel = level;

        // Check if player should be banned
        if (finalLevel <= 0) {
            dataManager.setBanned(victimUUID, true);
            dataManager.saveData();

            // Kick banned players
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    victim.kickPlayer(
                            "§cYou died! -1 Life\n§cYou're at §40 lives§c.\n§cYou are banned until someone crafts an Unban Token.");
                }
            }, 40L);
        } else {
            // Respawn alive players immediately
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && victim.isDead()) {
                    victim.spigot().respawn();

                    // Teleport to spawn after a brief delay
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (victim.isOnline()) {
                            org.bukkit.Location spawnLoc = victim.getBedSpawnLocation();
                            if (spawnLoc == null) {
                                spawnLoc = victim.getWorld().getSpawnLocation();
                            }
                            victim.teleport(spawnLoc);

                            // Send death message
                            victim.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                            victim.sendMessage("§c§lYOU DIED!");
                            victim.sendMessage("§e-1 Life");
                            victim.sendMessage("§eYou're at §6" + finalLevel + " "
                                    + (finalLevel == 1 ? "life" : "lives") + "§e.");
                            victim.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        }
                    }, 5L);
                }
            }, 1L); // Respawn almost immediately
        }

        dataManager.saveData();

        // Give killer a level
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            int killerLevel = dataManager.getLevel(killerUUID);
            if (killerLevel < 15) {
                killerLevel++;
            }
            dataManager.setLevel(killerUUID, killerLevel);

            killer.sendMessage("§aYou killed " + victim.getName() + "! Your level is now: " + killerLevel);

            dataManager.saveData();

            effectManager.applyEffects(killer, killerLevel);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null)
            return;

        // Check if right-clicking with Level Item
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && itemManager.isLevelItem(item)) {
            e.setCancelled(true);

            UUID uuid = p.getUniqueId();
            int currentLevel = dataManager.getLevel(uuid);

            if (currentLevel >= 15) {
                p.sendMessage("§cYou are already at max level (15)!");
                e.setCancelled(true);
                return;
            }

            dataManager.setLevel(uuid, currentLevel + 1);
            dataManager.saveData();

            p.sendMessage("§aYou used a Level Item! Your level is now: " + (currentLevel + 1));

            // Consume the item
            item.setAmount(item.getAmount() - 1);

            // Apply effects with new level
            effectManager.applyEffects(p, currentLevel + 1);
        }

        // Check if right-clicking with Unban Token
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && itemManager.isUnbanItem(item)) {
            e.setCancelled(true);
            unbanGUI.openUnbanMenu(p);
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent e) {
        if (e.getEntityType() == org.bukkit.entity.EntityType.WARDEN) {
            e.getDrops().add(itemManager.createWardenHeart());
        }
    }

    @EventHandler
    public void onPrepareItemCraft(org.bukkit.event.inventory.PrepareItemCraftEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result == null)
            return;

        // Check for Warden Mace recipe
        if (result.getType() == Material.MACE && result.hasItemMeta() &&
                "§3Warden Mace".equals(result.getItemMeta().getDisplayName())) {

            // Validate ingredients - specifically the Heart
            for (ItemStack ingredient : e.getInventory().getMatrix()) {
                if (ingredient != null && ingredient.getType() == Material.ECHO_SHARD) {
                    // This must be the actual Warden Heart item
                    if (!itemManager.isWardenHeart(ingredient)) {
                        e.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL
                && e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && item.getType() == Material.MACE && item.hasItemMeta()
                    && "§eChicken Mace".equals(item.getItemMeta().getDisplayName())) {
                e.setCancelled(true);
                p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
                // Play a soft chicken flap sound
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PARROT_FLY, 0.5f, 1.5f);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        // Check if attacker is a player with Warden Mace
        if (e.getDamager() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            ItemStack weapon = attacker.getInventory().getItemInMainHand();

            // Check if holding Warden Mace
            if (weapon.getType() == org.bukkit.Material.MACE && weapon.hasItemMeta()
                    && weapon.getItemMeta().getDisplayName().equals("§3Warden Mace")) {

                // Check if player has fallen at least 5 blocks
                double fallDistance = attacker.getFallDistance();
                if (fallDistance < 5.0) {
                    return; // No sonic boom if fall distance is less than 5 blocks
                }

                // 1 in 10 chance (10%)
                if (java.util.concurrent.ThreadLocalRandom.current().nextInt(10) != 0) {
                    return;
                }

                // Check cooldown
                UUID attackerUUID = attacker.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (sonicBoomCooldown.containsKey(attackerUUID)) {
                    long cooldownEnd = sonicBoomCooldown.get(attackerUUID);
                    if (currentTime < cooldownEnd) {
                        // Silent fail or message? User said "only work when...", implied random chance.
                        // If it fails random check, it just doesn't happen. If it passes but is on
                        // cooldown, we tell them.
                        // But wait, if it's a random chance, maybe we shouldn't have a cooldown?
                        // The user didn't ask to remove the cooldown, so I'll keep it to prevent spam
                        // if they get lucky.
                        // Actually, let's keep the cooldown logic as is, just triggered after the
                        // chance check.
                        return;
                    }
                }

                // Set cooldown (5 seconds)
                sonicBoomCooldown.put(attackerUUID, currentTime + 5000);

                // Trigger sonic boom on hit
                org.bukkit.Location hitLoc = e.getEntity().getLocation();

                // Play sonic boom sound
                hitLoc.getWorld().playSound(hitLoc, org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                hitLoc.getWorld().playSound(hitLoc, org.bukkit.Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 1.2f);

                // Create sonic boom particles in a sphere
                for (int i = 0; i < 20; i++) {
                    double angle1 = Math.random() * Math.PI * 2;
                    double angle2 = Math.random() * Math.PI;
                    double x = Math.sin(angle2) * Math.cos(angle1) * 2;
                    double y = Math.sin(angle2) * Math.sin(angle1) * 2;
                    double z = Math.cos(angle2) * 2;
                    org.bukkit.Location particleLoc = hitLoc.clone().add(x, y + 1, z);
                    hitLoc.getWorld().spawnParticle(org.bukkit.Particle.SONIC_BOOM, particleLoc, 1);
                    hitLoc.getWorld().spawnParticle(org.bukkit.Particle.SCULK_SOUL, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                }

                // Damage and knock back nearby entities (within 5 blocks)
                for (org.bukkit.entity.Entity nearby : e.getEntity().getNearbyEntities(5, 5, 5)) {
                    if (nearby instanceof org.bukkit.entity.LivingEntity && nearby != attacker) {
                        org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) nearby;
                        livingEntity.damage(3.0, attacker);

                        // Knockback effect
                        org.bukkit.util.Vector knockback = nearby.getLocation().toVector()
                                .subtract(hitLoc.toVector()).normalize().multiply(1.5);
                        knockback.setY(0.5);
                        nearby.setVelocity(knockback);
                    }
                }
            }
        }

        if (!(e.getEntity() instanceof Player))
            return;

        Player victim = (Player) e.getEntity();

        // Check if damage source is AreaEffectCloud (Dragon Breath)
        if (e.getDamager() instanceof org.bukkit.entity.AreaEffectCloud) {
            org.bukkit.entity.AreaEffectCloud cloud = (org.bukkit.entity.AreaEffectCloud) e.getDamager();

            if (cloud.getParticle() == org.bukkit.Particle.DRAGON_BREATH) {
                // Check if victim has Dragon Egg
                if (victim.getInventory().contains(org.bukkit.Material.DRAGON_EGG)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();
        ItemStack item = e.getItem().getItemStack();

        // Check if item is banned
        if (isBannedItem(item)) {
            e.setCancelled(true);
            e.getItem().remove();
            p.sendMessage("§cYou cannot pick up netherite armor or weapons!");
            return;
        }

        // Process the item (fix mace, downgrade protection)
        processItem(item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR)
            return;

        // Check if item is banned
        if (isBannedItem(item)) {
            e.setCancelled(true);
            ((Player) e.getWhoClicked()).sendMessage("§cYou cannot use netherite armor or weapons!");
            item.setAmount(0);
            return;
        }

        // Process the item
        processItem(item);
    }

    private boolean isBannedItem(ItemStack item) {
        if (item == null)
            return false;

        Material type = item.getType();

        // Ban netherite gear and fire resistance potions
        if (type == Material.NETHERITE_HELMET
                || type == Material.NETHERITE_CHESTPLATE
                || type == Material.NETHERITE_LEGGINGS
                || type == Material.NETHERITE_BOOTS
                || type == Material.NETHERITE_SWORD
                || type == Material.NETHERITE_AXE) {
            return true;
        }

        // Ban fire resistance potions
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            if (item.hasItemMeta()) {
                org.bukkit.inventory.meta.PotionMeta potionMeta = (org.bukkit.inventory.meta.PotionMeta) item
                        .getItemMeta();
                if (potionMeta.getBasePotionType() != null &&
                        potionMeta.getBasePotionType().toString().contains("FIRE_RESISTANCE")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void processItem(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        boolean changed = false;

        // Make maces unbreakable and remove forbidden enchants
        if (item.getType() == Material.MACE) {
            if (!meta.isUnbreakable()) {
                meta.setUnbreakable(true);
                changed = true;
            }

            // Remove density, breach, fire aspect
            if (meta.hasEnchant(Enchantment.DENSITY)) {
                meta.removeEnchant(Enchantment.DENSITY);
                changed = true;
            }
            if (meta.hasEnchant(Enchantment.BREACH)) {
                meta.removeEnchant(Enchantment.BREACH);
                changed = true;
            }
            if (meta.hasEnchant(Enchantment.FIRE_ASPECT)) {
                meta.removeEnchant(Enchantment.FIRE_ASPECT);
                changed = true;
            }
        }

        // Downgrade Protection 4 to Protection 3
        if (meta.hasEnchant(Enchantment.PROTECTION)) {
            int level = meta.getEnchantLevel(Enchantment.PROTECTION);
            if (level >= 4) {
                meta.removeEnchant(Enchantment.PROTECTION);
                meta.addEnchant(Enchantment.PROTECTION, 3, true);
                changed = true;
            }
        }

        if (changed) {
            item.setItemMeta(meta);
        }
    }

}
