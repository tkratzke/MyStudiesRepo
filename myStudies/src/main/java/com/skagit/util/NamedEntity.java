package com.skagit.util;

import java.util.Arrays;

public class NamedEntity implements Comparable<NamedEntity> {
    public final String _name;

    public NamedEntity(final String name) {
	_name = CleanWhiteSpace(name);
    }

    public NamedEntity(final String name, final int year) {
	_name = name + " " + year;
    }

    @Override
    public int compareTo(final NamedEntity namedEntity) {
	return namedEntity == null ? -1 : _name.compareTo(namedEntity._name);
    }

    public static void main(final String[] args) {
	final NamedEntity1[] namedEntity1s = new NamedEntity1[] { //
		new NamedEntity1("b", 2), //
		new NamedEntity1("a", 1), //
		new NamedEntity1("c", 3), //
	};
	Arrays.sort(namedEntity1s);
	for (int k = 0; k < namedEntity1s.length; ++k) {
	    System.out.println(namedEntity1s[k].getString());
	}
	System.out.println(Arrays.binarySearch(namedEntity1s, new NamedEntity("b")));
    }

    public static String CleanWhiteSpace(final String s) {
	String ss = MyStudiesStringUtils.CleanWhiteSpace(s);
	if (ss.startsWith("\'")) {
	    ss = ss.length() == 1 ? "" : ss.substring(1);
	}
	return ss.length() == s.length() ? s : ss;
    }
}

class NamedEntity1 extends NamedEntity {
    final int _abc;

    public NamedEntity1(final String name, final int abc) {
	super(name);
	_abc = abc;
    }

    public String getString() {
	return String.format("%s %d", _name, _abc);
    }

    @Override
    public String toString() {
	return getString();
    }

}
