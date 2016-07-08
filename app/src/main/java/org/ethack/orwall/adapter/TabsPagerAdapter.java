package org.ethack.orwall.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ethack.orwall.fragments.AppFragment;
import org.ethack.orwall.fragments.HomeFragment;

/**
 * A simple wrapper for tab management.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new HomeFragment();
            case 1:
                return new AppFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
