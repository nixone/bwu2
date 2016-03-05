package sk.nixone.bwu2.sample;

import java.util.List;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import sk.nixone.bwu2.math.Comparison;
import sk.nixone.bwu2.math.Relativity;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;
import sk.nixone.bwu2.selection.DistanceSelector;
import sk.nixone.bwu2.selection.MostInBackSelector;
import sk.nixone.bwu2.selection.MostInFrontSelector;
import sk.nixone.bwu2.selection.RealComparisonSelector;
import sk.nixone.bwu2.selection.UnitSelector;
import sk.nixone.bwu2.selection.UnitSelector.UnitTypeSelector;
import sk.nixone.bwu2.selection.UnitSet;
import sk.nixone.bwu2.selection.actions.AttackMoveAction;
import sk.nixone.bwu2.selection.actions.MoveAction;
import sk.nixone.bwu2.selection.actions.StopAction;
import sk.nixone.bwu2.selection.actions.UnitActionBuffer;

public class Bot extends DefaultBWListener {
	
	private Mirror mirror = new Mirror();
    private Game game;
    private Player self;
    private UnitActionBuffer actionBuffer;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        game.enableFlag(1);
        game.setLocalSpeed(30);
        actionBuffer = new UnitActionBuffer(game);
        
        BWTA.readMap();
        BWTA.analyze();
    }
    
    private Vector2D pointOfAttack = null;
    private Vector2D vectorOfAttack = null;
    private Vector2D armyPosition = null;
    
    private UnitSet mine = null;
    private UnitSet enemies = null;
    private UnitSet zealots = null;
    private UnitSet templars = null;
    private UnitSet dragoons = null;
    
    @Override
    public void onFrame() {
    	try {
	        mine = new UnitSet(self.getUnits());
	        armyPosition = mine.getArithmeticCenter();
	        zealots = mine.where(new UnitTypeSelector(UnitType.Protoss_Zealot));
	        templars = mine.where(new UnitTypeSelector(UnitType.Protoss_High_Templar));
	        dragoons = mine.where(new UnitTypeSelector(UnitType.Protoss_Dragoon));
	        enemies = new UnitSet(game.getAllUnits()).minus(mine);
	        
	        game.drawTextScreen(10, 10, "Frame: "+game.getFrameCount());
	        
	        if (enemies.isEmpty()) {
	        	boolean amAtStart = false;
	        	Vector2D otherStart = null;
	        	
	        	for(TilePosition startLocation : game.getStartLocations()) {
	        		Vector2D position = Vector2DMath.toVector(startLocation.toPosition());
	        		if (mine.areAt(position, 300)) {
	        			amAtStart = true;
	        		} else {
	        			otherStart = position;
	        		}
	        	}
	        	
	        	if (amAtStart) {
	        		pointOfAttack = otherStart;
	        	}
	        } else {
	        	pointOfAttack = enemies.getArithmeticCenter();
	        }
	        vectorOfAttack = pointOfAttack.sub(mine.getArithmeticCenter()).normalize();
	        
	        whatToDo();
	        
	        if (game.getFrameCount() % 25 == 0) {
	        	actionBuffer.executeAll();
	        }
	        
	        drawDebug();
    	} catch(Throwable t) {
    		t.printStackTrace();
    	}
    }
    
    private void drawDebug() {
    	game.drawCircleMap(mine.getArithmeticCenter().toPosition(), 10, Color.Green);
    	game.drawLineMap(armyPosition.toPosition(), armyPosition.add(vectorOfAttack.scale(200)).toPosition(), Color.Green);
    }
    
    private void whatToDo() {
    	if (enemies.isEmpty()) {
    		mine.act(actionBuffer, new AttackMoveAction(pointOfAttack, Relativity.ABSOLUTE));
    		
    		holdLine(UnitType.Protoss_Zealot, 100, 250);
    		holdLine(UnitType.Protoss_Dragoon, -50, 100);
    		holdLine(UnitType.Protoss_High_Templar, -150, -50);
    	} else {
    		zealots.act(actionBuffer, new AttackMoveAction(enemies.getArithmeticCenter(), Relativity.ABSOLUTE));
    		dragoons.act(actionBuffer, new AttackMoveAction(enemies.getArithmeticCenter(), Relativity.ABSOLUTE));
    		doStorms();
    		goAwayFromStorms();
    		
    		// has to be last
    		focusFire();
    	}
    }
    
    private void holdLine(UnitType type, float min, float max) {
    	Vector2D minCenter = armyPosition.add(vectorOfAttack.scale(min));
    	Vector2D maxCenter = armyPosition.add(vectorOfAttack.scale(max));
    	Vector2D lineCenter = minCenter.add(maxCenter).scale(0.5f);
    	
    	for (Unit unit : mine.where(new UnitTypeSelector(type))) {
    		Vector2D unitRelativePosition = Vector2DMath.toVector(unit.getPosition()).sub(armyPosition);
    		float line = Vector2DMath.dotProduct(unitRelativePosition, vectorOfAttack);
    		if (line < min || line > max) {
    			actionBuffer.act(unit, new MoveAction(lineCenter, Relativity.ABSOLUTE));
    			game.drawLineMap(lineCenter.toPosition(), unit.getPosition(), Color.White);
    		}
    	}

    	Vector2D[] orthos = vectorOfAttack.scale(500).getOrthogonal();
    	
    	game.drawLineMap(minCenter.add(orthos[0]).toPosition(), minCenter.add(orthos[1]).toPosition(), Color.Blue);
    	game.drawLineMap(maxCenter.add(orthos[0]).toPosition(), maxCenter.add(orthos[1]).toPosition(), Color.Blue);
    }
    
    private void goAwayFromStorms() {
    	if (!mine.where(UnitSelector.IS_UNDER_STORM).isEmpty()) {
    		Vector2D avoidance = vectorOfAttack.scale(-100);
    		mine.act(actionBuffer, new MoveAction(avoidance, Relativity.RELATIVE));
    	}
    }
    
    private void focusFire() {
    	for(Unit unit : zealots.union(dragoons)) {
    		if (!unit.isAttackFrame()) {
    			UnitSet enemiesInRange = enemies
    					.whereLessOrEqual(new DistanceSelector(unit), unit.getType().groundWeapon().maxRange());
    			
    			List<Unit> toKill = enemiesInRange.pickNOrdered(1, UnitSelector.HIT_POINTS);
    			if (!toKill.isEmpty()) {
    				unit.attack(toKill.get(0));
    				game.drawLineMap(unit.getPosition(), toKill.get(0).getPosition(), Color.Red);
    			}
    		}
    	}
    }
    
    private void doStorms() {
    	for (Unit templar : templars) {
			Unit bestTarget = null;
			int bestTargetCount = 2;
			for (Unit target : enemies) {
				int enemyCount = enemies.where(
								new RealComparisonSelector(
										new DistanceSelector(target), 
										TechType.Psionic_Storm.getWeapon().medianSplashRadius(),
										Comparison.LESS_OR_EQUAL
								)
							)
							.size();
				
				int mineCount = mine.where(
						new RealComparisonSelector(
								new DistanceSelector(target), 
								TechType.Psionic_Storm.getWeapon().medianSplashRadius(),
								Comparison.LESS_OR_EQUAL
						)
					)
					.size();
				
				int count = enemyCount - mineCount;
				
				if (count > bestTargetCount) {
					bestTarget = target;
					bestTargetCount = count;
				}
			}
			if (bestTarget != null) {
				if (game.getFrameCount() % 50 == 35) {
					game.drawCircleMap(bestTarget.getPosition(), 100, Color.Blue);
					templar.useTech(TechType.Psionic_Storm, bestTarget);
				} else if (game.getFrameCount() % 50 == 15) {
					templar.move(mine.getArithmeticCenter().sub(vectorOfAttack.scale(100)).toPosition());
				}
			}
		}
    }
    
    private void waitFor(UnitType firstUnitType, UnitType lastUnitType, float distance) {
    	List<Unit> firsts = mine
    			.where(new UnitTypeSelector(firstUnitType))
    			.pickNOrdered(1, new MostInFrontSelector(armyPosition, vectorOfAttack));
    	
    	List<Unit> lasts = mine
    			.where(new UnitTypeSelector(lastUnitType))
    			.pickNOrdered(1, new MostInBackSelector(armyPosition, vectorOfAttack));
    	
    	if (!firsts.isEmpty() && !lasts.isEmpty()) {
    		Unit first = firsts.get(0);
    		Unit last = lasts.get(0);
    		
    		game.drawLineMap(first.getPosition(), last.getPosition(), Color.White);
    		
    		float realDistance = Vector2DMath.toVector(first.getPosition()).sub(Vector2DMath.toVector(last.getPosition())).length;
    	
    		if (realDistance > distance*1.1f) {
    			actionBuffer.act(first, new MoveAction(last));
    		} else if (realDistance > distance) {
    			actionBuffer.act(first, new StopAction());
    		}
    	}
    }
    
    public static void main(String[] args) {
        new Bot().run();
    }
}
