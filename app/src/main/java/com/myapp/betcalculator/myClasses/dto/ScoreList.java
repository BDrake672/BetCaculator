package com.myapp.betcalculator.myClasses.dto;

import java.util.ArrayList;

public class ScoreList {
	private Score scores;
	
	public Score getScores() {
		return scores;
	}
	
	public class Score {
		private String winner;
		private String second;
		private String third;
		
		public String getWinner() {
			return winner;
		}
		
		public void setWinner(String winner) {
			this.winner = winner;
		}
		
		public String getSecond() {
			return second;
		}
		
		public void setSecond(String second) {
			this.second = second;
		}
		
		public String getThird() {
			return third;
		}
		
		public void setThird(String third) {
			this.third = third;
		}
	}
}
