package com.myapp.betcalculator.fragments.currentGame;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.activities.CalculatingActivity;
import com.myapp.betcalculator.activities.StartCalculatorActivity;
import com.myapp.betcalculator.fragments.CurrentGamesFragment;
import com.myapp.betcalculator.myClasses.dto.Game;
import com.myapp.betcalculator.myClasses.dto.PlayerList;
import com.myapp.betcalculator.myClasses.dto.ScoreList;

import java.util.ArrayList;

public class CurrentGame2Fragment extends Fragment {
	
	//필요한 변수 미리 선언
	private LinearLayout llPlayerListContainer;
	private Game.GameDetails gameDetails;
	private String tableName;
	private String typeOfDistributionStr;
	private String[] typeOfDistributionsArr;
	private Button btnSaveGame, btnNextGame, btnUpdateGame, btnFinishGame;
	private PlayerList playerList;
	private ViewGroup rootView;
	//게임 참가자의 정보를 표시할 변수들
	private CardView cvPlayer;
	private LinearLayout llPlayerContainer;
	private TextView tvPlayerName;
	private CheckBox cbWinner;
	private CheckBox cbSecond;
	private CheckBox cbThird;
	//플레이어들을 관리하기 위해 리스트 선언
	private ArrayList<CardView> cvPlayers = new ArrayList<>();
	private ArrayList<LinearLayout> llPlayerContainers = new ArrayList<>();
	private ArrayList<TextView> tvPlayerNames = new ArrayList<>();
	private ArrayList<CheckBox> cbWinners = new ArrayList<>();
	private ArrayList<CheckBox> cbSeconds = new ArrayList<>();
	private ArrayList<CheckBox> cbThirds = new ArrayList<>();
	
	private StartCalculatorActivity startCalculatorActivity;
	
	private String TAG;
	
	//프래그먼트를 생성할 때 플레이어 정보가 담긴 GameDetails와 테이블명을 파라미터로 받도록 하자.
	public CurrentGame2Fragment(Game.GameDetails gameDetails, String tableName) {
		this.gameDetails = gameDetails;
		this.tableName = tableName;
		Gson gson = new Gson();
		playerList = gson.fromJson(gameDetails.getGamePlayers(), PlayerList.class);
		typeOfDistributionStr = gameDetails.getTypeOfDistribution();
		TAG = gameDetails.get_id() + "번 게임입니다.";
	}
	
	//플레이어들의 숫자 만큼 카드뷰를 만들어서 add하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.N)
	private void setCardViews() {
		//리스트들을 초기화
		clearMyLists();
		//플레이어의 수 만큼 카드뷰를 만들고, 그걸 컨테이너에 append 하자
		for (int i = 0; i < playerList.getPlayers().size(); i++) {
			CardView cardView = makeCardView(i, playerList.getPlayers().get(i), gameDetails.getScore());
			llPlayerListContainer.addView(cardView, i);
		}
		//체크박스 Visibility 설정 메서드 호출
		setCheckboxesVisibility();
		//체크박스들 같은 등수만 체크되도록 세팅
		setCheckboxesLikeRadioBox2nd();
	}
	
	//체크박스들 1등, 2등 3등 한개씩만 선택할 수 있게 만드는 메서드
	private void setCheckboxesLikeRadioBox2nd() {
		for (int i = 0; i < cbWinners.size(); i++) {
			int index = i;
			cbWinners.get(index).setOnCheckedChangeListener((compoundButton, b) -> {
				if (b) {
					for (int j = 0; j < cbWinners.size(); j++) {
						if (j != index) { cbWinners.get(j).setChecked(false); }
					}
				}
			});
		}
		for (int i = 0; i < cbSeconds.size(); i++) {
			int index = i;
			cbSeconds.get(index).setOnCheckedChangeListener((compoundButton, b) -> {
				if (b) {
					for (int j = 0; j < cbSeconds.size(); j++) {
						if (j != index) { cbSeconds.get(j).setChecked(false); }
					}
				}
			});
		}
		for (int i = 0; i < cbThirds.size(); i++) {
			int index = i;
			cbThirds.get(index).setOnCheckedChangeListener((compoundButton, b) -> {
				if (b) {
					for (int j = 0; j < cbThirds.size(); j++) {
						if (j != index) { cbThirds.get(j).setChecked(false); }
					}
				}
			});
		}
	}
	
