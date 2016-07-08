package org.ethack.orwall.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ethack.orwall.R;
import org.ethack.orwall.adapter.AppListAdapter;
import org.ethack.orwall.iptables.InitializeIptables;
import org.ethack.orwall.lib.AppRule;
import org.ethack.orwall.lib.AppRuleComparator;
import org.ethack.orwall.lib.Constants;
import org.ethack.orwall.lib.NatRules;
import org.ethack.orwall.lib.PackageInfoData;
import org.sufficientlysecure.rootcommands.RootCommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manage "apps" tab fragment.
 *
 * @link org.ethack.orwall.TabbedMain
 */
public class AppFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        view  = inflater.inflate(R.layout.fragment_tabbed_apps, container, false);
        InitializeIptables initializeIptables = new InitializeIptables(getActivity());
        // Do we have root access ?
        if (RootCommands.rootAccessGiven()) {
            view.findViewById(R.id.warn_root).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.warn_root).setVisibility(View.VISIBLE);
        }
        // Hopefully there IS iptables on this device…
        if (initializeIptables.iptablesExists()) {
            view.findViewById(R.id.warn_iptables).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.warn_iptables).setVisibility(View.VISIBLE);
        }
        if (initializeIptables.initSupported() && !initializeIptables.isInitialized()) {
            view.findViewById(R.id.warn_init).setVisibility(View.VISIBLE);
        }

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
        LongSparseArray<AppRule> rulesIndex = new LongSparseArray<>();
        for (AppRule app: enabledApps) rulesIndex.put(app.getAppUID(), app);

        // get disabled apps (filtered with enabled)
        List<AppRule> disabledApps = listDisabledApps(rulesIndex);
        // Get special, disabled apps
        List<AppRule> specialDisabled = listSpecialApps(rulesIndex);

        // Merge both disabled apps
        disabledApps.addAll(specialDisabled);

        // Sort collection using a dedicated method
        Collections.sort(enabledApps, new AppRuleComparator(getActivity().getPackageManager()));
        Collections.sort(disabledApps, new AppRuleComparator(getActivity().getPackageManager()));

        // merge both collections so that enabled apps are above disabled
        enabledApps.addAll(disabledApps);

        listView.setAdapter(new AppListAdapter(this.getActivity(), enabledApps));

        return view;
    }

    /**
     * List all disabled application. Meaning: installed app requiring Internet, but NOT in NatRules.
     * It also filters out special apps like orbot and i2p.
     *
     * @return List of AppRule
     */
    private List<AppRule> listDisabledApps(LongSparseArray<AppRule> index) {
        PackageManager packageManager = this.getActivity().getPackageManager();
        List<AppRule> pkgList = new ArrayList<>();

        List<PackageInfo> pkgInstalled = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkgInfo : pkgInstalled) {
            if (needInternet(pkgInfo) && !isReservedApp(pkgInfo)) {
                if (index.indexOfKey((long) pkgInfo.applicationInfo.uid) < 0) {
                    pkgList.add(new AppRule(pkgInfo.packageName, (long) pkgInfo.applicationInfo.uid, Constants.DB_ONION_TYPE_NONE, false, false));
                }
            }
        }
        return pkgList;
    }

    private List<AppRule> listSpecialApps(LongSparseArray<AppRule> index) {
        List<AppRule> pkgList = new ArrayList<>();
        Map<String,PackageInfoData> specialApps = PackageInfoData.specialApps();

        for (PackageInfoData pkgInfo: specialApps.values()) {
            if (index.indexOfKey(pkgInfo.getUid()) < 0) {
                pkgList.add(new AppRule(pkgInfo.getPkgName(), pkgInfo.getUid(), Constants.DB_ONION_TYPE_NONE, false, false));
            }
        }

        return pkgList;
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
