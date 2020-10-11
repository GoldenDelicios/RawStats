package org.slabserver.plugin.rawstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scoreboard.Objective;

public class ObjComplete implements TabCompleter {

	public ObjComplete() {
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length != 1)
			return Collections.emptyList();
		
		List<String> names = new ArrayList<>();
		Set<Objective> objs = Bukkit.getScoreboardManager().getMainScoreboard().getObjectives();
		if (args[0].isEmpty()) {
			for (Objective obj : objs)
				names.add(obj.getName());
		}
		else {
			String prefix = args[0].toLowerCase();
			for (Objective obj : objs) {
				String name = obj.getName();
				if (name.toLowerCase().startsWith(prefix))
					names.add(name);
			}
		}
		
		names.sort(String.CASE_INSENSITIVE_ORDER);
		return names;
	}

}