	//리스트들을 초기화하는 메서드
	private void clearMyLists() {
		cvPlayers.clear();
		llPlayerContainers.clear();
		tvPlayerNames.clear();
		cbWinners.clear();
		cbSeconds.clear();
		cbThirds.clear();
	}
	
	//새로운 카드뷰를 만들어서 위젯들을 담고, 각각의 리스트에 추가한 다음 반환하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.N)
	@SuppressLint("UseCompatLoadingForDrawables")
	private CardView makeCardView(int index, PlayerList.Player player, String scores) {
		CardView cardView = new CardView(getContext());
		LinearLayout linearLayout = new LinearLayout(getContext());
		TextView textView = new TextView(getContext());
		CheckBox checkBoxWin = new CheckBox(getContext());
		CheckBox checkBoxSec = new CheckBox(getContext());
		CheckBox checkBoxThi = new CheckBox(getContext());
		//인자로 받은 스코어 리스트 객체로 만들기
		Gson gson = new Gson();
		ScoreList scoreList = null;
		if (scores != null) {
			scoreList = gson.fromJson(scores, ScoreList.class);
		}
		
		//미리 만들어놓은 위젯들의 속성을 복사해서 셋팅하자
		cardView.setLayoutParams(cvPlayer.getLayoutParams());
		//아이템 배경색 설정
		if (index == 0) { // 첫 번째 아이템 일 경우
			cardView.setBackground(getResources().getDrawable(R.drawable.background_for_first_item));
		}else if (index == (playerList.getPlayers().size() - 1) && (index % 2) == 0) { //마지막 아이템인데 짝수번째 일 경우
			cardView.setBackground(getResources().getDrawable(R.drawable.background_for_last_even_item));
		}else if (index == (playerList.getPlayers().size() - 1) && (index % 2) == 1) { //마지막 아이템인데 홀수번째 일 경우
			cardView.setBackground(getResources().getDrawable(R.drawable.background_for_last_odd_number_item));
		}else if (index % 2 == 0) {// 짝수 번째 아이템
			cardView.setBackground(getResources().getDrawable(R.drawable.background_even_number_item));
		}else {//홀수 번째 아이템
			cardView.setBackground(getResources().getDrawable(R.drawable.background_odd_number_item));
		}
		
		linearLayout.setLayoutParams(llPlayerContainer.getLayoutParams());
		linearLayout.setGravity(llPlayerContainer.getGravity());
		
		textView.setLayoutParams(tvPlayerName.getLayoutParams());
		textView.setGravity(tvPlayerName.getGravity());
		textView.setPadding(tvPlayerName.getPaddingLeft(), tvPlayerName.getPaddingTop(), tvPlayerName.getPaddingRight(), tvPlayerName.getPaddingBottom());
		textView.setSingleLine(true);
		textView.setText(player.getName());
		textView.setTextColor(tvPlayerName.getTextColors());
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		textView.setTypeface(tvPlayerName.getTypeface());
		linearLayout.addView(textView);
		
		checkBoxWin.setLayoutParams(cbWinner.getLayoutParams());
		checkBoxWin.setButtonTintList(cbWinner.getButtonTintList());
		checkBoxWin.setText(cbWinner.getText().toString());
		checkBoxWin.setTextColor(cbWinner.getTextColors());
		checkBoxWin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		checkBoxWin.setTypeface(cbWinner.getTypeface());
		
		checkBoxSec.setLayoutParams(cbSecond.getLayoutParams());
		checkBoxSec.setButtonTintList(cbSecond.getButtonTintList());
		checkBoxSec.setText(cbSecond.getText().toString());
		checkBoxSec.setTextColor(cbSecond.getTextColors());
		checkBoxSec.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		checkBoxSec.setTypeface(cbSecond.getTypeface());
		
		checkBoxThi.setLayoutParams(cbThird.getLayoutParams());
		checkBoxThi.setButtonTintList(cbThird.getButtonTintList());
		checkBoxThi.setText(cbThird.getText().toString());
		checkBoxThi.setTextColor(cbThird.getTextColors());
		checkBoxThi.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		checkBoxThi.setTypeface(cbThird.getTypeface());
		//스코어가 비어있지 않다면 체크박스들 체크해주기
		if (scoreList != null) {
			if (scoreList.getScores().getWinner() != null && scoreList.getScores().getWinner().equals(player.getName())) {
				checkBoxWin.setChecked(true);
			}else if (scoreList.getScores().getSecond() != null && scoreList.getScores().getSecond().equals(player.getName())) {
				checkBoxSec.setChecked(true);
			}else if (scoreList.getScores().getThird() != null && scoreList.getScores().getThird().equals(player.getName())){
				checkBoxThi.setChecked(true);
			}
		}
		linearLayout.addView(checkBoxWin);
		linearLayout.addView(checkBoxSec);
		linearLayout.addView(checkBoxThi);
		cardView.addView(linearLayout);
		
		//한 카드뷰 안에서 체크박스들을 라디오버튼처럼 쓰기 위한 메서드 호출
		setCheckBoxLikeRadioBox(checkBoxWin, checkBoxSec, checkBoxThi);
		
		//관리를 위해 각각의 리스트에 담기
		cvPlayers.add(cardView);
		llPlayerContainers.add(linearLayout);
		tvPlayerNames.add(textView);
		cbWinners.add(checkBoxWin);
		cbSeconds.add(checkBoxSec);
		cbThirds.add(checkBoxThi);
		
		return cardView;
	}
	
