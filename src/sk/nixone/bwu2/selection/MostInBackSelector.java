package sk.nixone.bwu2.selection;

import sk.nixone.bwu2.math.Vector2D;

public class MostInBackSelector extends DotProductSelector {

	public MostInBackSelector(Vector2D origin, Vector2D forward) {
		super(origin, forward);
	}

}
