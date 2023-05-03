package common;

import tree.NameOfTemp;

public class Constants {
    public static final NameOfTemp[] reservedSPARCRegisters = {
        new NameOfTemp("%o0"),
        new NameOfTemp("%o1"),
        new NameOfTemp("%o2"),
        new NameOfTemp("%o3"),
        new NameOfTemp("%o4"),
        new NameOfTemp("%o5"),
        new NameOfTemp("%o6"),
        new NameOfTemp("%o7"),
        new NameOfTemp("%i0"),
        new NameOfTemp("%i1"),
        new NameOfTemp("%i2"),
        new NameOfTemp("%i3"),
        new NameOfTemp("%i4"),
        new NameOfTemp("%i5"),
        new NameOfTemp("%i6"),
        new NameOfTemp("%i7"),
        new NameOfTemp("%sp"),
        new NameOfTemp("%fp")
    };

    public static final NameOfTemp[] usableSPARCRegisters = {
        new NameOfTemp("%l0"),
        new NameOfTemp("%l1"),
        new NameOfTemp("%l2"),
        new NameOfTemp("%l3"),
        new NameOfTemp("%l4"),
        new NameOfTemp("%l5"),
        new NameOfTemp("%l6"),
        new NameOfTemp("%l7"),
        new NameOfTemp("%g1"),
        new NameOfTemp("%g2"),
        new NameOfTemp("%g3"),
        new NameOfTemp("%g4"),
        new NameOfTemp("%g5"),
        new NameOfTemp("%g6"),
        new NameOfTemp("%g7")
    };
}
