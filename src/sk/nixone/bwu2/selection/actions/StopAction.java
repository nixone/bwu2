package sk.nixone.bwu2.selection.actions;

import bwapi.Game;
import bwapi.Unit;

public class StopAction implements UnitAction {

	@Override
	public void execute(Game game, Unit unit) {
		unit.stop();
	}
}
