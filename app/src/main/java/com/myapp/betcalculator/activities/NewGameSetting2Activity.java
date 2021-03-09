package com.myapp.betcalculator.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.myClasses.MyDatabaseHelper;
import com.myapp.betcalculator.myClasses.NumberTextWatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewGameSetting2Activity extends AppCompatActivity {
	
	//변수 선언
	private Spinner spinnerPlayerCount, spinnerDistribution;
	private EditText etPlayer, etAmountForPlayer, etAmount, etGameName, etFirstPrize, etSecondPrize, etThirdPrize;
	private TextView tvPlayerContainer;
	private Button btnDeletePlayer, btnInsertPlayer, btnStartGame;
	private LinearLayout llPlayersContainer, llPlayer;
	private ArrayList<EditText> etPlayers = new ArrayList<>();
	private ArrayList<Button> btnDeletePlayers = new ArrayList<>();
	private ArrayList<LinearLayout> llPlayers = new ArrayList<>();
	private ArrayList<EditText> etPrizes = new ArrayList<>();
	private SQLiteDatabase database;
	private MyDatabaseHelper myDatabaseHelper;
	private String typeOfDistribution;
	private String[] playerCountArr, typeOfDistributionArr;
	private InputMethodManager inputMethodManager;
	private int role;
	private String email = "";
	private FirstPageActivity firstPageActivity;
	private CalculatingActivity calculatingActivity;
	private StartCalculatorActivity startCalculatorActivity;
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game_setting2);
		
		//객체 참조
		spinnerPlayerCount = findViewById(R.id.spinnerPlayerCount); spinnerDistribution = findViewById(R.id.spinnerDistribution);
		etPlayer = findViewById(R.id.etPlayer); etAmountForPlayer = findViewById(R.id.etAmountForPlayer); etAmount = findViewById(R.id.etAmount); etGameName = findViewById(R.id.etGameName); etFirstPrize = findViewById(R.id.etFirstPrize); etSecondPrize = findViewById(R.id.etSecondPrize); etThirdPrize = findViewById(R.id.etThirdPrize);
		btnDeletePlayer = findViewById(R.id.btnDeletePlayer); btnInsertPlayer = findViewById(R.id.btnInsertPlayer); btnStartGame = findViewById(R.id.btnStartGame);
		llPlayer = findViewById(R.id.llPlayer); llPlayersContainer = findViewById(R.id.llPlayersContainer); tvPlayerContainer = findViewById(R.id.tvPlayerContainer);
		etPrizes.add(etFirstPrize); etPrizes.add(etSecondPrize); etPrizes.add(etThirdPrize);
		if (inputMethodManager == null) {inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);}
		
		role = getIntent().getIntExtra("role", -1);
		
		//로그아웃 버튼 이벤트
		Button btnLogout = findViewById(R.id.btnLogout);
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
		
		//	광고 관련 설정
		AdView adView = findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.build();
		adView.loadAd(adRequest);
