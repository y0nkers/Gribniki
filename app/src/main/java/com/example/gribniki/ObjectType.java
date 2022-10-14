package com.example.gribniki;

public enum ObjectType {
    MUSHROOM(0),
    BONUS(1),
    TRAP(2),
    NOTHING(3);

    private int code;

    ObjectType(int code) {
        this.code = code;
    }

    public int getType() {
        return code;
    }
}
