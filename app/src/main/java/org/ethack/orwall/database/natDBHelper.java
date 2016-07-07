package org.ethack.orwall.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.ethack.orwall.lib.Constants;

/**
 * Simple DB helper in order to manage SQLite for NAT rules.
 * This also prepare the way for more features.
 */
public class natDBHelper extends SQLiteOpenHelper {

    public static final String NAT_TABLE_NAME = "rules";
    public static final String COLUMN_APPUID = "appUID";
    public static final String COLUMN_APPNAME = "appName";
    public static final String COLUMN_ONIONTYPE = "onionType";
    public static final String COLUMN_LOCALHOST = "localhost";
    public static final String COLUMN_LOCALNETWORK = "localnetwork";

    @Deprecated
    private static final String COLUMN_PORTTYPE = "portType";
    @Deprecated
    private static final String DB_PORT_TYPE_FENCED = "Fenced";
/*
    @Deprecated
    private static final String COLUMN_ONIONPORT = "onionPort";

    private static final String NAT_TABLE_CREATE_V1 =
            String.format(
                    "CREATE TABLE %s (" +
                            "%s INTEGER PRIMARY KEY," +
                            "%s TEXT NOT NULL," +
                            "%s TEXT NOT NULL DEFAULT \"%s\"," +
                            "%s INTEGER NOT NULL DEFAULT '%d'," +
                            "%s TEXT NOT NULL DEFAULT \"TransProxy\")",
                    NAT_TABLE_NAME,
                    COLUMN_APPUID,
                    COLUMN_APPNAME,
                    COLUMN_ONIONTYPE, Constants.DB_ONION_TYPE_TOR,
                    COLUMN_ONIONPORT, Constants.ORBOT_TRANSPROXY,
                    COLUMN_PORTTYPE
            );
*/
    private static final String NAT_TABLE_CREATE_V2 =
            String.format(
                    "CREATE TABLE %s (" +
                            "%s INTEGER PRIMARY KEY," +
                            "%s TEXT NOT NULL," +
                            "%s TEXT," +
                            "%s INTEGER," +
                            "%s INTEGER)",
                    NAT_TABLE_NAME,
                    COLUMN_APPUID,
                    COLUMN_APPNAME,
                    COLUMN_ONIONTYPE,
                    COLUMN_LOCALHOST,
                    COLUMN_LOCALNETWORK
            );

    private static final int DATABASE_VERSION = 2;
    private static final String DB_NAME = "nat.s3db";

    public natDBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NAT_TABLE_CREATE_V2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == newVersion){
            return;
        }

        db.beginTransaction();
        try{
            for(int version = oldVersion; version < newVersion; version++){
                switch (version){
                    // VERSION 1 -----> 2
                    case 1:
                        db.execSQL(String.format("ALTER TABLE %s RENAME TO %s_backup;", NAT_TABLE_NAME, NAT_TABLE_NAME));
                        db.execSQL(NAT_TABLE_CREATE_V2);

                        String[] selection = {
                                COLUMN_APPUID,
                                COLUMN_APPNAME,
                                COLUMN_ONIONTYPE,
                                COLUMN_PORTTYPE
                        };
                        Cursor cursor = db.query(NAT_TABLE_NAME + "_backup", selection, null, null, null, null, null);
                        if (cursor.moveToFirst()) {
                            ContentValues data = new ContentValues();
                            String porType;
                            do {
                                data.put(COLUMN_APPUID, cursor.getLong(0));
                                data.put(COLUMN_APPNAME, cursor.getString(1));
                                data.put(COLUMN_ONIONTYPE, cursor.getString(2));
                                porType = cursor.getString(3);
                                // fenced was localhost (output)
                                if (porType != null && porType.equals(DB_PORT_TYPE_FENCED)){
                                    data.put(COLUMN_LOCALHOST, 1);
                                } else {
                                    data.put(COLUMN_LOCALHOST, 0);
                                }
                                data.put(COLUMN_LOCALNETWORK, 0);

                                db.insert(NAT_TABLE_NAME, null, data);

                            } while (cursor.moveToNext());
                        }
                        cursor.close();

                        db.execSQL(String.format("DROP TABLE %s_backup;", NAT_TABLE_NAME));
                        break;
                }
            }

            db.setTransactionSuccessful();
        } finally{
            db.endTransaction();
        }
    }

}
