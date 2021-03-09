package com.myapp.betcalculator.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.betcalculator.R;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

//로그인 화면 액티비티
public class FirstPageActivity extends AppCompatActivity implements AutoPermissionsListener {
	
	//변수 선언
	private ProgressBar progressBar;
	private EditText etId, etPwd;
	private Button btnLogin, btnCreate, btnGuest;
	private CheckBox cbRememberId, cbRememberPassword, cbRememberMe;
	private InputMethodManager inputMethodManager;
	private CreateAccountActivity createAccountActivity;
	private StartCalculatorActivity startCalculatorActivity;
	
//	private static RequestQueue requestQueue;
//	public static RequestQueue getRequestQueue() {
//		return requestQueue;
//	}
	
	private final String NO_ACCOUNT_STR = "There is no user record corresponding to this identifier. The user may have been deleted.";
	private final String WRONG_PASSWORD_STR = "The password is invalid or the user does not have a password.";
	
	private static SharedPreferences appData; //설정 정보를 저장할 SharedPreferences 객체
	
	private FirebaseAuth mAuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_page);
		
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
		
		//객체 참조
		if (progressBar == null) { progressBar = findViewById(R.id.progressBar3); }
		if(etId == null) {etId = findViewById(R.id.etId);}
		if(etPwd == null) {etPwd = findViewById(R.id.etPwd);}
		if(btnLogin == null) {btnLogin = findViewById(R.id.btnLogin);}
		if(btnCreate == null) {btnCreate = findViewById(R.id.btnJoin);}
		if(btnGuest == null) {btnGuest = findViewById(R.id.btnGuest);}
		
		appData = getSharedPreferences("appData", MODE_PRIVATE);
		cbRememberId = findViewById(R.id.cbRememberId);
		cbRememberPassword = findViewById(R.id.cbRememberPassword);
		cbRememberMe = findViewById(R.id.cbRememberMe);
		
		if(inputMethodManager == null) {inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);}
