package sk.nixone.bwu2.selection;

import java.util.Collection;
import java.util.Set;

import bwapi.Unit;

public interface Picker
{
	Unit pickFrom(Collection<Unit> units);
}
