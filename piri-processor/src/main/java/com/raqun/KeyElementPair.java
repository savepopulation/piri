package com.raqun;

import javax.lang.model.element.Element;

/**
 * Created by tyln on 19/05/2017.
 */

final class KeyElementPair {
    final String key;
    final Element element;

    KeyElementPair(String key, Element element) {
        this.key = key;
        this.element = element;
    }
}
