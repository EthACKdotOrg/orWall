package org.ethack.orwall.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ethack.orwall.R;
import org.ethack.orwall.adapter.AppListAdapter;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.ethack.orwall.lib.PackageComparator;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manage "apps" tab fragment.
 * @link org.ethack.orwall.TabbedMain
 */
public class AppFragment extends Fragment {

    private final static  String TAG = "AppFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabbed_apps, container, false);

        ListView enabledListView = (ListView)view.findViewById(R.id.id_enabled_apps);
        ListView disabledListView = (ListView)view.findViewById(R.id.id_disabled_apps);

        NatRules natRules = new NatRules(this.getActivity());
        List<AppRule> enabledApps = natRules.getAllRules();
        List<AppRule> disabledApps = listDisabledApps();

        Log.d(TAG, "Enabled size: " + String.valueOf(enabledApps.size()));
        Log.d(TAG, "Disabled size: " + String.valueOf(disabledApps.size()));

        if (natRules.getRuleCount() > 0) {
            enabledListView.setAdapter(new AppListAdapter(this.getActivity(), enabledApps));
        }
        disabledListView.setAdapter(new AppListAdapter(this.getActivity(), disabledApps));
        return view;
    }

    private List<AppRule> listDisabledApps() {
        NatRules natRules = new NatRules(this.getActivity());

        PackageManager packageManager = this.getActivity().getPackageManager();
        List<AppRule> pkgList = new ArrayList<AppRule>();

        List<PackageInfo> pkgInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        Collections.sort(pkgInstalled, new PackageComparator(packageManager));

        for (PackageInfo pkgInfo: pkgInstalled) {
            if (needInternet(pkgInfo) && !isReservedApp(pkgInfo)) {
                if (!natRules.isAppInRules((long)pkgInfo.applicationInfo.uid)) {
                    AppRule tmp = new AppRule(pkgInfo.packageName, (long)pkgInfo.applicationInfo.uid, "None", (long)0, "None");
                    pkgList.add(tmp);
                }
            }
        }
        return pkgList;
    }

    /**
     * Checks if package is System or not.
     *
     * @param pkgInfo
     * @return true if package is a system app
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
    }

    /**
     * Checks if application requires Internet
     *
     * @param pkg
     * @return
     */
    private boolean needInternet(PackageInfo pkg) {
        String[] permissions = (pkg.requestedPermissions);
        if (permissions != null) {
            for (String perm : permissions) {
                if (perm.equals("android.permission.INTERNET")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if app name is a reserved one, like orbot or i2p
     *
     * @param pkg
     * @return
     */
    private boolean isReservedApp(PackageInfo pkg) {
        return (pkg.packageName.equals(Constants.ORBOT_APP_NAME) || pkg.packageName.equals(Constants.I2P_APP_NAME));
    }
}
