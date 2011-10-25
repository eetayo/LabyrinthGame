package com.chocodroid.labyrinthGame;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Marcador extends TextView{

	final float scale = getContext().getResources().getDisplayMetrics().density;
	
	public static String TIPO_MARCADOR="MarcadorActual";
	public static String TIPO_RECORD="MarcadorRecord";

	private String nombre;
	private int valor;
	
	private int posicionX;
	private int posicionY;
	
	private MainActivity mActivity;
	private String cadenaTexto;
	
	public Marcador(Context context,int x, int y,String texto,MainActivity mActivity, String tipo) {
		super(context);
		// TODO Auto-generated constructor stub
		//convertir posiciones a dP		
		
		this.setPosicionX((int)(x*scale));
        this.setPosicionY((int)(y*scale));
        this.setCadenaTexto(texto);
        if(tipo.equals(TIPO_MARCADOR)){
        	valor=0;
        	this.setText(texto + valor);
        	
        	 SpannableStringBuilder ssb = new SpannableStringBuilder(texto + valor);
        	    //CharacterStyle c = new StyleSpan(Typeface.BOLD);
        	    //CharacterStyle c2 = new StyleSpan(Typeface.BOLD);
        	    
        	    
        	    
        	Typeface tf = Typeface.createFromAsset(context.getAssets(),"font/antipasto_regular.otf");
        	//str.setSpan(tf, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	ssb.setSpan(new StyleSpan(Typeface.BOLD), cadenaTexto.length(), this.getText().length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        	//Typeface tf2 = Typeface.createFromAsset(context.getAssets(),"font/fuente1.ttf");
        	//str.setSpan(tf2, 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	//ssb.setSpan(tf2, 4, 7, 1);
        	this.setText(ssb);
    		this.setTypeface(tf);
        	
    		this.setTextColor(Color.BLACK);
        }else if(tipo.equals(TIPO_RECORD)){
        	this.setText(texto);
        	Typeface tf = Typeface.createFromAsset(context.getAssets(),"font/antipasto_regular.otf");
    		this.setTypeface(tf);
    		this.setTextColor(Color.parseColor("#575757"));
        }
		
        this.setTextSize(22);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
        layoutParams.setMargins(this.getPosicionX(), this.getPosicionY(), 0, 0);
        this.setLayoutParams(layoutParams);  	
	}
	
	
	
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getNombre() {
		return nombre;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public int getValor() {
		return valor;
	}
	
	public void setPosicionX(int posicionX) {
		this.posicionX = posicionX;
	}

	public int getPosicionX() {
		return posicionX;
	}

	public void setPosicionY(int posicionY) {
		this.posicionY = posicionY;
	}

	public int getPosicionY() {
		return posicionY;
	}
	
	public void setmActivity(MainActivity mActivity) {
		this.mActivity = mActivity;
	}

	public MainActivity getmActivity() {
		return mActivity;
	}
	
	public void asignarTexto(String texto){
		this.setText(texto);
	}
	
	public void sumarMovimiento(){
		valor++;
		SpannableStringBuilder ssb = new SpannableStringBuilder(this.getCadenaTexto()+valor);
		ssb.setSpan(new StyleSpan(Typeface.BOLD), cadenaTexto.length(), ssb.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		this.setText(ssb);
		//this.setText(this.getCadenaTexto()+valor);
	}

	public void setCadenaTexto(String cadenaTexto) {
		this.cadenaTexto = cadenaTexto;
	}




	public String getCadenaTexto() {
		return cadenaTexto;
	}
}
