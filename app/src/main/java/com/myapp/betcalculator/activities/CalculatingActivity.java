package com.myapp.betcalculator.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.fragments.CurrentGamesFragment;
import com.myapp.betcalculator.fragments.GameResult2Fragment;
import com.myapp.betcalculator.fragments.ShowRuleFragment;
import com.myapp.betcalculator.myClasses.MyDatabaseHelper;
import com.myapp.betcalculator.myClasses.dto.Game;

public class CalculatingActivity extends AppCompatActivity {
	
	private FrameLayout currentGamePagerContainer;
	private static TabLayout tlFragmentSelector;
	private static Intent intent;
	public static Intent getMyIntent() {
		return intent;
	}
	private static SQLiteDatabase database;
	private static MyDatabaseHelper myDatabaseHelper;
	private Game game;
	public static SQLiteDatabase getDatabase() {
		return database;
	}
	public static TabLayout getTlFragmentSelector() { return tlFragmentSelector; }
	private int role;
	private FirstPageActivity firstPageActivity;
	private CurrentGamesFragment currentGamesFragment;
	private GameResult2Fragment gameResult2Fragment;
	private ShowRuleFragment showRuleFragment;
	private StartCalculatorActivity startCalculatorActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculating);
		
		intent = getIntent();
		role = intent.getIntExtra("role", -1);
		
		//로그아웃 버튼 이벤트
		Button btnLogout = findViewById(R.id.btnLogout3);
		if (role == 1) {
			btnLogout.setVisibility(View.VISIBLE);
			btnLogout.setOnClickListener(view -> {
				firstPageActivity = new FirstPageActivity();
				firstPageActivity.logout();
				Intent intent = new Intent(this, firstPageActivity.getClass());
				startActivity(intent);
				finish();
			});
		}else {
			btnLogout.setVisibility(View.INVISIBLE);
		}
		
		//로그인했으면 계정정보로 db생성
		String email = "";
		if (role == 1) {
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			email = user.getEmail().replace("@", ".");
			email = email.replace(".", "_");
			email += "_";
		}
		
		myDatabaseHelper = new MyDatabaseHelper(getApplicationContext(), email);
		database = myDatabaseHelper.getWritableDatabase();
		
		currentGamePagerContainer = findViewById(R.id.flFragmentContainer);
		tlFragmentSelector = findViewById(R.id.tlFragmentSelector);
		
		//	광고 관련 설정
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.build();
		adView.loadAd(adRequest);
//		//목업을 찍기 위해 잠시 숨겼다.
//		adView.setVisibility(View.GONE);
		
		//이전에 진행했던 게임이라면 결과보기 화면을 보여주고, 아니면 현재 게임 화면을 보여주자
		game = selectGame(intent.getStringExtra("tableName"));
		if (game.getGameDetailsArrayList().get(0).getScore() == null) {
			currentGamesFragment = new CurrentGamesFragment();
			getSupportFragmentManager().beginTransaction().replace(currentGamePagerContainer.getId(), currentGamesFragment).commit();
			tlFragmentSelector.getTabAt(0).select();
		}else {
			gameResult2Fragment = new GameResult2Fragment();
			getSupportFragmentManager().beginTransaction().replace(currentGamePagerContainer.getId(), gameResult2Fragment).commit();
			tlFragmentSelector.getTabAt(1).select();
		}
		
		//탭들한테 마진 세팅
		for (int i = 0; i < tlFragmentSelector.getTabCount(); i++) {
			if ((i + 1) < tlFragmentSelector.getTabCount()) {
				View tab = ((ViewGroup) tlFragmentSelector.getChildAt(0)).getChildAt(i);
				ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
				p.setMargins(0, 0, 5, 0);
			}
		}
		
		tlFragmentSelector.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				
				switch (tab.getPosition()) {
					case 0 : {
						//"현재 게임" 을 클릭했을 때.
						currentGamesFragment = new CurrentGamesFragment();
						getSupportFragmentManager().beginTransaction().replace(currentGamePagerContainer.getId(), currentGamesFragment).commit();
						break;
					}
					case 1 : {
						//"결과 보기" 를 클릭했을 때.
						gameResult2Fragment = new GameResult2Fragment();
						getSupportFragmentManager().beginTransaction().replace(currentGamePagerContainer.getId(), gameResult2Fragment).commit();
						break;
					}
					case 2 : {
						//"족보 보기" 를 클릭했을 때.
						showRuleFragment = new ShowRuleFragment();
						getSupportFragmentManager().beginTransaction().replace(currentGamePagerContainer.getId(), showRuleFragment).commit();
						break;
					}
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) { }
			@Override
			public void onTabReselected(TabLayout.Tab tab) { }
		});
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
	
	//뒤로가기 버튼을 누르면 앱을 종료할 지 물어보자
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			confirmFinish();
		}
		return true;
	}
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void confirmFinish() {
		new AlertDialog.Builder(this)
				.setTitle("돌아가기")
				.setMessage("게임 선택 화면으로 돌아갑니다.")
				.setPositiveButton("확인", (dialogInterface, i) -> {
					startCalculatorActivity = new StartCalculatorActivity();
					Intent intent = new Intent(getApplicationContext(), startCalculatorActivity.getClass());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					finish();
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
}