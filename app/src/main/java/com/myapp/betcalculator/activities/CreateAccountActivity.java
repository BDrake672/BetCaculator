package com.myapp.betcalculator.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.myapp.betcalculator.R;

//계정 생성 액티비티
public class CreateAccountActivity extends AppCompatActivity {
	
	private String TAG = "TEST";
	
	//변수 선언
	private ProgressBar progressBar;
	private EditText etEmailId, etEmailHost, etPassword, etPasswordCheck;
	private Spinner spinnerEmailHost;
	private Button btnJoin;
	private String emailRegex =  "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,3}";
	private String emailHostRegex = "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,3}$";
	private String digitRegex = ".*\\d.*";
	private String lowercaseRegex = ".*[a-z].*";
	private String uppercaseRegex = ".*[A-Z].*";
	private String specialCharacterRegex = ".*[*.!@$%^&(){}\\[\\]:;<>,?/~_+\\-=|].*";
	private String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}\\[\\]:;<>,?/~_+\\-=|]).{12,32}$";
	private FirstPageActivity firstPageActivity;
	private String[] emailHostArr;
	private InputMethodManager inputMethodManager;
	//firebase auth 변수 선언
	private FirebaseAuth mAuth;
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);
		
//	광고 관련 설정
		AdView adView = findViewById(R.id.adView2);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.build();
		adView.loadAd(adRequest);
//		//목업을 찍기 위해 잠시 숨겼다.
//		adView.setVisibility(View.GONE);
		
		//객체 참조
		progressBar = findViewById(R.id.progressBar);
		etEmailId = findViewById(R.id.etEmailId);
		etEmailHost = findViewById(R.id.etEmailHost);
		spinnerEmailHost = findViewById(R.id.spinnerEmailHost);
		etPassword = findViewById(R.id.etPassword);
		etPasswordCheck = findViewById(R.id.etPasswordCheck);
		btnJoin = findViewById(R.id.btnJoin);
//		if (requestQueue == null) {requestQueue = Volley.newRequestQueue(this); }
		emailHostArr = getResources().getStringArray(R.array.emailHost);
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		//firebase auth 객체 참조
		mAuth = FirebaseAuth.getInstance();
		
		//email host 입력창 입력 못하게 막아놓기(스피너로 선택해야됨)
		etEmailHost.setEnabled(false);
		
		//스피너 셋팅
		setSpinner();
		//editText 유효성 검사 추가
		setTextWatcher();
		
		//"회원가입" 버튼 이벤트
		btnJoin.setOnClickListener(view -> {
			//입력창이 모두 입력되었는지 확인
			if (!checkValidation(view)) {
				return;
			}
			
			//fireBase 서버로 회원가입 시도
			createUser(view);
		});
	}
	
	//firebase에 계정을 생성하는 메서드
	private void createUser(View view) {
		//통신하는데 시간이 필요하니까 프로그레스 바 보여주기
		showProgressBar();
		String email = etEmailId.getText().toString() + "@" + etEmailHost.getText().toString();
		String password = etPassword.getText().toString();
		mAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
//						입력한 이메일과 비밀번호로 계정 생성에 성공했다면 실행되는 부분
							sendVerifyAndFinishActivity(mAuth.getCurrentUser());
						} else {
//						계정 생성에 실패하면 실행되는 부분
							String alreadyExists = "The email address is already in use by another account.";
//						계정이 이미 존재한다면
							if (task.getException().getMessage().equals(alreadyExists)) {
								makeSnackBar(view, "이미 등록된 이메일 입니다.");
							}else{
								Log.w(TAG, "실패 원인은", task.getException());
							}
						}
