package org.ethack.orwall.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ethack.orwall.fragments.AppFragment;
import org.ethack.orwall.fragments.HomeFragment;
import org.ethack.orwall.fragments.LogFragment;

/**
 * Created by cedric on 9/23/14.
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
            case 2:
                return new LogFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
