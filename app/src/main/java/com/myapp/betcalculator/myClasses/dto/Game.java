package com.myapp.betcalculator.myClasses.dto;

import java.util.ArrayList;

public class Game {
	private String tableName;
	private String gameTitle;
	private String gamePlayers;
	private String regDate;
	private String typeOfDistribution;
	private ArrayList<GameDetails> gameDetailsArrayList = new ArrayList<>();
	
	public Game() {}
	public Game(String tableName, String gameTitle, String gamePlayers, String typeOfDistribution, String regDate) {
		this.tableName = tableName;
		this.gameTitle = gameTitle;
		this.gamePlayers = gamePlayers;
		this.typeOfDistribution = typeOfDistribution;
		this.regDate = regDate;
	}
	public String getTypeOfDistribution() {
		return typeOfDistribution;
	}
	public String getTableName() {
		return tableName;
	}
	public String getGamePlayers() {
		return gamePlayers;
	}
	public String getRegDate() {
		return regDate;
	}
	public String getGameTitle() {
		return gameTitle;
	}
	public void addGameDetail(GameDetails gameDetails) {
		gameDetailsArrayList.add(gameDetails);
	}
	public ArrayList<GameDetails> getGameDetailsArrayList() {
		return gameDetailsArrayList;
	}
	public void setGameDetailsArrayList(ArrayList<GameDetails> gameDetailsArrayList) {
		this.gameDetailsArrayList = gameDetailsArrayList;
	}
	
	public static class GameDetails {
		private int _id;
		private String gameTitle;
		private String gamePlayers;
		private String amountForOneGame;
		private String score;
		private String typeOfDistribution;
		private String regDate;
		public GameDetails(int _id, String gameTitle, String gamePlayers, String amountForOneGame, String score, String typeOfDistribution, String regDate) {
			this._id = _id;
			this.gameTitle = gameTitle;
			this.gamePlayers = gamePlayers;
			this.amountForOneGame = amountForOneGame;
			this.score = score;
			this.typeOfDistribution = typeOfDistribution;
			this.regDate = regDate;
		}
		
		public String getTypeOfDistribution() {
			return typeOfDistribution;
		}
		
		public int get_id() {
			return _id;
		}
		
		public void set_id(int _id) {
			this._id = _id;
		}
		
		public String getGameTitle() {
			return gameTitle;
		}
		
		public void setGameTitle(String gameTitle) {
			this.gameTitle = gameTitle;
		}
		
		public String getGamePlayers() {
			return gamePlayers;
		}
		
		public void setGamePlayers(String gamePlayers) {
			this.gamePlayers = gamePlayers;
		}
		
		public String getAmountForOneGame() {
			return amountForOneGame;
		}
		
		public void setAmountForOneGame(String amountForOneGame) {
			this.amountForOneGame = amountForOneGame;
		}
		
		public String getScore() {
			return score;
		}
		
		public void setScore(String score) {
			this.score = score;
		}
		
		public String getRegDate() {
			return regDate;
		}
		
		public void setRegDate(String regDate) {
			this.regDate = regDate;
		}
		
		@Override
		public String toString() {
			return "GameDetails{" +
					"_id=" + _id +
					", gameTitle='" + gameTitle + '\'' +
					", gamePlayers='" + gamePlayers + '\'' +
					", amountForOneGame='" + amountForOneGame + '\'' +
					", score='" + score + '\'' +
					", regDate='" + regDate + '\'' +
					'}';
		}
	}
}
