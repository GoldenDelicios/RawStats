package org.slabserver.plugin.rawstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;

public class TabComplete implements TabCompleter {
	private static final Map<String, Type> statTypes = new HashMap<>();
	private static final List<String> allStats = new ArrayList<>();
	private static final List<String> allBlocks = new ArrayList<>();
	private static final List<String> allItems = new ArrayList<>();
	private static final List<String> allMobs = new ArrayList<>();
	
	static {
		for (Statistic statistic : Statistic.values()) {
			String name = statistic.name().toLowerCase();
			statTypes.put(name, statistic.getType());
			allStats.add(name);
		}
		
		for (Material material : Material.values()) {
			String name = material.name().toLowerCase();
			if (material.isBlock())
				allBlocks.add(name);
			if (material.isItem())
				allItems.add(name);
		}
		
		for (EntityType entityType : EntityType.values()) {
			String name = entityType.name().toLowerCase();
			if (entityType.isAlive())
				allMobs.add(name);
		}
		
		allStats.sort(null);
		allBlocks.sort(null);
		allItems.sort(null);
		allMobs.sort(null);
	}

	public TabComplete() {
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equals("statistics")) {
			String last = args[args.length - 1];
			
			switch (args.length) {
			case 1:
				return filter(allStats, last);
			case 2:
				String stat = args[0];
				Type type = statTypes.get(stat);
				
				if (type != null) {
					switch (type) {
					case BLOCK:
						return filter(allBlocks, last);
					case ITEM:
						return filter(allItems, last);
					case ENTITY:
						return filter(allMobs, last);
					default:
						return Collections.emptyList();
					}
				}
			}
		}
		
		return Collections.emptyList();
	}
	
	public static List<String> filter(List<String> list, String prefix) {
		if (prefix.isEmpty())
			return list;
		
		List<String> newList = new ArrayList<>();
		for (String str : list) {
			if (str.startsWith(prefix)) {
				newList.add(str);
			}
		}
		return newList;
	}

}
