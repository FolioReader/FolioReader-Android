package com.folioreader.model.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FolioDatabaseHelper extends SQLiteOpenHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "SQLiteOpenHelper";

    private static FolioDatabaseHelper mInstance;
    private static SQLiteDatabase myWritableDb;

    public static final String DATABASE_NAME = "FolioReader.db";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ID = "_id";
    private final Context mContext;

    public FolioDatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static FolioDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FolioDatabaseHelper(context);
        }
        return mInstance;
    }

    public SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }

        return myWritableDb;
    }

    @Override
    public void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }

    @Override
    public final void onCreate(final SQLiteDatabase db) {
        //onUpgradeDropTables(db);
        Log.d("create table highlight", "****" + HighLightTable.SQL_CREATE);
        db.execSQL(HighLightTable.SQL_CREATE);
        /*db.execSQL(EmailAddressesTable.SQL_CREATE);
		db.execSQL(PhoneNumbersTable.SQL_CREATE);
		db.execSQL(LeadTypesTable.SQL_CREATE);
		db.execSQL(LeadStagesTable.SQL_CREATE);
		db.execSQL(LeadSourcesTable.SQL_CREATE);
		db.execSQL(ExpensesTable.SQL_CREATE);
		db.execSQL(SMSPatternsTable.SQL_CREATE);
		db.execSQL(SMSLeadTable.SQL_CREATE);
		*//*SMSPattern pattern=new SMSPattern(1, "^You received a Zillow contact from (.*) at (.*): I am interested in (.*). View here:(.*)$",
					new String[]{"name,phone,reference"}, "Zillow", 0);
		db.insert(SMSPatternsTable.TABLE_NAME, null, pattern.getValues());
		*//*
		if(Preferences.DEBUG) 
			Log.d(TAG, "Database Created");*/
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                                final int newVersion) {
		/* PROTECTED REGION ID(DatabaseUpdate) ENABLED START */

        // TODO Implement your database update functionality here and remove the
        // following method call!
        //onUpgradeDropTables(db);
        //onCreate(db);
        resetAllPreferences(mContext);

		/* PROTECTED REGION END */
    }


    /**
     * This basic upgrade functionality will destroy all old data on upgrade
     */
    private final void onUpgradeDropTables(final SQLiteDatabase db) {

    }

    /**
     * Resets all shared preferences
     *
     * @param context
     */
    private final void resetAllPreferences(Context context) {

    }
}
