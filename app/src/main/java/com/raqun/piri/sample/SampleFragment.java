package com.raqun.piri.sample;


import android.app.Fragment;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Size;
import android.util.SizeF;

import com.raqun.PiriFragment;
import com.raqun.PiriParam;
import com.raqun.piri.sample.model.Book;
import com.raqun.piri.sample.model.User;

/**
 * Created by tyln on 31/05/2017.
 */

@PiriFragment
public class SampleFragment extends Fragment {

    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_USER = "extra_user";
    private static final String EXTRA_BOOK = "extra_book";

    @PiriParam(key = EXTRA_ID)
    private Long id;

    @PiriParam(key = EXTRA_USER)
    private User user;

    @PiriParam(key = EXTRA_BOOK)
    private Book book;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            id = args.getLong(EXTRA_ID, 0);
            user = args.getParcelable(EXTRA_USER);
            book = (Book) args.getSerializable(EXTRA_BOOK);
        }
    }
}
