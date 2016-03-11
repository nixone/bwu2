package sk.nixone.bwu2.sample;

import java.util.Iterator;
import java.util.LinkedList;
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
    
    private LinkedList<Vector2D> pointsToDiscover = new LinkedList<>();
    private Vector2D pointToDiscover = null;
    
    private Vector2D pointOfAttack = null;
    private Vector2D vectorOfAttack = null;
    private Vector2D armyPosition = null;
    
    private List<WalkPosition> path = null;
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
	        	map = new Map(game)
	        			.remapFromBound(10)
	        			.downscale(2, true);
	        }
	        
	        game.drawTextScreen(10, 10, "Frame: "+game.getFrameCount());
	        
	        if (pointsToDiscover.isEmpty()) {
	        	for(TilePosition startLocation : game.getStartLocations()) {
	        		Vector2D position = Vector2DMath.toVector(startLocation.toPosition());
	        		if (!mine.areAt(position, 100)) {
	        			pointsToDiscover.add(position);
	        		}
	        	}
	        }
	        if (pointToDiscover == null || mine.areAt(pointToDiscover, 100)) {
	        	pointToDiscover = pointsToDiscover.removeFirst();
	        }
	        
	        if (pointToDiscover != null) {
	        	path = map.getPath(armyPosition.toWalkPosition(), pointToDiscover.toWalkPosition());
	        }
	        
	        if (!enemies.isEmpty()) {
	        	pointOfAttack = enemies.getArithmeticCenter();
	        } else if(path != null && !path.isEmpty()) {
	        	pointOfAttack = new Vector2D(path.get(0));
	        } else {
	        	pointOfAttack = new Vector2D(0, 0);
	        }
	        
	        vectorOfAttack = pointOfAttack.sub(mine.getArithmeticCenter()).normalize();
	        
	        whatToDo();
	        
	        if (game.getFrameCount() % 3 == 0) {
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
        	Iterator<WalkPosition> it = path.iterator();
        	while (it.hasNext()) {
        		Position current = new Vector2D(it.next()).toPosition();
        		
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
    		for (Unit unit : mine) {
    			Vector2D newPosition = new Vector2D(unit.getPosition())
    					.sub(armyPosition)
    					.add(pointOfAttack);
    			
    			actionBuffer.act(unit, new AttackMoveAction(newPosition, Relativity.ABSOLUTE));
    		}
    		
    		holdLine(UnitType.Protoss_Zealot, 150, 300);
    		holdLine(UnitType.Protoss_Dragoon, 0, 100);
    		holdLine(UnitType.Protoss_High_Templar, -50, 0);
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
    	UnitSet units = mine.where(new UnitTypeSelector(type));
    	
    	Vector2D minCenter = armyPosition.add(vectorOfAttack.scale(min));
    	Vector2D maxCenter = armyPosition.add(vectorOfAttack.scale(max));
    	Vector2D lineCenter = minCenter.add(maxCenter).scale(0.5f);
    	
    	for (Unit unit : units) {
    		Vector2D unitRelativePosition = Vector2DMath.toVector(unit.getPosition()).sub(armyPosition);
    		float line = Vector2DMath.dotProduct(unitRelativePosition, vectorOfAttack);
    		
    		if (line < min || line > max) {
    			Vector2D lineRelative = new Vector2D(unit.getPosition()).sub(lineCenter);
    			Vector2D lineVector = vectorOfAttack.getOrthogonal()[0];
    			float positionInLine = Vector2DMath.dotProduct(lineRelative, lineVector);
    			float normalizedPositionInLine = positionInLine / units
    					.getMaximumDistanceFrom(new DistanceSelector(lineCenter));
    			
    			float realPositionInLine = normalizedPositionInLine * units.size() * 32;
    			
    			Vector2D whereToMove = lineCenter.add(lineVector.scale(realPositionInLine));
    			
    			actionBuffer.act(unit, new MoveAction(whereToMove, Relativity.ABSOLUTE));
    			game.drawLineMap(whereToMove.toPosition(), unit.getPosition(), Color.White);
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
    	int dragoonRange = (int)(UnitType.Protoss_Dragoon.groundWeapon().maxRange() * 1.5f);
    	
    	for(Unit unit : zealots) {
    		int realRange = unit.getType().airWeapon().maxRange();
    		if (unit.getType().equals(UnitType.Protoss_Dragoon)) {
    			realRange = dragoonRange;
    		}
    		
    		int testRange = (int)(realRange*1.5f);
    		
    		UnitSet enemiesInRange = enemies
    				.where(UnitSelector.CAN_ATTACK_GROUND)
					.whereLessOrEqual(new DistanceSelector(unit), testRange);
    		
			List<Unit> toKill = enemiesInRange.pickNOrdered(1, UnitSelector.HIT_POINTS);
			
			if (!toKill.isEmpty()) {
				game.drawLineMap(unit.getPosition(), toKill.get(0).getPosition(), Color.Red);
				
				if (!unit.isAttackFrame() && !unit.isStartingAttack()) {
					Unit target = toKill.get(0);
					unit.attack(target, true);
					game.drawCircleMap(target.getPosition(), 10, Color.Red, true);
					game.drawCircleMap(unit.getPosition(), 10, Color.Red, true);
				}
			}
    	}
    }
    
    private int nextPossibleStorm = 0;
    
    private void doStorms() {
    	float splashRadius = TechType.Psionic_Storm.getWeapon().medianSplashRadius() * 1.5f;
    	float stormRange = TechType.Psionic_Storm.getWeapon().maxRange();
    	
    	for (Unit templar : templars) {
			Vector2D bestTarget = null;
			float bestScore = 0.5f;
			for (Unit target : enemies) {
				for (int ox=-2; ox<=2; ox++) {
					for (int oy=-2; oy<=2; oy++) {
						int enemyCount = enemies.where(
								new RealComparisonSelector(
										new DistanceSelector(target), 
										splashRadius,
										Comparison.LESS_OR_EQUAL
								)
							)
							.size();
						
						int mineCount = mine.where(
								new RealComparisonSelector(
										new DistanceSelector(target), 
										splashRadius,
										Comparison.LESS_OR_EQUAL
								)
							)
							.size();
						
						float score = enemyCount - mineCount*1.9f;
						
						if (score > bestScore) {
							bestTarget = new Vector2D(target.getPosition()).add(new Vector2D(ox*16, oy*16));
							bestScore = score;
						}
					}
				}
			}
			if (bestTarget != null) {
				float realRange = new Vector2D(templar.getPosition()).sub(bestTarget).length;
				game.drawCircleMap(bestTarget.toPosition(), (int)stormRange, Color.Yellow);
				
				if (realRange <= stormRange*0.9f && game.getFrameCount() >= nextPossibleStorm && templar.canUseTech(TechType.Psionic_Storm, bestTarget.toPosition())) {
					game.drawCircleMap(bestTarget.toPosition(), (int)splashRadius, Color.Blue, true);
					if (game.getFrameCount() % 10 == 5) {
						templar.useTech(TechType.Psionic_Storm, bestTarget.toPosition());
						nextPossibleStorm = game.getFrameCount() + 50;
					}
				} else {
					game.drawCircleMap(bestTarget.toPosition(), (int)splashRadius, Color.Blue, false);
					if (game.getFrameCount() % 10 == 5) {
						templar.move(bestTarget.sub(vectorOfAttack.scale(stormRange).scale(0.8f)).toPosition());
					}
				}
			}
		}
    }
    
    public static void main(String[] args) {
        new Bot().run();
    }
}
