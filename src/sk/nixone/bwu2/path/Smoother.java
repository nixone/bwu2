package sk.nixone.bwu2.path;

import java.util.ArrayList;
import java.util.List;

import sk.nixone.bwu2.math.Vector2D;

public class Smoother {
	
	public static List<Vector2D> neighbour(List<Vector2D> path, int neighbours) {
		ArrayList<Vector2D> result = new ArrayList<>();
		for (int i=0; i<path.size(); i++) {
			Vector2D node = Vector2D.ZERO;
			int n = 0;
			for (int j=Math.max(0, i-neighbours); j<Math.min(path.size(), i+neighbours); j++) {
				n++;
				node = node.add(path.get(j));
			}
			result.add(node.scale(1f/n));
		}
		return result;
	}
}
