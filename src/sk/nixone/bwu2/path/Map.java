package sk.nixone.bwu2.path;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import bwapi.Game;
import bwapi.WalkPosition;

public class Map {
	private boolean [][] matrix;
	private int width;
	private int height;
	
	public Map(boolean[][] matrix) {
		this.matrix = matrix;
		this.width = matrix.length;
		this.height = matrix[0].length;
	}
	
	public Map(Game game) {
		width = game.mapWidth()*4;
		height = game.mapHeight()*4;
		matrix = new boolean[width][height];
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				matrix[i][j] = game.isWalkable(i, j);
			}
		}
	}
	
	public boolean isWalkable(int x, int y) {
		if (x < 0 || x >= width) return false;
		if (y < 0 || y >= height) return false;
		return matrix[x][y];
	}
	
	public Map remapFromBound(float distance) {
		boolean[][] newMatrix = new boolean[width][height];
		
		int distanceUp = (int)Math.round(Math.ceil(distance));
		
		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				
				boolean passed = true;
				
				for (int offX=-distanceUp; offX <= distanceUp; offX++) {
					for (int offY=-distanceUp; offY <= distanceUp; offY++) {
						float d = (float)Math.sqrt(offX*offX + offY*offY);
						if (d > distance) continue;
						if (!isWalkable(x+offX, y+offY)) {
							passed = false;
							break;
						}
					}
					if (!passed) break;
				}
				
				newMatrix[x][y] = passed;
			}
		}
		return new Map(newMatrix);
	}
	
	public List<int[]> getPath(WalkPosition from, WalkPosition to) {
		final int targetX = to.getX();
		final int targetY = to.getY();
		
		int [][][] pred = new int[width][height][2];
		boolean [][] discovered = new boolean[width][height];
		final double [][] lengths = new double[width][height];
		
		PriorityQueue<int[]> queue = new PriorityQueue<int[]>(10, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int tx = o1[0]-targetX;
				int ty = o1[1]-targetY;
				int dx = o1[0]-o1[2];
				int dy = o1[1]-o1[3];
				double l1 = lengths[o1[2]][o1[3]] + Math.sqrt(tx*tx+ty*ty) + Math.sqrt(dx*dx+dy*dy);
				tx = o2[0]-targetX;
				ty = o2[1]-targetY;
				dx = o2[0]-o2[2];
				dy = o2[1]-o2[3];
				double l2 = lengths[o2[2]][o2[3]] + Math.sqrt(tx*tx+ty*ty) + Math.sqrt(dx*dx+dy*dy);
				return Double.compare(l1, l2);
			}
		});
		
		queue.add(new int[]{ from.getX(), from.getY(), from.getX(), from.getY() });
		
		while(!queue.isEmpty()) {
			int [] pos = queue.poll();
			
			// discover
			if (discovered[pos[0]][pos[1]]) continue;
			discovered[pos[0]][pos[1]] = true;
			
			// set predecessors
			pred[pos[0]][pos[1]][0] = pos[2];
			pred[pos[0]][pos[1]][1] = pos[3];
			
			// set lengths
			int dx = pos[0]-pos[2];
			int dy = pos[1]-pos[3];
			lengths[pos[0]][pos[1]] = lengths[pos[2]][pos[3]] + Math.sqrt(dx*dx+dy*dy);
			
			if (pos[0]==targetX && pos[1]==targetY) {
				LinkedList<int[]> path = new LinkedList<>();
				
				while (pos[0] != from.getX() || pos[1] != from.getY()) {
					path.addFirst(new int[]{ pos[0], pos[1]});
					pos[0] = pos[2];
					pos[1] = pos[3];
					pos[2] = pred[pos[0]][pos[1]][0];
					pos[3] = pred[pos[0]][pos[1]][1];
				}
				return path;
			}
			for (int ox=-1; ox<=1; ox++) {
				for (int oy=-1; oy<=1; oy++) {
					int nx = pos[0]+ox;
					int ny = pos[1]+oy;
					if (isWalkable(nx, ny) && !discovered[nx][ny]) {
						queue.add(new int[]{nx, ny, pos[0], pos[1]});
					}
				}
			}
		}
		return null;
	}
}
