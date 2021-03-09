package com.myapp.betcalculator.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.myClasses.MyDatabaseHelper;
import com.myapp.betcalculator.myClasses.dto.Game;
import com.myapp.betcalculator.myClasses.dto.PlayerList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class StartCalculatorActivity extends AppCompatActivity {
	
	//변수 선언
	private TextView tvNoGame;
	private RecyclerView rvPreGames;
	private RccVAdapterPreGames rccVAdapterPreGames;
	private ArrayList<Game> gameList = new ArrayList<>();
	private SQLiteDatabase database;
	private MyDatabaseHelper databaseHelper;
	private int role;
	private PlayerList playerList;
	private FirstPageActivity firstPageActivity;
	private NewGameSetting2Activity newGameSetting2Activity;
	private CalculatingActivity calculatingActivity;
	
	//리사클러 뷰를 셋팅하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void setRecyclerview() {
		rccVAdapterPreGames = new RccVAdapterPreGames();
		//리사이클러뷰 셋팅
		rccVAdapterPreGames.setItems(gameList);
		rvPreGames.setAdapter(rccVAdapterPreGames);
		rvPreGames.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_calculator);
		
		role = getIntent().getIntExtra("role", -1);
		//객체 참조
		rvPreGames = findViewById(R.id.rvPreGames);
		tvNoGame = findViewById(R.id.tvNoGame);
		
		//로그인했으면 계정정보로 db생성
		String email = "";
		if (role == 1) {
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			email = user.getEmail().replace("@", ".");
			email = email.replace(".", "_");
			email += "_";
		}
		
		if(databaseHelper == null) {databaseHelper = new MyDatabaseHelper(this, email);}
		if(database == null) {database = databaseHelper.getWritableDatabase();}
		
		
		//로그아웃 버튼 이벤트
		Button btnLogout = findViewById(R.id.btnLogout2);
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
		
		/*광고 관련 설정
		광고 로드 전 광고 SDK초기화*/
		MobileAds.initialize(this, "ca-app-pub-3358204980194151~8057797344");
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.build();
		adView.loadAd(adRequest);
