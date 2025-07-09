package co.farns.emptycauldronswithdispensers;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyCauldronsWithDispensers implements ModInitializer {
	public static final String MOD_ID = "empty-cauldrons-with-dispensers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Empty Cauldrons with Dispensers mod initializing!");

		// Save the original vanilla bucket behavior
		var vanillaBucketBehavior = DispenserBlock.BEHAVIORS.get(Items.BUCKET);

		// Register custom behavior for empty buckets in dispensers
		DispenserBlock.registerBehavior(Items.BUCKET, (pointer, stack) -> {
			ServerWorld world = pointer.world();
			Direction facing = pointer.state().get(DispenserBlock.FACING);
			BlockPos targetPos = pointer.pos().offset(facing);
			BlockState targetState = world.getBlockState(targetPos);
			Block targetBlock = targetState.getBlock();

			// Check if the block in front is a full cauldron (lava, water, or powder snow)
			if (targetBlock == Blocks.LAVA_CAULDRON || targetBlock == Blocks.WATER_CAULDRON
					|| targetBlock == Blocks.POWDER_SNOW_CAULDRON) {
				// Decide which filled bucket to give
				ItemStack filledBucket;
				if (targetBlock == Blocks.LAVA_CAULDRON) {
					filledBucket = new ItemStack(Items.LAVA_BUCKET);
				} else if (targetBlock == Blocks.WATER_CAULDRON) {
					filledBucket = new ItemStack(Items.WATER_BUCKET);
				} else {
					filledBucket = new ItemStack(Items.POWDER_SNOW_BUCKET);
				}

				// Replace with empty cauldron
				world.setBlockState(targetPos, Blocks.CAULDRON.getDefaultState());

				// Drop the filled bucket item centered in the cauldron block
				ItemScatterer.spawn(
						world,
						targetPos.getX() + 0.5,
						targetPos.getY() + 0.5,
						targetPos.getZ() + 0.5,
						filledBucket);

				// Consume one empty bucket from dispenser
				stack.decrement(1);

				// Return the remaining stack in the dispenser
				return stack;
			}

			// Not a cauldron → fallback to vanilla dispenser behavior
			return vanillaBucketBehavior.dispense(pointer, stack);
		});
	}
}