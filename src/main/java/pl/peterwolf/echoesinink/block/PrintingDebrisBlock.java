package pl.peterwolf.echoesinink.block;

/**
 * Printing debris uses the shared investigatable block with DEBRIS loot profile.
 * Kept as a named type for clarity in commands and docs.
 */
public class PrintingDebrisBlock extends InvestigatableBlock {
	public PrintingDebrisBlock(Properties properties) {
		super(properties, InvestigationLoot.Profile.DEBRIS);
	}
}
