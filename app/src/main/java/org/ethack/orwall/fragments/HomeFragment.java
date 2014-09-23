package org.ethack.orwall.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ethack.orwall.R;

/**
 * Manage "home" tab fragment.
 * @link org.ethack.orwall.TabbedMain
 */
public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabbed_home, container, false);
    }
}
