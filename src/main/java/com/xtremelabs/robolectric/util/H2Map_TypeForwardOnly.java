package com.xtremelabs.robolectric.util;

import java.sql.ResultSet;


public class H2Map_TypeForwardOnly extends H2Map implements DatabaseConfig.DatabaseMap {

    @Override
    public int getResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

}
