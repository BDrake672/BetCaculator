package com.myapp.betcalculator.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.myapp.betcalculator.R;

public class ShowRuleFragment extends Fragment {
	
	private Spinner spinGameCategory;
	private String[] spinGameCategoryArr;
	private String selectedCategory;
	private ImageView ivRule;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_show_rule, container, false);
		
		spinGameCategory = rootView.findViewById(R.id.spinGameCategory);
		spinGameCategoryArr = getResources().getStringArray(R.array.gameCategory);
		ivRule = rootView.findViewById(R.id.ivRule);
		
		//스피너에 배열 추가
		ArrayAdapter distributionAdapter = ArrayAdapter.createFromResource(getContext(), R.array.gameCategory, android.R.layout.simple_spinner_dropdown_item);
		spinGameCategory.setAdapter(distributionAdapter);
		
		//스피너에 이벤트 추가
		spinGameCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				((TextView)adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.font_color_for_bold_TextView));
				selectedCategory = spinGameCategory.getSelectedItem().toString();
				if (selectedCategory.equals(spinGameCategoryArr[0])) {
					//--- 일 때.
					ivRule.setImageDrawable(null);
				}else if (selectedCategory.equals(spinGameCategoryArr[1])) {
					//포커 일 때
					ivRule.setImageResource(R.drawable.rule_porker);
				}else if (selectedCategory.equals(spinGameCategoryArr[2])){
					//섯다 일 때
					ivRule.setImageResource(R.drawable.rule_sutdda);
				}else {
					makeSnackBar(view, "준비중 입니다.");
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) { }
		});
		return rootView;
	}
	
	private void makeSnackBar(View view, String s) {
		Snackbar.make(view, s, BaseTransientBottomBar.LENGTH_SHORT).show();
	}
}