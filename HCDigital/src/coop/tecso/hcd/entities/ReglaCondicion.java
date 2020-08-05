package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

import coop.tecso.udaa.domain.base.AbstractEntity;

@DatabaseTable(tableName = "hcd_reglaCondicion")
public final class ReglaCondicion extends AbstractEntity {

    @DatabaseField(canBeNull = false)
    public int idRegla;

    @DatabaseField(canBeNull = false)
    public Integer campo;

    @DatabaseField(canBeNull = false)
    public String operador;

    @DatabaseField(canBeNull = false)
    public String valor;

    @DatabaseField
    public int aplicacionPerfil;

}