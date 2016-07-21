package org.ethack.orwall;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import org.ethack.orwall.fragments.WizardFragment;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.Preferences;

/**
 * Simple wizard activity.
 * It will displays orWall capabilities, explain some stuff
 * <p/>
 * Aim: provide main information in a smooth though complete way to the User, so that
 * he knows what to do.
 * <p/>
 * Taken from example in here:
 * https://developer.android.com/training/animation/screen-slide.html
 */
public class WizardActivity extends FragmentActivity {

    // Number of pages (wizard steps)
    private static final int NUM_PAGES = 3;

    // Pager widget
    private ViewPager viewPager;

    // Pager adapter
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        viewPager = (ViewPager) findViewById(R.id.wizard_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            Preferences.setFirstRun(this, false);
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return WizardFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
