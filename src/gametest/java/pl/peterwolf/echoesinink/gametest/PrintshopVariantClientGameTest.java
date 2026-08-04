package pl.peterwolf.echoesinink.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.lwjgl.glfw.GLFW;
import pl.peterwolf.echoesinink.EchoesInInk;
import pl.peterwolf.echoesinink.block.ModBlocks;
import pl.peterwolf.echoesinink.block.entity.InvestigationBlockEntity;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.progression.InvestigationRole;
import pl.peterwolf.echoesinink.progression.WorkshopLayoutPlan;
import pl.peterwolf.echoesinink.progression.WorkshopVariant;
import pl.peterwolf.echoesinink.structure.AbandonedPrintshopPiece;

/** Integrated-server generation and visual smoke test for all four printshop types. */
public final class PrintshopVariantClientGameTest implements FabricClientGameTest {
	private static final List<InvestigationRole> REQUIRED = List.of(
		InvestigationRole.PRESS_FRAME,
		InvestigationRole.MACHINE_REMAINS,
		InvestigationRole.CELLAR_CACHE,
		InvestigationRole.FLOOR_CACHE,
		InvestigationRole.MATRIX_BENCH,
		InvestigationRole.ARCHIVE_DESK,
		InvestigationRole.INK_STATION,
		InvestigationRole.PLAQUE_CLUE
	);

	@Override
	public void runTest(ClientGameTestContext context) {
		context.getInput().resizeWindow(1920, 1080);
		AtomicReference<List<Scene>> scenesRef = new AtomicReference<>(List.of());

		try (TestSingleplayerContext singleplayer = context.worldBuilder()
			.setUseConsistentSettings(true)
			.create()) {
			TestServerContext serverContext = singleplayer.getServer();
			singleplayer.getClientLevel().waitForChunksRender();

			serverContext.runOnServer(server -> {
				var level = server.overworld();
				List<Scene> scenes = new ArrayList<>();
				WorkshopVariant[] variants = WorkshopVariant.values();
				for (int i = 0; i < variants.length; i++) {
					WorkshopVariant variant = variants[i];
					int west = i * 40;
					int north = 32;
					int layout = i & 1;
					for (int chunkX = (west - 2) >> 4; chunkX <= (west + 20) >> 4; chunkX++) {
						for (int chunkZ = (north - 2) >> 4; chunkZ <= (north + 18) >> 4; chunkZ++) {
							level.getChunk(chunkX, chunkZ);
						}
					}
					AbandonedPrintshopPiece piece = new AbandonedPrintshopPiece(
						RandomSource.create(1200L + i),
						west,
						north,
						"client_test_" + variant.id(),
						variant,
						layout
					);
					piece.postProcess(
						level,
						level.structureManager(),
						level.getChunkSource().getGenerator(),
						RandomSource.create(2400L + i),
						BoundingBox.infinite(),
						new ChunkPos(west >> 4, north >> 4),
						new BlockPos(west, 64, north)
					);
					BoundingBox bounds = piece.getBoundingBox();
					assertVariant(level, variant, layout, bounds);
					BlockPos center = bounds.getCenter();
					scenes.add(new Scene(variant, center.getX(), bounds.minY(), center.getZ()));
				}
				scenesRef.set(List.copyOf(scenes));
			});

			serverContext.runCommand("time set noon");
			serverContext.runCommand("weather clear");
			serverContext.runCommand("gamemode spectator @a");
			context.getInput().pressKey(GLFW.GLFW_KEY_F1);

			for (Scene scene : scenesRef.get()) {
				serverContext.runCommand(
					"tp @a " + (scene.x() + 0.5) + " " + (scene.baseY() + 13.0) + " "
						+ (scene.z() - 10.5) + " 0 35"
				);
				context.waitTicks(12);
				singleplayer.getClientLevel().waitForChunksRender();
				takeScreenshot(context, "printshop_variant_" + scene.variant().id());
				serverContext.runCommand(
					"tp @a " + (scene.x() + 0.5) + " " + (scene.baseY() + 2.25) + " "
						+ (scene.z() + 0.5) + " 45 8"
				);
				context.waitTicks(6);
				takeScreenshot(context, "printshop_variant_" + scene.variant().id() + "_interior");
			}

			context.getInput().pressKey(GLFW.GLFW_KEY_F1);
		}
	}

