package pl.peterwolf.echoesinink.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.util.PrintedContent;

/**
 * Client animation of press parts. Server phase/progress is authoritative;
 * this only presents motion. Always draws something visible once parts exist.
 */
public class PrintingPressRenderer implements BlockEntityRenderer<PrintingPressBlockEntity, PrintingPressRenderState> {
	private static final float WORKTABLE_RISE = 0.75F;
	private static final float CARRIAGE_CENTER_Y = WORKTABLE_RISE + 0.22F;
	private static final float MATRIX_CENTER_Y = WORKTABLE_RISE + 0.321F;
	private static final float METAL_TYPE_MATRIX_Y = WORKTABLE_RISE + 0.335F;
	private static final float METAL_TYPE_MATRIX_X_SCALE = 0.70F;
	private static final float METAL_TYPE_MATRIX_Y_SCALE = 0.18F;
	private static final float METAL_TYPE_MATRIX_Z_SCALE = 0.62F;
	private static final float WOODEN_MATRIX_Y = WORKTABLE_RISE + 0.328F;
	private static final float WOODEN_MATRIX_X_SCALE = 0.66F;
	private static final float WOODEN_MATRIX_Y_SCALE = 0.14F;
	private static final float WOODEN_MATRIX_Z_SCALE = 0.58F;
	private static final float INPUT_SHEET_CENTER_Y = WORKTABLE_RISE + 0.366F;
	private static final float OUTPUT_SHEET_CENTER_Y = WORKTABLE_RISE + 0.3225F;
	private static final float IMPRESSION_TEXT_Y = WORKTABLE_RISE + 0.348F;
	private static final float IMPRESSION_TEXT_Z_OFFSET = 0.0F;
	private static final float MAX_IMPRESSION_TEXT_SCALE = 0.0042F;
	private static final float IMPRESSION_TEXT_WIDTH = 0.4F;
	private static final float MAX_PLATEN_TRAVEL = 0.142F;
	/** Above the 28/16-block wooden cap even at maximum screw travel. */
	private static final float HANDLE_MOUNT_Y = WORKTABLE_RISE + 1.35F;
	private static final float[][] METAL_TYPE_CAPS = {
		{2.5F, 5.75F, 2.5F, 4.75F},
		{6.25F, 9.75F, 2.5F, 4.75F},
		{10.25F, 13.5F, 2.5F, 4.75F},
		{2.5F, 5.75F, 5.25F, 7.5F},
		{6.25F, 9.75F, 5.25F, 7.5F},
		{10.25F, 13.5F, 5.25F, 7.5F},
		{2.5F, 5.75F, 8.0F, 10.25F},
		{6.25F, 9.75F, 8.0F, 10.25F},
		{10.25F, 13.5F, 8.0F, 10.25F},
		{2.5F, 5.75F, 10.75F, 13.5F},
		{6.25F, 9.75F, 10.75F, 13.5F},
		{10.25F, 13.5F, 10.75F, 13.5F}
	};
	private static final float[][] FLAT_MATRIX_INK_MARKS = {
		{-0.18F, -0.16F, 0.12F, 0.035F},
		{0.0F, -0.16F, 0.12F, 0.035F},
		{0.18F, -0.16F, 0.12F, 0.035F},
		{-0.18F, -0.05F, 0.035F, 0.13F},
		{0.0F, -0.05F, 0.14F, 0.06F},
		{0.18F, -0.05F, 0.035F, 0.13F},
		{-0.18F, 0.16F, 0.12F, 0.035F},
		{0.0F, 0.16F, 0.12F, 0.035F},
		{0.18F, 0.16F, 0.12F, 0.035F}
	};
	/** Raised woodcut faces that take ink the same way metal type caps do. */
	private static final float[][] WOODEN_MATRIX_INK_CAPS = METAL_TYPE_CAPS;

	private final ItemModelResolver itemModelResolver;
	private final Font font;

