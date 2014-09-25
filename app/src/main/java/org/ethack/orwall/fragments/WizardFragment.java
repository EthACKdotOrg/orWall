package org.ethack.orwall.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ethack.orwall.R;
import org.ethack.orwall.lib.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Will display a simple Wizard explaining User what orWall can do.
 */
public class WizardFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;


    public WizardFragment() {}

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static WizardFragment create(int position) {
        WizardFragment fragment = new WizardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wizard, container, false);

        int[] titles = {
                R.string.wizard_title_one,
                R.string.wizard_title_two,
                R.string.wizard_title_three,
        };

        String title = getString(R.string.wizard_title_one);
        String step = "Step 1: ";
        if (mPageNumber < titles.length) {
            title = getString(titles[mPageNumber]);
            step = String.format("Step %d: ", mPageNumber+1);
        }
        ((TextView) rootView.findViewById(R.id.wizard_step_title))
                .setText(step + title);

        int[] fragments = {
                R.string.wizard_first,
                R.string.wizard_second,
                R.string.wizard_third,
        };

        String fragment = getString(fragments[0]);
        if (mPageNumber < fragments.length) {
            fragment = getString(fragments[mPageNumber]);
        }
        ((TextView) rootView.findViewById(R.id.wizard_fragment_content)).setText(fragment);

        ((Button) rootView.findViewById(R.id.wizard_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREF_KEY_FIRST_RUN, true).apply();
                getActivity().finish();
            }
        });
        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

}
