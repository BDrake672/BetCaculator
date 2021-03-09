package com.myapp.betcalculator.myClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "bet_calculator.db";
	private static final int DATABASE_VERSION = 1;
	
	public static String TABLE_NAME = "game_";
	public static final String COLUMN_NAME_ID = "_id";
	public static final String COLUMN_NAME_GAME_TITLE = "game_title";
	public static final String COLUMN_NAME_PLAYERS = "game_players";
	public static final String COLUMN_NAME_AMOUNT_FOR_ONE_GAME = "amount_for_one_game";
	public static final String COLUMN_NAME_SCORE = "score";
	public static final String COLUMN_NAME_TYPEOFDISTRIBUTION = "type_of_distribution";
	public static final String COLUMN_NAME_REG_DATE = "reg_date";
	
	public MyDatabaseHelper(@Nullable Context context, String email) {
		super(context, email + DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) { }
	
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }
}
