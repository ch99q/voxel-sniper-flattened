package com.thevoxelbox.voxelsniper.util.material;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public class MaterialSet implements Iterable<Material> {

	private Set<Material> materials;

	public static MaterialSetBuilder builder() {
		return new MaterialSetBuilder();
	}

	public MaterialSet(Collection<Material> materials) {
		this.materials = EnumSet.copyOf(materials);
	}

	public boolean contains(Block block) {
		Material type = block.getType();
		return contains(type);
	}

	public boolean contains(BlockData blockData) {
		Material material = blockData.getMaterial();
		return contains(material);
	}

	public boolean contains(BlockState blockState) {
		Material type = blockState.getType();
		return contains(type);
	}

	public boolean contains(Material material) {
		return this.materials.contains(material);
	}

	@Override
	public Iterator<Material> iterator() {
		return this.materials.iterator();
	}

	public Set<Material> getMaterials() {
		return Sets.newHashSet(this.materials);
	}
}
