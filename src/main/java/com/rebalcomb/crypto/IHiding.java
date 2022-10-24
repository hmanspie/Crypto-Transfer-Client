package com.rebalcomb.crypto;

import java.util.List;

public interface IHiding {
    default List<String> generateHidingMassage(String rawMassage) {
        return null;
    }

    default String getOpenMassageForHidingMassage(List<String> hidingMassage) {
        return null;
    }

    default List<String> addRedundantPictures(List<String> hidingMassage, int count) {
        return null;
    }
}
