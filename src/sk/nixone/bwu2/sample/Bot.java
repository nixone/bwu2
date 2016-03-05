package sk.nixone.bwu2.sample;

import java.util.Iterator;
import java.util.List;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WalkPosition;
import bwta.BWTA;
import sk.nixone.bwu2.math.Comparison;
import sk.nixone.bwu2.math.Relativity;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;
import sk.nixone.bwu2.path.Map;
import sk.nixone.bwu2.selection.DistanceSelector;
import sk.nixone.bwu2.selection.RealComparisonSelector;
import sk.nixone.bwu2.selection.UnitSelector;
import sk.nixone.bwu2.selection.UnitSelector.UnitTypeSelector;
import sk.nixone.bwu2.selection.UnitSet;
import sk.nixone.bwu2.selection.actions.AttackMoveAction;
import sk.nixone.bwu2.selection.actions.MoveAction;
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
    
    private List<int[]> path = null;
    private Map map = null;
    
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
	       
	        if (map == null) {
	        	map = new Map(game);
	        	map = map.remapFromBound(5);
	        }
	        
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
	        path = map.getPath(armyPosition.toWalkPosition(), pointOfAttack.toWalkPosition());
	        
	        whatToDo();
	        
	        if (game.getFrameCount() % 10 == 0) {
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
    	
    	if (path != null) {
    		game.drawTextScreen(10, 50, "Army: "+armyPosition);
    		Position lastPosition = null;
        	Iterator<int[]> it = path.iterator();
        	while (it.hasNext()) {
        		int[] p = it.next();
        		Position current = new Vector2D(new WalkPosition(p[0], p[1])).toPosition();
        		
        		if (lastPosition != null) {
        			game.drawLineMap(lastPosition, current, Color.Green);
        		} else {
        			game.drawTextScreen(10, 30, "First: "+current);
        		}
        		
        		lastPosition = current;
        	}
        	game.drawTextScreen(10, 40, "Last: "+lastPosition);
    	}
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

    	Vector2D[] orthos = vectorOfAttack.scale(50).getOrthogonal();
    	
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
    		UnitSet enemiesInRange = enemies
    				.where(UnitSelector.CAN_ATTACK_GROUND)
					.whereLessOrEqual(new DistanceSelector(unit), unit.getType().groundWeapon().maxRange()*2f);
    		
			List<Unit> toKill = enemiesInRange.pickNOrdered(1, UnitSelector.HIT_POINTS);
			
			if (!toKill.isEmpty()) {
				game.drawLineMap(unit.getPosition(), toKill.get(0).getPosition(), Color.Red);
				
				if (!unit.isAttackFrame()) {
					Unit target = toKill.get(0);
					unit.attack(target, true);
					game.drawCircleMap(target.getPosition(), 10, Color.Red, true);
					game.drawCircleMap(unit.getPosition(), 10, Color.Red, true);
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
    
    public static void main(String[] args) {
        new Bot().run();
    }
}
