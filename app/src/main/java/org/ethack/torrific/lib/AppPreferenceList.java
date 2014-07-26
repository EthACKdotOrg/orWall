package org.ethack.torrific.lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.ethack.torrific.R;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by cedric on 7/26/14.
 */
public class AppPreferenceList extends ListPreference {

    private final PackageManager packageManager;
    private final Context context;

    public AppPreferenceList(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.packageManager = context.getPackageManager();
        this.context = context;
    }

    public AppPreferenceList(Context context) {
        super(context);
        this.packageManager = context.getPackageManager();
        this.context = context;
    }

    @Override
    protected View onCreateDialogView() {
        PreferenceManager preferenceManager = getPreferenceManager();
        String chosen_app = preferenceManager.getSharedPreferences().getString(this.getKey(), "0");
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());
        setEntries(entries());
        setEntryValues(entryValues());
        setValue(chosen_app);
        setPersistent(true);
        setDefaultValue(chosen_app);
        return view;
    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

    private CharSequence[] entries() {
        List<PackageInfo> pkgList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        Collection<CharSequence> list = new ArrayList<CharSequence>();
        for (PackageInfo pkg: pkgList) {
            if(isInternet(pkg))
                list.add(packageManager.getApplicationLabel(pkg.applicationInfo));
        }
        return list.toArray(new CharSequence[list.size()]);
    }

    private CharSequence[] entryValues() {
        List<PackageInfo> pkgList = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        Collection<CharSequence> list = new ArrayList<CharSequence>();
        for (PackageInfo pkg: pkgList) {
            if(isInternet(pkg))
                list.add(Long.toString(pkg.applicationInfo.uid));
        }
        return list.toArray(new CharSequence[list.size()]);
    }

    private int initializeIndex() {
        return 0;
    }

    private boolean isInternet(PackageInfo pkg) {
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
}
