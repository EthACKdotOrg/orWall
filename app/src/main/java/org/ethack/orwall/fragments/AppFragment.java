package org.ethack.orwall.fragments;

import android.content.pm.ApplicationInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ethack.orwall.R;

import java.util.List;

/**
 * Manage "apps" tab fragment.
 * @link org.ethack.orwall.TabbedMain
 */
public class AppFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabbed_apps, container, false);
    }

    private List<ApplicationInfo> listAllApps() {
        return null;
    }
}
