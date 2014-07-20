package org.ethack.torrific.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrap some SQLite stuff and creates helpers for NAT configurations
 */
public class NATLiteSource {

    private SQLiteDatabase database;
    private NATDBLite dbHelper;
    private String[] allColumns = {
            NATDBLite.COLUMN_APPNAME,
            NATDBLite.COLUMN_APPUID
    };

    /**
     * Constructor
     * @param context
     */
    public NATLiteSource(Context context) {
        dbHelper = new NATDBLite(context);
    }

    /**
     * Open database read-write
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Close database
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Create a NAT preference and add it to DB
     * @param appUID
     * @param appName
     */
    public void createNAT(long appUID, String appName) {
        ContentValues values = new ContentValues();
        values.put(NATDBLite.COLUMN_APPUID, appUID);
        values.put(NATDBLite.COLUMN_APPNAME, appName);
        database.insert(NATDBLite.TABLE_NAT, null, values);
    }

    /**
     * Delete a NAT preference
     * @param appUID
     */
    public void deleteNAT(long appUID) {
        database.delete(
                NATDBLite.TABLE_NAT,
                String.format("%s = %d", NATDBLite.COLUMN_APPUID, appUID),
                null);
    }

    /**
     * Get all NAT from DB
     * @return List
     */
    public List<NATLite> getAllNats() {
        List<NATLite> nated = new ArrayList<NATLite>();
        String[] columns = {NATDBLite.COLUMN_APPUID, NATDBLite.COLUMN_APPNAME};
        Cursor cursor = database.query(NATDBLite.TABLE_NAT, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NATLite natLite = new NATLite();

            natLite.setAppName(cursor.getString(1));
            natLite.setAppUID(cursor.getLong(0));

            Log.d("UID", Long.toString(natLite.getAppUID()));
            Log.d("Name", natLite.getAppName());
            nated.add(natLite);
            cursor.moveToNext();
        }
        cursor.close();
        return nated;
    }

    /**
     * Check if NAT exists in DB
     * @param appUID
     * @return true if exists
     */
    public boolean natExists(long appUID) {
        Cursor cursor = database.query(
                NATDBLite.TABLE_NAT, allColumns,
                String.format("%s = %d", NATDBLite.COLUMN_APPUID, appUID),
                null,null,null,null
        );
        long num_rows = cursor.getCount();
        cursor.close();
        return num_rows != 0;
    }

    /**
     * Internal helper, transform a cursor to NATLite object
     * @param cursor
     * @return NATLite object
     */
    private NATLite cursorToPref(Cursor cursor) {
        NATLite nat = new NATLite();
        nat.setAppUID(cursor.getLong(0));
        nat.setAppName(cursor.getString(1));
        return nat;
    }
}
