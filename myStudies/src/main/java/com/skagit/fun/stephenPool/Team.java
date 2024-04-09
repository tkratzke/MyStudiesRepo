package com.skagit.fun.stephenPool;

public class Team implements Comparable<Team> {
    final public String _name;

    public Team(final String mainName) {
	_name = mainName;
    }

    @Override
    public boolean equals(final Object team) {
	if (!(team instanceof Team)) {
	    return false;
	}
	return compareTo((Team) team) == 0;
    }

    @Override
    public int hashCode() {
	return _name.hashCode();
    }

    @Override
    public int compareTo(final Team team) {
	return _name.compareTo(team._name);
    }

    public String getString() {
	return _name;
    }

    @Override
    public String toString() {
	return getString();
    }
}