//					프로그레스 바 숨기기
						hideProgressBar();
					}
				});
	}
	
	//계정 생성에 성공하면 이메일로 인증 메세지를 보내고, 액티비티를 종료하자
	private void sendVerifyAndFinishActivity(FirebaseUser user) {
		user.sendEmailVerification()
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {
//							인증 메일 전송에 성공했다면
						showConfirmDialog(user.getEmail());
					} else {
//							인증 메일 전송에 실패했다면,
						Log.w(TAG, "인증 메일 전송 실패", task.getException());
					}
				});
	}
	
	//키보드 숨기기 이벤트
	private void hideKeyBoard() {
		etEmailId.clearFocus();
		etEmailHost.clearFocus();
		etPassword.clearFocus();
		etPasswordCheck.clearFocus();
		inputMethodManager.hideSoftInputFromWindow(etEmailId.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etEmailHost.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
		inputMethodManager.hideSoftInputFromWindow(etPasswordCheck.getWindowToken(), 0);
	}
	
	//프로그레스 바를 보여주는 메서드
	private void showProgressBar() {
		if (progressBar != null) { progressBar.setVisibility(View.VISIBLE); }
	}
	//프로그래스 바를 숨기는 메서드
	private void hideProgressBar() {
		if (progressBar != null) { progressBar.setVisibility(View.INVISIBLE); }
	}
	
	//이메일 중복 확인을 위한 이메일창 입력했는지 확인하는 메서드
	private boolean checkEmailValidation(View view) {
		if (TextUtils.isEmpty(etEmailId.getText().toString()) || etEmailId.getError() != null) {
			makeSnackBar(view, "이메일을 입력 후 버튼을 클릭하세요.");
			return false;
		}
		if (TextUtils.isEmpty(etEmailHost.getText().toString()) || etEmailHost.getError() != null) {
			makeSnackBar(view, "이메일을 입력 후 버튼을 클릭하세요.");
			return false;
		}
		return true;
	}
	
	//스피너를 셋팅해주는 메서드
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void setSpinner() {
		spinnerEmailHost.setAdapter(ArrayAdapter.createFromResource(this, R.array.emailHost, android.R.layout.simple_spinner_dropdown_item));
		//스피너 이벤트 추가
		spinnerEmailHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				hideKeyBoard();
				//글자색 변경
				((TextView)adapterView.getChildAt(0)).setTextColor(getColor(R.color.font_white));
				String selectHost = spinnerEmailHost.getSelectedItem().toString();
				switch (spinnerEmailHost.getSelectedItemPosition()) {
					case 1 : {//"직접입력" 일 때
						//host 입력창 입력 가능하도록
						etEmailHost.setEnabled(true);
						etEmailHost.setText("");
						break;
					}
					default: {
						etEmailHost.setEnabled(false);
						etEmailHost.setText(selectHost);
						etEmailHost.setError(null);
						break;
					}
				}
			}
			
			
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) { hideKeyBoard(); }
		});
	}
	
	//edit text 들한테 textWatcher를 추가하는 메서드. 유효성 검사용.
	private void setTextWatcher() {
		//주소 정규식에 맞는지 유효성 검사
		etEmailHost.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (!charSequence.toString().matches(emailHostRegex)) {
					etEmailHost.setError("정확한 주소를 입력하세요.");
				}else {
					etEmailHost.setError(null);
				}
			}
			@Override
			public void afterTextChanged(Editable editable) { }
		});
		//비밀번호 정규식 확인 이벤트 추가
		etPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (!charSequence.toString().matches(digitRegex)) {
					etPassword.setError("한 개 이상의 숫자 필요");
				}else if (!charSequence.toString().matches(lowercaseRegex)) {
					etPassword.setError("한 개 이상의 영문 소문자 필요");
				}else if (!charSequence.toString().matches(uppercaseRegex)) {
					etPassword.setError("한 개 이상의 영문 대문자 필요");
				}else if (!charSequence.toString().matches(specialCharacterRegex)) {
					etPassword.setError("한 개 이상의 특수문자 필요");
				}else if (charSequence.toString().length() < 12) {
					etPassword.setError("12자 이상 필요");
				}else if (charSequence.toString().length() > 32) {
					etPassword.setError("32자 이하 입력 가능");
				}else {
					etPassword.setError(null);
				}
			}
			
			@Override
			public void afterTextChanged(Editable editable) { }
		});
		//비밀번호 확인 체크 이벤트 추가
		etPasswordCheck.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			
			@Override
			public void afterTextChanged(Editable editable) {
				String password = etPassword.getText().toString();
				if (!editable.toString().equals(password)){
					etPasswordCheck.setError("비밀번호가 다릅니다.");
				}else {
					etPasswordCheck.setError(null);
				}
			}
		});
	}
	
	//모든 입력창들이 입력되었는지 확인하는 메서드
	private boolean checkValidation(View view) {
		String email = etEmailId.getText().toString() + "@" + etEmailHost.getText().toString();
		String password = etPassword.getText().toString();
		String passwordCheck = etPasswordCheck.getText().toString();
		//일단 에러가 있는지 확인
		if (etEmailHost.getError() != null || etPassword.getError() != null || etPasswordCheck.getError() != null) {
			makeSnackBar(view,"에러를 확인하세요.");
			return false;
		}
		//정규식 다시한번 확인
		if (!email.matches(emailRegex)) {
			makeSnackBar(view, "이메일을 정확하게 입력하세요.");
			return false;
		}
		if (!password.matches(passwordRegex) || !passwordCheck.matches(passwordRegex)) {
			makeSnackBar(view, "비밀번호를 확인하세요.");
			return false;
		}
		return true;
	}
	
	//스낵바 출력하는 메서드
	private void makeSnackBar(View view, String message) {
		Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_SHORT).show();
	}
	
	//뒤로가기 버튼을 누르면 로그인 화면으로 돌아가도록 하기
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
					Intent intent = new Intent(getApplicationContext(), FirstPageActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					finish();
				})
				.setNegativeButton("취소", (dialogInterface, i) -> {
					//되둘아가기
				}).show();
	}
	
	//이메일 인증을 하라고 알려주고, 액티비티를 종료하고, 로그인 액티비티로 돌아가자
	private void showConfirmDialog(String email) {
		new AlertDialog.Builder(this)
				.setTitle("계정 생성 완료")
				.setMessage("생성된 이메일 주소로 계정 인증 메일이 전송되었습니다.\n메일함을 확인해주세요.")
				.setPositiveButton("확인", (dialogInterface, i) -> {
					firstPageActivity = new FirstPageActivity();
					Intent intent = new Intent(getApplicationContext(), firstPageActivity.getClass());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					finish();
				}).show();
	}
}