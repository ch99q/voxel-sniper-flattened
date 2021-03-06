package com.thevoxelbox.voxelsniper.brush.type.performer.splatter;

import com.thevoxelbox.voxelsniper.brush.type.performer.AbstractPerformerBrush;
import com.thevoxelbox.voxelsniper.sniper.Sniper;
import com.thevoxelbox.voxelsniper.sniper.snipe.Snipe;
import com.thevoxelbox.voxelsniper.sniper.snipe.message.SnipeMessenger;
import com.thevoxelbox.voxelsniper.sniper.toolkit.ToolkitProperties;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;

import java.util.Random;

public class SplatterDiscBrush extends AbstractPerformerBrush {

	private static final int GROW_PERCENT_MIN = 1;
	private static final int GROW_PERCENT_DEFAULT = 1000;
	private static final int GROW_PERCENT_MAX = 9999;
	private static final int SEED_PERCENT_MIN = 1;
	private static final int SEED_PERCENT_DEFAULT = 1000;
	private static final int SEED_PERCENT_MAX = 9999;
	private static final int SPLATTER_RECURSIONS_PERCENT_MIN = 1;
	private static final int SPLATTER_RECURSIONS_PERCENT_DEFAULT = 3;
	private static final int SPLATTER_RECURSIONS_PERCENT_MAX = 10;

	private int seedPercent; // Chance block on first pass is made active
	private int growPercent; // chance block on recursion pass is made active
	private int splatterRecursions; // How many times you grow the seeds
	private Random generator = new Random();

	@Override
	public void handleCommand(String[] parameters, Snipe snipe) {
		SnipeMessenger messenger = snipe.createMessenger();
		for (String parameter : parameters) {
			if (parameter.equalsIgnoreCase("info")) {
				snipe.createMessageSender()
					.message(ChatColor.GOLD + "Splatter Disc brush Parameters:")
					.message(ChatColor.AQUA + "/b sd s[int] -- set a seed percentage (1-9999). 100 = 1% Default is 1000")
					.message(ChatColor.AQUA + "/b sd g[int] -- set a growth percentage (1-9999).  Default is 1000")
					.message(ChatColor.AQUA + "/b sd r[int] -- set a recursion (1-10).  Default is 3")
					.send();
				return;
			} else if (!parameter.isEmpty() && parameter.charAt(0) == 's') {
				double temp = Integer.parseInt(parameter.replace("s", ""));
				if (temp >= SEED_PERCENT_MIN && temp <= SEED_PERCENT_MAX) {
					messenger.sendMessage(ChatColor.AQUA + "Seed percent set to: " + temp / 100 + "%");
					this.seedPercent = (int) temp;
				} else {
					messenger.sendMessage(ChatColor.RED + "Seed percent must be an integer 1-9999!");
				}
			} else if (!parameter.isEmpty() && parameter.charAt(0) == 'g') {
				double temp = Integer.parseInt(parameter.replace("g", ""));
				if (temp >= GROW_PERCENT_MIN && temp <= GROW_PERCENT_MAX) {
					messenger.sendMessage(ChatColor.AQUA + "Growth percent set to: " + temp / 100 + "%");
					this.growPercent = (int) temp;
				} else {
					messenger.sendMessage(ChatColor.RED + "Growth percent must be an integer 1-9999!");
				}
			} else if (!parameter.isEmpty() && parameter.charAt(0) == 'r') {
				int temp = Integer.parseInt(parameter.replace("r", ""));
				if (temp >= SPLATTER_RECURSIONS_PERCENT_MIN && temp <= SPLATTER_RECURSIONS_PERCENT_MAX) {
					messenger.sendMessage(ChatColor.AQUA + "Recursions set to: " + temp);
					this.splatterRecursions = temp;
				} else {
					messenger.sendMessage(ChatColor.RED + "Recursions must be an integer 1-10!");
				}
			} else {
				messenger.sendMessage(ChatColor.RED + "Invalid brush parameters! use the info parameter to display parameter info.");
			}
		}
	}

	@Override
	public void handleArrowAction(Snipe snipe) {
		Block targetBlock = getTargetBlock();
		splatterDisc(snipe, targetBlock);
	}

	@Override
	public void handleGunpowderAction(Snipe snipe) {
		Block lastBlock = getLastBlock();
		splatterDisc(snipe, lastBlock);
	}

