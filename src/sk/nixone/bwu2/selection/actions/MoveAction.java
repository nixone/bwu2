package sk.nixone.bwu2.selection.actions;

import bwapi.Game;
import bwapi.Unit;
import sk.nixone.bwu2.math.Relativity;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;

public class MoveAction implements UnitAction {
	
	private Unit unit = null;
	private Vector2D vector = null;
	private Relativity relativity = null;
	
	public MoveAction(Unit unit) {
		this.unit = unit;
	}
	
	public MoveAction(Vector2D vector, Relativity relativity) {
		this.vector = vector;
		this.relativity = relativity;
	}
	
	@Override
	public void execute(Game game, Unit unit) {
		if (this.unit != null) {
			unit.move(unit.getPosition());
		} else {
			if (relativity == Relativity.ABSOLUTE) {
				unit.move(vector.toPosition());
			} else {
				unit.move(Vector2DMath.toVector(unit.getPosition()).add(vector).toPosition());
			}
		}
	}
}
