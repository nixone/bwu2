package sk.nixone.bwu2.selection.actions;

import bwapi.Game;
import bwapi.TechType;
import bwapi.Unit;
import sk.nixone.bwu2.math.Vector2D;

public class UseTechAction implements UnitAction {

	private TechType tech;
	private Unit targetUnit;
	private Vector2D targetPosition;
	
	public UseTechAction(TechType tech) {
		this.tech = tech;
		targetUnit = null;
		targetPosition = null;
	}
	
	public UseTechAction(TechType tech, Unit target) {
		this.tech = tech;
		targetUnit = target;
	}
	
	public UseTechAction(TechType tech, Vector2D target) {
		this.tech = tech;
		targetPosition = target;
	}
	
	@Override
	public void execute(Game game, Unit unit) {
		if (targetUnit != null) {
			unit.useTech(tech, targetUnit);
		} else if (targetPosition != null) {
			unit.useTech(tech, targetPosition.toPosition());
		} else {
			unit.useTech(tech);
		}
	}
}
