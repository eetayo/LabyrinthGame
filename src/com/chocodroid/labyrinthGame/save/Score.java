package com.chocodroid.labyrinthGame.save;

public class Score {

	//"level","score","tiempo","online"
	private String level;
	private int score;
	private String tiempo;
	private int movMin;
	private int numEstrellas;
	//POR DEFECTO 'N'
	//CUANDO SE ACTUALIZA 'S'
	private String online;
	
	public Score(){}
	
	public Score(String level, int score, String tiempo,String online){
		this.level = level;
		this.score = score;
		this.online=online;
		this.setTiempo(tiempo);
	}

	

	public void setScore(int score) {
		this.score = score;
	}
	public int getScore() {
		return score;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public String getOnline() {
		return online;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel() {
		return level;
	}

	public void setTiempo(String tiempo) {
		this.tiempo = tiempo;
	}

	public String getTiempo() {
		return tiempo;
	}

	public int getMovMin() {
		return movMin;
	}

	public void setMovMin(int movMin) {
		this.movMin = movMin;
	}

	

	  public int devolverNumeroEstrellas(){
	    	if(this.getScore()==this.getMovMin()){
	    		return 3;
	    	}else if(this.getScore()<=(this.getMovMin()*1.5)){
	    		return 2;
	    	}else if(this.getScore()<=(this.getMovMin()*2)){
	    		return 1;
	    	}
	    	return 0;
	    }

	
}

