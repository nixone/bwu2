package sk.nixone.bwu2.selection.actions;

import java.util.HashMap;

import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import sk.nixone.bwu2.selection.UnitSelector.UnitTypeSelector;

public class UnitActionBuffer {
	
	private Game game;
	
	private HashMap<Unit, UnitAction> unitActions = new HashMap<>();
	
	public UnitActionBuffer(Game game) {
		this.game = game;
	}
	
	public void act(Unit unit, UnitAction action) {
		unitActions.put(unit, action);
	}
	
	public void executeAll() {
		for (Unit unit : unitActions.keySet()) {
			if (!unit.isAttackFrame() && !unit.isStartingAttack()) {
				unitActions.get(unit).execute(game, unit);
			}
		}
		unitActions.clear();
	}
	
	public void clear(Unit unit) {
		unitActions.remove(unit);
	}
	
	public void draw(Game game) {
		for (Unit unit : unitActions.keySet()) {
			//if (unit.getType() != UnitType.Protoss_High_Templar) continue;
			//System.out.println("["+game.getFrameCount()+"] "+unit.getType()+": "+unitActions.get(unit).getClass().getSimpleName());
		}
	}
}
