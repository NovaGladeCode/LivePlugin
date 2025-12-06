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
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class GameListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerDataManager dataManager;
    private final ItemManager itemManager;
    private final EffectManager effectManager;
    private final UnbanGUI unbanGUI;

    // Warden Mace sonic boom cooldown tracking
    private final HashMap<UUID, Long> sonicBoomCooldown = new HashMap<>();

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

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && itemManager.isUnbanItem(item)) {
            e.setCancelled(true);
            unbanGUI.openUnbanMenu(p);
        }

        // Chicken Bow "No Arrow" firing logic
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && item != null && item.getType() == Material.BOW && item.hasItemMeta()
                && "§eChicken Bow".equals(item.getItemMeta().getDisplayName())) {

            // Check if player already charging? Vanilla handles charging if arrow present.
            // If no arrow, vanilla does nothing. We manually fire.
            if (!p.getInventory().contains(Material.ARROW) && !p.getInventory().contains(Material.SPECTRAL_ARROW)
                    && !p.getInventory().contains(Material.TIPPED_ARROW)) {
                // Instant fire logic
                Arrow arrow = p.launchProjectile(Arrow.class);
                arrow.setMetadata("chicken_arrow", new FixedMetadataValue(plugin, true));
                arrow.setVelocity(p.getLocation().getDirection().multiply(3.0)); // Fast shot
                p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            ItemStack bow = e.getBow();

            // Ban Tipped Arrows
            ItemStack arrowItem = e.getConsumable();
            if (arrowItem != null && arrowItem.getType() == Material.TIPPED_ARROW) {
                e.setCancelled(true);
                ((Player) e.getEntity()).sendMessage("§cTipped Arrows are disabled on this server!");
                return;
            }

            if (bow != null && bow.hasItemMeta() && "§eChicken Bow".equals(bow.getItemMeta().getDisplayName())) {
                e.getProjectile().setMetadata("chicken_arrow", new FixedMetadataValue(plugin, true));
                e.setConsumeItem(false); // Do not consume arrows
            }
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent e) {
        if (e.getEntityType() == org.bukkit.entity.EntityType.WARDEN) {
            e.getDrops().add(itemManager.createWardenHeart());
        }
    }

    // Track active rituals
    private final Map<UUID, Integer> activeRituals = new HashMap<>(); // Player -> TaskID (unused, just existence check)
    private final Map<Location, UUID> ritualLocations = new HashMap<>(); // Table Loc -> Player
    private final Map<UUID, List<Item>> ritualVisuals = new HashMap<>(); // Player -> Floating Items

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (ritualLocations.containsKey(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cThis table is protected by a dark ritual...");
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        if (ritualLocations.containsKey(e.getBlock().getLocation())) {
            // Apply Mining Fatigue 3 to simulate Obsidian hardness/slow mining
            e.getPlayer().addPotionEffect(
                    new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.MINING_FATIGUE, 40, 2));
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

            // Uniqueness check: If global flag is true, NO ONE can craft it
            if (dataManager.isWardenMaceCrafted()) {
                e.getInventory().setResult(null);
                return;
            }

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
    public void onCraftItem(org.bukkit.event.inventory.CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        ItemStack result = e.getCurrentItem();
        if (result == null || result.getType() != Material.MACE || !result.hasItemMeta()) {
            return;
        }

        String displayName = result.getItemMeta().getDisplayName();
        boolean isWarden = "§3Warden Mace".equals(displayName);
        boolean isNether = "§cNether Mace".equals(displayName);
        boolean isEnd = "§5End Mace".equals(displayName);

        if (!isWarden && !isNether && !isEnd)
            return;

        // Warden Uniqueness Check
        if (isWarden && dataManager.isWardenMaceCrafted()) {
            e.setCancelled(true);
            p.sendMessage("§cThe Warden Mace has already been forged! Only one may exist.");
            return;
        }

        // Prevent starting multiple rituals
        if (activeRituals.containsKey(p.getUniqueId())) {
            p.sendMessage("§cYou are already performing a ritual!");
            e.setCancelled(true);
            return;
        }

        // Check if Workbench
        if (e.getInventory().getType() != InventoryType.WORKBENCH) {
            e.setCancelled(true);
            p.sendMessage("§cYou must forge this in a Crafting Table!");
            return;
        }
        Location tableLoc = e.getInventory().getLocation();
        if (tableLoc == null) {
            tableLoc = p.getLocation().getBlock().getLocation();
        }

        // START RITUAL
        e.setCancelled(true);

        // Spawn Visual Items
        List<Item> visualItems = new ArrayList<>();
        Location spawnLoc = tableLoc.clone().add(0.5, 1.1, 0.5);
        for (ItemStack ingredient : e.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                Item itemEntity = tableLoc.getWorld().dropItem(spawnLoc, ingredient.clone());
                itemEntity.setPickupDelay(32767); // Infinite pickup delay
                itemEntity.setInvulnerable(true);
                itemEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                itemEntity.setGravity(false);
                itemEntity.setGlowing(true);
                visualItems.add(itemEntity);
            }
        }

        e.getInventory().setMatrix(new ItemStack[9]); // Clear inputs
        p.closeInventory();

        String coordMsg = String.format("§c§lX: %d, Y: %d, Z: %d", tableLoc.getBlockX(), tableLoc.getBlockY(),
                tableLoc.getBlockZ());

        if (isWarden) {
            Bukkit.broadcastMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            Bukkit.broadcastMessage("§4§lTHE FORGING OF THE WARDEN MACE HAS BEGUN!");
            Bukkit.broadcastMessage("§e" + p.getName() + " §cis attempting the ritual!");
            Bukkit.broadcastMessage("§cLocation: " + coordMsg);
            Bukkit.broadcastMessage("§cThe mace will appear in §43 MINUTES§c!");
            Bukkit.broadcastMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("§e§lSURVIVE! §7Stay close to this location for 3 minutes.");

            startRitualTask(p, tableLoc, 180, 1, visualItems);
        } else if (isNether) {
            Bukkit.broadcastMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            Bukkit.broadcastMessage("§6§lTHE NETHER MACE RITUAL HAS BEGUN!");
            Bukkit.broadcastMessage("§e" + p.getName() + " §cis attempting the ritual!");
            Bukkit.broadcastMessage("§cLocation: " + coordMsg);
            Bukkit.broadcastMessage("§cThe mace will appear in §61 MINUTE§c!");
            Bukkit.broadcastMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("§e§lSURVIVE! §7Stay close to this location for 1 minute.");

            startRitualTask(p, tableLoc, 60, 2, visualItems);
        } else if (isEnd) {
            Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            Bukkit.broadcastMessage("§5§lTHE END MACE RITUAL HAS BEGUN!");
            Bukkit.broadcastMessage("§e" + p.getName() + " §cis attempting the ritual!");
            Bukkit.broadcastMessage("§cLocation: " + coordMsg);
            Bukkit.broadcastMessage("§cThe mace will appear in §52 MINUTES§c!");
            Bukkit.broadcastMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("§e§lSURVIVE! §7Stay close to this location for 2 minutes.");

            startRitualTask(p, tableLoc, 120, 3, visualItems);
        }
    }

    private void startRitualTask(Player p, Location origin, int durationSeconds, int maceType,
            List<Item> visualItems) {
        activeRituals.put(p.getUniqueId(), 1);
        ritualLocations.put(origin, p.getUniqueId());
        ritualVisuals.put(p.getUniqueId(), visualItems);

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            final int MAX_TICKS = 20 * durationSeconds;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    failRitual("Player died or disconnected.");
                    return;
                }

                if (p.getLocation().distance(origin) > 10) {
                    failRitual("Player moved too far away.");
                    return;
                }

                // Effects
                if (ticks % 20 == 0) {
                    if (maceType == 1) { // Warden
                        origin.getWorld().playSound(origin, org.bukkit.Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.0f, 0.5f);
                        origin.getWorld().spawnParticle(org.bukkit.Particle.SCULK_SOUL, origin.clone().add(0.5, 1, 0.5),
                                20, 0.5, 0.5, 0.5, 0.05);
                    } else if (maceType == 2) { // Nether
                        origin.getWorld().playSound(origin, org.bukkit.Sound.BLOCK_LAVA_POP, 1.0f, 0.5f);
                        origin.getWorld().spawnParticle(org.bukkit.Particle.FLAME, origin.clone().add(0.5, 1, 0.5), 20,
                                0.5, 0.5, 0.5, 0.05);
                    } else if (maceType == 3) { // End
                        origin.getWorld().playSound(origin, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                        origin.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, origin.clone().add(0.5, 1, 0.5), 20,
                                0.5, 0.5, 0.5, 0.05);
                        origin.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH,
                                origin.clone().add(0.5, 1, 0.5), 10, 0.2, 0.2, 0.2, 0.01);
                    }
                    p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            new net.md_5.bungee.api.chat.TextComponent(
                                    "§bRitual Progress: " + (ticks / 20) + "s / " + durationSeconds + "s"));
                }

                if (ticks >= MAX_TICKS) {
                    completeRitual();
                    return;
                }
                ticks += 20;
            }

            private void failRitual(String reason) {
                String name = (maceType == 1) ? "Warden" : (maceType == 2) ? "Nether" : "End";
                Bukkit.broadcastMessage("§cThe " + name + " Mace ritual FAILED! " + reason);
                cleanup();
                this.cancel();
            }

            private void completeRitual() {
                if (maceType == 1) {
                    if (dataManager.isWardenMaceCrafted()) {
                        p.sendMessage("§cToo late!");
                        cleanup();
                        this.cancel();
                        return;
                    }
                    dataManager.setWardenMaceCrafted(true);
                    giveItem(itemManager.createWardenMace());
                    Bukkit.broadcastMessage("§b§lTHE WARDEN MACE HAS BEEN FORGED BY " + p.getName() + "!");
                } else if (maceType == 2) {
                    giveItem(itemManager.createNetherMace());
                    Bukkit.broadcastMessage("§6§lTHE NETHER MACE HAS BEEN FORGED BY " + p.getName() + "!");
                } else if (maceType == 3) {
                    giveItem(itemManager.createEndMace());
                    Bukkit.broadcastMessage("§5§lTHE END MACE HAS BEEN FORGED BY " + p.getName() + "!");
                }

                p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                cleanup();
                this.cancel();
            }

            private void cleanup() {
                activeRituals.remove(p.getUniqueId());
                ritualLocations.remove(origin);
                List<Item> visuals = ritualVisuals.remove(p.getUniqueId());
                if (visuals != null) {
                    for (Item i : visuals) {
                        i.remove();
                    }
                }
            }

            private void giveItem(ItemStack item) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(item);
                } else {
                    p.getWorld().dropItem(p.getLocation(), item);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        Player p = (Player) e.getEntity();

        // Check if player has Nether or End Mace in inventory
        boolean hasNetherMace = false;
        boolean hasEndMace = false;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == Material.MACE && item.hasItemMeta()) {
                String dn = item.getItemMeta().getDisplayName();
                if ("§cNether Mace".equals(dn)) {
                    hasNetherMace = true;
                } else if ("§5End Mace".equals(dn)) {
                    hasEndMace = true;
                }
            }
        }

        if (hasNetherMace) {
            // "Gets fire resistance and takes no damage to effect"
            // Block Fire types
            if (e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.HOT_FLOOR) {
                e.setCancelled(true);
                // Ensure they have Fire Res effect too (visual + logic)
                p.addPotionEffect(
                        new org.bukkit.potion.PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, false));
            }
            // Block Effects (Wither/Poison/Magic)
            if (e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.WITHER
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.POISON
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.MAGIC) {
                e.setCancelled(true);
            }
        }

        if (hasEndMace) {
            if (e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.DRAGON_BREATH
                    || e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                e.setCancelled(true);
            }
        }

        // Cancel persistent Wither damage if it's the cosmetic effect (Level < 10)
        // (Existing logic)
        if (e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.WITHER) {
            // If the player is low level, the Wither effect is cosmetic
            if (dataManager.getLevel(p.getUniqueId()) < 10) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent e) {

        // Chicken Bow Logic
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (arrow.hasMetadata("chicken_arrow")) {
                if (e.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) e.getEntity();
                    // 50% Slow Falling
                    if (java.util.concurrent.ThreadLocalRandom.current().nextBoolean()) {
                        target.addPotionEffect(
                                new org.bukkit.potion.PotionEffect(PotionEffectType.SLOW_FALLING, 300, 0));
                    }
                    // 10% Chance Deadly Chicken -> Changed to 40% (More likely)
                    if (java.util.concurrent.ThreadLocalRandom.current().nextInt(10) < 4) {
                        org.bukkit.Location loc = target.getLocation();
                        Chicken chicken = (Chicken) loc.getWorld().spawnEntity(loc, EntityType.CHICKEN);
                        Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                        zombie.setBaby(true);
                        chicken.addPassenger(zombie);
                        if (target instanceof Player)
                            ((Player) target).sendMessage("§cThe Chicken Army attacks!");
                    }
                }
            }
        }

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

        // Ban fire resistance potions and Strength II
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            if (item.hasItemMeta()) {
                org.bukkit.inventory.meta.PotionMeta potionMeta = (org.bukkit.inventory.meta.PotionMeta) item
                        .getItemMeta();
                if (potionMeta.getBasePotionType() != null) {
                    String typeName = potionMeta.getBasePotionType().toString();
                    if (typeName.contains("FIRE_RESISTANCE")) {
                        return true;
                    }
                    if (typeName.contains("STRONG_STRENGTH")
                            || (typeName.contains("STRENGTH") && typeName.contains("STRONG"))) {
                        return true;
                    }
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