	public PrintingPressRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
		this.font = context.font();
	}

	@Override
	public PrintingPressRenderState createRenderState() {
		return new PrintingPressRenderState();
	}

	@Override
	public void extractRenderState(
		PrintingPressBlockEntity be,
		PrintingPressRenderState state,
		float partialTick,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
		state.facing = be.getBlockState().getValue(PrintingPressBlock.FACING);
		state.phase = be.phase();
		// Smooth during PRESSING/RESETTING using partial ticks when possible.
		float base = be.animProgress();
		if (be.phase() == PressPhase.PRESSING && be.maxProgress() > 0) {
			base = (be.progress() + partialTick) / (float) be.maxProgress();
		} else if (be.phase() == PressPhase.RESETTING) {
			base = 1.0F - (be.progress() + partialTick) / 20.0F;
		} else if (be.phase() == PressPhase.INKING && be.maxProgress() > 0) {
			base = (be.progress() + partialTick) / (float) be.maxProgress();
		}
		state.animProgress = Mth.clamp(base, 0.0F, 1.0F);
		state.matrixInked = be.matrixInked();
		state.inkingProgress = state.phase == PressPhase.INKING
			? state.animProgress
			: state.matrixInked ? 1.0F : 0.0F;
		state.hasScrew = be.hasScrew();
		state.hasHandle = be.hasHandle();
		state.hasPlaten = be.hasPlaten();
		state.hasCarriage = be.hasCarriage();
		state.output = be.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).copy();

		Level level = be.getLevel();
		int seed = be.getBlockPos().hashCode();
		ItemStack matrix = be.getItem(PrintingPressBlockEntity.SLOT_MATRIX);
		state.metalTypeMatrix = matrix.is(ModItems.METAL_TYPE_PIECE);
		state.woodenMatrix = matrix.is(ModItems.WOODEN_PRINTING_MATRIX)
			|| matrix.is(ModItems.VILLAGE_CHRONICLE_MATRIX)
			|| matrix.is(ModItems.CHARCOAL_RUBBING);
		state.matrixRenderState = resolveItem(matrix, level, seed + 1);
		state.inkRenderState = resolveItem(
			be.getItem(PrintingPressBlockEntity.SLOT_INK), level, seed + 2
		);
		state.inkLayerRenderState = state.matrixRenderState == null
			? null
			: resolveItem(new ItemStack(Blocks.CONCRETE.black()), level, seed + 8);
		state.woodBedRenderState = state.woodenMatrix
			? resolveItem(new ItemStack(Blocks.SPRUCE_PLANKS), level, seed + 9)
			: null;
		ItemStack sheet = !state.output.isEmpty()
			? state.output
			: be.getItem(PrintingPressBlockEntity.SLOT_PAPER);
		state.sheetRenderState = resolveItem(sheet, level, seed + 3);

		boolean renderAssembly = state.phase != PressPhase.INCOMPLETE;
		state.screwRenderState = state.hasScrew || renderAssembly
			? resolveItem(new ItemStack(ModItems.PRESS_SCREW), level, seed + 4)
			: null;
		state.handleRenderState = state.hasHandle || renderAssembly
			? resolveItem(new ItemStack(ModItems.PRESS_HANDLE), level, seed + 5)
			: null;
		state.platenRenderState = state.hasPlaten || renderAssembly
			? resolveItem(new ItemStack(ModItems.PRESS_PLATEN), level, seed + 6)
			: null;
		state.carriageRenderState = state.hasCarriage || renderAssembly
			? resolveItem(new ItemStack(ModItems.PRESS_CARRIAGE), level, seed + 7)
			: null;
	}

	@Override
	public void submit(
		PrintingPressRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		CameraRenderState camera
	) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

		float carriageZ = carriageOffset(state);
		float platenY = platenOffset(state);
		float handleAngle = handleAngle(state);

		if (state.carriageRenderState != null) {
			submitItem(poseStack, collector, state, state.carriageRenderState,
				0.0F, CARRIAGE_CENTER_Y, carriageZ, 0.9F, 0.28F, 0.82F);
		}

		if (state.matrixRenderState != null && state.output.isEmpty()) {
			float inkCoverage = inkCoverage(state);
			if (state.metalTypeMatrix) {
				submitMetalTypeMatrix(
					poseStack, collector, state, state.matrixRenderState,
					state.inkLayerRenderState, carriageZ, inkCoverage
				);
			} else if (state.woodenMatrix) {
				submitWoodenMatrix(
					poseStack, collector, state, state.matrixRenderState,
					state.woodBedRenderState, state.inkLayerRenderState,
					carriageZ, inkCoverage
				);
			} else {
				submitFlatItem(poseStack, collector, state, state.matrixRenderState,
					0.0F, MATRIX_CENTER_Y, carriageZ, 0.64F);
				submitFlatMatrixInk(
					poseStack, collector, state, state.inkLayerRenderState,
					carriageZ, inkCoverage
				);
			}
		}

		if (state.sheetRenderState != null) {
			float sheetY = state.output.isEmpty() ? INPUT_SHEET_CENTER_Y : OUTPUT_SHEET_CENTER_Y;
			float sheetX = state.phase == PressPhase.INKING ? 0.43F : 0.0F;
			if (state.phase == PressPhase.INKING) {
				sheetY += 0.04F;
			}
			submitFlatItem(poseStack, collector, state, state.sheetRenderState,
				sheetX, sheetY, carriageZ, 0.72F);
		}

		if (state.inkRenderState != null && state.output.isEmpty()) {
			if (state.phase == PressPhase.INKING) {
				submitInkingTool(poseStack, collector, state, state.inkRenderState, carriageZ);
			} else if (state.phase != PressPhase.PRESSING && state.phase != PressPhase.RESETTING) {
				submitItem(poseStack, collector, state, state.inkRenderState,
					-0.36F, WORKTABLE_RISE + 0.37F, carriageZ + 0.16F, 0.22F, 0.22F, 0.22F);
			}
		}

		if (!state.output.isEmpty()) {
			submitPrintedContent(poseStack, collector, state, carriageZ);
		}

		if (state.platenRenderState != null) {
			submitItem(poseStack, collector, state, state.platenRenderState,
				0.0F, WORKTABLE_RISE + 0.61F + platenY, 0.0F, 0.82F, 0.32F, 0.68F);
		}

		if (state.screwRenderState != null) {
			poseStack.pushPose();
			poseStack.translate(0.0F, WORKTABLE_RISE + 0.88F + platenY, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(handleAngle * 1.8F));
			poseStack.scale(0.3F, 0.72F, 0.3F);
			draw(poseStack, collector, state, state.screwRenderState);
			poseStack.popPose();
		}

		if (state.handleRenderState != null) {
			poseStack.pushPose();
			poseStack.translate(0.0F, HANDLE_MOUNT_Y + platenY, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(handleAngle));
			// The extended spindle and iron socket stay above the upper timber even
			// at full pressure, while the two-block bar projects beyond both sides.
			poseStack.scale(1.0F, 0.3F, 0.3F);
			draw(poseStack, collector, state, state.handleRenderState);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private void submitItem(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState itemRenderState,
		float x, float y, float z,
		float sx, float sy, float sz
	) {
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.scale(sx, sy, sz);
		draw(poseStack, collector, state, itemRenderState);
		poseStack.popPose();
	}

	private void submitFlatItem(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState itemRenderState,
		float x,
		float y,
		float z,
		float scale
	) {
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(scale, scale, scale);
		draw(poseStack, collector, state, itemRenderState);
		poseStack.popPose();
	}

	private void submitMetalTypeMatrix(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState matrix,
		@Nullable ItemStackRenderState inkLayer,
		float carriageZ,
		float inkCoverage
	) {
		poseStack.pushPose();
		poseStack.translate(0.0F, METAL_TYPE_MATRIX_Y, carriageZ);
		poseStack.scale(
			METAL_TYPE_MATRIX_X_SCALE,
			METAL_TYPE_MATRIX_Y_SCALE,
			METAL_TYPE_MATRIX_Z_SCALE
		);
		draw(poseStack, collector, state, matrix);

		if (inkLayer != null && inkCoverage > 0.0F) {
			submitInkCaps(poseStack, collector, state, inkLayer, METAL_TYPE_CAPS, inkCoverage, 0.0125F, 0.025F);
		}
		poseStack.popPose();
	}

	private void submitWoodenMatrix(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState matrix,
		@Nullable ItemStackRenderState woodBed,
		@Nullable ItemStackRenderState inkLayer,
		float carriageZ,
		float inkCoverage
	) {
		poseStack.pushPose();
		poseStack.translate(0.0F, WOODEN_MATRIX_Y, carriageZ);
		poseStack.scale(
			WOODEN_MATRIX_X_SCALE,
			WOODEN_MATRIX_Y_SCALE,
			WOODEN_MATRIX_Z_SCALE
		);
		if (woodBed != null) {
			draw(poseStack, collector, state, woodBed);
		}
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.52F, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(0.92F, 0.92F, 0.92F);
		draw(poseStack, collector, state, matrix);
		poseStack.popPose();
		if (inkLayer != null && inkCoverage > 0.0F) {
			submitInkCaps(poseStack, collector, state, inkLayer, WOODEN_MATRIX_INK_CAPS, inkCoverage, 0.62F, 0.10F);
		}
		poseStack.popPose();
	}

	private void submitInkCaps(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState inkLayer,
		float[][] caps,
		float inkCoverage,
		float localY,
		float localHeight
	) {
		int capCount = Mth.clamp(Mth.ceil(inkCoverage * caps.length), 0, caps.length);
		for (int index = 0; index < capCount; index++) {
			float[] cap = caps[index];
			float centerX = (cap[0] + cap[1]) / 32.0F - 0.5F;
			float centerZ = (cap[2] + cap[3]) / 32.0F - 0.5F;
			float width = (cap[1] - cap[0]) / 16.0F;
			float depth = (cap[3] - cap[2]) / 16.0F;
			poseStack.pushPose();
			poseStack.translate(centerX, localY, centerZ);
			poseStack.scale(width, localHeight, depth);
			draw(poseStack, collector, state, inkLayer);
			poseStack.popPose();
		}
	}

	private void submitFlatMatrixInk(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		@Nullable ItemStackRenderState inkLayer,
		float carriageZ,
		float inkCoverage
	) {
		if (inkLayer == null || inkCoverage <= 0.0F) {
			return;
		}
		int markCount = Mth.clamp(Mth.ceil(inkCoverage * FLAT_MATRIX_INK_MARKS.length), 0, FLAT_MATRIX_INK_MARKS.length);
		for (int index = 0; index < markCount; index++) {
			float[] mark = FLAT_MATRIX_INK_MARKS[index];
			submitItem(
				poseStack, collector, state, inkLayer,
				mark[0], MATRIX_CENTER_Y + 0.028F, carriageZ + mark[1],
				mark[2], 0.012F, mark[3]
			);
		}
	}

	private void submitInkingTool(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState inkTool,
		float carriageZ
	) {
		float[] tool = inkingToolOffset(state, carriageZ);
		float strokes = Mth.clamp(state.inkingProgress, 0.0F, 0.9999F) * 4.0F;
		int pass = Math.min(3, Mth.floor(strokes));
		float passProgress = strokes - pass;
		float smoothPass = passProgress * passProgress * (3.0F - 2.0F * passProgress);
		float sweep = (pass & 1) == 0 ? smoothPass : 1.0F - smoothPass;

		poseStack.pushPose();
		poseStack.translate(tool[0], MATRIX_CENTER_Y + 0.10F + tool[2], tool[1]);
		poseStack.mulPose(Axis.YP.rotationDegrees(18.0F * Mth.sin(state.inkingProgress * Mth.TWO_PI)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F + 20.0F * sweep));
		poseStack.scale(0.22F, 0.22F, 0.22F);
		draw(poseStack, collector, state, inkTool);
		poseStack.popPose();
	}

	private static float[] inkingToolOffset(PrintingPressRenderState state, float carriageZ) {
		float strokes = Mth.clamp(state.inkingProgress, 0.0F, 0.9999F) * 4.0F;
		int pass = Math.min(3, Mth.floor(strokes));
		float passProgress = strokes - pass;
		float smoothPass = passProgress * passProgress * (3.0F - 2.0F * passProgress);
		float sweep = (pass & 1) == 0 ? smoothPass : 1.0F - smoothPass;
		float x = Mth.lerp(sweep, -0.27F, 0.27F);
		float z = carriageZ + Mth.lerp(pass / 3.0F, -0.17F, 0.17F);
		float lift = 0.025F + Mth.sin(passProgress * Mth.PI) * 0.035F;
		return new float[] {x, z, lift};
	}

	private static float inkCoverage(PrintingPressRenderState state) {
		if (state.matrixInked) {
			return 1.0F;
		}
		if (state.phase != PressPhase.INKING) {
			return 0.0F;
		}
		float progress = Mth.clamp(state.inkingProgress, 0.0F, 1.0F);
		return progress * progress * (3.0F - 2.0F * progress);
	}

	/**
	 * The 16px item icon still identifies the output in inventory, while these
	 * localized lines make the fresh impression readable on the carriage.
	 */
	private void submitPrintedContent(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		float carriageZ
	) {
		String suffix = printedContentSuffix(state.output);
		if (suffix == null) {
			return;
		}

		FormattedCharSequence[] lines = new FormattedCharSequence[3];
		int widestLine = 1;
		for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
			lines[lineIndex] = Component.translatable(
				"press.echoes_in_ink.impression." + suffix + "." + (lineIndex + 1)
			).getVisualOrderText();
			widestLine = Math.max(widestLine, font.width(lines[lineIndex]));
		}
		float textScale = Math.min(MAX_IMPRESSION_TEXT_SCALE, IMPRESSION_TEXT_WIDTH / widestLine);

		poseStack.pushPose();
		poseStack.translate(0.0F, IMPRESSION_TEXT_Y, carriageZ + IMPRESSION_TEXT_Z_OFFSET);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(textScale, textScale, textScale);

		for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
			FormattedCharSequence line = lines[lineIndex];
			float x = -font.width(line) / 2.0F;
			float y = -10.0F + lineIndex * 10.0F;
			collector.submitText(
				poseStack,
				x,
				y,
				line,
				false,
				Font.DisplayMode.POLYGON_OFFSET,
				state.lightCoords,
				0xFF18130F,
				0,
				0
			);
		}
		poseStack.popPose();
	}

	@Nullable
	private static String printedContentSuffix(ItemStack output) {
		return PrintedContent.impressionSuffix(output);
	}

	@Nullable
	private ItemStackRenderState resolveItem(ItemStack stack, @Nullable Level level, int seed) {
		if (stack.isEmpty()) {
			return null;
		}
		ItemStackRenderState itemRenderState = new ItemStackRenderState();
		itemModelResolver.updateForTopItem(
			itemRenderState,
			stack,
			ItemDisplayContext.FIXED,
			level,
			null,
			seed
		);
		return itemRenderState.isEmpty() ? null : itemRenderState;
	}

	private void draw(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStackRenderState itemRenderState
	) {
		itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
	}

	private static float carriageOffset(PrintingPressRenderState state) {
		return switch (state.phase) {
			case CARRIAGE_IN, PRESSING, RESETTING, IMPRESSION_DONE -> 0.05F;
			// Keep every outward/resting state just beyond the fixed bed rails.
			// A shallower idle offset made the carriage runners overlap the rails
			// after the finished sheet was collected and shimmer while the camera moved.
			case INCOMPLETE, IDLE, INKING, OUTPUT_READY, JAMMED -> 0.85F;
		};
	}

	private static float platenOffset(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING) {
			return -MAX_PLATEN_TRAVEL * state.animProgress;
		}
		if (state.phase == PressPhase.RESETTING) {
			return -MAX_PLATEN_TRAVEL * state.animProgress;
		}
		return 0.0F;
	}

	private static float handleAngle(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING || state.phase == PressPhase.RESETTING) {
			return 100.0F * state.animProgress;
		}
		return 0.0F;
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}
}
