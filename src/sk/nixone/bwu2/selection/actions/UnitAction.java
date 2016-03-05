package sk.nixone.bwu2.selection.actions;

import bwapi.Game;
import bwapi.Unit;

public interface UnitAction {
	public void execute(Game game, Unit unit);
}
