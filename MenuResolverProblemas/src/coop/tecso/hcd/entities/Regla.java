package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;

@DatabaseTable(tableName = "hcd_regla")
public final class Regla extends AbstractEntity {

    @DatabaseField
    public Integer campoObligatorio;

    @DatabaseField
    public Integer campoValorOblig;

    @DatabaseField
    public Integer campoValorOpcionOblig;

}