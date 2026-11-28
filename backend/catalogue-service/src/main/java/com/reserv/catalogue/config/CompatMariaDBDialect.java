package com.reserv.catalogue.config;

import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * Dialecte MariaDB compatible avec les versions MariaDB 10.4.x (ex. XAMPP),
 * desactivant la clause {@code IF EXISTS} apres {@code ALTER TABLE}.
 */
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
