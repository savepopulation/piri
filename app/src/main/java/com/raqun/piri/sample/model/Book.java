package com.raqun.piri.sample.model;

import java.io.Serializable;

/**
 * Created by tyln on 13/06/2017.
 */

public class Book implements Serializable {
    private int bookId;
    private String bookName;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
