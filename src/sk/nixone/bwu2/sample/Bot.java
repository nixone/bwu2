package sk.nixone.bwu2.sample;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Unit;
import bwta.BWTA;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.selection.DistanceSelector;
import sk.nixone.bwu2.selection.UnitSet;

public class Bot extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    private Game game;
    private Player self;

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        game.enableFlag(1);
        BWTA.readMap();
        BWTA.analyze();
    }
    
    @Override
    public void onFrame() {
        UnitSet myUnits = new UnitSet(self.getUnits());
        
        game.drawCircleMap(myUnits.getArithmeticCenter().toPosition(), 10, Color.Red);
    	game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        //iterate through my units
        for (Unit unit : self.getUnits()) {
        	for (Unit unit2 : myUnits.minus(unit).pickNOrdered(2, new DistanceSelector(unit))) {
        		game.drawLineMap(unit.getPosition(), unit2.getPosition(), Color.Red);
        	}
        }
    }
    
    
    public static void main(String[] args) {
        new Bot().run();
    }
}
