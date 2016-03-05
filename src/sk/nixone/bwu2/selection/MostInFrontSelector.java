package sk.nixone.bwu2.selection;

import sk.nixone.bwu2.math.Vector2D;

public class MostInFrontSelector extends NegativeRealSelector {

	public MostInFrontSelector(Vector2D origin, Vector2D forward) {
		super(new DotProductSelector(origin, forward));
	}
	
}
