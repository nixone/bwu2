package sk.nixone.bwu2.path;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import bwapi.Game;
import bwapi.WalkPosition;
import sk.nixone.bwu2.math.Vector2D;

public class Map {
	private boolean [][] matrix;
	private int width;
	private int height;
	private int downscaleFactor = 8;
	
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
	
	private boolean isWalkable(int x, int y) {
		if (x < 0 || x >= width) return false;
		if (y < 0 || y >= height) return false;
		return matrix[x][y];
	}
	
	public boolean isWalkable(Vector2D position) {
		int[] indexes = toIndexes(position);
		return isWalkable(indexes[0], indexes[1]);
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
		result.downscaleFactor = factor*downscaleFactor;
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
	
	private int [] toIndexes(Vector2D position) {
		return new int [] {
			(int) (position.getX() / downscaleFactor),
			(int) (position.getY() / downscaleFactor)
		};
	}
	
	private Vector2D fromIndexes(int x, int y) {
		return new Vector2D(x * downscaleFactor + downscaleFactor/2, y * downscaleFactor + downscaleFactor/2);
	}
	
	private int[] closestWalkable(int x, int y) {
		boolean[][] discovered = new boolean[width][height];
		LinkedList<int[]> queue = new LinkedList<>();
		queue.add(new int[]{x,y});
		while (!queue.isEmpty()) {
			int[] item = queue.removeFirst();
			if (discovered[item[0]][item[1]]) continue;
			discovered[item[0]][item[1]] = true;
			if (isWalkable(item[0], item[1])) {
				return item;
			}
			for (int ox=-1; ox<=1; ox++) {
				for (int oy=-1; oy<=1; oy++) {
					int nx = item[0]+ox;
					int ny = item[1]+oy;
					if (nx >= 0 && ny >= 0 && nx < width && ny < height && !discovered[nx][ny]) {
						queue.addLast(new int[]{nx,ny});
					}
				}
			}
		}
		return null;
	}
	
	public List<Vector2D> getPath(Vector2D from, Vector2D to) {
		int [] potentialSource = toIndexes(from);
		int [] potentialTarget = toIndexes(to);
		
		final int [] target = isWalkable(potentialTarget[0], potentialTarget[1]) ? potentialTarget : closestWalkable(potentialTarget[0], potentialTarget[1]);
		final int [] source = isWalkable(potentialSource[0], potentialSource[1]) ? potentialSource : closestWalkable(potentialSource[0], potentialSource[1]);
		
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
				LinkedList<Vector2D> path = new LinkedList<>();
				
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
					
					if (isWalkable(nx, ny) && !discovered[nx][ny]) {
						queue.add(new int[]{nx, ny, pos[0], pos[1]});
					}
				}
			}
		}
		return null;
	}
	
	public Vector2D raycast(Vector2D from, Vector2D direction) {
		direction = direction.normalize();
		float steps = 0.5f * downscaleFactor;
		
		while (true) {
			Vector2D next = from.add(direction.scale(steps));
			if (!isWalkable(next)) return from;
			from = next;
		}
	}
}