//		//목업을 찍기 위해 잠시 숨겼다.
//		adView.setVisibility(View.GONE);

		//"새 게임 시작" 버튼 이벤트.
		findViewById(R.id.btnStartNewGame).setOnClickListener(v -> {
			//새로운 게임을 셋팅할 수 있는 액티비티로 화면을 전환한다.
			newGameSetting2Activity = new NewGameSetting2Activity();
			Intent intent = new Intent(getApplicationContext(), newGameSetting2Activity.getClass());
			intent.putExtra("role", role);
			startActivity(intent);
			finish();
		});
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onResume() {
		super.onResume();
		
		//DB에서 게임 내역들 읽어와서 리스트에 담기
		getPreGames();
		
		//게임 내역이 없으면 없다고 알려주기
		if(gameList.size() == 0) {
			tvNoGame.setVisibility(View.VISIBLE);
		}else {
			tvNoGame.setVisibility(View.GONE);
		}
		
		setRecyclerview();
	}
	
	//DB에서 게임 내역을 읽어오는 메서드
	private void getPreGames() {
		gameList.clear();
		if(database != null) {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android%' AND name NOT LIKE 'sqlite%'";
			
			Cursor cursor = database.rawQuery(sql, null);
			
			while (cursor.moveToNext()) {
				//조회된 테이블명으로 db select 하고 reg date, player 등 필요한 정보 가져오기
				String tableName = cursor.getString(0);
				String selectTableSql = "SELECT * FROM " + tableName + " LIMIT 1";
				Cursor cursor1 = database.rawQuery(selectTableSql, null);
				while (cursor1.moveToNext()) {
					gameList.add(new Game(tableName,
							cursor1.getString(cursor1.getColumnIndex(databaseHelper.COLUMN_NAME_GAME_TITLE)),
							cursor1.getString(cursor1.getColumnIndex(databaseHelper.COLUMN_NAME_PLAYERS)),
							cursor1.getString(cursor1.getColumnIndex(databaseHelper.COLUMN_NAME_TYPEOFDISTRIBUTION)),
							cursor1.getString(cursor1.getColumnIndex(databaseHelper.COLUMN_NAME_REG_DATE))));
				}
				cursor1.close();
			}
			cursor.close();
		}
	}
	
	//이전 게임 내역을 보여주기 위한 RecyclerView의 Adapter객체.
	public class RccVAdapterPreGames extends RecyclerView.Adapter<RccVAdapterPreGames.MyViewHolder> {
		
		private ArrayList<Game> items = new ArrayList<>();
		private Context context;
		
		public ArrayList<Game> getItems() {
			return items;
		}
		
		public void setItems(ArrayList<Game> items) {
			this.items = items;
		}
		
		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		@NonNull
		@Override
		public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_pre_games, parent, false));
		}
		
		@RequiresApi(api = Build.VERSION_CODES.O)
		@Override
		public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
			holder.setItem(items.get(position), position);
		}
		
		@Override
		public int getItemCount() {
			return items.size();
		}
		
		class MyViewHolder extends RecyclerView.ViewHolder {
			
			//변수 선언
			private TextView tvGameTitle, tvPlayers, tvGameDate, tvStartNewGame;
			private CardView cvPreGame;
			private String tableName;
			private Button btnDeleteGame;
			
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@SuppressLint("ClickableViewAccessibility")
			public MyViewHolder(@NonNull View itemView) {
				super(itemView);
				//객체 참조
				tvGameTitle = itemView.findViewById(R.id.tvGameTitle);
				tvPlayers = itemView.findViewById(R.id.tvPlayers);
				tvGameDate = itemView.findViewById(R.id.tvGameDate);
				cvPreGame = itemView.findViewById(R.id.cvPreGame);
				btnDeleteGame = itemView.findViewById(R.id.btnDeleteGame);
				tvStartNewGame = itemView.findViewById(R.id.tvStartNewGame);
				
				//아이템들 터치시에 눌렸다는거 육안으로 확인되도록 터치 이벤트 걸기
				cvPreGame.setOnTouchListener((view, motionEvent) -> {
					float originElevation = cvPreGame.getCardElevation();
					switch (motionEvent.getAction()) {
						case MotionEvent.ACTION_DOWN: {
							cvPreGame.setCardElevation((float) 0);
							return true;
						}
						case MotionEvent.ACTION_UP: {
							cvPreGame.setCardElevation(originElevation);
							//DB조회해서 날짜로 해당 게임 조회, 상세 내역 보여주기.
							calculatingActivity = new CalculatingActivity();
							Intent intent = new Intent(getApplicationContext(), calculatingActivity.getClass());
							intent.putExtra("tableName", tableName);
							intent.putExtra("role", role);
							startActivity(intent);
							finish();
							return true;
						}
						default: {
							cvPreGame.setCardElevation(originElevation);
							return true;
						}
					}
				});
				
				//테이블 삭제 이벤트
				btnDeleteGame.setOnClickListener(view -> {
					confirmDeleteTable(view, tvGameTitle.getText().toString(), tableName);
				});
			}
			
			//카드뷰 안에 내용을 채우는 메서드
			@RequiresApi(api = Build.VERSION_CODES.O)
			@SuppressLint("UseCompatLoadingForDrawables")
			public void setItem(Game item, int position) {
				//아이템 내역들 보여주기
				tvGameTitle.setText(item.getGameTitle());
				processJsonPlayerStr(tvPlayers, item.getGamePlayers());
				tvGameDate.setText(item.getRegDate());
				tableName = item.getTableName();
				
				if (items.size() == 1) { //아이템이 1개일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_for_one_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
				}else if (position == 0) {//첫 번째 아이템 일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_for_first_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
				}else if ((items.size() - 1) == position && position % 2 == 0) {//마지막 아이템인데 짝수 번째 일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_for_last_even_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
				}else if ((items.size() - 1) == position && position % 2 == 1) {//마지막 아이템인데 홀수 번째 일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_for_last_odd_number_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
				}else if (position % 2 == 0) {//짝수 번째 일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_even_number_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_even_number_item));
				}else if (position % 2 == 1){//홀수 번째 일 경우
					cvPreGame.setBackground(getDrawable(R.drawable.background_odd_number_item));
					tvGameTitle.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
					tvPlayers.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
					tvGameDate.setBackground(getDrawable(R.drawable.border_top_for_odd_number_item));
				}
				
				if (position == (items.size() - 1)) {
					tvStartNewGame.setVisibility(View.VISIBLE);
					if (position % 2 == 0) {
						tvStartNewGame.setBackground(getDrawable(R.drawable.background_for_last_odd_number_item));
					}else {
						tvStartNewGame.setBackground(getDrawable(R.drawable.background_for_last_even_item));
					}
				}
			}
		}
		
		//JSON 형식의 player를 정렬해서 보여주자.
		private void processJsonPlayerStr(TextView tvPlayers, String gamePlayersStr) {
			Gson gson = new Gson();
			playerList = gson.fromJson(gamePlayersStr, PlayerList.class);
			String playerStr = "";
			int i = 0;
			for (PlayerList.Player player : playerList.getPlayers()) {
				playerStr += player.getName() + ".\n";
				i++;
			}
			tvPlayers.setText(playerStr);
		}
	}
	
	//테이블을 삭제할 지 물어보는 메서드
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void confirmDeleteTable(View view, String gameTitle, String tableName) {
		new AlertDialog.Builder(this)
				.setTitle("게임 삭제")
				.setMessage("\"" + gameTitle + "\" 에 대한 정보가 삭제됩니다.")
				.setPositiveButton("확인", (dialogInterface, i) -> {
					deleteTable(tableName);
					onResume();//리프레쉬
					//스낵바로 삭제되었다고 알려주기
					makeSnackbar(view, gameTitle);
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
	
	//테이블을 삭제하는 메서드
	private void deleteTable(String tableName) {
		String sql = "DROP TABLE " + tableName;
		database.execSQL(sql);
	}
	private void makeSnackbar(View view, String gameTitle) {
		Snackbar.make(view, "\"" + gameTitle + "\" 에 대한 정보가 삭제되었습니다.", BaseTransientBottomBar.LENGTH_SHORT).show();
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
				.setMessage("로그인 화면으로 돌아갑니다.")
				.setPositiveButton("확인", (dialogInterface, i) -> {
					firstPageActivity = new FirstPageActivity();
					Intent intent = new Intent(getApplicationContext(), firstPageActivity.getClass());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					finish();
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
}