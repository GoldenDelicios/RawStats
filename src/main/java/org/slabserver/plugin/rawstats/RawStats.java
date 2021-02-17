package org.slabserver.plugin.rawstats;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class RawStats extends JavaPlugin implements Listener {
	Map<UUID, String> playerNames;
	
	public RawStats() {
		
	}

	public RawStats(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		
		playerNames = new HashMap<>();
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			playerNames.put(player.getUniqueId(), player.getName());
		}
		
		this.getCommand("statistics").setTabCompleter(new StatComplete());
		this.getCommand("objective").setTabCompleter(new ObjComplete());
	}

	@Override
	public void onDisable() {
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		playerNames.put(player.getUniqueId(), player.getName());
	}
	
	public String getCriteria(Statistic statistic, Material material, EntityType entity) {
		switch (statistic.getType()) {
		case BLOCK:
		case ITEM:
			if (material == null)
				throw new IllegalArgumentException();
			break;
		case ENTITY:
			if (entity == null)
				throw new IllegalArgumentException();
			break;
		default:
			break;
		}
		
		switch (statistic) {
		case ARMOR_CLEANED:
			return "minecraft.custom:minecraft.clean_armor";
		case BANNER_CLEANED:
			return "minecraft.custom:minecraft.clean_banner";
		case BEACON_INTERACTION:
			return "minecraft.custom:minecraft.interact_with_beacon";
		case BREAK_ITEM:
			return "minecraft.broken:minecraft." + material.name().toLowerCase();
		case BREWINGSTAND_INTERACTION:
			return "minecraft.custom:minecraft.interact_with_brewingstand";
		case CAKE_SLICES_EATEN:
			return "minecraft.custom:minecraft.eat_cake_slice";
		case CAULDRON_FILLED:
			return "minecraft.custom:minecraft.fill_cauldron";
		case CAULDRON_USED:
			return "minecraft.custom:minecraft.use_cauldron";
		case CHEST_OPENED:
			return "minecraft.custom:minecraft.open_chest";
		case CRAFTING_TABLE_INTERACTION:
			return "minecraft.custom:minecraft.interact_with_crafting_table";
		case CRAFT_ITEM:
			return "minecraft.crafted:minecraft." + material.name().toLowerCase();
		case DISPENSER_INSPECTED:
			return "minecraft.custom:minecraft.inspect_dispenser";
		case DROP:
			return "minecraft.dropped:minecraft." + material.name().toLowerCase();
		case DROPPER_INSPECTED:
			return "minecraft.custom:minecraft.inspect_dropper";
		case DROP_COUNT:
			return "minecraft.custom:minecraft.drop";
		case ENDERCHEST_OPENED:
			return "minecraft.custom:minecraft.open_enderchest";
		case ENTITY_KILLED_BY:
			return "minecraft.killed_by:minecraft." + entity.name().toLowerCase();
		case FLOWER_POTTED:
			return "minecraft.custom:minecraft.pot_flower";
		case FURNACE_INTERACTION:
			return "minecraft.custom:minecraft.interact_with_furnace";
		case HOPPER_INSPECTED:
			return "minecraft.custom:minecraft.inspect_hopper";
		case ITEM_ENCHANTED:
			return "minecraft.custom:minecraft.enchant_item";
		case KILL_ENTITY:
			return "minecraft.killed:minecraft." + entity.name().toLowerCase();
		case MINE_BLOCK:
			return "minecraft.mined:minecraft." + material.name().toLowerCase();
		case NOTEBLOCK_PLAYED:
			return "minecraft.custom:minecraft.play_noteblock";
		case NOTEBLOCK_TUNED:
			return "minecraft.custom:minecraft.tune_noteblock";
		case PICKUP:
			return "minecraft.picked_up:minecraft." + material.name().toLowerCase();
		case RECORD_PLAYED:
			return "minecraft.custom:minecraft.play_record";
		case SHULKER_BOX_OPENED:
			return "minecraft.custom:minecraft.open_shulker_box";
		case TRAPPED_CHEST_TRIGGERED:
			return "minecraft.custom:minecraft.trigger_trapped_chest";
		case USE_ITEM:
			return "minecraft.used:minecraft." + material.name().toLowerCase();
		default:
			return "minecraft.custom:minecraft." + statistic.name().toLowerCase();
		}
	}
	
	public Objective emptyObjective(Statistic statistic, Material material, EntityType entity) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective statistics = scoreboard.getObjective("Statistics");
		if (statistics != null)
			statistics.unregister();
		
		String criteria = getCriteria(statistic, material, entity);
		this.getLogger().info(criteria);
		this.getServer().dispatchCommand(getServer().getConsoleSender(), 
				"scoreboard objectives add Statistics " + criteria);
		
		statistics = scoreboard.getObjective("Statistics");
		if (statistics == null) {
			this.getLogger().info("Could not get objective normally, trying again. Criteria may have been invalid.");
			statistics = scoreboard.registerNewObjective("Statistics", criteria, "Statistics");
		}
		return statistics;
	}
	
	public static String displayName(Enum<?>... enums) {
		StringBuilder s = new StringBuilder();
		for (Enum<?> e : enums) {
			boolean upper = true;
			for (char c : e.name().toCharArray()) {
				if (c == '_')
					upper = true;
				else if (upper) {
					s.append(c);
					upper = false;
				}
				else
					s.append(Character.toLowerCase(c));
			}
		}
		
		return s.toString();
	}
	
	public void getStats(Statistic statistic) {
		Objective statistics = emptyObjective(statistic, null, null);
		String displayName = displayName(statistic);
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		async(() -> {
			playerNames.forEach((uuid, name) -> {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				int count = player.getStatistic(statistic);
				statistics.getScore(name).setScore(count);
			});
		});
	}
	
	public void getStats(Statistic statistic, Material material) {
		Objective statistics = emptyObjective(statistic, material, null);
		String displayName = displayName(statistic, material);
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		async(() -> {
			playerNames.forEach((uuid, name) -> {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				int count = player.getStatistic(statistic, material);
				statistics.getScore(name).setScore(count);
			});
		});
	}
	
	public void getStats(Statistic statistic, EntityType entity) {
		Objective statistics = emptyObjective(statistic, null, entity);
		String displayName = displayName(statistic, entity);
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		async(() -> {
			playerNames.forEach((uuid, name) -> {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				int count = player.getStatistic(statistic, entity);
				statistics.getScore(name).setScore(count);
			});
		});
	}
	
	public String getScores(String objectiveName) {
		Map<String, Integer> map = new HashMap<>();
		
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null)
			return null;
		
		for (String player : scoreboard.getEntries()) {
			Score score = objective.getScore(player);
			if (score.isScoreSet())
				map.put(player, score.getScore());
		}
		
		StringBuilder s = new StringBuilder("===============");
		map.entrySet().stream().sorted((a, b) -> {
			int av = a.getValue(), bv = b.getValue();
			return av < bv ? 1 : av == bv ? a.getKey().compareToIgnoreCase(b.getKey()) : -1;
		}).forEach(entry -> s.append("\n" + entry.getKey() + " " + entry.getValue()));
		return s.toString();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("objective")) {
			if (args.length == 1) {
				String scores = getScores(args[0]);
				if (scores == null)
					sender.sendMessage(args[0] + " does not exist");
				else
					sender.sendMessage(scores);
				return true;
			}
		}
		
		else if (cmd.getName().equals("statistics")) {
			for (int i = 0; i < args.length; ++i) {
				args[i] = args[i].toUpperCase();
			}
			
			Statistic statistic = null;
			Material material = null;
			EntityType entity = null;
			try {
				if (args.length == 0) {
					Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
					Objective statistics = scoreboard.getObjective("Statistics");
					if (statistics == null || statistics.getDisplaySlot() == null)
						return false;
					else
						statistics.setDisplaySlot(null);
				}
				else if (args.length == 1) {
					statistic = Statistic.valueOf(args[0]);
					getStats(statistic);
				}
				else if (args.length == 2) {
					statistic = Statistic.valueOf(args[0]);
					switch (statistic.getType()) {
					case BLOCK:
					case ITEM:
						material = Material.valueOf(args[1]);
						getStats(statistic, material);
						break;
					case ENTITY:
						entity = EntityType.valueOf(args[1]);
						getStats(statistic, entity);
						break;
					default:
						sender.sendMessage("Statistic not found: This is not an item/entity statistic");
					}
				}
				else {
					return false;
				}
			}
			catch (IllegalArgumentException e) {
				getLogger().info("statistic=" + statistic + ",material=" + material + ",entity=" + entity);
				sender.sendMessage("Statistic not found: Statistic is invalid");
			}
			return true;
		}
		
		return false;
	}
	
	public void async(Runnable runnable) {
		new Thread(() -> {
			synchronized (this) {
				runnable.run();
			}
		}).start();
	}

}
