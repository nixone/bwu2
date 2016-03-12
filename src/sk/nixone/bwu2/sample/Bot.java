package sk.nixone.bwu2.sample;

import java.util.HashMap;
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
import sk.nixone.bwu2.path.Smoother;
import sk.nixone.bwu2.selection.DistanceSelector;
import sk.nixone.bwu2.selection.DotProductSelector;
import sk.nixone.bwu2.selection.RealComparisonSelector;
import sk.nixone.bwu2.selection.UnitSelector;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;
import sk.nixone.bwu2.selection.UnitSelector.UnitTypeSelector;
import sk.nixone.bwu2.selection.Units;
import sk.nixone.bwu2.selection.actions.AttackMoveAction;
import sk.nixone.bwu2.selection.actions.MoveAction;
import sk.nixone.bwu2.selection.actions.UnitActionBuffer;
import sk.nixone.bwu2.selection.actions.UseTechAction;

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
    
    private List<Vector2D> path = null;
    private Map map = null;
    
    private Units mine = null;
    private Units enemies = null;
    private Units zealots = null;
    private Units templars = null;
    private Units dragoons = null;
    
    private HashMap<Unit, Vector2D> assignedPositions = new HashMap<>();
    
    private boolean mergeIssued = false;
    
    private UnitSelector.RealSelector OFF_ASSIGNMENT = new UnitSelector.RealSelector() {
		
		@Override
		public double getValue(Unit unit) {
			if (!assignedPositions.containsKey(unit)) return 0;
			return new Vector2D(unit).sub(assignedPositions.get(unit)).length;
		}
	};
    
    @Override
    public void onFrame() {
    	try {
	        mine = new Units(self.getUnits());
	        armyPosition = mine.getArithmeticCenter();
	        zealots = mine.where(new UnitTypeSelector(UnitType.Protoss_Zealot));
	        templars = mine.where(new UnitTypeSelector(UnitType.Protoss_High_Templar));
	        dragoons = mine.where(new UnitTypeSelector(UnitType.Protoss_Dragoon));
	        enemies = new Units(game.getAllUnits()).minus(mine);
	       
	        if (map == null) {
	        	map = new Map(game)
	        			.remapFromBound(20)
	        			.downscale(2, false);
	        }
	        
	        game.drawTextScreen(10, 10, "Frame: "+game.getFrameCount());
	        
	        if (pointsToDiscover.isEmpty()) {
	        	for(TilePosition startLocation : game.getStartLocations()) {
	        		Vector2D position = Vector2DMath.toVector(startLocation.toPosition());
	        		if (!mine.areAt(position, 300)) {
	        			pointsToDiscover.add(position);
	        		}
	        	}
	        }
	        if (pointToDiscover == null || mine.areAt(pointToDiscover, 300)) {
	        	pointToDiscover = pointsToDiscover.removeFirst();
	        }
	        
	        if (pointToDiscover != null) {
	        	path = map.getPath(armyPosition, pointToDiscover);
	        	if (path != null) {
	        		path = Smoother.neighbour(path, 3);
	        	}
	        }
	        
	        if (!enemies.isEmpty()) {
	        	pointOfAttack = enemies.getArithmeticCenter();
	        	pointsToDiscover.add(enemies.getArithmeticCenter());
	        } else if(path != null && !path.isEmpty()) {
	        	pointOfAttack = path.get(0);
	        } else {
	        	pointOfAttack = new Vector2D(0, 0);
	        }
	        
	        vectorOfAttack = pointOfAttack.sub(mine.getArithmeticCenter()).normalize();
	        
	        whatToDo();
	        
	        if (game.getFrameCount() % 3 == 1) {
	        	actionBuffer.executeAll();
	        }
    	} catch(Throwable t) {
    		t.printStackTrace();
    	}
    }
    
    private void assignGroup(Units group, int line, Vector2D targetCenter) {
    	Vector2D currentCenter = armyPosition;
    	Vector2D movementVector = targetCenter.sub(currentCenter).normalize();
    	Vector2D movementOrtho = movementVector.getOrthogonal()[0];
    	group = group.order(new DotProductSelector(armyPosition, movementOrtho));
    	int grid = 50;
    	
    	int offset = -group.size() / 2;
    	for (Unit unit : group) {
    		Vector2D targetPosition = targetCenter
    				.add(movementVector.scale(line*grid))
    				.add(movementOrtho.scale(offset*grid));
    		
    		assignedPositions.put(unit, targetPosition);
    		actionBuffer.act(unit, new MoveAction(targetPosition, Relativity.ABSOLUTE));
    				
    		offset++;
    	}
    }
    
    private void assignLines(int perLine, Vector2D targetCenter) {
    	UnitType[] order = new UnitType[]{UnitType.Protoss_High_Templar, UnitType.Protoss_Dragoon, UnitType.Protoss_Zealot, UnitType.Protoss_Archon};
    	int currentLine = -1;
    	for (UnitType type : order) {
    		Units units = mine.where(new UnitTypeSelector(type));
    		while (!units.isEmpty()) {
    			Units picked = units.limit(perLine);
    			assignGroup(picked, currentLine, targetCenter);
    			units = units.minus(picked);
    			currentLine++;
    		}
    	}
    }
    
    private void whatToDo() {
    	if (enemies.isEmpty()) {
    		if (mine.collectMax(OFF_ASSIGNMENT) < 100) {
    			assignLines(4, pointOfAttack);
    		}
    	} else {
    		zealots.act(actionBuffer, new AttackMoveAction(enemies.getArithmeticCenter(), Relativity.ABSOLUTE));
    		dragoons.act(actionBuffer, new AttackMoveAction(enemies.getArithmeticCenter(), Relativity.ABSOLUTE));
    		
    		focusFire();
    		goAwayFromStorms();
    		
    		if (!mergeIssued) {
    			doStorms();
    		} else {
    			mergeArchonIfNecessary();
    		}
    	}
    }
    
    private void goAwayFromStorms() {
    	if (!mine.where(UnitSelector.IS_UNDER_STORM).isEmpty()) {
    		Vector2D avoidance = vectorOfAttack.scale(-100);
    		mine.act(actionBuffer, new MoveAction(avoidance, Relativity.RELATIVE));
    	}
    }
    
    private void focusFire() {
    	int dragoonRange = (int)(UnitType.Protoss_Dragoon.groundWeapon().maxRange() * 1.25f);
    	
    	for(Unit unit : dragoons) {
    		int realRange = unit.getType().airWeapon().maxRange();
    		if (unit.getType().equals(UnitType.Protoss_Dragoon)) {
    			realRange = dragoonRange;
    		}
    		
    		int testRange = (int)(realRange*1.5f);
    		
    		Units enemiesInRange = enemies
    				.where(UnitSelector.CAN_ATTACK_GROUND)
					.whereLessOrEqual(new DistanceSelector(unit), testRange);
    		
			Unit toKill = enemiesInRange.order(UnitSelector.HIT_POINTS).first();
			
			if (toKill != null) {
				Unit target = toKill;
				actionBuffer.act(unit, new AttackMoveAction(target));
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
					bestTarget = new Vector2D(target.getPosition());
					bestScore = score;
				}
			}
			if (bestTarget != null) {
				float realRange = new Vector2D(templar.getPosition()).sub(bestTarget).length;
				if (realRange <= stormRange*0.9f && game.getFrameCount() >= nextPossibleStorm && templar.canUseTech(TechType.Psionic_Storm, bestTarget.toPosition())) {
					actionBuffer.act(templar, new UseTechAction(TechType.Psionic_Storm, bestTarget));
					nextPossibleStorm = game.getFrameCount() + 50;
				} else {
					actionBuffer.act(templar, new MoveAction(bestTarget.sub(vectorOfAttack.scale(stormRange).scale(0.8f)), Relativity.ABSOLUTE));
				}
			}
		}
    }
    
    private void mergeArchonIfNecessary() {
    	if (templars.size() == 2 && templars.collectMin(UnitSelector.ENERGY) <= 200) {
    		actionBuffer.act(templars.get(0), new UseTechAction(TechType.Archon_Warp, templars.get(1)));
    		mergeIssued = true;
    	}
    }
    
    public static void main(String[] args) {
        new Bot().run();
    }
}
