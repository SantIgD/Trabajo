package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;

@DatabaseTable(tableName = "hcd_score")
public class Score extends AbstractEntity {

    @DatabaseField(canBeNull = false)
    private int idSucursal;
    @DatabaseField(canBeNull = false)
    private String grupo;
    @DatabaseField(canBeNull = false)
    private String titulo;
    @DatabaseField(canBeNull = false)
    private int ordenTitulo;
    @DatabaseField(canBeNull = false)
    private String opcion;
    @DatabaseField(canBeNull = false)
    private String ordenOpcion;
    @DatabaseField(canBeNull = false)
    private boolean esValorDefecto;
    @DatabaseField(canBeNull = false)
    private int peso;

    @DatabaseField(canBeNull = false)
    private int tipo;


    public int getIdSucursal() {
        return idSucursal;
    }

    public void setIdSucursal(int idSucursal) {
        this.idSucursal = idSucursal;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getOrdenTitulo() {
        return ordenTitulo;
    }

    public void setOrdenTitulo(int ordenTitulo) {
        this.ordenTitulo = ordenTitulo;
    }

    public String getOpcion() {
        return opcion;
    }

    public void setOpcion(String opcion) {
        this.opcion = opcion;
    }

    public String getOrdenOpcion() {
        return ordenOpcion;
    }

    public void setOrdenOpcion(String ordenOpcion) {
        this.ordenOpcion = ordenOpcion;
    }

    public boolean isEsValorDefecto() {
        return esValorDefecto;
    }

    public void setEsValorDefecto(boolean esValorDefecto) {
        this.esValorDefecto = esValorDefecto;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
}
