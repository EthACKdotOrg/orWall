package org.ethack.orwall.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.ethack.orwall.database.OpenHelper;

import java.util.ArrayList;

/**
 * Helper: manage apps in SQLite, in order to prevent concurrent accesses to the DB.
 */
public class NatRules {
    private OpenHelper dbHelper;

    public NatRules(Context context) {
        this.dbHelper = new OpenHelper(context);
    }

    public boolean isAppInRules(Long appUID) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String[] projection = {OpenHelper.COLUMN_APPUID, OpenHelper.COLUMN_APPNAME};
        String[] filterArgs = {String.valueOf(appUID)};

        Cursor cursor = db.query(
                OpenHelper.NAT_TABLE_NAME,
                projection,
                OpenHelper.COLUMN_APPUID,
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
        String filter = OpenHelper.COLUMN_APPUID;
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
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] selection = {
                OpenHelper.COLUMN_APPNAME,
                OpenHelper.COLUMN_APPUID,
                OpenHelper.COLUMN_ONIONTYPE,
                OpenHelper.COLUMN_ONIONPORT,
                OpenHelper.COLUMN_PORTTYPE,
        };
        Cursor cursor = db.query(OpenHelper.NAT_TABLE_NAME, selection, null, null, null, null, null);
        cursor.moveToFirst();

        ArrayList<AppRule> list = new ArrayList<AppRule>();
        AppRule appRule;

        while(cursor.moveToNext()) {
            appRule = new AppRule(
                    cursor.getString(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getLong(3),
                    cursor.getString(4)
            );
            list.add(appRule);
        }

        cursor.close();
        db.close();
        return list;

    }
}
