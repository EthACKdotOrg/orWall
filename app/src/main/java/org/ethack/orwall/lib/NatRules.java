package org.ethack.orwall.lib;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import org.ethack.orwall.database.natDBHelper;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Helper: manage apps in SQLite, in order to prevent concurrent accesses to the DB.
 */
public class NatRules {
    private final static String TAG = "NatRules";
    private natDBHelper dbHelper;
    private Context context;

    public NatRules(Context context) {
        this.dbHelper = new natDBHelper(context);
        this.context = context;
    }

    public boolean removeAppFromRules(Long appUID) {
        String filter = natDBHelper.COLUMN_APPUID + "=?";
        String[] filterArgs = {String.valueOf(appUID)};

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int result = db.delete(natDBHelper.NAT_TABLE_NAME, filter, filterArgs);
        db.close();
        return (result == 1);
    }

    public boolean addAppToRules(Long appUID, String appName, String onionType, Boolean localHost, Boolean localNetwork) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(natDBHelper.COLUMN_APPNAME, appName);
        contentValues.put(natDBHelper.COLUMN_APPUID, String.valueOf(appUID));
        contentValues.put(natDBHelper.COLUMN_ONIONTYPE, onionType);
        contentValues.put(natDBHelper.COLUMN_LOCALHOST, localHost);
        contentValues.put(natDBHelper.COLUMN_LOCALNETWORK, localNetwork);

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        long result = db.insert(natDBHelper.NAT_TABLE_NAME, null, contentValues);
        db.close();
        return (result > 0);
    }

    public boolean addAppToRules(AppRule appRule) {
        return addAppToRules(
                appRule.getAppUID(),
                appRule.getPkgName(),
                appRule.getOnionType(),
                appRule.getLocalHost(),
                appRule.getLocalNetwork()
        );
    }

    public ArrayList<AppRule> getAllRules() {
        ArrayList<AppRule> list = new ArrayList<>();

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] selection = {
                natDBHelper.COLUMN_APPNAME,
                natDBHelper.COLUMN_APPUID,
                natDBHelper.COLUMN_ONIONTYPE,
                natDBHelper.COLUMN_LOCALHOST,
                natDBHelper.COLUMN_LOCALNETWORK
        };
        Cursor cursor = db.query(natDBHelper.NAT_TABLE_NAME, selection, null, null, null, null, null);

        if (!cursor.moveToFirst()) {
            Log.e(TAG, "getAllRules size is null!");
            return list;
        }

        AppRule appRule;

        do {
            appRule = new AppRule(
                    cursor.getString(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getLong(3) == 1,
                    cursor.getLong(4) == 1
            );
            list.add(appRule);
        } while (cursor.moveToNext());

        cursor.close();
        db.close();
        Log.d(TAG, "getAllRules size: " + String.valueOf(list.size()));
        return list;
    }

    public int getRuleCount() {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = db.query(natDBHelper.NAT_TABLE_NAME, null, null, null, null, null, null);
        cursor.moveToFirst();

        int total = cursor.getCount();
        cursor.close();
        db.close();
        return total;
    }

    public void importFromSharedPrefs(Set oldRules) {
        PackageManager packageManager = this.context.getPackageManager();
        for (Object rule : oldRules.toArray()) {
            HashMap<String, Long> r = (HashMap) rule;
            Long uid = (Long) r.values().toArray()[0];
            String name = (String) r.keySet().toArray()[0];
            // ensure we migrate only existing applications
            try {
                packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
                addAppToRules(uid, name, Constants.DB_ONION_TYPE_TOR, false, false);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
    }

    public boolean update(AppRule appRule) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(natDBHelper.COLUMN_APPNAME, appRule.getPkgName());
        contentValues.put(natDBHelper.COLUMN_APPUID, String.valueOf(appRule.getAppUID()));
        contentValues.put(natDBHelper.COLUMN_ONIONTYPE, appRule.getOnionType());
        contentValues.put(natDBHelper.COLUMN_LOCALHOST, appRule.getLocalHost()?1:0);
        contentValues.put(natDBHelper.COLUMN_LOCALNETWORK, appRule.getLocalNetwork()?1:0);

        String filter = natDBHelper.COLUMN_APPUID + "=?";
        String[] filterArgs = {String.valueOf(appRule.getAppUID())};
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        int nb_row = 0;
        try {
            nb_row = db.update(natDBHelper.NAT_TABLE_NAME, contentValues, filter, filterArgs);
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "Constraint exception");
            Log.e(TAG, e.getMessage());
        }
        db.close();

        return (nb_row == 1);
    }

    public AppRule getAppRule(Long appUID) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String[] filterArgs = {
                String.valueOf(appUID)
        };
        String[] selection = {
                natDBHelper.COLUMN_APPNAME,
                natDBHelper.COLUMN_APPUID,
                natDBHelper.COLUMN_ONIONTYPE,
                natDBHelper.COLUMN_LOCALHOST,
                natDBHelper.COLUMN_LOCALNETWORK
        };

        Cursor cursor = db.query(
                natDBHelper.NAT_TABLE_NAME,
                selection,
                natDBHelper.COLUMN_APPUID + "=?",
                filterArgs,
                null,
                null,
                null
        );

        AppRule appRule;
        if (cursor.moveToFirst()) {
            appRule = new AppRule(
                    cursor.getString(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getLong(3) == 1,
                    cursor.getLong(4) == 1
            );
        } else {
            appRule = new AppRule();
            Log.e(TAG, "Unable to get rules for " + String.valueOf(appUID));
        }
        cursor.close();
        db.close();

        return appRule;
    }
}
