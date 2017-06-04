package com.raqun.piri.sample;


import android.app.Fragment;
import android.os.Bundle;

import com.raqun.PiriFragment;
import com.raqun.PiriParam;

/**
 * Created by tyln on 31/05/2017.
 */

@PiriFragment
public class SampleFragment extends Fragment {

    private static final String EXTRA_ID = "extra_key";
    private static final String EXTRA_NAME = "extra_name";

    @PiriParam(key = EXTRA_ID)
    private Long id;

    @PiriParam(key = EXTRA_NAME)
    private String name;


    public static SampleFragment newInstance() {

        Bundle args = new Bundle();

        SampleFragment fragment = new SampleFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
