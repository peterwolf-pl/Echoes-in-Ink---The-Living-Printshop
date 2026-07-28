package pl.peterwolf.echoesinink.echo;

import java.util.List;

/**
 * Scripted beats for The Last Print Run (percent of total duration 0..1).
 */
public final class EchoScript {
	public static final String LAST_PRINT_RUN = "last_print_run";

	public record Beat(float at, String subtitleKey, String sfx) {}

	/** Relative timeline (~30s default). */
	public static final List<Beat> LAST_PRINT_BEATS = List.of(
		new Beat(0.00F, "echo.echoes_in_ink.last_print.darken", "ambient"),
		new Beat(0.08F, "echo.echoes_in_ink.last_print.dust", "dust"),
		new Beat(0.18F, "echo.echoes_in_ink.last_print.printer", "footstep"),
		new Beat(0.32F, "echo.echoes_in_ink.last_print.helper", "footstep"),
		new Beat(0.45F, "echo.echoes_in_ink.last_print.press", "press"),
		new Beat(0.62F, "echo.echoes_in_ink.last_print.impact", "impact"),
		new Beat(0.72F, "echo.echoes_in_ink.last_print.hide", "hide"),
		new Beat(0.85F, "echo.echoes_in_ink.last_print.fade", "fade"),
		new Beat(0.95F, "echo.echoes_in_ink.last_print.clue", "clue")
	);

	private EchoScript() {}

	public static String archiveEntryId(String echoId) {
		if (LAST_PRINT_RUN.equals(echoId)) {
			return "echo_last_print";
		}
		return "echo_" + echoId;
	}
}