//		//목업을 찍기 위해 잠시 숨겼다.
//		adView.setVisibility(View.GONE);
		
		//로그인했으면 계정정보로 db생성
		if (role == 1) {
			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			email = user.getEmail().replace("@", ".");
			email = email.replace(".", "_");
			email += "_";
		}
		
		//데이타베이스 참조
		if (myDatabaseHelper == null) {myDatabaseHelper = new MyDatabaseHelper(this, email);}
		if (database == null) {database = myDatabaseHelper.getWritableDatabase();}
		//스피너 세팅
		playerCountArr = getResources().getStringArray(R.array.PlayerCount);
		typeOfDistributionArr = getResources().getStringArray(R.array.typeOfDistribution);
		setSpinner();
		
		//금액 입력하는곳에 콤마설정
		setEditTextWithComma();
		
		//게임명 미기입시 에러 출력
		etGameName.setOnFocusChangeListener((view, b) -> {
			if(!b) {
				if(TextUtils.isEmpty(etGameName.getText().toString())) {
					etGameName.setError("게임명을 입력하세요.");
				}
			}
		});
		
		//판돈 미입력시 에러 출력
		etAmountForPlayer.setOnFocusChangeListener((view, b) -> {
			
			if (!b) {
				if (TextUtils.isEmpty(etAmountForPlayer.getText().toString()) || etAmountForPlayer.getText().toString().equals("0")) {
					etAmountForPlayer.setText("0");
					etAmountForPlayer.setError("판돈을 입력하세요.");
					etAmount.setText("0");
				}else {
					if (spinnerPlayerCount.getSelectedItemPosition() > 0) {
						int amountForPlayer = Integer.parseInt(etAmountForPlayer.getText().toString().replace(",", ""));
						etAmount.setText((amountForPlayer*Integer.parseInt(spinnerPlayerCount.getSelectedItem().toString())) + "");
					}
				}
				if (Integer.parseInt(etAmountForPlayer.getText().toString().replace(",", "")) > 10000) {
					String title = "알림";
					String message = "판돈이 큰 경우 일시오락의 기준을 넘을 수 있습니다.\n\n" +
							"[ 형법 제246조(도박, 상습도박) ]\n" +
							"① 도박을 한 사람은 1천만원 이하의 벌금에 처한다. 다만, 일시오락 정도에 불과한 경우에는 예외로 한다.\n" +
							"② 상습으로 제1항의 죄를 범한 사람은 3년 이하의 징역 또는 2천만원 이하에 벌금에 처한다.\n\n" +
							"[ 도박문제 헬프라인 : 1336 ]";
					showAlertDialog(title, message);
				}
			}
		});
		
		//플레이어 추가 버튼 이벤트
		btnInsertPlayer.setOnClickListener(view -> {
			hideKeyboard();
			spinnerPlayerCount.setSelection(spinnerPlayerCount.getSelectedItemPosition()+1, true);
			if (llPlayers.size() == 20) {
				btnInsertPlayer.setVisibility(View.GONE);
			}
		});
		
		//"시작" 버튼 이벤트
		btnStartGame.setOnClickListener(view -> {
			int inputAllPrize = Integer.parseInt(etAmount.getText().toString().replace(",", ""));
			int editAllPrize = 0;
			for (EditText et : etPrizes) {
				editAllPrize += Integer.parseInt(et.getText().toString().replace(",", ""));
			}
			//게임명, 판돈 공백, 분배금 정상 분배 체크
			if (etGameName.getText().toString().isEmpty()) {
				makeSnackBar(view, "게임명을 입력하세요.");
				return;
			}
			if (etAmountForPlayer.getText().toString().isEmpty() || etAmountForPlayer.getText().toString().equals("0")) {
				makeSnackBar(view, "인당 판돈을 입력하세요.");
				return;
			}
			if (spinnerPlayerCount.getSelectedItem().toString().equals(playerCountArr[0])) {
				makeSnackBar(view, "참가자 수를 선택하세요.");
				return;
			}
			//분배 방식 선택했는지 체크
			if (typeOfDistribution.equals(typeOfDistributionArr[0])) {
				makeSnackBar(view, "분배 방식을 선택하세요.");
				return;
			}
			if (inputAllPrize != editAllPrize) {
				makeSnackBar(view, "판당 금액과 승자 분배금이 다릅니다.");
				return;
			}
			if (etPlayers != null) {
				for (int i = 0; i < etPlayers.size(); i++) {
					if (etPlayers.get(i).getText().toString().isEmpty()) {
						makeSnackBar(view, "참가자 명을 입력해야 합니다.");
						return;
					}
				}
			}
			
			//참가자들 배열로 만들어서 넣기
			Map<String, String> players = new HashMap<>();
			for (EditText et : etPlayers) {
				if (et.getVisibility() == View.VISIBLE && !et.getText().toString().isEmpty()) {
					players.put("player" + (players.size() + 1), et.getText().toString());
				}
			}
			
			//분배 방식에 따라 인원수가 충분한지 체크
			if (typeOfDistribution.equals(typeOfDistributionArr[1])) {
				//1등 몰아주기 일 경우, 최소 2명 이상
				if (players.size() < 2) {
					makeSnackBar(view, "참가자를 추가해야합니다.");
					return;
				}
			}else if (typeOfDistribution.equals(typeOfDistributionArr[2])) {
				//1, 2등 분배 일 경우, 최소 3명 이상
				if (players.size() < 3) {
					makeSnackBar(view, "참가자를 추가해야합니다.");
					return;
				}
			}else if (typeOfDistribution.equals(typeOfDistributionArr[3])) {
				//3등 까지 분배 일 경우 최소 4명 이상
				if (players.size() < 4) {
					makeSnackBar(view, "참가자를 추가해야합니다.");
					return;
				}
			}
			
			//유효성 검사 통과 했으면 입력한 정보 대로 진행할 것인지 사용자에게 보여주고 물어보도록 하자.
			String str = "입력한 정보로 시작합니다.\n";
			str += "게임명 : " + etGameName.getText().toString() + "\n"
					+ "인당 판돈 : " + etAmountForPlayer.getText().toString() + "\n"
					+ "판당 판돈 : " + etAmount.getText().toString() + "\n";
			for (int i = 0; i < players.size(); i++) {
				str += "참가자" + (i+1) + " : " + players.get("player" + (i+1)) + "\n";
			}
			//컨펌창을 불러오는 메서드
			confirm(view, str);
		});
	}
	
	//스피너를 세팅하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void setSpinner() {
		spinnerPlayerCount.setAdapter(ArrayAdapter.createFromResource(this, R.array.PlayerCount, android.R.layout.simple_spinner_dropdown_item));
		spinnerDistribution.setAdapter(ArrayAdapter.createFromResource(this, R.array.typeOfDistribution, android.R.layout.simple_spinner_dropdown_item));
		typeOfDistribution = typeOfDistributionArr[0];
		
		//분배 방식 스피너 선택 이벤트
		spinnerDistribution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				hideKeyboard();
				//글자색 변경
				((TextView)adapterView.getChildAt(0)).setTextColor(getColor(R.color.font_white));
				typeOfDistribution = spinnerDistribution.getSelectedItem().toString();
				if (typeOfDistribution.equals(typeOfDistributionArr[0])){
					//상금 조정 못하게 막기
					etFirstPrize.setFocusable(false); etSecondPrize.setFocusable(false); etThirdPrize.setFocusable(false);
					etFirstPrize.setFocusableInTouchMode(false); etSecondPrize.setFocusableInTouchMode(false); etThirdPrize.setFocusableInTouchMode(false);
					etFirstPrize.setText("0"); etSecondPrize.setText("0"); etThirdPrize.setText("0");
				}else if (typeOfDistribution.equals(typeOfDistributionArr[1])) {
					//1등 상금만 조정할 수 있게 변경
					etFirstPrize.setFocusable(true); etSecondPrize.setFocusable(false); etThirdPrize.setFocusable(false);
					etFirstPrize.setFocusableInTouchMode(true); etSecondPrize.setFocusableInTouchMode(false); etThirdPrize.setFocusableInTouchMode(false);
					etFirstPrize.setText(etAmount.getText().toString()); etSecondPrize.setText("0"); etThirdPrize.setText("0");
				}else if (typeOfDistribution.equals(typeOfDistributionArr[2])) {
					//2등까지만 상금 조정 할 수 있게 하기
					etFirstPrize.setFocusable(true); etSecondPrize.setFocusable(true); etThirdPrize.setFocusable(false);
					etFirstPrize.setFocusableInTouchMode(true); etSecondPrize.setFocusableInTouchMode(true); etThirdPrize.setFocusableInTouchMode(false);
					etThirdPrize.setText("0");
				}else if (typeOfDistribution.equals(typeOfDistributionArr[3])){
					//3등까지 상금 조정 할 수 있게 하기
					etFirstPrize.setFocusable(true); etSecondPrize.setFocusable(true); etThirdPrize.setFocusable(true);
					etFirstPrize.setFocusableInTouchMode(true); etSecondPrize.setFocusableInTouchMode(true); etThirdPrize.setFocusableInTouchMode(true);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				hideKeyboard();
			}
		});
		
		//참가자 수 스피너 선택 이벤트
		spinnerPlayerCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				hideKeyboard();
				//글자색 변경
				((TextView)adapterView.getChildAt(0)).setTextColor(getColor(R.color.font_white));
				if (spinnerPlayerCount.getSelectedItem().toString().equals(playerCountArr[0])) {
					etAmount.setText("0");
				}else {
					int playerCount = Integer.parseInt(spinnerPlayerCount.getSelectedItem().toString());
					insertPlayer(playerCount);
					int amountForPlayer = Integer.parseInt(etAmountForPlayer.getText().toString().replace(",", ""));
					etAmount.setText((amountForPlayer*playerCount) + "");
					if (spinnerPlayerCount.getSelectedItem().toString().equals(playerCountArr[20])) {
						btnInsertPlayer.setVisibility(View.GONE);
					}else {
						if (btnInsertPlayer.getVisibility() == View.GONE) {
							btnInsertPlayer.setVisibility(View.VISIBLE);
						}
					}
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				hideKeyboard();
			}
		});
	}
	
	//선택된 플레이어 수 만큼 레이아웃을 만들어서 추가하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.M)
	@SuppressLint("UseCompatLoadingForDrawables")
	private void insertPlayer(int playerCount) {
		initList();
		llPlayersContainer.removeAllViewsInLayout();
		for (int i = 0; i < playerCount; i++) {
			//플레이어 정보를 감싸는 레이아웃
			LinearLayout newLlPlayer = new LinearLayout(this);
			newLlPlayer.setLayoutParams(llPlayer.getLayoutParams());
			newLlPlayer.setGravity(View.TEXT_ALIGNMENT_CENTER);
			newLlPlayer.setOrientation(llPlayer.getOrientation());
			if (i == 0) { // 첫 번째 아이템 일 경우
				newLlPlayer.setBackground(getDrawable(R.drawable.background_for_first_item));
			}else if (i == (playerCount - 1) && (i % 2) == 0) { //마지막 아이템인데 짝수번째 일 경우
				newLlPlayer.setBackground(getDrawable(R.drawable.background_for_last_even_item));
			}else if (i == (playerCount - 1) && (i % 2) == 1) { //마지막 아이템인데 홀수번째 일 경우
				newLlPlayer.setBackground(getDrawable(R.drawable.background_for_last_odd_number_item));
			}else if (i % 2 == 0) {// 짝수 번째 아이템
				newLlPlayer.setBackground(getDrawable(R.drawable.background_even_number_item));
			}else {//홀수 번째 아이템
				newLlPlayer.setBackground(getDrawable(R.drawable.background_odd_number_item));
			}
			//"참가자" 텍스트뷰
			TextView newTvPlayerContainer = new TextView(this);
			newTvPlayerContainer.setLayoutParams(tvPlayerContainer.getLayoutParams());
			newTvPlayerContainer.setGravity(tvPlayerContainer.getGravity());
			newTvPlayerContainer.setText(tvPlayerContainer.getText().toString());
			newTvPlayerContainer.setTextColor(tvPlayerContainer.getTextColors());
			newTvPlayerContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvPlayerContainer.setTypeface(tvPlayerContainer.getTypeface()); //텍스트 스타일
			//플레이어 정보에 추가
			newLlPlayer.addView(newTvPlayerContainer);
			//플레이어명 et
			EditText newEtPlayer = new EditText(this);
			newEtPlayer.setLayoutParams(etPlayer.getLayoutParams());
			newEtPlayer.setBackground(etPlayer.getBackground());
			newEtPlayer.setGravity(etPlayer.getGravity());
			newEtPlayer.setInputType(etPlayer.getInputType());
			newEtPlayer.setTextColor(etPlayer.getTextColors());
			newEtPlayer.setHint(etPlayer.getHint());
			newEtPlayer.setHintTextColor(etPlayer.getHintTextColors());
			newEtPlayer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			//플레이어 정보에 추가
			newLlPlayer.addView(newEtPlayer);
			//제외 버튼
			Button newBtnDeletePlayer = new Button(this);
			newBtnDeletePlayer.setLayoutParams(btnDeletePlayer.getLayoutParams());
			newBtnDeletePlayer.setBackground(btnDeletePlayer.getBackground());
			newBtnDeletePlayer.setText(btnDeletePlayer.getText().toString());
			newBtnDeletePlayer.setTextColor(getColor(R.color.font_white));
			newBtnDeletePlayer.setTypeface(btnDeletePlayer.getTypeface());
			newBtnDeletePlayer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			//플레이어 정보에 추가
			newLlPlayer.addView(newBtnDeletePlayer);
			
			//관리를 위해 리스트에 추가
			etPlayers.add(newEtPlayer);
			btnDeletePlayers.add(newBtnDeletePlayer);
			llPlayers.add(newLlPlayer);
			
			//스크롤뷰 레이아웃에 추가
			llPlayersContainer.addView(newLlPlayer, i);
		}
		
		//플레이어 삭제 버튼 이벤트
		for (int i = 0; i < btnDeletePlayers.size(); i++) {
			final int index = i;
			btnDeletePlayers.get(index).setOnClickListener(view -> {
				hideKeyboard();
				spinnerPlayerCount.setSelection(spinnerPlayerCount.getSelectedItemPosition()-1, true);
				if(btnInsertPlayer.getVisibility() == View.GONE) {
					btnInsertPlayer.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
	//리스트를 초기화하는 메서드
	private void initList() {
		etPlayers.clear();
		btnDeletePlayers.clear();
		llPlayers.clear();
	}
	
	//et에 천 단위로 콤마 찍어주는 메서드
	private void setEditTextWithComma() {
		etAmountForPlayer.addTextChangedListener(new NumberTextWatcher(etAmountForPlayer));
		etAmount.addTextChangedListener(new NumberTextWatcher(etAmount));
		//화폐 입력란들 눌릴 때 마다 천 단위 기준 콤마 찍기/판돈 입력 시, 남은 분배금 변화시키기
		for (int i = 0; i < etPrizes.size(); i++) {
			//콤마찍기
			etPrizes.get(i).addTextChangedListener(new NumberTextWatcher(etPrizes.get(i)));
			//남은 분배금 변경
			int finalI = i;
			etPrizes.get(i).setOnFocusChangeListener((view, b) -> {
				if (!b) { //포커스 아웃이 됐다면 실행
					if(etPrizes.get(finalI).getText().toString().isEmpty()) { etPrizes.get(finalI).setText("0");}
				}
			});
		}
	}
	
	//AlertDialog 출력하기
	@RequiresApi(api = Build.VERSION_CODES.O)
	private void confirm(View view, String str) {
		new AlertDialog.Builder(this)
				.setTitle("입력 정보 확인")
				.setMessage(str)
				.setPositiveButton("확인", (dialogInterface, i) -> {
					startCalculating();
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					return;
				}).show();
	}
	
	//입력한 정보를 이용하여 테이블을 생성하고 계산 Activity로 이동하는 메서드.
	@RequiresApi(api = Build.VERSION_CODES.O)
	private void startCalculating() {
		//일단 db에 테이블을 만들자.
		if(myDatabaseHelper == null) {myDatabaseHelper = new MyDatabaseHelper(getApplicationContext(), email);}
		if(database == null) {database = myDatabaseHelper.getWritableDatabase();}
		if(database != null) {
			LocalDateTime currentTime = LocalDateTime.now();
			String tableName = myDatabaseHelper.TABLE_NAME + currentTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm_ss"));
			String sql = "CREATE TABLE " + tableName + "(" +
					myDatabaseHelper.COLUMN_NAME_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
					myDatabaseHelper.COLUMN_NAME_GAME_TITLE + " text, " +
					myDatabaseHelper.COLUMN_NAME_AMOUNT_FOR_ONE_GAME + " text, " +
					myDatabaseHelper.COLUMN_NAME_PLAYERS + " text, " +
					myDatabaseHelper.COLUMN_NAME_SCORE + " text, " +
					myDatabaseHelper.COLUMN_NAME_TYPEOFDISTRIBUTION + " text, " +
					myDatabaseHelper.COLUMN_NAME_REG_DATE + " text)";
			database.execSQL(sql);
			//생성된 테이블에 입력한 정보로 DB를 입력하자.
			String gameTitle = "'" + etGameName.getText().toString() + "'";
			String typeOfDistributionStr = "'" + typeOfDistribution + "'";
			String regDate = "'" + currentTime.format(DateTimeFormatter.ofPattern("MM월dd일yyyy년")) + "'";
			//입력한 참가자들을 json 문자열로 만들어서 db에 넣자.
			String players = "'{\"players\":[";
			//입력한 player들을 json문자열로 바꾸자
			ArrayList<EditText> editTexts = new ArrayList<>();
			for (int i = 0; i < etPlayers.size(); i++) {
				if (etPlayers.get(i).getVisibility() == View.VISIBLE && !etPlayers.get(i).getText().toString().isEmpty()) {
					editTexts.add(etPlayers.get(i));
				}
			}
			for (int i = 0; i < editTexts.size(); i++) {
				final int index = i;
				if ((index+1) == editTexts.size()) {
					players += "{\"name\":\"" + editTexts.get(i).getText().toString() + "\"}]}'";
				}else {
					players += "{\"name\":\"" + editTexts.get(i).getText().toString() + "\"},";
				}
			}
			//판돈도 json문자열로 만들어서 db에 넣자.
			String amountForGame = "'{\"prizes\":{\"allPrize\":\"" + etAmount.getText().toString().replace(",", "") + "\",";
			for (int i = 0; i < etPrizes.size(); i++) {
				if ((i+1) == etPrizes.size()) {
					amountForGame += "\"" + etPrizes.get(i).getHint() + "\":\"" + etPrizes.get(i).getText().toString().replace(",", "") + "\"}}'";
				}else {
					amountForGame += "\"" + etPrizes.get(i).getHint() + "\":\"" + etPrizes.get(i).getText().toString().replace(",", "") + "\",";
				}
			}
			//테이블에 인서트. 테이블의 _id대로 n번째 게임이 될 것이다.
			String sql2 = "INSERT INTO " + tableName + "(" + myDatabaseHelper.COLUMN_NAME_GAME_TITLE + "," +
					myDatabaseHelper.COLUMN_NAME_AMOUNT_FOR_ONE_GAME + "," + myDatabaseHelper.COLUMN_NAME_PLAYERS + "," + myDatabaseHelper.COLUMN_NAME_TYPEOFDISTRIBUTION + "," +
					myDatabaseHelper.COLUMN_NAME_REG_DATE + ") VALUES(" + gameTitle +"," + amountForGame + "," + players + "," + typeOfDistributionStr + "," + regDate + ")";
			database.execSQL(sql2);
			calculatingActivity = new CalculatingActivity();
			//테이블명으로 해당 게임을 검색해야 하므로, 테이블명을 인텐트에 넣어주고 activity를 시작하자.
			Intent intent = new Intent(getApplicationContext(), calculatingActivity.getClass());
			intent.putExtra("tableName", tableName);
			startActivity(intent);
			finish();
		}
	}
	
	//스낵바 출력 메서드
	private void makeSnackBar(View view, String msg) {
		Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
	}
	
	//키보드 숨기기 이벤트
	private void hideKeyboard() {
		etAmountForPlayer.clearFocus();
		etAmount.clearFocus();
		etGameName.clearFocus();
		inputMethodManager.hideSoftInputFromWindow(etAmountForPlayer.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etGameName.getWindowToken(), 0);
		for (EditText e : etPlayers) {
			e.clearFocus();
			inputMethodManager.hideSoftInputFromWindow(e.getWindowToken(), 0);
		}
	}
	
	//알림창을 띄우는 메서드
	private void showAlertDialog(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("확인", (dialogInterface, i) -> { })
				.show();
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