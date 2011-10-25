package com.chocodroid.labyrinthGame;

import com.chocodroid.labyrinthGame.save.Score;

public class DatosNivel {

	private int nivel;
	private int record;
	private int numBolas;
	private int casillasRojas;
	
	private Score resultadosJugador;

	public DatosNivel(int nivel,int record, int numBolas, int casilllasRojas){
		this.nivel=nivel;
		this.record=record;
		this.numBolas=numBolas;
		this.casillasRojas=casilllasRojas;
		
	}
	
	public void setRecord(int record) {
		this.record = record;
	}

	public int getRecord() {
		return record;
	}

	public void setNumBolas(int numBolas) {
		this.numBolas = numBolas;
	}

	public int getNumBolas() {
		return numBolas;
	}

	public void setCasillasRojas(int casillasRojas) {
		this.casillasRojas = casillasRojas;
	}

	public int getCasillasRojas() {
		return casillasRojas;
	}


	public void setNivel(int nivel) {
		this.nivel = nivel;
	}

	public int getNivel() {
		return nivel;
	}

	public void setResultadosJugador(Score resultadosJugador) {
		this.resultadosJugador = resultadosJugador;
	}

	public Score getResultadosJugador() {
		return resultadosJugador;
	}
	
}
