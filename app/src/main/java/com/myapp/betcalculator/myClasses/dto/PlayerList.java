package com.myapp.betcalculator.myClasses.dto;

import java.util.ArrayList;

public class PlayerList {
	private ArrayList<Player> players = new ArrayList<Player>();
	public ArrayList<Player> getPlayers() {
		return players;
	}
	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}
	
	public class Player {
		private String name;
		private String play;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPlay() { return play; }
		public void setPlay(String play) { this.play = play; }
	}
}
