package sk.nixone.bwu2.selection;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;

/**
 * Conatiner for UnitSelectors by boolean, integer or real information
 * 
 * @author nixone
 * 
 */
public interface UnitSelector
{
	/**
	 * Selector for boolean information from a Unit
	 * 
	 * @author nixone
	 * 
	 */
	
	public interface BooleanSelector
	{
		/**
		 * Get a value for this selector from a unit
		 * 
		 * @param unit
		 * @return
		 */
		public boolean isTrueFor(Unit unit);
	}

	/**
	 * Selector for integer information from a Unit
	 * 
	 * @author nixone
	 * 
	 */
	public interface IntegerSelector
	{
		/**
		 * Get a value for this selector from a unit
		 * 
		 * @param unit
		 * @return
		 */
		public int getValue(Unit unit);
	}

	/**
	 * Selector for real information from a unit
	 * 
	 * @author nixone
	 * 
	 */
	public interface RealSelector
	{
		/**
		 * Get a value for this selector from a unit
		 * 
		 * @param unit
		 * @return
		 */
		public double getValue(Unit unit);
	}

	/**
	 * Selector for some vector information from a unit
	 *
	 */
	public interface Vector2DSelector
	{
		/**
		 * Get a value for this selector from a unit
		 * 
		 * @param unit
		 * @return
		 */
		public Vector2D getValue(Unit unit);
	}
	
	public abstract class ObjectEqualitySelector<C> implements BooleanSelector
	{
		private C toBeEqualTo;

		public ObjectEqualitySelector(C toBeEqualTo)
		{
			this.toBeEqualTo = toBeEqualTo;
		}

		@Override
		public boolean isTrueFor(Unit unit)
		{
			return getObjectFrom(unit) == toBeEqualTo;
		}

		public abstract C getObjectFrom(Unit unit);
	}

	static public class AirWeaponTypeSelector extends ObjectEqualitySelector<WeaponType>
	{
		public AirWeaponTypeSelector(WeaponType toBeEqualTo)
		{
			super(toBeEqualTo);
		}

		@Override
		public WeaponType getObjectFrom(Unit unit)
		{
			return unit.getType().airWeapon();
		}
	}

	static public class GroundWeaponTypeSelector extends ObjectEqualitySelector<WeaponType>
	{
		public GroundWeaponTypeSelector(WeaponType toBeEqualTo)
		{
			super(toBeEqualTo);
		}

		@Override
		public WeaponType getObjectFrom(Unit unit)
		{
			return unit.getType().groundWeapon();
		}
	}

	static public class PlayerSelector extends ObjectEqualitySelector<Player>
	{
		public PlayerSelector(Player toBeEqualTo)
		{
			super(toBeEqualTo);
		}

		@Override
		public Player getObjectFrom(Unit unit)
		{
			return unit.getPlayer();
		}
	}

	static public class UnitTypeSelector extends ObjectEqualitySelector<UnitType>
	{
		public UnitTypeSelector(UnitType toBeEqualTo)
		{
			super(toBeEqualTo);
		}

		@Override
		public UnitType getObjectFrom(Unit unit)
		{
			return unit.getType();
		}
	}

