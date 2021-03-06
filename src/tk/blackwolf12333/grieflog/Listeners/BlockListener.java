package tk.blackwolf12333.grieflog.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;

import tk.blackwolf12333.grieflog.GLPlayer;
import tk.blackwolf12333.grieflog.GriefLog;
import tk.blackwolf12333.grieflog.GriefLogger;
import tk.blackwolf12333.grieflog.SearchTask;
import tk.blackwolf12333.grieflog.action.BlockProtectionAction;
import tk.blackwolf12333.grieflog.utils.config.ConfigHandler;

public class BlockListener implements Listener {

	GriefLog plugin;
	
	public static HashMap<String, Integer> tntIgnited = new HashMap<String, Integer>();
	public HashMap<Player, ArrayList<Block>> igniterOfBlocks = new HashMap<Player, ArrayList<Block>>();
	public static HashMap<Block, String> playerTNT = new HashMap<Block, String>();
	public static HashMap<Block, String> playerTorch = new HashMap<Block, String>();
	
	public BlockListener(GriefLog plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(ConfigHandler.values.getBlockprotection()) {
			if(!isBlockOnBlacklist(event.getBlock().getTypeId())) {
				GLPlayer player = GriefLog.players.get(event.getPlayer().getName());
				
				int x = event.getBlock().getX();
				int y = event.getBlock().getY();
				int z = event.getBlock().getZ();
				String world = event.getBlock().getWorld().getName();
				String loc = x + ", " + y + ", " + z + " in: " + world;
				String evt = "[BLOCK_PLACE]";
				
				new SearchTask(player, new BlockProtectionAction(player, event, null), loc, evt);
				
				/*if(player.result != null) {
					String[] split1 = new String[player.result.size()];
					for(int i = 0; i < split1.length; i++) {
						split1[i] = player.result.get(i);
					}
					
					String[] split2 = null;
					if(split1.length == 0) {
						
					} else if(split1.length == 1) {
						split2 = split1[0].split(" ");
					} else if(split1.length - 1 == -1) {
						split2 = split1[0].split(" ");
					} else {
						split2 = split1[split1.length - 1].split(" ");
					}
					if(!(split2 == null)) {
						String owner = split2[4];
						boolean isOnFriendsList = ConfigHandler.isOnFriendsList(owner, event.getPlayer());
						if(!isOnFriendsList) {
							event.setCancelled(true);
							event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "Sorry this block is protected by " + owner + ".");
						}
					}
				} else {
					// if the search result is null ignore it because than nothing happened on that location
				}*/
			}
		}
		
		if(!event.isCancelled()) {
			Integer blockX = event.getBlock().getLocation().getBlockX();
			Integer blockY = event.getBlock().getLocation().getBlockY();
			Integer blockZ = event.getBlock().getLocation().getBlockZ();
			Player player = event.getPlayer();
			String type = event.getBlock().getType().toString();
			String namePlayer = player.getName();
			String worldName = player.getWorld().getName();
			Integer gm = player.getGameMode().getValue();
			
			if(type.equalsIgnoreCase("AIR")) {
				if(player.getGameMode() != GameMode.CREATIVE) {
					for(Player p : plugin.getServer().getOnlinePlayers()) {
						if(GriefLog.permission.has(p, "grieflog.getwarning.onbreakair")) {
							p.sendMessage(ChatColor.DARK_RED + "[GriefLog] Player " + player.getName() + " might be a hacker, he tried to break air!");
						} else {
							continue;
						}
					}
				}
				return;
			}

			String data = " [BLOCK_BREAK] By: " + namePlayer + " GM: " + gm + " What: " + type + " on Pos: " + blockX.toString() + ", " + blockY.toString() + ", " + blockZ.toString() + " in: " + worldName + System.getProperty("line.separator");
			
			GriefLogger logger = new GriefLogger(data);
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, logger);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.TNT) {
			for(BlockFace face : BlockFace.values()) {
				if(event.getBlock().getRelative(face).getType() == Material.REDSTONE_TORCH_ON) {
					playerTNT.put(event.getBlock(), event.getPlayer().getName());
				}
			}
		} else if(event.getBlock().getType() == Material.REDSTONE_TORCH_ON) {
			for(BlockFace face : BlockFace.values()) {
				if(event.getBlock().getRelative(face).getType() == Material.TNT) {
					playerTorch.put(event.getBlock().getRelative(face), event.getPlayer().getName());
				}
			}
		}
		
