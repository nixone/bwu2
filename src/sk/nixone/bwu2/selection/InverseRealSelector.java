package sk.nixone.bwu2.selection;

import bwapi.Unit;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;

public class InverseRealSelector implements RealSelector {

	private RealSelector base;
	
	public InverseRealSelector(RealSelector base) {
		this.base = base;
	}
	
	@Override
	public double getValue(Unit unit) {
		return 1./base.getValue(unit);
	}
}
