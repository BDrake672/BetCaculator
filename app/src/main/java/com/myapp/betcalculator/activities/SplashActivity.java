package com.myapp.betcalculator.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.myapp.betcalculator.R;

//인트로 화면을 관리할 액티비티
public class SplashActivity extends AppCompatActivity {
	private FirstPageActivity firstPageActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		firstPageActivity = new FirstPageActivity();
		Intent intent = new Intent(this, firstPageActivity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		
		finish();
	}
}