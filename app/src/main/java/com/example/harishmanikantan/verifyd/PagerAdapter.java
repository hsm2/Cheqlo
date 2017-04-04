package com.example.harishmanikantan.verifyd;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by harishmanikantan on 1/20/17.
 */

public class PagerAdapter extends FragmentStatePagerAdapter{
    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs){
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Fragment_1 fragment1 = new Fragment_1();
                return fragment1;
            case 1:
                Fragment_2 fragment2 = new Fragment_2();
                return fragment2;
            case 2:
                Fragment_3 fragment3 = new Fragment_3();
                return fragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