	private static void assertVariant(
		net.minecraft.server.level.ServerLevel level,
		WorkshopVariant variant,
		int layout,
		BoundingBox bounds
	) {
		BoundingBox scan = bounds.inflatedBy(2, 5, 2);
		Map<InvestigationRole, Integer> roles = new EnumMap<>(InvestigationRole.class);
		int looseFloors = 0;
		int hiddenFloors = 0;
		int cobwebs = 0;
		int hangingPrints = 0;
		int laidPrints = 0;
		int populatedLaidPrints = 0;
		Block signature = signatureBlock(variant);
		boolean signatureFound = false;
		for (int x = scan.minX(); x <= scan.maxX(); x++) {
			for (int y = scan.minY(); y <= scan.maxY(); y++) {
				for (int z = scan.minZ(); z <= scan.maxZ(); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					Block block = level.getBlockState(pos).getBlock();
					if (block == signature) {
						signatureFound = true;
					}
					if (block == Blocks.COBWEB) {
						cobwebs++;
					} else if (block == ModBlocks.HANGING_POSTER) {
						hangingPrints++;
					} else if (block == ModBlocks.LAID_PAPER) {
						laidPrints++;
						if (level.getBlockEntity(pos) instanceof LaidPaperBlockEntity paper && !paper.page().isEmpty()) {
							populatedLaidPrints++;
						}
					}
					if (block == ModBlocks.LOOSE_INK_STAINED_FLOORBOARDS) {
						looseFloors++;
					} else if (block == ModBlocks.HIDDEN_FLOOR_COMPARTMENT) {
						hiddenFloors++;
					}
					if (level.getBlockEntity(pos) instanceof InvestigationBlockEntity investigation
						&& investigation.workshopId().equals("client_test_" + variant.id())) {
						roles.merge(InvestigationRole.byId(investigation.investigationRole()), 1, Integer::sum);
					}
				}
			}
		}
		for (InvestigationRole role : REQUIRED) {
			if (roles.getOrDefault(role, 0) != 1) {
				throw new AssertionError(variant.id() + " expected one " + role.id() + ", got " + roles);
			}
		}
		int expectedFloors = WorkshopLayoutPlan.suspiciousFloorCount(variant, layout, 4);
		if (hiddenFloors != 1 || looseFloors + hiddenFloors != expectedFloors) {
			throw new AssertionError(
				variant.id() + " suspicious floors: loose=" + looseFloors + " hidden=" + hiddenFloors
			);
		}
		if (!signatureFound) {
			throw new AssertionError(variant.id() + " missing signature block " + signature);
		}
		if (cobwebs < 4 || hangingPrints < 2 || laidPrints < 2 || populatedLaidPrints != laidPrints) {
			throw new AssertionError(
				variant.id() + " atmosphere: cobwebs=" + cobwebs
					+ ", hangingPrints=" + hangingPrints
					+ ", laidPrints=" + laidPrints
					+ ", populated=" + populatedLaidPrints
			);
		}
		EchoesInInk.LOGGER.info(
			"VARIANT_GAMETEST_OK variant={} layout={} roles={} suspiciousFloors={} cobwebs={} hangingPrints={} laidPrints={}",
			variant.id(), layout, roles, expectedFloors, cobwebs, hangingPrints, laidPrints
		);
	}

	private static Block signatureBlock(WorkshopVariant variant) {
		return switch (variant) {
			case RURAL_WOODCUT -> Blocks.LOOM;
			case TOWN_TYPE_FOUNDRY -> Blocks.BLAST_FURNACE;
			case SCHOLARLY_ARCHIVE -> Blocks.LECTERN;
			case BURNED_CLANDESTINE -> Blocks.COAL_BLOCK;
		};
	}

	private static Path takeScreenshot(ClientGameTestContext context, String name) {
		Path directory = Path.of(System.getProperty("user.dir"))
			.resolve("client-gametest-screenshots")
			.toAbsolutePath()
			.normalize();
		try {
			Files.createDirectories(directory);
		} catch (IOException exception) {
			throw new AssertionError("Could not create screenshot directory " + directory, exception);
		}
		Path screenshot = context.takeScreenshot(
			TestScreenshotOptions.of(name)
				.disableCounterPrefix()
				.withSize(1920, 1080)
				.withDestinationDir(directory)
		);
		if (!Files.isRegularFile(screenshot)) {
			throw new AssertionError("Missing printshop variant screenshot: " + screenshot);
		}
		EchoesInInk.LOGGER.info("Printshop variant screenshot: {}", screenshot);
		return screenshot;
	}

	private record Scene(WorkshopVariant variant, int x, int baseY, int z) {}
}
