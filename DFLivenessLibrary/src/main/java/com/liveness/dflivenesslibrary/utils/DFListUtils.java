package com.liveness.dflivenesslibrary.utils;

import java.util.ArrayList;
import java.util.List;

public class DFListUtils {
    public static <T> List<T> refreshData(List<T> destList, List<? extends T> srcList) {
        if (destList == null) {
            destList = new ArrayList<>();
        } else {
            destList.clear();
        }
        if (srcList != null) {
            destList.addAll(srcList);
        }
        return destList;
    }

    public static <T> boolean isEmpty(List<T> list) {
        boolean isEmpty = true;
        if (list != null && !list.isEmpty()) {
            isEmpty = false;
        }
        return isEmpty;
    }

    public static <T> List<T> addAllData(List<T> destList, List<? extends T> srcList) {
        if (destList == null) {
            destList = new ArrayList<>();
        }
        if (srcList != null) {
            destList.addAll(srcList);
        }
        return destList;
    }

    public static <T> List<T> clear(List<T> list) {
        if (list != null) {
            list.clear();
        }
        return list;
    }

    public static <T> List<T> cloneList(List<T> srcList) {
        List<T> dest = new ArrayList<>();
        if (srcList != null) {
            dest.addAll(srcList);
        }
        return dest;
    }
}
