package org.ethack.orwall;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.ethack.orwall.adapter.TabsPagerAdapter;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.Set;

/**
 * New main layout: using a tabbed layout allows to get a cleaner view
 * and a more friendly experience for end-users.
 */
public class TabbedMain extends FragmentActivity implements ActionBar.TabListener {

    // Debug tag
    private String TAG = "TabbedMain";

    // Private variables we may need across multiple methods
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // TODO: use R content for tab names if needed.
    private String[] tabs = {"Home", "Apps"/*, "Logs"*/};

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        Log.d(TAG, String.valueOf(tab.getPosition()));
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_main);

        // Import old settings to SQLite, and remove them from SharedPreferences
        NatRules natRules = new NatRules(this);
        Set oldRules = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getStringSet("nat_rules", null);
        if (natRules.getRuleCount() == 0 && oldRules != null) {
            natRules.importFromSharedPrefs(oldRules);
            getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit().remove("nat_rules").apply();
        }

        // Is it the first application run?
        boolean first_run = this.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).getBoolean(Constants.PREF_KEY_FIRST_RUN, true);
        if (first_run) {
            // Initialize orWall iptables rules - #72 should be better after that
            InitializeIptables initializeIptables = new InitializeIptables(this);
            initializeIptables.boot();
            // Start Wizard
            Intent wizard = new Intent(this, WizardActivity.class);
            startActivity(wizard);
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // create the tab header
        for (String tab : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab).setTabListener(this));
        }

        // changer in order to take care of tab switching
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }


    /**
     * No more menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // We don't need menu anymore now. "Settings" entry is on home page, as well as quick actions
        return true;
    }

    /**
     * We do not provide a menu.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
}