	/**
	 * Selector for units by hit points.
	 */
	static public final IntegerSelector HIT_POINTS = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getHitPoints();
		}
	};

	static public final IntegerSelector SHIELDS = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getShields();
		}
	};

	static public final IntegerSelector ENERGY = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getEnergy();
		}
	};

	static public final IntegerSelector RESOURCES = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getResources();
		}
	};

	static public final IntegerSelector INITIAL_HIT_POINTS = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getInitialHitPoints();
		}
	};

	static public final IntegerSelector KILL_COUNT = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getKillCount();
		}
	};

	static public final IntegerSelector ACID_SPORE_COUNT = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getAcidSporeCount();
		}
	};

	static public final IntegerSelector INTERCEPTOR_COUNT = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getInterceptorCount();
		}
	};

	static public final IntegerSelector SCARAB_COUNT = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getScarabCount();
		}
	};

	static public final IntegerSelector GROUND_WEAPON_COOLDOWN = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getGroundWeaponCooldown();
		}
	};

	static public final IntegerSelector AIR_WEAPON_COOLDOWN = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getAirWeaponCooldown();
		}
	};

	static public final IntegerSelector SPELL_COOLDOWN = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getSpellCooldown();
		}
	};

	static public final IntegerSelector DEFENSE_MATRIX_POINTS = new IntegerSelector()
	{
		@Override
		public int getValue(Unit unit)
		{
			return unit.getDefenseMatrixPoints();
		}
	};

	// TIMERS

	/**
	 * Selector for units that are not flyers (therefore are on a ground).
	 */
	static public final BooleanSelector IS_GROUND = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return !unit.getType().isFlyer();
		}
	};

	/**
	 * Selector for units that are flyers (therefore are not on a ground).
	 */
	static public final BooleanSelector IS_FLYER = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().isFlyer();
		}
	};

	static public final BooleanSelector IS_ATTACKING = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.isAttacking();
		}
	};
	
	static public final BooleanSelector IS_LOCKED_DOWN = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.isLockedDown();
		}
	};

	static public final BooleanSelector IS_VISIBLE = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.isVisible();
		}
	};

	static public final BooleanSelector IS_UNDER_ATTACK = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.isUnderAttack();
		}
	};

	static public final BooleanSelector IS_BUILDING = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().isBuilding();
		}
	};
	
	static public final BooleanSelector IS_COMPLETED = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.isCompleted();
		}
	};

	static public final BooleanSelector CAN_ATTACK_AIR = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().airWeapon().targetsAir() || unit.getType().groundWeapon().targetsAir();
		}
	};
	
	static public final BooleanSelector CAN_ATTACK_GROUND = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().airWeapon().targetsGround() || unit.getType().groundWeapon().targetsGround();
		}
	};
	
	static public final BooleanSelector CAN_ATTACK = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().canAttack();
		}
	};
	
	static public final BooleanSelector IS_RESOURCE_DEPOT = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return unit.getType().isResourceDepot();
		}
	};
	
	static public final BooleanSelector IS_WORKER = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			UnitType type = unit.getType();
			
			if (type == UnitType.Terran_SCV)
			{
				return true;
			}
			
			if (type == UnitType.Zerg_Drone)
			{
				return true;
			}
			
			if (type == UnitType.Protoss_Probe)
			{
				return true;
			}

			return false;
		}
	};
	
	static public final BooleanSelector IS_RESOURCE = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return IS_MINERAL.isTrueFor(unit) || IS_GAS_GEYSER.isTrueFor(unit);
		}
	};
	
	static public final BooleanSelector IS_MINERAL = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			UnitType type = unit.getType();
			
			return type == UnitType.Resource_Mineral_Field || type == UnitType.Resource_Mineral_Field_Type_2 || type == UnitType.Resource_Mineral_Field_Type_3;
		}
	};
	
	static public final BooleanSelector IS_GAS_SOURCE = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			return IS_GAS_GEYSER.isTrueFor(unit) || IS_GAS_EXTRACTION_BUILDING.isTrueFor(unit);
		}
	};
	
	static public final BooleanSelector IS_GAS_GEYSER = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			UnitType type = unit.getType();
			
			return type == UnitType.Resource_Vespene_Geyser;
		}
	};
	
	static public final BooleanSelector IS_GAS_EXTRACTION_BUILDING = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			UnitType type = unit.getType();
			
			return type == UnitType.Terran_Refinery || type == UnitType.Zerg_Extractor || type == UnitType.Protoss_Assimilator;
		}
	};
	
	static public final BooleanSelector IS_SPAWNING_LARVAE = new BooleanSelector()
	{
		@Override
		public boolean isTrueFor(Unit unit)
		{
			UnitType type = unit.getType();
			
			return type == UnitType.Zerg_Hatchery || type == UnitType.Zerg_Lair || type == UnitType.Zerg_Hive;
		}
	};
	
	static public final Vector2DSelector POSITION = new Vector2DSelector()
	{
		@Override
		public Vector2D getValue(Unit unit)
		{
			Position position = unit.getPosition();
			
			if(position != null)
			{
				return Vector2DMath.toVector(position);
			}
			
			return null;
		}
	};
	
	static public final Vector2DSelector TARGET_POSITION = new Vector2DSelector()
	{
		@Override
		public Vector2D getValue(Unit unit)
		{
			Unit targetUnit = unit.getTarget();
			
			if(targetUnit != null)
			{
				return Vector2DMath.toVector(targetUnit.getPosition());
			}
			
			Position targetPosition = unit.getTargetPosition();
			
			if(targetPosition != null)
			{
				return Vector2DMath.toVector(targetPosition);
			}
			
			return null;
		}
	};
}