	private void splatterDisc(Snipe snipe, Block targetBlock) {
		ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
		SnipeMessenger messenger = snipe.createMessenger();
		if (this.seedPercent < SEED_PERCENT_MIN || this.seedPercent > SEED_PERCENT_MAX) {
			messenger.sendMessage(ChatColor.BLUE + "Seed percent set to: 10%");
			this.seedPercent = SEED_PERCENT_DEFAULT;
		}
		if (this.growPercent < GROW_PERCENT_MIN || this.growPercent > GROW_PERCENT_MAX) {
			messenger.sendMessage(ChatColor.BLUE + "Growth percent set to: 10%");
			this.growPercent = GROW_PERCENT_DEFAULT;
		}
		if (this.splatterRecursions < SPLATTER_RECURSIONS_PERCENT_MIN || this.splatterRecursions > SPLATTER_RECURSIONS_PERCENT_MAX) {
			messenger.sendMessage(ChatColor.BLUE + "Recursions set to: 3");
			this.splatterRecursions = SPLATTER_RECURSIONS_PERCENT_DEFAULT;
		}
		int brushSize = toolkitProperties.getBrushSize();
		int[][] splat = new int[2 * brushSize + 1][2 * brushSize + 1];
		// Seed the array
		for (int x = 2 * brushSize; x >= 0; x--) {
			for (int y = 2 * brushSize; y >= 0; y--) {
				if (this.generator.nextInt(SEED_PERCENT_MAX + 1) <= this.seedPercent) {
					splat[x][y] = 1;
				}
			}
		}
		// Grow the seeds
		int gref = this.growPercent;
		int[][] tempSplat = new int[2 * brushSize + 1][2 * brushSize + 1];
		for (int r = 0; r < this.splatterRecursions; r++) {
			this.growPercent = gref - ((gref / this.splatterRecursions) * (r));
			for (int x = 2 * brushSize; x >= 0; x--) {
				for (int y = 2 * brushSize; y >= 0; y--) {
					tempSplat[x][y] = splat[x][y]; // prime tempsplat
					int growcheck = 0;
					if (splat[x][y] == 0) {
						if (x != 0 && splat[x - 1][y] == 1) {
							growcheck++;
						}
						if (y != 0 && splat[x][y - 1] == 1) {
							growcheck++;
						}
						if (x != 2 * brushSize && splat[x + 1][y] == 1) {
							growcheck++;
						}
						if (y != 2 * brushSize && splat[x][y + 1] == 1) {
							growcheck++;
						}
					}
					if (growcheck >= 1 && this.generator.nextInt(GROW_PERCENT_MAX + 1) <= this.growPercent) {
						tempSplat[x][y] = 1; // prevent bleed into splat
					}
				}
			}
			// integrate tempsplat back into splat at end of iteration
			for (int x = 2 * brushSize; x >= 0; x--) {
				if (2 * brushSize + 1 >= 0) {
					System.arraycopy(tempSplat[x], 0, splat[x], 0, 2 * brushSize + 1);
				}
			}
		}
		this.growPercent = gref;
		// Fill 1x1 holes
		for (int x = 2 * brushSize; x >= 0; x--) {
			for (int y = 2 * brushSize; y >= 0; y--) {
				if (splat[Math.max(x - 1, 0)][y] == 1 && splat[Math.min(x + 1, 2 * brushSize)][y] == 1 && splat[x][Math.max(0, y - 1)] == 1 && splat[x][Math.min(2 * brushSize, y + 1)] == 1) {
					splat[x][y] = 1;
				}
			}
		}
		// Make the changes
		double rSquared = Math.pow(brushSize + 1, 2);
		for (int x = 2 * brushSize; x >= 0; x--) {
			double xSquared = Math.pow(x - brushSize - 1, 2);
			for (int y = 2 * brushSize; y >= 0; y--) {
				if (splat[x][y] == 1 && xSquared + Math.pow(y - brushSize - 1, 2) <= rSquared) {
					this.performer.perform(targetBlock.getRelative(x - brushSize, 0, y - brushSize));
				}
			}
		}
		Sniper sniper = snipe.getSniper();
		sniper.storeUndo(this.performer.getUndo());
	}

	@Override
	public void sendInfo(Snipe snipe) {
		if (this.seedPercent < SEED_PERCENT_MIN || this.seedPercent > SEED_PERCENT_MAX) {
			this.seedPercent = SEED_PERCENT_DEFAULT;
		}
		if (this.growPercent < GROW_PERCENT_MIN || this.growPercent > GROW_PERCENT_MAX) {
			this.growPercent = GROW_PERCENT_DEFAULT;
		}
		if (this.splatterRecursions < SPLATTER_RECURSIONS_PERCENT_MIN || this.splatterRecursions > SPLATTER_RECURSIONS_PERCENT_MAX) {
			this.splatterRecursions = SPLATTER_RECURSIONS_PERCENT_DEFAULT;
		}
		snipe.createMessageSender()
			.brushNameMessage()
			.brushSizeMessage()
			.message(ChatColor.BLUE + "Seed percent set to: " + this.seedPercent / 100 + "%")
			.message(ChatColor.BLUE + "Growth percent set to: " + this.growPercent / 100 + "%")
			.message(ChatColor.BLUE + "Recursions set to: " + this.splatterRecursions)
			.send();
	}
}
