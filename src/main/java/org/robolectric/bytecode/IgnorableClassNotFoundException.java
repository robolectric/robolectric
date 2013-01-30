package com.xtremelabs.robolectric.bytecode;

import javassist.NotFoundException;

public class IgnorableClassNotFoundException extends NotFoundException {
    public IgnorableClassNotFoundException(NotFoundException e) {
        super("msg", e);
    }
}
