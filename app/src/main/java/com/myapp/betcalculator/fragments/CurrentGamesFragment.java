package com.myapp.betcalculator.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.activities.CalculatingActivity;
import com.myapp.betcalculator.fragments.currentGame.CurrentGame2Fragment;
import com.myapp.betcalculator.myClasses.MyDatabaseHelper;
import com.myapp.betcalculator.myClasses.MyViewPagerAdapter;
import com.myapp.betcalculator.myClasses.dto.Game;

import java.util.ArrayList;

//현재 게임의 진행상황을 입력하는 Activity.
//ViewPager 를 갖고있고, 하나씩 넘길 수 있다.
public class CurrentGamesFragment extends Fragment {
	
	//필요한 변수 선언
	private static TabLayout tlGameSelector;
	private static ViewPager2 gamePager;
	private static SQLiteDatabase database;
	private MyDatabaseHelper myDatabaseHelper;
	private static MyViewPagerAdapter myViewPagerAdapter;
	private static Game game;
	private static ArrayList<Fragment> gameFragments = new ArrayList<>();
	private String tableName;
	private static FragmentActivity fragmentActivity;
	public static SQLiteDatabase getDatabase() {
		return database;
	}
	
	public static TabLayout getTlGameSelector() {
		return tlGameSelector;
	}
	
	private String email = "";
	private int role;
	
	//파라미터로 받은 정보를 이용해서 새로운 Fragment를 만들고,
	//아답타에 그 프레그먼트를 추가, 포커스를 이동하는 메서드
	public static void createNewFragment(Game.GameDetails newGame, String tableName) {
		setViewPager(tableName);
		int lastIndex = (gameFragments.size() - 1);
		gamePager.setCurrentItem(lastIndex);
	}
	
	public static void setViewPager(String tableName) {
		//테이블명으로 db에서 게임 정보를 가져오자.
		game = selectGame(tableName);
		//game > gameDetails 의 객체 수 만큼 fragment 를 만들자.
		myViewPagerAdapter = new MyViewPagerAdapter(fragmentActivity);
		for (int i = 0; i < game.getGameDetailsArrayList().size(); i++) {
			myViewPagerAdapter.addFragment(game.getGameDetailsArrayList().get(i), tableName);
			gameFragments.add(myViewPagerAdapter.getGameFragments().get(i));
		}
		gamePager.setAdapter(myViewPagerAdapter);
		new TabLayoutMediator(tlGameSelector, gamePager, (tab, position) -> tab.setText("ROUND " + (position+1))).attach();
		
		//탭들한테 마진 세팅
		for (int i = 0; i < tlGameSelector.getTabCount(); i++) {
			if ((i + 1) < tlGameSelector.getTabCount()) {
				View tab = ((ViewGroup) tlGameSelector.getChildAt(0)).getChildAt(i);
				ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
				p.setMargins(5, 0, 5, 0);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_current_games, container, false);
		
		Intent intent = CalculatingActivity.getMyIntent();
		
		role = intent.getIntExtra("role", role);
		
		//로그인했으면 계정정보로 db생성
		email = "";
		if (role == 1) {
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			email = user.getEmail().replace("@", ".");
			email = email.replace(".", "_");
			email += "_";
		}
		
		//객체 참조
		if (myDatabaseHelper == null) {myDatabaseHelper = new MyDatabaseHelper(rootView.getContext(), email);}
		if (database == null) {database = myDatabaseHelper.getWritableDatabase(); }
		fragmentActivity = getActivity();
		gamePager = rootView.findViewById(R.id.gamePager);
		tlGameSelector = rootView.findViewById(R.id.tlCurrentGameSelector);
		
		//viewPager 세팅
		tableName = intent.getStringExtra("tableName");
		
		//테이블명으로 db에서 게임 정보를 가져오자.
		game = selectGame(tableName);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		setViewPager(tableName);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		int lastIndex = (gameFragments.size() - 1);
		gamePager.setCurrentItem(lastIndex);
	}
	
	//현재 게임정보를 DB에서 찾아서 반환하는 메서드
	private static Game selectGame(String tableName) {
		Game thisGame = new Game();
		if (database != null) {
			String sql = "SELECT * FROM " + tableName;
			Cursor cursor = database.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				thisGame.addGameDetail(new Game.GameDetails(
						//int id, String gameTitle, String gamePlayers, String amountForOneGame, String, score, String regDate
						cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_ID)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_GAME_TITLE)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_PLAYERS)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_AMOUNT_FOR_ONE_GAME)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_SCORE)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_TYPEOFDISTRIBUTION)),
						cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NAME_REG_DATE))));
			}
		}
		return thisGame;
	}
}