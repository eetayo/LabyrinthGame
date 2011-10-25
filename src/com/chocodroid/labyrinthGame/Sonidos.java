package com.chocodroid.labyrinthGame;




import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

public class Sonidos {

	private Context mContext;
	private static MediaPlayer choque_bola;
	private static MediaPlayer pulsacion_boton;
	private static MediaPlayer musica;
	
	ConfiguracionDeUsuario config;
	
	//private boolean sonidoActivado= true;
	
	public Sonidos(Context _mContext,ConfiguracionDeUsuario config) {
		this.mContext = _mContext;
		this.config = config;
		choque_bola = MediaPlayer.create(mContext, R.raw.choque_bola);
		pulsacion_boton = MediaPlayer.create(mContext, R.raw.pulsacion_boton_corta);
		musica = MediaPlayer.create(mContext, R.raw.musica);
	}
	
	
	 
	 public void ejecutarSonidoChoque(){
		 if(config!=null && config.isSonidoActivado()){
			 if(!choque_bola.isPlaying()){
				 choque_bola.start();
				 //mActivity.v.vibrate(50);
			 }
		 }
	 }
	 
	 public void ejecutarMusicaJuego(){
		 if(config!=null && config.isMusicaActivada()){
			 if(!musica.isPlaying()){
				 musica = MediaPlayer.create(mContext, R.raw.musica);
				 musica.start();
				 musica.setLooping(true);
				 //mActivity.v.vibrate(50);
			 }
		 }
	 }
	 
	 public void ejecutarPulsacionBoton(){
		 if(config!=null && config.isSonidoActivado()){
			 if(!pulsacion_boton.isPlaying()){
				 pulsacion_boton.start();
				 //mActivity.v.vibrate(50);
			 }
		 }
	 }
	 
	 public void pararMusicaJuego(){
		 if(config!=null &&  config.isMusicaActivada()){
			 musica.stop();
		 }
	 }
	 
	 
}
