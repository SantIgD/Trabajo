package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;

@DatabaseTable(tableName = "hcd_despachador")
public final class Despachador extends AbstractEntity
{

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] foto;

    @DatabaseField(canBeNull = false)
    private String informacionPersonal;

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getInformacionPersonal() {
        return informacionPersonal;
    }

    public void setInformacionPersonal(String informacionPersonal) {
        this.informacionPersonal = informacionPersonal;
    }

}
