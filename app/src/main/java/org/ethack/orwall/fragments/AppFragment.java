package org.ethack.orwall.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ethack.orwall.R;
import org.ethack.orwall.adapter.AppListAdapter;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.AppRuleComparator;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.ethack.orwall.lib.PackageInfoData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage "apps" tab fragment.
 *
 * @link org.ethack.orwall.TabbedMain
 */
public class AppFragment extends Fragment {

    private final static String TAG = "AppFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(Constants.PREF_KEY_ORWALL_ENABLED, true)) {

            view  = inflater.inflate(R.layout.fragment_tabbed_apps, container, false);
            ListView listView = (ListView) view.findViewById(R.id.id_enabled_apps);

            // Toggle hint layer
            boolean hide_hint = getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
                    .getBoolean(Constants.PREF_KEY_HIDE_PRESS_HINT, false);

            if (hide_hint) {
                view.findViewById(R.id.hint_press).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.id_hide_hint).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((View) view.getParent()).setVisibility(View.GONE);
                        getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(Constants.PREF_KEY_HIDE_PRESS_HINT, true).apply();
                    }
                });
            }

            // get enabled apps
            NatRules natRules = new NatRules(this.getActivity());
            List<AppRule> enabledApps = natRules.getAllRules();

            // get disabled apps (filtered with enabled)
            List<AppRule> disabledApps = listDisabledApps();
            // Get special, disabled apps
            List<AppRule> specialDisabled = listSpecialApps();

            // Merge both disabled apps
            disabledApps.addAll(specialDisabled);

            // Sort collection using a dedicated method
            Collections.sort(enabledApps, new AppRuleComparator(getActivity().getPackageManager()));
            Collections.sort(disabledApps, new AppRuleComparator(getActivity().getPackageManager()));

            // merge both collections so that enabled apps are above disabled
            enabledApps.addAll(disabledApps);

            listView.setAdapter(new AppListAdapter(this.getActivity(), enabledApps));
        } else {
            view  = inflater.inflate(R.layout.fragment_tabbed_noapps, container, false);
        }

        return view;
    }

    /**
     * List all disabled application. Meaning: installed app requiring Internet, but NOT in NatRules.
     * It also filters out special apps like orbot and i2p.
     *
     * @return List of AppRule
     */
    private List<AppRule> listDisabledApps() {
        NatRules natRules = new NatRules(this.getActivity());

        PackageManager packageManager = this.getActivity().getPackageManager();
        List<AppRule> pkgList = new ArrayList<AppRule>();

        List<PackageInfo> pkgInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkgInfo : pkgInstalled) {
            if (needInternet(pkgInfo) && !isReservedApp(pkgInfo)) {
                if (!natRules.isAppInRules((long) pkgInfo.applicationInfo.uid)) {
                    pkgList.add(new AppRule(pkgInfo.packageName, (long) pkgInfo.applicationInfo.uid, "None", (long) 0, "None"));
                }
            }
        }
        return pkgList;
    }

    private List<AppRule> listSpecialApps() {
        NatRules natRules = new NatRules(this.getActivity());
        List<AppRule> pkgList = new ArrayList<AppRule>();
        Map<String,PackageInfoData> specialApps = PackageInfoData.specialApps();

        for (PackageInfoData pkgInfo: specialApps.values()) {
            if (!natRules.isAppInRules(pkgInfo.getUid())) {
                pkgList.add(new AppRule(pkgInfo.getPkgName(), pkgInfo.getUid(), "None", (long) 0, "None"));
            }
        }

        return pkgList;
    }

    /**
     * Checks if package is System or not.
     *
     * @param pkgInfo PackageInfo object
     * @return true if package is a system app
     */
    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0);
    }

    /**
     * Checks if application requires Internet
     *
     * @param pkg PackageInfo object
     * @return true if package requires internet
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
     * @param pkg PackageInfo object
     * @return true if package name matches one of the reserved names
     */
    private boolean isReservedApp(PackageInfo pkg) {
        return (
                pkg.packageName.equals(Constants.ORBOT_APP_NAME) ||
                        pkg.packageName.equals(Constants.I2P_APP_NAME)
        );
    }

}