		if(!event.isCancelled()) {
			Integer blockX = event.getBlock().getLocation().getBlockX();
			Integer blockY = event.getBlock().getLocation().getBlockY();
			Integer blockZ = event.getBlock().getLocation().getBlockZ();
			Player player = event.getPlayer();
			String type = event.getBlockPlaced().getType().toString();
			String namePlayer = player.getName();
			String worldName = player.getWorld().getName();
			Integer gm = player.getGameMode().getValue();
			
			if (type.equalsIgnoreCase("FIRE")) {
				// this gets handled by the onBlockIgnite() function
				return;
			}
			
			if(type.contains("redstone") || type.contains("REDSTONE")) {
				Block b = event.getBlock();
				for(BlockFace face : BlockFace.values()) {
					Block relative = b.getRelative(face);
					if(relative.getType() == Material.TNT) {
						if(tntIgnited.get(namePlayer) == null) {
							tntIgnited.put(namePlayer, 1);
						} else {
							tntIgnited.put(namePlayer, (tntIgnited.get(namePlayer)+1));
						}
					}
				}
			}
			
			// log it
			String data = " [BLOCK_PLACE] By: " + namePlayer + " GM: " + gm + " What: " + type + " on Pos: " + blockX.toString() + ", " + blockY.toString() + ", " + blockZ.toString() + " in: " + worldName + System.getProperty("line.separator");
			
			GriefLogger logger = new GriefLogger(data);
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, logger);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(ConfigHandler.values.getAntifire()) {
			event.setCancelled(true);
			return;
		}
		
		if((ConfigHandler.values.getBlockIgnite()) && (!event.isCancelled())) {
			String data = "";
			Player player = event.getPlayer();
			
			// check if it was the environment that ignited the block
			if (player == null) {
				if (ConfigHandler.values.getIgnoreEnvironment()) {
					return;
				} else {
					IgniteCause ic = event.getCause();
					String worldName = event.getBlock().getWorld().getName();
					int x = event.getBlock().getX();
					int y = event.getBlock().getY();
					int z = event.getBlock().getZ();
					
					data = " [BLOCK_IGNITE] By: Environment" + " How: " + ic.toString() + " Where: " + x + ", " + y + ", " + z + " In: " + worldName + System.getProperty("line.separator");
				}
			} else { // it is a player
				if(event.getBlock().getType() == Material.TNT) {
					GLPlayer p = new GLPlayer(plugin, player);
					p.playersIgnitedTNT.put(event.getBlock().getLocation(), player.getName());
				}
				IgniteCause ic = event.getCause();
				String playerName = player.getName();
				String worldName = event.getBlock().getWorld().getName();
				Integer gm = player.getGameMode().getValue();
				int x = event.getBlock().getX();
				int y = event.getBlock().getY();
				int z = event.getBlock().getZ();
				
				if(!igniterOfBlocks.containsKey(playerName)) {
					ArrayList<Block> blockList = new ArrayList<Block>();
					blockList.add(event.getBlock());
					igniterOfBlocks.put(player, blockList);
				} else {
					ArrayList<Block> blockList = igniterOfBlocks.get(playerName);
					blockList.add(event.getBlock());
					igniterOfBlocks.put(player, blockList);
				}
				

				data = " [BLOCK_IGNITE] By: " + playerName + " GM: " + gm + " How: " + ic.toString() + " Where: " + x + ", " + y + ", " + z + " In: " + worldName + System.getProperty("line.separator");
			}
			
			GriefLogger logger = new GriefLogger(data);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, logger);
		}
	}
	
	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockSpread(BlockSpreadEvent event) {
		// TODO: make sure the spreads are getting logged by the name of the igniter
		Block from = event.getSource();
		Block to = event.getBlock();
		
		for(Iterator<Player> it = igniterOfBlocks.keySet().iterator(); it.hasNext();) {
			ArrayList<Block> blockList = igniterOfBlocks.get(it.next());
			for(int i = 0; i < blockList.size(); i++) {
				System.out.print("debugz");
				if(from.getLocation() == blockList.get(i).getLocation()) {
					blockList.add(to);
					igniterOfBlocks.put(it.next(), blockList);
					System.out.print("hoihoi");
				}
			}
		}
	}*/
	
	public boolean isBlockOnBlacklist(int id) {
		List<Integer> blacklist = ConfigHandler.values.getItemBlacklist();
		if(blacklist.size() == -1) {
			return false;
		}
		
		for(int i = 0; i < blacklist.size(); i++) {
			if(id == blacklist.get(i)) {
				return true;
			}
		}
		
		return false;
	}
}
