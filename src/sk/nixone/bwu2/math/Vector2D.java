package sk.nixone.bwu2.math;
import bwapi.Position;

/**
 * Class representing two dimensional vector and operations defined on it. This
 * class is immutable.
 * 
 * @author nixone
 * 
 */
public class Vector2D
{
	/**
	 * Classic zero vector
	 */
	static final public Vector2D ZERO = new Vector2D(0, 0);

	/**
	 * Vector pointing to negative y.
	 */
	static final public Vector2D NORTH = new Vector2D(0, -1);

	/**
	 * Vector pointing to positive x.
	 */
	static final public Vector2D EAST = new Vector2D(1, 0);

	/**
	 * Vector pointing to positive y.
	 */
	static final public Vector2D SOUTH = new Vector2D(0, 1);

	/**
	 * Vector pointing to negative x.
	 */
	static final public Vector2D WEST = new Vector2D(-1, 0);

	/**
	 * Components.
	 */
	final public float x, y;

	/**
	 * Length of the vector, or the distance of components from the origin (0,
	 * 0)
	 */
	final public float length;

	/**
	 * Creates a vector.
	 * 
	 * @param x
	 * @param y
	 */
	public Vector2D(float x, float y)
	{
		if (Float.isNaN(x) || Float.isNaN(y))
		{
			throw new IllegalArgumentException("Cannot crate a NaN vector!");
		}

		this.x = x;
		this.y = y;

		float lng = (float)Math.sqrt(x * x + y * y);
		this.length = Float.isNaN(lng) ? 0 : lng;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public float getLength()
	{
		return length;
	}

	/**
	 * Scales this vector by a specific aspect.
	 * 
	 * @param aspect
	 * @return
	 */
	public Vector2D scale(float aspect)
	{
		return Vector2DMath.scale(this, aspect);
	}

	/**
	 * Scales this vector by specific aspects in specific components. Be
	 * careful, this method does change the vector heading if the aspects are
	 * different.
	 * 
	 * @param aspectX
	 * @param aspectY
	 * @return new vector
	 */
	public Vector2D scale(float aspectX, float aspectY)
	{
		return Vector2DMath.scale(this, aspectX, aspectY);
	}

	/**
	 * Scale this vector by specific aspects in specific components. Be careful,
	 * this method does change the vector heading if the aspects are different.
	 * 
	 * @param aspect
	 * @return
	 */
	public Vector2D scale(Vector2D aspect)
	{
		return Vector2DMath.scale(this, aspect);
	}

	/**
	 * Normalizes this vector.
	 * 
	 * @return new vector
	 */
	public Vector2D normalize()
	{
		return Vector2DMath.normalize(this);
	}

	/**
	 * Adds vector to this vector.
	 * 
	 * @param v
	 *            vector to be added
	 * @return new vector
	 */
	public Vector2D add(Vector2D v)
	{
		return Vector2DMath.add(this, v);
	}

	/**
	 * Substracts a specified vector from this vector.
	 * 
	 * @param v
	 *            vector to be substracted
	 * @return new vector
	 */
	public Vector2D sub(Vector2D v)
	{
		return Vector2DMath.sub(this, v);
	}

	/**
	 * Inverts this vector (scales it by aspect of -1)
	 * 
	 * @return
	 */
	public Vector2D invert()
	{
		return Vector2DMath.invert(this);
	}

	/**
	 * Returns two trivial orthogonal vectors to this vector.
	 * 
	 * @return
	 */
	public Vector2D[] getOrthogonal()
	{
		return Vector2DMath.getOrthogonal(this);
	}

	/**
	 * Prints this vector.
	 */
	@Override
	public String toString()
	{
		return String.format("(%.2f; %.2f)", x, y);
	}

	/**
	 * Decides if these two vectors are the same.
	 * 
	 * @param v
	 * @return
	 */
	public boolean sameAs(Vector2D v)
	{
		return v.x == x && v.y == y;
	}

	@Override
	public boolean equals(Object object)
	{
		if(!(object instanceof Vector2D))
		{
			return false;
		}
	
		return ((Vector2D)object).sameAs(this);
	}
	
	/**
	 * Converts the vector to JNIBWAPI position.
	 * 
	 * @return
	 */
	public Position toPosition()
	{
		return Vector2DMath.toPosition(this);
	}
}