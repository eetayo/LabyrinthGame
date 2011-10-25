package com.chocodroid.labyrinthGame;

import java.io.Serializable;

public class ConfiguracionDeUsuario implements Serializable{

	public boolean sonidoActivado;
	private boolean musicaActivada;
	public boolean pulsadoresMostrar;
	
	public boolean isSonidoActivado() {
		return sonidoActivado;
	}
	public void setSonidoActivado(boolean sonidoActivado) {
		this.sonidoActivado = sonidoActivado;
	}
	public boolean isPulsadoresMostrar() {
		return pulsadoresMostrar;
	}
	public void setPulsadoresMostrar(boolean pulsadoresMostrar) {
		this.pulsadoresMostrar = pulsadoresMostrar;
	}
	public boolean isMusicaActivada() {
		return musicaActivada;
	}
	public void setMusicaActivada(boolean musicaActivada) {
		this.musicaActivada = musicaActivada;
	}
	
}
