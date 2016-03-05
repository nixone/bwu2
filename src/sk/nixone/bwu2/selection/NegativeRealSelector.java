package sk.nixone.bwu2.selection;

import bwapi.Unit;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;

public class NegativeRealSelector implements RealSelector {

	private RealSelector base;
	
	public NegativeRealSelector(RealSelector base) {
		this.base = base;
	}
	
	@Override
	public double getValue(Unit unit) {
		return base.getValue(unit)*-1;
	}

}
