package com.myapp.betcalculator.fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.internal.Constants;
import com.google.gson.Gson;
import com.myapp.betcalculator.BuildConfig;
import com.myapp.betcalculator.R;
import com.myapp.betcalculator.activities.CalculatingActivity;
import com.myapp.betcalculator.myClasses.NumberTextWatcher;
import com.myapp.betcalculator.myClasses.dto.AmountForGame;
import com.myapp.betcalculator.myClasses.dto.Game;
import com.myapp.betcalculator.myClasses.dto.PlayerList;
import com.myapp.betcalculator.myClasses.dto.ScoreList;
import com.pedro.library.AutoPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

//결과보기 화면 프레그먼트
public class GameResult2Fragment extends Fragment {
	
	//변수 선언
	private String tableName;
	private String typeOfDistribution;
	private String[] typeOfDistributions;
	private ArrayList<Game.GameDetails> gameDetails = new ArrayList<>();
	private PlayerList playerList;
	private ScoreList scoreList;
	private AmountForGame amount;
	private TextView tvGameTitle, tvTotalGameCount, tvPlayerNameContainer;
	private EditText etAmountForGame;
	private LinearLayout llPlayersContainer, llPlayerContainer, llPlayerInfoContainer, llRankingContainer, llPlayerNameContainer, llAllPrizeContainer, llWinCountContainer, llWinContainer, llSecContainer, llThiContainer;
	private TextView tvRankingContainer, tvRanking, tvPlayerName, tvAllPrizeContainer, tvAllPrize, tvWinContainer, tvCntWin, tvSecContainer, tvCntSec, tvThiContainer, tvCntThi;
	private TextView tvRankingName1, tvRankingName2, tvRankingName3, tvRankingPrize1, tvRankingPrize2, tvRankingPrize3;
	private Button btnShare;
	private ArrayList<LinearLayout> llPlayerContainers = new ArrayList<>();
	private ArrayList<TextView> tvRankings = new ArrayList<>();
	private ArrayList<TextView> tvPlayerNames = new ArrayList<>();
	private ArrayList<TextView> tvAllPrizes = new ArrayList<>();
	private ArrayList<TextView> tvWinContainers = new ArrayList<>();
	private ArrayList<TextView> tvSecContainers = new ArrayList<>();
	private ArrayList<TextView> tvThiContainers = new ArrayList<>();
	private ArrayList<TextView> tvCntWins = new ArrayList<>();
	private ArrayList<TextView> tvCntSecs = new ArrayList<>();
	private ArrayList<TextView> tvCntThis = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_game_result2, container, false);
		//객체 참조
		tvGameTitle = rootView.findViewById(R.id.tvGameTitle); tvTotalGameCount = rootView.findViewById(R.id.tvTotalGameCount);
		etAmountForGame = rootView.findViewById(R.id.etAmountForGame);
		llPlayersContainer = rootView.findViewById(R.id.llPlayersContainer2);llPlayerContainer = rootView.findViewById(R.id.llPlayerContainer2);llPlayerInfoContainer = rootView.findViewById(R.id.llPlayerInfoContainer);llPlayerNameContainer = rootView.findViewById(R.id.llPlayerNameContainer);llRankingContainer = rootView.findViewById(R.id.llRankingContainer);
		llAllPrizeContainer = rootView.findViewById(R.id.llAllPrizeContainer);llWinCountContainer = rootView.findViewById(R.id.llWinCountContainer);llWinContainer = rootView.findViewById(R.id.llWinContainer);llSecContainer = rootView.findViewById(R.id.llSecContainer);llThiContainer = rootView.findViewById(R.id.llThiContainer);
		tvRankingContainer = rootView.findViewById(R.id.tvRankingContainer);tvRanking = rootView.findViewById(R.id.tvRanking);tvPlayerNameContainer= rootView.findViewById(R.id.tvPlayerNameContainer);tvPlayerName = rootView.findViewById(R.id.tvPlayerName);
		tvAllPrizeContainer = rootView.findViewById(R.id.tvAllPrizeContainer);tvAllPrize = rootView.findViewById(R.id.tvAllPrize);tvWinContainer = rootView.findViewById(R.id.tvWinContainer);tvCntWin = rootView.findViewById(R.id.tvCntWin);
		tvSecContainer = rootView.findViewById(R.id.tvSecContainer);tvCntSec = rootView.findViewById(R.id.tvCntSec);tvThiContainer = rootView.findViewById(R.id.tvThiContainer);tvCntThi = rootView.findViewById(R.id.tvCntThi);
		tvRankingName1 = rootView.findViewById(R.id.tvRankingName1); tvRankingName2 = rootView.findViewById(R.id.tvRankingName2); tvRankingName3 = rootView.findViewById(R.id.tvRankingName3);
		tvRankingPrize1 = rootView.findViewById(R.id.tvRankingPrize1); tvRankingPrize2 = rootView.findViewById(R.id.tvRankingPrize2); tvRankingPrize3 = rootView.findViewById(R.id.tvRankingPrize3);
		btnShare = rootView.findViewById(R.id.btnShare);
		typeOfDistributions = getResources().getStringArray(R.array.typeOfDistribution);
		
		//테이블 명을 가져오자.
		Intent intent = CalculatingActivity.getMyIntent();
		tableName = intent.getStringExtra("tableName");
		
		//테이블 명으로 테이블을 조회 후 모든 raw를 가져오자.
		selectAll(gameDetails);
		typeOfDistribution = gameDetails.get(0).getTypeOfDistribution();
		//총 인원수 객체로 만들어놓기.
		Gson gson = new Gson();
		playerList = gson.fromJson(gameDetails.get(0).getGamePlayers(), PlayerList.class);
		
		//판돈 입력
		amount = gson.fromJson(gameDetails.get(0).getAmountForOneGame(), AmountForGame.class);
		int amountForGame = (amount.getPrizes().getAllPrize() / playerList.getPlayers().size());
		etAmountForGame.setText(NumberFormat.getInstance().format(amountForGame) + " 원");
		
		//게임명 입력
		tvGameTitle.setText(gameDetails.get(0).getGameTitle());
		//승자가 정해진 총 게임 수 입력
		int count = 0;
		for (Game.GameDetails gd : gameDetails) {
			if (gd.getScore() != null) { count++; }
		}
		tvTotalGameCount.setText(count + "회");
		
		//"공유하기"버튼 이벤트
		btnShare.setOnClickListener(view -> {
			//llPlayersContainer 안에 있는 내용을 공유하자.
			//결과를 공유하자.
			Bitmap bitmap = makeBitmap(llPlayersContainer, llPlayersContainer.getWidth(), llPlayersContainer.getHeight());
			Uri uri = saveImageAtCache(bitmap);
			shareImageUri(uri);
		});
		
		return rootView;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//인원 수 만큼 리니어 레이아웃을 만들고 셋팅
		createPlayerContainer(playerList);
		setItem(playerList);
		showRanking();
	}
	
	//뷰로부터 비트맵 이미지를만들어서 반환하는 메서드
	private Bitmap makeBitmap(View view, int totalWidth, int totalHeight) {
		//스크롤바 긴 스크롤바 등 캡쳐할 때
		Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Drawable drawable = view.getBackground();
		if (drawable != null) {
			drawable.draw(canvas);
		}else {
			canvas.drawColor(Color.GRAY);
		}
		view.draw(canvas);
		return bitmap;
		//일반 적인 view들 캡쳐 할 때.
//		llPlayersContainer.setDrawingCacheEnabled(true);
//		llPlayersContainer.buildDrawingCache(true);
//
//		Bitmap bitmap = Bitmap.createBitmap(llPlayersContainer.getDrawingCache());
//		llPlayersContainer.setDrawingCacheEnabled(false);
//		return bitmap;
	}
	
	//인자로 받은 bitmap 이미지를 캐쉬 메모리에 저장하는 메서드
	public Uri saveImageAtCache(Bitmap bitmap) {
		//이 메서드는 메인 스레드가 아닌 다른 스레드에서 호출되어야 한다.
		File imagesFolder = new File(getContext().getCacheDir(), "images");
		Uri uri = null;
		try {
			imagesFolder.mkdirs();
			File file = new File(imagesFolder, "shared_image.png");
			
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			uri = FileProvider.getUriForFile(getContext(), "com.myapp.betcalculator.fileprovider", file);
		} catch (IOException e) {
			Log.d("TAG", "IOException while trying to write file for sharing : " + e.getMessage());
		}
		return uri;
	}
	
	//인자로 받은 Uri에 담겨있는 이미지를 공유하는 메서드
	private void shareImageUri(Uri uri) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		shareIntent.setType("image/png");
		startActivity(Intent.createChooser(shareIntent, "게임 결과 공유"));
	}
	
	//테이블 명으로 모든 raw를 가져오는 메서드
	private void selectAll(ArrayList<Game.GameDetails> gameList) {
		String sql = "SELECT _id, game_title, game_players, amount_for_one_game, score, type_of_distribution, reg_date FROM " + tableName;
		Cursor cursor = CalculatingActivity.getDatabase().rawQuery(sql, null);
		gameList.clear();
		while (cursor.moveToNext()) {
			gameList.add(new Game.GameDetails(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getString(cursor.getColumnIndex("game_title")),
					cursor.getString(cursor.getColumnIndex("game_players")),
					cursor.getString(cursor.getColumnIndex("amount_for_one_game")),
					cursor.getString(cursor.getColumnIndex("score")),
					cursor.getString(cursor.getColumnIndex("type_of_distribution")),
					cursor.getString(cursor.getColumnIndex("reg_date"))));
		}
	}
	
	//플레이어 수 만큼 플레이어 리스트를 만드는 메서드
	@SuppressLint("UseCompatLoadingForDrawables")
	@RequiresApi(api = Build.VERSION_CODES.N)
	private void createPlayerContainer(PlayerList playerList) {
		//리스트들 초기화
		initList();
		for (int i = 0; i < playerList.getPlayers().size(); i++) {
			//제일 바깥을 감싸는 레이아웃
			LinearLayout newllPlayerContainer = new LinearLayout(getContext());
			newllPlayerContainer.setLayoutParams(llPlayerContainer.getLayoutParams());
			newllPlayerContainer.setGravity(llPlayerContainer.getGravity());
			newllPlayerContainer.setOrientation(llPlayerContainer.getOrientation());
			//아이템 배경색 설정
			if (i == 0) { // 첫 번째 아이템 일 경우
				newllPlayerContainer.setBackground(getResources().getDrawable(R.drawable.background_for_first_item));
			}else if (i == (playerList.getPlayers().size() - 1) && (i % 2) == 0) { //마지막 아이템인데 짝수번째 일 경우
				newllPlayerContainer.setBackground(getResources().getDrawable(R.drawable.background_for_last_even_item));
			}else if (i == (playerList.getPlayers().size() - 1) && (i % 2) == 1) { //마지막 아이템인데 홀수번째 일 경우
				newllPlayerContainer.setBackground(getResources().getDrawable(R.drawable.background_for_last_odd_number_item));
			}else if (i % 2 == 0) {// 짝수 번째 아이템
				newllPlayerContainer.setBackground(getResources().getDrawable(R.drawable.background_even_number_item));
			}else {//홀수 번째 아이템
				newllPlayerContainer.setBackground(getResources().getDrawable(R.drawable.background_odd_number_item));
			}
			
			//플레이어의 순위, 이름, 총상금을 감싸는 레이아웃
			LinearLayout newllPlayerInfoContainer = new LinearLayout(getContext());
			newllPlayerInfoContainer.setLayoutParams(llPlayerInfoContainer.getLayoutParams());
			if (i % 2 == 0) {
				newllPlayerInfoContainer.setBackground(getResources().getDrawable(R.drawable.border_right_for_even_number_item));
			}else {
				newllPlayerInfoContainer.setBackground(getResources().getDrawable(R.drawable.border_right_for_odd_number_item));
			}
			newllPlayerInfoContainer.setOrientation(llPlayerInfoContainer.getOrientation());
			newllPlayerInfoContainer.setGravity(llPlayerInfoContainer.getGravity());
			//플레이어의 순위 정보를 감싸는 레이아웃.
			LinearLayout newLlRankingContainer = new LinearLayout(getContext());
			newLlRankingContainer.setLayoutParams(llRankingContainer.getLayoutParams());
			if (i % 2 == 0) {
				newLlRankingContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_even_number_item));
			}else {
				newLlRankingContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_odd_number_item));
			}
			newLlRankingContainer.setOrientation(llRankingContainer.getOrientation());
			newLlRankingContainer.setGravity(llRankingContainer.getGravity());
			//플레이어 순위 정보
			TextView newTvRankingContainer = new TextView(getContext());
			newTvRankingContainer.setLayoutParams(tvRankingContainer.getLayoutParams());
			newTvRankingContainer.setGravity(tvRankingContainer.getGravity());
			newTvRankingContainer.setText(tvRankingContainer.getText().toString());
			newTvRankingContainer.setTextColor(tvRankingContainer.getTextColors());
			newTvRankingContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvRankingContainer.setTypeface(tvRankingContainer.getTypeface());
			newLlRankingContainer.addView(newTvRankingContainer);
			//플레이어의 순위
			TextView newTvRanking = new TextView(getContext());
			newTvRanking.setLayoutParams(tvRanking.getLayoutParams());
			newTvRanking.setGravity(tvRanking.getGravity());
			newTvRanking.setTextColor(tvRanking.getTextColors());
			newTvRanking.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvRanking.setTypeface(tvRanking.getTypeface());
			newLlRankingContainer.addView(newTvRanking);
			//인포컨테이너에 애드
			newllPlayerInfoContainer.addView(newLlRankingContainer);
			//참가자명을 감싸는 레이아웃
			LinearLayout newLlPlayerNameContainer = new LinearLayout(getContext());
			newLlPlayerNameContainer.setLayoutParams(llPlayerNameContainer.getLayoutParams());
			if (i % 2 == 0) {
				newLlPlayerNameContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_even_number_item));
			}else {
				newLlPlayerNameContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_odd_number_item));
			}
			newLlPlayerNameContainer.setGravity(llPlayerNameContainer.getGravity());
			newLlPlayerNameContainer.setOrientation(llPlayerNameContainer.getOrientation());
			//"참가자명" 텍스트뷰
			TextView newTvPlayerNameContainer = new TextView(getContext());
			newTvPlayerNameContainer.setLayoutParams(tvPlayerNameContainer.getLayoutParams());
			newTvPlayerNameContainer.setGravity(tvPlayerNameContainer.getGravity());
			newTvPlayerNameContainer.setText(tvPlayerNameContainer.getText().toString());
			newTvPlayerNameContainer.setTextColor(tvPlayerNameContainer.getTextColors());
			newTvPlayerNameContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvPlayerNameContainer.setTypeface(tvPlayerNameContainer.getTypeface());
			newLlPlayerNameContainer.addView(newTvPlayerNameContainer);
			//참가자명
			TextView newTvPlayerName = new TextView(getContext());
			newTvPlayerName.setLayoutParams(tvPlayerName.getLayoutParams());
			newTvPlayerName.setGravity(tvPlayerName.getGravity());
			newTvPlayerName.setTextColor(tvPlayerName.getTextColors());
			newTvPlayerName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvPlayerName.setTypeface(tvPlayerName.getTypeface());
			newLlPlayerNameContainer.addView(newTvPlayerName);
			//인포컨테이너에 애드
			newllPlayerInfoContainer.addView(newLlPlayerNameContainer);
			//총상금 정보를 감싸는 레이아웃
			LinearLayout newLlAllPrizeContainer = new LinearLayout(getContext());
			newLlAllPrizeContainer.setLayoutParams(llAllPrizeContainer.getLayoutParams());
			newLlAllPrizeContainer.setGravity(llAllPrizeContainer.getGravity());
			//총상금 정보
			TextView newTvAllPrizeContainer = new TextView(getContext());
			newTvAllPrizeContainer.setLayoutParams(tvAllPrizeContainer.getLayoutParams());
			newTvAllPrizeContainer.setGravity(tvAllPrizeContainer.getGravity());
			newTvAllPrizeContainer.setText(tvAllPrizeContainer.getText().toString());
			newTvAllPrizeContainer.setTextColor(tvAllPrizeContainer.getTextColors());
			newTvAllPrizeContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvAllPrizeContainer.setTypeface(tvAllPrizeContainer.getTypeface());
			newLlAllPrizeContainer.addView(newTvAllPrizeContainer);
			//총상금
			TextView newTvAllPrize = new TextView(getContext());
			newTvAllPrize.setLayoutParams(tvAllPrize.getLayoutParams());
			newTvAllPrize.setGravity(tvAllPrize.getGravity());
			newTvAllPrize.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvAllPrize.setTypeface(tvAllPrize.getTypeface());
			newLlAllPrizeContainer.addView(newTvAllPrize);
			//인포컨테이너에 애드
			newllPlayerInfoContainer.addView(newLlAllPrizeContainer);
			
			//플레이어 컨테이너에 애드
			newllPlayerContainer.addView(newllPlayerInfoContainer);
			
			//우승 정보를 감싸고 있는 레이아웃
			LinearLayout newLlWinCountContainer = new LinearLayout(getContext());
			newLlWinCountContainer.setLayoutParams(llWinCountContainer.getLayoutParams());
			newLlWinCountContainer.setGravity(llWinCountContainer.getGravity());
			newLlWinCountContainer.setOrientation(llWinCountContainer.getOrientation());
			//1등 정보를 감싸는 레이아웃
			LinearLayout newLlWinContainer = new LinearLayout(getContext());
			newLlWinContainer.setLayoutParams(llWinContainer.getLayoutParams());
			if (i % 2 == 0) {
				newLlWinContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_even_number_item));
			}else {
				newLlWinContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_odd_number_item));
			}
			newLlWinContainer.setGravity(llWinContainer.getGravity());
			newLlWinContainer.setOrientation(llWinContainer.getOrientation());
			//"우승" 텍스트뷰
			TextView newTvWinContainer = new TextView(getContext());
			newTvWinContainer.setLayoutParams(tvWinContainer.getLayoutParams());
			newTvWinContainer.setGravity(tvWinContainer.getGravity());
			newTvWinContainer.setText(tvWinContainer.getText().toString());
			newTvWinContainer.setTextColor(tvWinContainer.getTextColors());
			newTvWinContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvWinContainer.setTypeface(tvWinContainer.getTypeface());
			newLlWinContainer.addView(newTvWinContainer);
			//우승 횟수 텍스트뷰
			TextView newTvCntWin = new TextView(getContext());
			newTvCntWin.setLayoutParams(tvCntWin.getLayoutParams());
			newTvCntWin.setGravity(tvCntWin.getGravity());
			newTvCntWin.setTextColor(tvCntWin.getTextColors());
			newTvCntWin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvCntWin.setTypeface(tvCntWin.getTypeface());
			newLlWinContainer.addView(newTvCntWin);
			//우승 정보 레이아웃에 애드
			newLlWinCountContainer.addView(newLlWinContainer);
			//2등 정보를 감싸는 레이아웃
			LinearLayout newLlSecContainer = new LinearLayout(getContext());
			newLlSecContainer.setLayoutParams(llSecContainer.getLayoutParams());
			if (i % 2 == 0) {
				newLlSecContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_even_number_item));
			}else {
				newLlSecContainer.setBackground(getResources().getDrawable(R.drawable.border_bottom_for_odd_number_item));
			}
			newLlSecContainer.setGravity(llSecContainer.getGravity());
			newLlSecContainer.setOrientation(llSecContainer.getOrientation());
			//"2등" 텍스트뷰
			TextView newTvSecContainer = new TextView(getContext());
			newTvSecContainer.setLayoutParams(tvSecContainer.getLayoutParams());
			newTvSecContainer.setGravity(tvSecContainer.getGravity());
			newTvSecContainer.setText(tvSecContainer.getText().toString());
			newTvSecContainer.setTextColor(tvSecContainer.getTextColors());
			newTvSecContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvSecContainer.setTypeface(tvSecContainer.getTypeface());
			newLlSecContainer.addView(newTvSecContainer);
			//2등 횟수 텍스트뷰
			TextView newTvCntSec = new TextView(getContext());
			newTvCntSec.setLayoutParams(tvCntSec.getLayoutParams());
			newTvCntSec.setGravity(tvCntSec.getGravity());
			newTvCntSec.setTextColor(tvCntSec.getTextColors());
			newTvCntSec.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvCntSec.setTypeface(tvCntSec.getTypeface());
			newLlSecContainer.addView(newTvCntSec);
			//우승 정보 레이아웃에 애드
			newLlWinCountContainer.addView(newLlSecContainer);
			//3등 정보를 감싸는 레이아웃
			LinearLayout newLlThiContainer = new LinearLayout(getContext());
			newLlThiContainer.setLayoutParams(llThiContainer.getLayoutParams());
			newLlThiContainer.setGravity(llThiContainer.getGravity());
			newLlThiContainer.setOrientation(llThiContainer.getOrientation());
			//"우승" 텍스트뷰
			TextView newTvThiContainer = new TextView(getContext());
			newTvThiContainer.setLayoutParams(tvThiContainer.getLayoutParams());
			newTvThiContainer.setGravity(tvThiContainer.getGravity());
			newTvThiContainer.setText(tvThiContainer.getText().toString());
			newTvThiContainer.setTextColor(tvThiContainer.getTextColors());
			newTvThiContainer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			newTvThiContainer.setTypeface(tvThiContainer.getTypeface());
			newLlThiContainer.addView(newTvThiContainer);
			//우승 횟수 텍스트뷰
			TextView newTvCntThi = new TextView(getContext());
			newTvCntThi.setLayoutParams(tvCntThi.getLayoutParams());
			newTvCntThi.setGravity(tvCntThi.getGravity());
			newTvCntThi.setTextColor(tvCntThi.getTextColors());
			newTvCntThi.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			newTvCntThi.setTypeface(tvCntThi.getTypeface());
			newLlThiContainer.addView(newTvCntThi);
			//우승 정보 레이아웃에 애드
			newLlWinCountContainer.addView(newLlThiContainer);
			
			//플레이어 컨테이너에 애드
			newllPlayerContainer.addView(newLlWinCountContainer);
			
			//관리를 위해 리스트들에 애드
			llPlayerContainers.add(newllPlayerContainer);
			tvRankings.add(newTvRanking);
			tvPlayerNames.add(newTvPlayerName);
			tvAllPrizes.add(newTvAllPrize);
			tvWinContainers.add(newTvWinContainer);
			tvCntWins.add(newTvCntWin);
			tvSecContainers.add(newTvSecContainer);
			tvCntSecs.add(newTvCntSec);
			tvThiContainers.add(newTvThiContainer);
			tvCntThis.add(newTvCntThi);
			
			//스크롤뷰 안에 있는 레이아웃에 애드
			llPlayersContainer.addView(newllPlayerContainer);
		}
	}
	
	//리스트를 초기화 하는 메서드
	private void initList() {
		llPlayerContainers.clear();
		tvRankings.clear();
		tvPlayerNames.clear();
		tvAllPrizes.clear();
		tvCntWins.clear();
		tvCntSecs.clear();
		tvCntThis.clear();
	}
	
	
	//인자로 받은 참가자가 받을 금액 혹은 지불할 금액이 얼마인지 계산하는 메서드
	private int procTotalPrize(PlayerList playerList, int cntWin, int cntSec, int cntThi) {
		int prize = 0;
		Gson gson = new Gson();
		AmountForGame amountForGame = gson.fromJson(gameDetails.get(0).getAmountForOneGame(), AmountForGame.class);
		//전체 지불해야 할 판돈.
		int size = 0;
		for (int i = 0; i < gameDetails.size(); i++) {
			if (gameDetails.get(i).getScore() != null) { size++; }
		}
		int allDef = ((amountForGame.getPrizes().getAllPrize() / playerList.getPlayers().size()) * size);
		//우승 상금
		int allWinPrize = (amountForGame.getPrizes().getWinnerPrize() * cntWin);
		//2등 상금
		int allSecPrize = (amountForGame.getPrizes().getSecondPrize() * cntSec);
		//3등 상금
		int allThiPrize = (amountForGame.getPrizes().getThirdPrize() * cntThi);
		prize = (allWinPrize + allSecPrize + allThiPrize) - allDef;
		return prize;
	}
	//인자로 받은 플레이어가 몇번 우승 했는지 계산하는 메서드
	private int procWinnerCount(String name) {
		int count = 0;
		for (int i = 0; i < gameDetails.size(); i++) {
			if (gameDetails.get(i).getScore() != null) {
				Gson gson = new Gson();
				scoreList = gson.fromJson(gameDetails.get(i).getScore(), ScoreList.class);
				if (scoreList.getScores().getWinner() != null &&
						name.equals(scoreList.getScores().getWinner())) {
					count++;
				}
			}
		}
		return count;
	}
	//인자로 받은 플레이어가 몇번 2등 했는지 계산하는 메서드
	private int procSecondCOunt(String name) {
		int count = 0;
		for (int i = 0; i < gameDetails.size(); i++) {
			if (gameDetails.get(i).getScore() != null) {
				Gson gson = new Gson();
				ScoreList scoreList = gson.fromJson(gameDetails.get(i).getScore(), ScoreList.class);
				if (scoreList.getScores().getSecond() != null &&
						name.equals(scoreList.getScores().getSecond())) {
					count++;
				}
			}
		}
		return count;
	}
	//인자로 받은 플레이어가 몇번 3등 했는지 계산하는 메서드
	private int procThirdCOunt(String name) {
		int count = 0;
		for (int i = 0; i < gameDetails.size(); i++) {
			if (gameDetails.get(i).getScore() != null) {
				Gson gson = new Gson();
				ScoreList scoreList = gson.fromJson(gameDetails.get(i).getScore(), ScoreList.class);
				if (scoreList.getScores().getThird() != null &&
						name.equals(scoreList.getScores().getThird())) {
					count++;
				}
			}
		}
		return count;
	}
	
	//플레이어 수 만큼 아이템을 셋팅하는 메서드
	@RequiresApi(api = Build.VERSION_CODES.O)
	private void setItem(PlayerList playerList) {
		ArrayList<Integer> prizeList = new ArrayList<>();
		//판돈 분배 방식에 따라 횟수 textView 숨기기
		if (typeOfDistribution.equals(typeOfDistributions[1])) { // 1등 몰아주기
			for (int i = 0; i < playerList.getPlayers().size(); i++) {
				tvSecContainers.get(i).setVisibility(View.GONE);
				tvCntSecs.get(i).setVisibility(View.GONE);
				tvThiContainers.get(i).setVisibility(View.GONE);
				tvCntThis.get(i).setVisibility(View.GONE);
			}
		}else if (typeOfDistribution.equals(typeOfDistributions[2])) { // 2등 까지 분배
			for (int i = 0; i < playerList.getPlayers().size(); i++) {
				tvThiContainers.get(i).setVisibility(View.GONE);
				tvCntThis.get(i).setVisibility(View.GONE);
			}
		}
		for (int i = 0; i < playerList.getPlayers().size(); i++) {
			//이름 입력
			tvPlayerNames.get(i).setText(playerList.getPlayers().get(i).getName());
			//1등 횟수 입력
			int countWin = procWinnerCount(playerList.getPlayers().get(i).getName());
			tvCntWins.get(i).setText(countWin+"회");
			//2등 횟수 입력
			int countSec = procSecondCOunt(playerList.getPlayers().get(i).getName());
			tvCntSecs.get(i).setText(countSec+"회");
			//3등 횟수 입력
			int countThi = procThirdCOunt(playerList.getPlayers().get(i).getName());
			tvCntThis.get(i).setText(countThi+"회");
			//총 상금 입력
			int allPrize = procTotalPrize(playerList, countWin, countSec, countThi);
			tvAllPrizes.get(i).setText(allPrize+"");
			//순위 분석
			prizeList.add(allPrize);
		}
		//금액별 오름차순으로 정렬
		Collections.sort(prizeList);
		Collections.reverse(prizeList);
		//중첩 반복문을 이용.
		for (int i = 0; i < playerList.getPlayers().size(); i++) {
			for (int j = 0; j < prizeList.size(); j++) {
				if (tvAllPrizes.get(j).getText().toString().equals(prizeList.get(i).toString())) {
					tvRankings.get(j).setText((i+1) + "등");
					if (i > 2) {
						tvRankings.get(j).setTypeface(getResources().getFont(R.font.gmarket_snas_medium), Typeface.NORMAL);
					}
				}
			}
		}
		//총상금에 금액 포맷 적용
		for (int i = 0; i < playerList.getPlayers().size(); i++) {
			int prize = Integer.parseInt(tvAllPrizes.get(i).getText().toString());
			tvAllPrizes.get(i).setText(NumberFormat.getInstance(Locale.getDefault()).format(prize) + " 원");
			//금액별로 색상 적용
			if (prize < 0) {
				tvAllPrizes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.minusAmount));
			}else {
				tvAllPrizes.get(i).setTextColor(ContextCompat.getColor(getContext(), R.color.plusAmount));
			}
		}
	}
	
	//Ranking에 따라 플레이어명을 입력하는 메서드
	private void showRanking() {
		String playerName1 = ""; String playerName2 = ""; String playerName3 = "";
		String prize1 = ""; String prize2 = ""; String prize3 = "";
		int i = 0;
		for (TextView tv : tvRankings) {
			int ranking = Integer.parseInt(tv.getText().toString().replace("등", "").trim());
			if (ranking == 1) {
				playerName1 += tvPlayerNames.get(i).getText().toString() + "\t";
				prize1 += tvAllPrizes.get(i).getText().toString() + "\t";
			}
			if (ranking == 2) {
				playerName2 += tvPlayerNames.get(i).getText().toString() + "\t";
				prize2 += tvAllPrizes.get(i).getText().toString() + "\t";
			}
			if (ranking == 3) {
				playerName3 += tvPlayerNames.get(i).getText().toString() + "\t";
				prize3 += tvAllPrizes.get(i).getText().toString() + "\t";
			}
			i++;
		}
		tvRankingName1.setText(playerName1); tvRankingPrize1.setText(prize1);
		tvRankingName2.setText(playerName2); tvRankingPrize2.setText(prize2);
		tvRankingName3.setText(playerName3); tvRankingPrize3.setText(prize3);
	}
}