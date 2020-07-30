package coop.tecso.hcd.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?,?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> void sort(ArrayList<T> list, Comparator<T> comparator) {
        if (isEmpty(list)) {
            return;
        }

        int size = list.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                    T temp = list.get(j + 1);
                    list.set(j + 1, list.get(j));
                    list.set(j, temp);
                }
            }
        }
    }

    public interface Transformer<Source, Destination> {
        Destination transform(Source source);
    }

    public static <Element> boolean matchElement(Collection<Element> collection, Transformer<Element, Boolean> matcher) {
        if (isEmpty(collection)) {
            return false;
        }

        for (Element element: collection) {
            if (matcher.transform(element)) {
                return true;
            }
        }

        return false;
    }

    public static <Source, Destination> List<Destination> map(List<Source> list, Transformer<Source, Destination> transformer) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }

        ArrayList<Destination> result = new ArrayList<>();
        for (Source item: list) {
            Destination newItem = transformer.transform(item);
            result.add(newItem);
        }
        return result;
    }

    public interface FilterList<Element> {
        boolean filterCondition(Element element);
    }

    public static <Element> List<Element> filter(List<Element> list, FilterList<Element> elementFilter) {
        ArrayList<Element> result = new ArrayList<>();
        for (Element item: list) {
            if (elementFilter.filterCondition(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public interface FilterMap<Key, Value> {
        boolean filterCondition(Key key, Value value);
    }

    public static <Key, Value> List<Key> filterKeys(Map<Key, Value> map, FilterMap<Key, Value> filter) {
        ArrayList<Key> result = new ArrayList<>();
        for (Key key: map.keySet()) {
            Value value = map.get(key);
            if (filter.filterCondition(key, value)) {
                result.add(key);
            }
        }
        return result;
    }

    public static <Element> boolean contains (Element[] elements, Element element){
        if (element == null || elements == null){
            return false;
        }

        for (Element elmt : elements){
            if (elmt.equals(element)){
                return true;
            }
        }
        return false;
    }

    /**
     * Busca un elemento en una lista ignorando mayusculas y minúsculas y retorna el elemento de la lista
     * de manera fiel. como buddy de toi Estori  (el amigo de bus laigt shear)
     * @param elements lista donde se va a buscar
     * @param element  elemento que se va a buscar
     */

    public static String matchStringIgnoreCaseS (List<String> elements, String element){
        if (element == null || elements == null){
            return null;
        }

        element = element.toLowerCase();

        for (String elmt : elements){
            if (elmt.toLowerCase().equals(element)){
                return elmt;
            }
        }
        return null;
    }

    public interface YesFunction {
        void function() throws SQLException;
    }

    public interface NoFunction {
        void function();
    }


    public static void createAlertDialog(int title, int mensaje, YesFunction yesFunction, NoFunction noFunction, Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(mensaje);
        builder.setPositiveButton(R.string.yes,  (dialog, id) -> {
            try {
                yesFunction.function();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton(R.string.no,  (dialog, id) -> {
            noFunction.function();
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

}
