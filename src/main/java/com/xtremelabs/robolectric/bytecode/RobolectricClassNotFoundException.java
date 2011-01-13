package com.xtremelabs.robolectric.bytecode;

import javassist.NotFoundException;

public class RobolectricClassNotFoundException extends NotFoundException {
    public RobolectricClassNotFoundException(NotFoundException e) {
        super("msg", e);
    }
}
