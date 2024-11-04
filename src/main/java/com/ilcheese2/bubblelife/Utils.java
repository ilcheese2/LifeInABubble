package com.ilcheese2.bubblelife;

import java.util.List;
import java.util.function.Predicate;

public class Utils {
    public static <T> T filterOrderedList(List<T> list, Predicate<T> remove) {
        if (list.isEmpty()) {
            return null;
        }

        T last = list.getFirst();
        while (remove.test(last)) {
            list.removeFirst();
            if (list.isEmpty()) {
                return null;
            }
            last = list.getFirst();
        }
        return last;
    }
}
