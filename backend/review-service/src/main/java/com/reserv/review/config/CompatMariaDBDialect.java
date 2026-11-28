package com.reserv.review.config;

import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

public class CompatMariaDBDialect extends MariaDBDialect {

    public CompatMariaDBDialect() {
        super();
    }

    public CompatMariaDBDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    public boolean supportsIfExistsAfterAlterTable() {
        return false;
    }
}
