package org.ethack.orwall.lib;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.ethack.orwall.database.OpenHelper;
import org.sufficientlysecure.rootcommands.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Helper: manage apps in SQLite, in order to prevent concurrent accesses to the DB.
 */
public class NatRules {
    private OpenHelper dbHelper;
    private Context context;
    private final static String TAG = "NatRules";

    public NatRules(Context context) {
        this.dbHelper = new OpenHelper(context);
        this.context = context;
    }

    public boolean isAppInRules(Long appUID) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String[] filterArgs = {
                String.valueOf(appUID)
        };

        Cursor cursor = db.query(
                OpenHelper.NAT_TABLE_NAME,
                null,
                OpenHelper.COLUMN_APPUID + "=?",
                filterArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        boolean appExists = (cursor.getCount() == 1);
        cursor.close();
        db.close();
        return appExists;
    }

    public void removeAppFromRules(Long appUID) {
        String filter = OpenHelper.COLUMN_APPUID + "=?";
        String[] filterArgs = {String.valueOf(appUID)};

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.delete(OpenHelper.NAT_TABLE_NAME, filter, filterArgs);
        db.close();
    }

    public void addAppToRules(Long appUID, String appName, String onionType, Long onionPort, String portType) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(OpenHelper.COLUMN_APPNAME, appName);
        contentValues.put(OpenHelper.COLUMN_APPUID, String.valueOf(appUID));
        contentValues.put(OpenHelper.COLUMN_ONIONPORT, String.valueOf(onionPort));
        contentValues.put(OpenHelper.COLUMN_ONIONTYPE, onionType);
        contentValues.put(OpenHelper.COLUMN_PORTTYPE, portType);

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.insert(OpenHelper.NAT_TABLE_NAME, null, contentValues);
        db.close();
    }

    public ArrayList<AppRule> getAllRules() {
        ArrayList<AppRule> list = new ArrayList<AppRule>();

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] selection = {
                OpenHelper.COLUMN_APPNAME,
                OpenHelper.COLUMN_APPUID,
                OpenHelper.COLUMN_ONIONTYPE,
                OpenHelper.COLUMN_ONIONPORT,
                OpenHelper.COLUMN_PORTTYPE,
        };
        Cursor cursor = db.query(OpenHelper.NAT_TABLE_NAME, selection, null, null, null, null, null);

        if (!cursor.moveToFirst()){
            Log.e(TAG, "getAllRules size is null!");
            return list;
        }

        AppRule appRule;

        do {
            appRule = new AppRule(
                    cursor.getString(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getString(4)
            );
            list.add(appRule);
        } while (cursor.moveToNext());

        cursor.close();
        db.close();
        Log.e(TAG, "getAllRules size: "+ String.valueOf(list.size()));
        return list;
    }

    public int getRuleCount() {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        Cursor cursor = db.query(OpenHelper.NAT_TABLE_NAME, null, null, null, null, null, null);
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
                addAppToRules(uid, name, Constants.DB_ONION_TYPE_TOR, Constants.ORBOT_TRANSPROXY, Constants.DB_PORT_TYPE_TRANS);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
    }

    public AppRule getAppRule(Long appUID) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String[] filterArgs = {
                String.valueOf(appUID)
        };
        String[] selection = {
                OpenHelper.COLUMN_APPNAME,
                OpenHelper.COLUMN_APPUID,
                OpenHelper.COLUMN_ONIONTYPE,
                OpenHelper.COLUMN_ONIONPORT,
                OpenHelper.COLUMN_PORTTYPE,
        };

        Cursor cursor = db.query(
                OpenHelper.NAT_TABLE_NAME,
                selection,
                OpenHelper.COLUMN_APPUID + "=?",
                filterArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        AppRule appRule = new AppRule(
                cursor.getString(0),
                cursor.getLong(1),
                cursor.getString(2),
                cursor.getLong(3),
                cursor.getString(4)
        );
        cursor.close();
        db.close();

        return appRule;
    }
}
