package org.ethack.orwall.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.AppRule;

import java.util.List;

/**
 * New adapter class
 * This will create the new application list, for the new tabbed layout.
 * It will provide, in the end, two lists:
 * - enabled application
 * - disabled application
 *
 * Enabled application will get the following information/characteristics:
 * - white color
 * - name of proxy being used (default: Tor/Orbot)
 *  - if there's a pass-through, it will be shown in here
 * - an arrow in order to get a special panel with dedicated options for this app
 * - The dedicate panel will provide:
 *  - choice for another proxy app (if i2p is installed for example)
 *  - choice between "forcing" or "native" connection to the proxy
 *      - native will create a fenced way: the app may not connect to anything else than the proxy port
 *      - forcing will apply the -j REDIRECT we already use now
 *  - choice to allow the app to go to the Net without using any proxy (with a timer)
 *
 * Disabled application will get the following information/characteristics:
 * - grey name (of something showing up "hey, disabled")
 *
 * Once we touch either a disabled or enabled app, it should go to the opposite list, and rules should
 * be removed or added.
 */
public class AppListAdapter extends ArrayAdapter {

    private final static String TAG = "AppListAdapter";

    private final Context context;
    private final List<AppRule> enabledPkgs;
    private final List<PackageInfo> disabledPkgs;

    /**
     * Constructor.
     * @param context - application context
     * @param pkgs - list of all installed packages
     * @param enabledPkgs - list of enabled packages (the ones having some rule associated with)
     */
    public AppListAdapter(Context context, List<PackageInfo> pkgs, List<AppRule> enabledPkgs) {
        super(context, R.layout.rowlayout, pkgs);

        this.context = context;
        this.enabledPkgs = enabledPkgs;
        this.disabledPkgs = pkgs;
    }

    /**
     * Creates the view with both lists.
     * @param position - position in list
     * @param convertView - conversion view
     * @param parent - parent view group
     * @return - the formatted view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        return null;
    }
}
