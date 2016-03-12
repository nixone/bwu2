package sk.nixone.bwu2.selection.actions;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import sk.nixone.bwu2.math.Relativity;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;

public class AttackMoveAction implements UnitAction {
	
	private Unit unit = null;
	private Vector2D vector = null;
	private Relativity relativity = null;
	
	public AttackMoveAction(Unit unit) {
		this.unit = unit;
	}
	
	public AttackMoveAction(Vector2D vector, Relativity relativity) {
		this.vector = vector;
		this.relativity = relativity;
	}
	
	@Override
	public void execute(Game game, Unit unit) {
		if (this.unit != null) {
			if (unit.getType().canAttack()) {
				unit.attack(unit, true);
			} else {
				unit.move(unit.getPosition());
			}
		} else {
			Position position;
			if (relativity == Relativity.ABSOLUTE) {
				position = vector.toPosition();
			} else {
				position = Vector2DMath.toVector(unit.getPosition()).add(vector).toPosition();
			}
			if (unit.getType().canAttack()) {
				unit.attack(position, true);
			} else {
				unit.move(position);
			}
		}
	}
}
