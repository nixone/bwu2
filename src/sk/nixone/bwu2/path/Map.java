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
	private int downscaleFactor = 1;
	
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
	
	public Map downscale(int factor, boolean or) {
		int newWidth = width / factor + (width % factor != 0 ? 1 : 0);
		int newHeight = height / factor + (height % factor != 0 ? 1 : 0);
		boolean[][] newMatrix = new boolean[newWidth][newHeight];
		
		for (int x=0; x<newWidth; x++) {
			for (int y=0; y<newHeight; y++) {
				boolean walkable = !or;
				for (int ox=0; ox<factor; ox++) {
					for (int oy=0; oy<factor; oy++) {
						if (or == isWalkable(x*factor + ox, y*factor + oy)) {
							walkable = or;
						}
					}
				}
				newMatrix[x][y] = walkable;
			}
		}
		
		Map result = new Map(newMatrix);
		result.downscaleFactor = factor;
		return result;
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
	
	private int [] toIndexes(WalkPosition position) {
		return new int [] {
			position.getX() / downscaleFactor,
			position.getY() / downscaleFactor
		};
	}
	
	private WalkPosition fromIndexes(int x, int y) {
		return new WalkPosition(x * downscaleFactor + downscaleFactor/2, y * downscaleFactor + downscaleFactor/2);
	}
	
	// TODO return even the closest
	public List<WalkPosition> getPath(WalkPosition from, WalkPosition to) {
		final int [] target = toIndexes(to);
		final int [] source = toIndexes(from);
		
		int [][][] pred = new int[width][height][2];
		boolean [][] discovered = new boolean[width][height];
		final double [][] lengths = new double[width][height];
		
		PriorityQueue<int[]> queue = new PriorityQueue<int[]>(10, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int tx = o1[0]-target[0];
				int ty = o1[1]-target[1];
				int dx = o1[0]-o1[2];
				int dy = o1[1]-o1[3];
				double l1 = lengths[o1[2]][o1[3]] + Math.sqrt(tx*tx+ty*ty) + Math.sqrt(dx*dx+dy*dy);
				tx = o2[0]-target[0];
				ty = o2[1]-target[1];
				dx = o2[0]-o2[2];
				dy = o2[1]-o2[3];
				double l2 = lengths[o2[2]][o2[3]] + Math.sqrt(tx*tx+ty*ty) + Math.sqrt(dx*dx+dy*dy);
				return Double.compare(l1, l2);
			}
		});
		
		queue.add(new int[]{ source[0], source[1], source[0], source[1] });
		
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
			
			if (pos[0]==target[0] && pos[1]==target[1]) {
				LinkedList<WalkPosition> path = new LinkedList<>();
				
				while (pos[0] != source[0] || pos[1] != source[1]) {
					path.addFirst(fromIndexes(pos[0], pos[1]));
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
					
					if ((!isWalkable(pos[0], pos[1]) || isWalkable(nx, ny)) && !discovered[nx][ny]) {
						queue.add(new int[]{nx, ny, pos[0], pos[1]});
					}
				}
			}
		}
		return null;
	}
}
