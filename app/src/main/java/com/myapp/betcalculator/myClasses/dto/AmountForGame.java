package com.myapp.betcalculator.myClasses.dto;

public class AmountForGame {
	private Prizes prizes;
	public Prizes getPrizes() {
		return prizes;
	}
	
	public class Prizes {
		//판돈
		private int allPrize;
		//1등 상금
		private int winnerPrize;
		//2등 상금
		private int secondPrize;
		//3등 상금
		private int thirdPrize;
		
		public int getAllPrize() {
			return allPrize;
		}
		
		public void setAllPrize(int allPrize) {
			this.allPrize = allPrize;
		}
		
		public int getWinnerPrize() {
			return winnerPrize;
		}
		
		public void setWinnerPrize(int winnerPrize) {
			this.winnerPrize = winnerPrize;
		}
		
		public int getSecondPrize() {
			return secondPrize;
		}
		
		public void setSecondPrize(int secondPrize) {
			this.secondPrize = secondPrize;
		}
		
		public int getThirdPrize() {
			return thirdPrize;
		}
		
		public void setThirdPrize(int thirdPrize) {
			this.thirdPrize = thirdPrize;
		}
	}
}
