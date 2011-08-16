package org.jbox2d.common;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Arrays;
import java.util.Comparator;

public class FastSort {
  public static <T> void unstableSort(T[] array, Comparator<? super T> comparator) {
    if (false/*GWT.isProdMode()*/) {
      unstableSort(array, 0, array.length, getNativeComparator(comparator));
    } else {
      Arrays.sort(array, comparator);
    }
  }

  public static <T> void unstableSort(T[] array, int fromIndex, int toIndex, Comparator<? super T> comparator) {
    if (false/*GWT.isProdMode()*/) {
      unstableSort(array, fromIndex, toIndex, getNativeComparator(comparator));
    } else {
      Arrays.sort(array, fromIndex, toIndex, comparator);
    }
  }

  private static native JavaScriptObject getNativeComparator(Comparator<?> comparator) /*-{
    return comparator.@java.util.Comparator::compare(Ljava/lang/Object;Ljava/lang/Object;);
  }-*/;

  private static native <T> void unstableSort(T[] array, int fromIndex, int toIndex, JavaScriptObject comparator) /*-{
    var subArray = array.slice(fromIndex, toIndex);
    subArray.sort(comparator);
    var n = toIndex - fromIndex;
    Array.prototype.splice.apply(array, [ fromIndex, n ].concat(subArray
        .slice(0, n)));
  }-*/;
}
