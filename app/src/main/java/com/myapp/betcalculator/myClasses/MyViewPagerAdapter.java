package com.myapp.betcalculator.myClasses;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.myapp.betcalculator.fragments.currentGame.CurrentGame2Fragment;
import com.myapp.betcalculator.myClasses.dto.Game;

import java.util.ArrayList;

//싱글톤으로 쓰자
public class MyViewPagerAdapter extends FragmentStateAdapter {
	public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity); }
	
	private ArrayList<Fragment> gameFragments = new ArrayList<>();
	public void addFragment(Game.GameDetails gameDetails, String tableName) {
		gameFragments.add(new CurrentGame2Fragment(gameDetails, tableName));
	}
	public ArrayList<Fragment> getGameFragments() {
		return gameFragments;
	}
	
	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return gameFragments.get(position);
	}
	
	@Override
	public int getItemCount() {
		return gameFragments.size();
	}
}
