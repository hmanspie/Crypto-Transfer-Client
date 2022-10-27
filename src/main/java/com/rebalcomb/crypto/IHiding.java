package com.rebalcomb.crypto;

import java.util.List;

public interface IHiding {
    default String generateHidingMassage(String rawMassage) {
        return null;
    }

    default String getOpenMassageForHidingMassage(String hidingMassage) {
        return null;
    }

    default String addRedundantPictures(String hidingMassage, int count) {
        return null;
    }
}
