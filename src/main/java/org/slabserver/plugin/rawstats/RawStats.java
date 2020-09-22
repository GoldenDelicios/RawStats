package org.slabserver.plugin.rawstats;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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
		playerNames = new HashMap<>();
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			playerNames.put(player.getUniqueId(), player.getName());
		}
		
		this.getCommand("statistics").setTabCompleter(new TabComplete());
		this.getCommand("objective").setTabCompleter(new TabCompleter() {
			@Override
			public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
				if (args.length > 1)
					return Collections.emptyList();
				
				List<String> names = new ArrayList<>();
				for (Objective obj : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
					names.add(obj.getName());
				}
				
				if (!args[0].isEmpty()) {
					String prefix = args[0].toLowerCase();
					names.removeIf(name -> !name.toLowerCase().startsWith(prefix));
				}
				names.sort(String.CASE_INSENSITIVE_ORDER);
				return names;
			}
		});
	}

	@Override
	public void onDisable() {
		
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		playerNames.put(player.getUniqueId(), player.getName());
	}
	
	public void getStats(Statistic statistic) {
		Objective statistics = getObjective();
		playerNames.forEach((uuid, name) -> {
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			int count = player.getStatistic(statistic);
			statistics.getScore(name).setScore(count);
		});
		
		String displayName = displayName(statistic.name());
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void getStats(Statistic statistic, Material material) {
		Objective statistics = getObjective();
		playerNames.forEach((uuid, name) -> {
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			int count = player.getStatistic(statistic, material);
			statistics.getScore(name).setScore(count);
		});
		
		String displayName = displayName(statistic.name() + '_' + material.name());
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void getStats(Statistic statistic, EntityType entity) {
		Objective statistics = getObjective();
		playerNames.forEach((uuid, name) -> {
			OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
			int count = player.getStatistic(statistic, entity);
			statistics.getScore(name).setScore(count);
		});
		
		String displayName = displayName(statistic.name() + '_' + entity.name());
		statistics.setDisplayName(displayName);
		statistics.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public static String displayName(String name) {
		StringBuilder s = new StringBuilder();
		boolean upper = true;
		for (char c : name.toCharArray()) {
			if (c == '_')
				upper = true;
			else if (upper) {
				s.append(c);
				upper = false;
			}
			else {
				s.append(Character.toLowerCase(c));
			}
		}
		return s.toString();
	}
	
	public Objective getObjective() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective statistics = scoreboard.getObjective("Statistics");
		if (statistics != null)
			statistics.unregister();
		return scoreboard.registerNewObjective("Statistics", "dummy", "Statistics");
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
					Statistic statistic = Statistic.valueOf(args[0]);
					async(() -> getStats(statistic));
				}
				else if (args.length == 2) {
					Statistic statistic = Statistic.valueOf(args[0]);
					Material material;
					switch (statistic.getType()) {
					case BLOCK:
						material = Material.valueOf(args[1]);
						async(() -> getStats(statistic, material));
						break;
					case ENTITY:
						EntityType entity = EntityType.valueOf(args[1]);
						async(() -> getStats(statistic, entity));
						break;
					case ITEM:
						material = Material.valueOf(args[1]);
						async(() -> getStats(statistic, material));
						break;
					default:
						sender.sendMessage("Statistic not found");
					}
				}
				else {
					return false;
				}
			}
			catch (IllegalArgumentException e) {
				sender.sendMessage("Statistic not found");
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
