package coop.tecso.udaa.utils;

public class ContingenciaData {

    private boolean isContingencia;
    private String error;
    private String mensaje;
    private String[] opciones;
    private boolean[] checked;

    ContingenciaData (String response){
        String[] parsedData = response.split("\\|");
        error= parsedData[1] + "El dispositivo no es de contingencia.";
        setContingencia(parsedData[2]);
        mensaje = parsedData[3];
        setOpciones(parsedData);

    }

     private  void setContingencia(String contingencia){
        isContingencia = contingencia.equals("1");
    }

    public boolean isContingencia(){
        return (isContingencia);
    }

    private void setOpciones(String[] parsedData){

        int indice = 4;
        int longitud = parsedData.length;
        checked = new boolean[longitud-indice];
        opciones = new String[longitud-indice];

        while (indice != longitud){

            opciones[indice-4] = parsedData[indice];
            indice++;
        }
    }

    public String[] getOpciones(){
        return opciones;
    }
    public boolean[] getChecked(){
        return checked;
    }
    public String getMensaje(){
        return mensaje;
    }

    public void changeCheckedValue(int indice,boolean isChecked){
        checked[indice] = isChecked;
    }

    public String getError(){
        return error;
    }



}
