package pl.peterwolf.echoesinink.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.item.ReadablePrintItem;

/** Visual and readable hanging-poster designs used by generated printshops. */
public enum PosterKind implements StringRepresentable {
	WARNING("warning"),
	WOODCUT("woodcut"),
	CHRONICLE("chronicle"),
	NOTICE("notice"),
	MAP("map"),
	SPECIMEN("specimen");

	private final String name;

	PosterKind(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Item dropItem() {
		return switch (this) {
			case WARNING -> ModItems.PRINTED_WARNING_POSTER;
			case WOODCUT -> ModItems.DECORATIVE_WOODCUT;
			case CHRONICLE -> ModItems.VILLAGE_CHRONICLE_PRINT;
			case NOTICE -> ModItems.FORBIDDEN_NOTICE_PRINT;
			case MAP -> ModItems.WORKSHOP_MAP_FRAGMENT;
			case SPECIMEN -> ModItems.PRINTERS_INSTRUCTION_SHEET;
		};
	}

	public static PosterKind fromItem(Item item) {
		if (item == ModItems.DECORATIVE_WOODCUT) {
			return WOODCUT;
		}
		if (item == ModItems.VILLAGE_CHRONICLE_PRINT) {
			return CHRONICLE;
		}
		if (item == ModItems.FORBIDDEN_NOTICE_PRINT) {
			return NOTICE;
		}
		if (item == ModItems.WORKSHOP_MAP_FRAGMENT) {
			return MAP;
		}
		if (item == ModItems.PRINTERS_INSTRUCTION_SHEET) {
			return SPECIMEN;
		}
		return WARNING;
	}

	public void showPrint(net.minecraft.server.level.ServerPlayer player) {
		if (dropItem() instanceof ReadablePrintItem readable) {
			readable.showPrint(player);
		}
	}
}