	//판돈 배분 방식에 따라 체크박스들을 숨기는 메서드
	private void setCheckboxesVisibility() {
		if (typeOfDistributionStr.equals(typeOfDistributionsArr[1])) {
			for (CheckBox cb : cbWinners) { cb.setVisibility(View.VISIBLE); }
			for (CheckBox cb : cbSeconds) { cb.setVisibility(View.GONE); }
			for (CheckBox cb : cbThirds) { cb.setVisibility(View.GONE); }
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[2])) {
			for (CheckBox cb : cbWinners) { cb.setVisibility(View.VISIBLE); }
			for (CheckBox cb : cbSeconds) { cb.setVisibility(View.VISIBLE); }
			for (CheckBox cb : cbThirds) { cb.setVisibility(View.GONE); }
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[3])) {
			for (CheckBox cb : cbWinners) { cb.setVisibility(View.VISIBLE); }
			for (CheckBox cb : cbSeconds) { cb.setVisibility(View.VISIBLE); }
			for (CheckBox cb : cbThirds) { cb.setVisibility(View.VISIBLE); }
		}
	}
	
	//한 카드뷰 안에 있는 체크박스들을 라디오버튼처럼 사용하기 위한 메서드
	private void setCheckBoxLikeRadioBox(CheckBox cbWinner, CheckBox cbSecond, CheckBox cbThird) {
		ArrayList<CheckBox> checkBoxes = new ArrayList<>();
		checkBoxes.add(cbWinner);
		checkBoxes.add(cbSecond);
		checkBoxes.add(cbThird);
		for (int i = 0; i < checkBoxes.size(); i++) {
			int index = i;
			checkBoxes.get(index).setOnClickListener(view -> {
				if (checkBoxes.get(index).isChecked()) {
					for (int j = 0; j < checkBoxes.size(); j++) {
						if (j != index) { checkBoxes.get(j).setChecked(false); }
					}
				}
			});
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		//객체 참조
		rootView =  (ViewGroup) inflater.inflate(R.layout.fragment_current_game2, container, false);
		llPlayerListContainer = rootView.findViewById(R.id.llPlayerListContainer);
		typeOfDistributionsArr = getResources().getStringArray(R.array.typeOfDistribution);
		btnSaveGame = rootView.findViewById(R.id.btnSaveGame);
		btnNextGame = rootView.findViewById(R.id.btnNextGame);
		btnUpdateGame = rootView.findViewById(R.id.btnUpdateGame);
		btnFinishGame = rootView.findViewById(R.id.btnFinishGame);
		//cardView 이하 위젯들 참조
		cvPlayer = rootView.findViewById(R.id.cvPlayer);
		llPlayerContainer = rootView.findViewById(R.id.llPlayerContainer);
		tvPlayerName = rootView.findViewById(R.id.tvPlayerName);
		cbWinner = rootView.findViewById(R.id.cbWinner);
		cbSecond = rootView.findViewById(R.id.cbSecond);
		cbThird = rootView.findViewById(R.id.cbThird);
		
		//이전에 했던 게임을 보는거면 "수정"버튼을 보여주고, 새로 시작되는 게임이면 "저장"버튼을 보여준다.
		if (gameDetails.getScore() == null) {
			btnUpdateGame.setVisibility(View.GONE);
			btnSaveGame.setVisibility(View.VISIBLE);
			btnNextGame.setVisibility(View.GONE);
		}else {
			btnSaveGame.setVisibility(View.GONE);
			btnUpdateGame.setVisibility(View.VISIBLE);
			//게임을 저장할 때 다음 판을 진행하지 않고 바로 결과보기로 갔을 수도 있다. 다음 게임이 없다면 "다음게임"버튼을 보여주자
			if (selectLast_id(tableName) == gameDetails.get_id()) {
				btnNextGame.setVisibility(View.VISIBLE);
			}else {
				btnNextGame.setVisibility(View.GONE);
			}
		}
		
		//"저장" 버튼 이벤트
		btnSaveGame.setOnClickListener(view -> {
			//승자들을 체크했는지 확인하자.
			boolean allChecked = allCheck();
			if (allChecked) {
				//모두 다 체크 했다면, 해당 정보를 DB에 저장하고,
				updateRaw();
				//새로운 판을 생성할지 묻는다.
				String title = "저장 완료";
				String message = "다음 판을 생성하여 게임을 계속 진행합니다.\n" +
						"\"그만하기\"버튼을 누르면 게임 결과 화면으로 전환됩니다.";
				askingMakeNextGame(title, message, gameDetails, tableName);
			}else {
				//승자를 체크하지 않았다면, 체크하라는 메세지를 띄우자.
				makeSnackBar(rootView, "이긴 사람을 선택해야합니다.");
			}
		});
		
		//"다음 게임" 버튼 이벤트
		btnNextGame.setOnClickListener(view -> {
			String title = "다음 판을 생성";
			String message = "다음 판을 생성하여 게임을 계속 진행합니다.\n" +
					"\"그만하기\"버튼을 누르면 게임 결과 화면으로 전환됩니다.";
			askingMakeNextGame(title, message, gameDetails, tableName);
		});
		
		//"수정" 버튼 이벤트
		btnUpdateGame.setOnClickListener(v -> {
			String msg = "승자를 변경합니다.\n";
			for (int i = 0; i < playerList.getPlayers().size(); i++) {
				if (cbWinners != null && cbWinners.get(i) != null && cbWinners.get(i).isChecked()) {
					msg += "우승 : " + playerList.getPlayers().get(i).getName() + ".";
				}
			}
			for (int i = 0; i < playerList.getPlayers().size(); i++) {
				if (cbSeconds != null && cbSeconds.get(i) != null && cbSeconds.get(i).isChecked()) {
					msg += "\n2등 : " + playerList.getPlayers().get(i).getName() + ".";
				}
			}
			for (int i = 0; i < playerList.getPlayers().size(); i++) {
				if (cbThirds != null && cbThirds.get(i) != null && cbThirds.get(i).isChecked()) {
					msg += "\n3등 : " + playerList.getPlayers().get(i).getName() + ".";
				}
			}
			confirmUpdate(rootView, "업데이트 정보 확인", msg);
		});
		
		//"종료" 버튼 이벤트
		btnFinishGame.setOnClickListener(v -> {
			//현재 게임이 저장되어있지 않으면 확인을 해야한다.
			String sql = "SELECT _id, game_title, amount_for_one_game, game_players, score, type_of_distribution, reg_date " +
					"FROM " + tableName + " ORDER BY _id DESC LIMIT 1";
			Cursor cursor = CurrentGamesFragment.getDatabase().rawQuery(sql, null);
			Game.GameDetails ruSave = null;
			if (cursor.moveToNext()) {
				ruSave = new Game.GameDetails(
						cursor.getInt(cursor.getColumnIndex("_id")),
						cursor.getString(cursor.getColumnIndex("game_title")),
						cursor.getString(cursor.getColumnIndex("amount_for_one_game")),
						cursor.getString(cursor.getColumnIndex("game_players")),
						cursor.getString(cursor.getColumnIndex("score")),
						cursor.getString(cursor.getColumnIndex("type_of_distribution")),
						cursor.getString(cursor.getColumnIndex("reg_date")));
			}
			
			if (ruSave.getScore() == null) {
				String msg = (CurrentGamesFragment.getTlGameSelector().getSelectedTabPosition()+1) +
						"번 게임의 승자를 입력하지 않았습니다.\n종료 하시겠습니까?";
				confirmDelete(v, "종료 승인", msg);
			}else {
				confirmDelete(v, "종료 확인", "종료 하시겠습니까?");
			}
		});
		return rootView;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setCardViews();
	}
	
	//db에서 game Id를 조회하는 메서드
	private int selectLast_id(String tableName) {
		int lastGameId = 0;
		String sql = "SELECT _id FROM " + tableName + " ORDER BY _id DESC LIMIT 1";
		Cursor cursor = CurrentGamesFragment.getDatabase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			lastGameId = cursor.getInt(cursor.getColumnIndex("_id"));
		}
		cursor.close();
		return lastGameId;
	}
	
	//다음 판을 생성할지 묻는 메서드
	private void askingMakeNextGame(String title, String message, Game.GameDetails gameDetails, String tableName) {
		new AlertDialog.Builder(getContext())
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton("한판 더!", (dialogInterface, i) -> {
					//새로운 게임을 생성
					Game.GameDetails newGame = insertRaw(gameDetails, tableName);
					//새로운 프레그먼트를 생성하고, 그 프레그먼트로 포커스를 맞춘다.
					CurrentGamesFragment.createNewFragment(newGame, tableName);
					//이 프레그먼트에서의 승자 정보는 수정 할 수 있도록 수정 버튼 보여주기. 저장 및 다음 게임으로 버튼은 숨기기.
					btnUpdateGame.setVisibility(View.VISIBLE);
					btnSaveGame.setVisibility(View.GONE);
					btnNextGame.setVisibility(View.GONE);
				})
				.setNegativeButton("그만하기", (dialogInterface, i) -> {
					//"저장" 버튼은 숨기고, "다음 게임" 과 "수정" 버튼은 보여준다.
					btnSaveGame.setVisibility(View.GONE);
					btnNextGame.setVisibility(View.VISIBLE);
					btnUpdateGame.setVisibility(View.VISIBLE);
					//"결과 보기" 프레그먼트를 보여주자
					CalculatingActivity.getTlFragmentSelector().getTabAt(1).select();
				}).show();
	}
	
	//종료를 확인하는 메서드.
	private void confirmDelete(View view, String title, String msg) {
		new AlertDialog.Builder(getContext())
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton("확인", (dialogInterface, i) -> {
					//종료할 때
					startCalculatorActivity = new StartCalculatorActivity();
					Intent intent = new Intent(getContext(), startCalculatorActivity.getClass());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					getActivity().finish();
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
	
	//업데이트를 확인하는 메서드.
	private void confirmUpdate(View view, String title, String msg) {
		new AlertDialog.Builder(getContext())
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton("확인", (dialogInterface, i) -> {
					updateRaw();
					makeSnackBar(view, "수정되었습니다.");
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
	
	//승자를 모두 선택했는지 확인하는 메서드
	public boolean allCheck() {
		if (typeOfDistributionStr.equals(typeOfDistributionsArr[1])) {
			int cnt = 0;
			for (CheckBox cb : cbWinners) {
				if (cb.isChecked()) { cnt++; }
			}
			if (cnt > 0) { return true; }
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[2])) {
			int cnt1 = 0, cnt2 = 0;
			for (CheckBox cb : cbWinners) {
				if (cb.isChecked()) { cnt1++; }
			}
			for (CheckBox cb : cbSeconds) {
				if (cb.isChecked()) { cnt2++; }
			}
			if (cnt1 > 0 && cnt2 > 0) { return true; }
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[3])) {
			int cnt1 = 0, cnt2 = 0, cnt3 = 0;
			for (CheckBox cb : cbWinners) {
				if (cb.isChecked()) { cnt1++; }
			}
			for (CheckBox cb : cbSeconds) {
				if (cb.isChecked()) { cnt2++; }
			}
			for (CheckBox cb : cbThirds) {
				if (cb.isChecked()) { cnt3++; }
			}
			if (cnt1 > 0 && cnt2 > 0 && cnt3 > 0) { return true; }
		}
		return false;
	}
	
	//새로운 raw를 insert하고 insert 한 정보를 반환하자.
	private Game.GameDetails insertRaw(Game.GameDetails details, String tableName) {
		//새로운 raw를 insert하자
		String sql = "INSERT INTO " + tableName + "(game_title, amount_for_one_game, game_players, type_of_distribution, reg_date) " +
				"VALUES(\"" + details.getGameTitle() + "\", '" + details.getAmountForOneGame() + "', '" +
				details.getGamePlayers() + "', '" + details.getTypeOfDistribution() + "', \"" + details.getRegDate() + "\")";
		CurrentGamesFragment.getDatabase().execSQL(sql);
		//insert된 raw의 정보를 담아오자.
		String selectSql = "SELECT _id, game_title, amount_for_one_game, game_players, type_of_distribution, reg_date " +
				"FROM " + tableName + " WHERE _id=" + (gameDetails.get_id()+1);
		Cursor cursor = CurrentGamesFragment.getDatabase().rawQuery(selectSql, null);
		Game.GameDetails newGame = null;
		if (cursor.moveToNext()) {
			newGame = new Game.GameDetails(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getString(cursor.getColumnIndex("game_title")),
					cursor.getString(cursor.getColumnIndex("amount_for_one_game")),
					cursor.getString(cursor.getColumnIndex("game_players")),
					null,
					cursor.getString(cursor.getColumnIndex("type_of_distribution")),
					cursor.getString(cursor.getColumnIndex("reg_date")));
		}
		//newGame을 반환하자.
		return newGame;
	}
	
	//수정된 정보로 업데이트 하는 메서드.
	private void updateRaw() {
		String scoreStr = "'{\"scores\":{";
		if (typeOfDistributionStr.equals(typeOfDistributionsArr[1])) {
			for (int i = 0; i < cbWinners.size(); i++) {
				if (cbWinners.get(i).isChecked()) { scoreStr += "\"winner\":\"" +  playerList.getPlayers().get(i).getName() + "\"}}"; }
			}
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[2])) {
			for (int i = 0; i < cbWinners.size(); i++) {
				if (cbWinners.get(i).isChecked()) { scoreStr += "\"winner\":\"" +  playerList.getPlayers().get(i).getName() + "\","; }
			}
			for (int i = 0; i < cbSeconds.size(); i++) {
				if (cbSeconds.get(i).isChecked()) { scoreStr += "\"second\":\"" +  playerList.getPlayers().get(i).getName() + "\"}}"; }
			}
		}else if (typeOfDistributionStr.equals(typeOfDistributionsArr[3])) {
			for (int i = 0; i < cbWinners.size(); i++) {
				if (cbWinners.get(i).isChecked()) { scoreStr += "\"winner\":\"" +  playerList.getPlayers().get(i).getName() + "\","; }
			}
			for (int i = 0; i < cbSeconds.size(); i++) {
				if (cbSeconds.get(i).isChecked()) { scoreStr += "\"second\":\"" +  playerList.getPlayers().get(i).getName() + "\","; }
			}
			for (int i = 0; i < cbThirds.size(); i++) {
				if (cbThirds.get(i).isChecked()) { scoreStr += "\"third\":\"" +  playerList.getPlayers().get(i).getName() + "\"}}"; }
			}
		}
		scoreStr += "'";
		String sql = "UPDATE " + tableName + " SET score=" + scoreStr +
				" WHERE _id=" + gameDetails.get_id();
		CurrentGamesFragment.getDatabase().execSQL(sql);
	}
	
	private void makeSnackBar(View view, String msg) {
		Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
	}
}