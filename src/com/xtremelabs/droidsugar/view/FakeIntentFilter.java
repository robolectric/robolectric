package com.xtremelabs.droidsugar.view;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeIntentFilter {
    List<String> actions = new ArrayList<String>();

    public void addAction(String action) {
        actions.add(action);
    }

    public String getAction(int index) {
        return actions.get(index);
    }
}
