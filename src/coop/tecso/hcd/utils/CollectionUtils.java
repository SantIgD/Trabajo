package coop.tecso.hcd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

}