//		if(requestQueue == null) {requestQueue = Volley.newRequestQueue(this);}
		if (mAuth == null) { mAuth = FirebaseAuth.getInstance(); }
		
		//권한요청
		AutoPermissions.Companion.loadAllPermissions(this, 101);
		
		//저장한 설정값 가져오기
		loadData();
		
		//"Login" 버튼 이벤트
		btnLogin.setOnClickListener(view -> {
			//유효성 검사 실행 후 서버 DB와 비교, 로그인 하기.
			hideKeyboard();
			if(etId.getText().toString().isEmpty() && etPwd.getText().toString().isEmpty()) {
				Snackbar.make(view, "아이디와 비밀번호를 입력하세요.", Snackbar.LENGTH_SHORT).show();
				return;
			}
			if (etId.getText().toString().isEmpty()) {
				Snackbar.make(view, "아이디를 입력하세요.", Snackbar.LENGTH_SHORT).show();
				return;
			}
			if (etPwd.getText().toString().isEmpty()) {
				Snackbar.make(view, "비밀번호를 입력하세요.", Snackbar.LENGTH_SHORT).show();
				return;
			}
			String email = etId.getText().toString();
			String password = etPwd.getText().toString();
			login(view, email, password);
		});
		
		//"Create" 버튼 이벤트
		btnCreate.setOnClickListener(v -> {
			//계정생성 Activity로 이동하기.
			createAccountActivity = new CreateAccountActivity();
			Intent intent = new Intent(getApplicationContext(), createAccountActivity.getClass());
			startActivity(intent);
			finish();
		});
		
		//"Guest" 버튼 이벤트
		btnGuest.setOnClickListener(v -> {
			//계정생성 없이 계산기 시작.
			startCalculatorActivity = new StartCalculatorActivity();
			Intent intent = new Intent(getApplicationContext(), startCalculatorActivity.getClass());
			intent.putExtra("role", 0);
			startActivity(intent);
			finish();
		});
	}
	
	//사용자가 저장한 설정 값을 불러오는 메서드
	private void loadData() {
		etId.setText(appData.getString("email", ""));
		if (appData.getBoolean("SAVE_EMAIL", false)) {
			cbRememberId.setChecked(true);
		}
		etPwd.setText(appData.getString("password", ""));
		if (appData.getBoolean("SAVE_PASSWORD", false)) {
			cbRememberPassword.setChecked(true);
		}
		if (appData.getBoolean("AUTO_LOGIN", false)) {
			cbRememberMe.setChecked(true);
			login(btnLogin, etId.getText().toString(), etPwd.getText().toString());
		}
	}
	
	//cb가 체크되어있다면 정보를 저장하는 메서드
	private void saveData() {
		SharedPreferences.Editor editor = appData.edit();
		editor.putBoolean("SAVE_EMAIL", cbRememberId.isChecked());
		editor.putString("email", etId.getText().toString());
		editor.putBoolean("SAVE_PASSWORD", cbRememberPassword.isChecked());
		editor.putString("password", etPwd.getText().toString());
		editor.putBoolean("AUTO_LOGIN", cbRememberMe.isChecked());
		if (cbRememberMe.isChecked()) {
			editor.putString("email", etId.getText().toString());
			editor.putString("password", etPwd.getText().toString());
		}
		editor.apply();
	}
	
	public static void logout() {
		SharedPreferences.Editor editor = appData.edit();
		editor.putBoolean("SAVE_EMAIL", false);
		editor.putString("email", "");
		editor.putBoolean("SAVE_PASSWORD", false);
		editor.putString("password", "");
		editor.putBoolean("AUTO_LOGIN", false);
		editor.apply();
	}
	
	//아이디와 비밀번호를 이용해서 로그인 하는 메서드
	private void login(View view, String email, String password) {
		showProgressBar();
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {
						FirebaseUser user = mAuth.getCurrentUser();
						if (user.isEmailVerified()) {
							saveData();
							startCalculatorActivity = new StartCalculatorActivity();
							Intent intent = new Intent(getApplicationContext(), startCalculatorActivity.getClass());
							intent.putExtra("role", 1);
							startActivity(intent);
							finish();
						}else {
							Snackbar.make(view, "이메일 인증 후 이용하세요.", Snackbar.LENGTH_SHORT).show();
						}
					}else {
						Log.i("TAG", "login: 에서 오류 발생 " + task.getException().getMessage());
						String error = task.getException().getMessage();
						if (error.equals(NO_ACCOUNT_STR)) {
							Snackbar.make(view, "존재하지 않는 아이디 입니다.", Snackbar.LENGTH_SHORT).show();
						}
						if (error.equals(WRONG_PASSWORD_STR)) {
							Snackbar.make(view, "비밀번호가 맞지 않습니다.", Snackbar.LENGTH_SHORT).show();
						}
					}
					
					hideProgressBar();
				});
	}
	
	//키보드 숨기기 이벤트
	private void hideKeyboard() {
		inputMethodManager.hideSoftInputFromWindow(etId.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etPwd.getWindowToken(), 0);
	}
	
	//프로그레스 바를 보여주는 메서드
	private void showProgressBar() {
		if (progressBar != null) { progressBar.setVisibility(View.VISIBLE); }
	}
	//프로그래스 바를 숨기는 메서드
	private void hideProgressBar() {
		if (progressBar != null) { progressBar.setVisibility(View.INVISIBLE); }
	}
	
	@Override
	public void onDenied(int i, String[] strings) {
		new AlertDialog.Builder(this)
				.setTitle("권한 설정")
				.setMessage("권한 설정을 거부하면 결과 공유하기 기능을 사용할 수 없습니다.\n" +
						"설정을 통해 권한을 승인해주세요.")
				.setPositiveButton("확인", (dialogInterface, i1) ->{ })
				.show();
	}
	
	@Override
	public void onGranted(int i, String[] strings) { }
	
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
				.setTitle("앱을 종료합니다.")
				.setMessage("종료 하시겠습니까?")
				.setPositiveButton("확인", (dialogInterface, i) -> {
					moveTaskToBack(true);						// 태스크를 백그라운드로 이동
					finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
					android.os.Process.killProcess(android.os.Process.myPid());	// 앱 프로세스 종료
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
}