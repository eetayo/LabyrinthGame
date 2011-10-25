package com.chocodroid.labyrinthGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Inflater;

import com.chocodroid.labyrinthGame.save.SaveGame;
import com.chocodroid.labyrinthGame.save.Score;

import de.marcreichelt.android.RealViewSwitcher;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;



public class ActividadLevel extends Activity{
	
	public int numero;
	public int nivelActual;
	public int mundoActual;
	public ArrayList<DatosNivel> listadoNiveles=new ArrayList();
	public ArrayList<Score>  listaResultados;
	private SaveGame sv;
	
	public Sonidos sonidos;
	public ConfiguracionDeUsuario configUsuario;
	
	public HashMap listaFramesLayOut;
	public RealViewSwitcher realViewSwitcher;
	
	private int numeroEstrellasMundo1=0;
	private int numeroEstrellasMundo2=0;
	private int numeroEstrellasMundo3=0;
	private int numeroEstrellasMundo4=0;
	private int numeroEstrellasMundo5=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(1);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		sv = new SaveGame(this);
		listaFramesLayOut = new HashMap();
	  final Bundle extras = getIntent().getExtras();
	 //ASIGNAR CONFIG
	        //0 activado
	        //1 desactivado
	        configUsuario = new ConfiguracionDeUsuario();
	        if(sv.readInt("pulsadores")==0)
	        configUsuario.setPulsadoresMostrar(true);
	        if(sv.readInt("sonido")==0)
	        configUsuario.setSonidoActivado(true);
	        if(sv.readInt("musica")==0)
	        configUsuario.setMusicaActivada(true);
	 //FIN ASIGNACION DE CONFIG  
	        
		sonidos = new Sonidos(this.getApplicationContext(),configUsuario);
	        
		//recuperar Nivel Actual
		 //int nivel = Integer.valueOf(getIntent().getExtras().getString("nivel"));
		 //if(nivel>0)nivel=nivel-1;
	     //this.nivelActual = nivel;
		
	     this.mundoActual=0;
	 //RecuperarDatosNiveles
	    this.cargarDatosNiveles("datos_niveles.txt"); 
	    this.cargarResultados();
	     
		// create the view switcher
		realViewSwitcher = new RealViewSwitcher(getApplicationContext());
		
		// add some views to it
		//final int[] backgroundColors = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW };
		for (int num = 1; num <= Constantes.NUMERO_MUNDOS; num++) {
			FrameLayout fly = new FrameLayout(this); 
			//fly.setOrientation(LinearLayout.VERTICAL);
			//fly.setPadding(0, (int)(75*this.getResources().getDisplayMetrics().density), 0, 0);
			
			
			//CARGAR FONDO
			fly.setBackgroundResource(this.cargarFondoSegunMundo(num));
			
			//BOTON MUNDO
			Typeface tf = Typeface.createFromAsset(this.getAssets(),"font/damnarc.ttf");
			final Button btnMundo = new Button(this);
			//Recuperar texto y ver el tema–o del texto, segun el tama–o coloar N Espacios
			String txtEM = this.getString(R.string.botonElegirMundo);
			btnMundo.setText(txtEM + "   ");
			if(num==1){
				btnMundo.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr4_off));
			}else if(num==2){
				btnMundo.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr5_off));
			}else if(num==3){
				btnMundo.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr2_off));
			}else if(num==4){
				btnMundo.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr_off));
			}else if(num==5){
				btnMundo.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr3_off));
			}
			
			btnMundo.setTextSize(40);
			btnMundo.setGravity(Gravity.CENTER);
			btnMundo.setWidth((int)(250*this.getResources().getDisplayMetrics().density));
			btnMundo.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsMundo = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
			layoutParamsMundo.setMargins(0, (int)(70*getResources().getDisplayMetrics().density), 0, 0);
			btnMundo.setLayoutParams(layoutParamsMundo); 
			btnMundo.setTypeface(tf);
			btnMundo.setShadowLayer(3, 0, 0, Color.BLACK); 
			btnMundo.setId(num);
			btnMundo.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cargarGridNivelesDeMundo(btnMundo.getId());
					mundoActual=btnMundo.getId();
				}
			});
			fly.addView(btnMundo); 
			
			//IMAGEN DEL NUMERO DE MUNDO
			ImageView numero = new ImageView(this);
			if(num==1){
				numero.setBackgroundResource(R.drawable.elegir_mundo_numero_1);
			}else if(num==2){
				numero.setBackgroundResource(R.drawable.elegir_mundo_numero_2);
			}else if(num==3){
				numero.setBackgroundResource(R.drawable.elegir_mundo_numero_3);
			}else if(num==4){
				numero.setBackgroundResource(R.drawable.elegir_mundo_numero_4);
			}else if(num==5){
				numero.setBackgroundResource(R.drawable.elegir_mundo_numero_5);
			}
			
	        FrameLayout.LayoutParams layoutParamsNumero = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
	        layoutParamsNumero.setMargins((int)(70*getResources().getDisplayMetrics().density),(int)(80*getResources().getDisplayMetrics().density), 0, 0);
	        numero.setLayoutParams(layoutParamsNumero);      
	        fly.addView(numero);
			
			//CARGAR VENTANA
			ImageView ventana = new ImageView(this);
			ventana.setBackgroundResource(this.cargarVentanaSegunMundo(num));
	        FrameLayout.LayoutParams layoutParamsFondoCabecera = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
	        layoutParamsFondoCabecera.setMargins(0,(int)(150*getResources().getDisplayMetrics().density), 0, 0);
	        ventana.setLayoutParams(layoutParamsFondoCabecera);      
	        fly.addView(ventana);
			
	        listaFramesLayOut.put(num, fly);
	        
	        //CARGAR VENTANA
	        Button contenedorNumeroEstrellas = new Button(this);
			if(num==1){
				contenedorNumeroEstrellas.setBackgroundResource(R.drawable.boton_estrellas_amarilla_on);
			}else if(num==2){
				contenedorNumeroEstrellas.setBackgroundResource(R.drawable.boton_estrellas_verde_on);
			}else if(num==3){
				contenedorNumeroEstrellas.setBackgroundResource(R.drawable.boton_estrellas_negro_on);
			}else if(num==4){
				contenedorNumeroEstrellas.setBackgroundResource(R.drawable.boton_estrellas_azul_on);
			}else if(num==5){
				contenedorNumeroEstrellas.setBackgroundResource(R.drawable.boton_estrellas_rojo_on);
			}
			Typeface tfBerlin = Typeface.createFromAsset(this.getAssets(),"font/berlin_sans_bold.ttf");
			FrameLayout.LayoutParams layoutParamsContenedorNumeroEstrellas = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
	        layoutParamsContenedorNumeroEstrellas.setMargins(0,(int)(370*getResources().getDisplayMetrics().density), 0, 0);
	        contenedorNumeroEstrellas.setLayoutParams(layoutParamsContenedorNumeroEstrellas);  
	        contenedorNumeroEstrellas.setTypeface(tfBerlin);
	        if(num==1){
		        contenedorNumeroEstrellas.setText(this.numeroEstrellasMundo1 + "/60  ");
			}else if(num==2){
				 contenedorNumeroEstrellas.setText(this.numeroEstrellasMundo2 + "/60  ");
			}else if(num==3){
				 contenedorNumeroEstrellas.setText(this.numeroEstrellasMundo3 + "/60  ");
			}else if(num==4){
				 contenedorNumeroEstrellas.setText(this.numeroEstrellasMundo4 + "/60  ");
			}else if(num==5){
				 contenedorNumeroEstrellas.setText(this.numeroEstrellasMundo5 + "/60  ");
			}
	        contenedorNumeroEstrellas.setTextSize(30);
			contenedorNumeroEstrellas.setTextColor(Color.WHITE);
	        fly.addView(contenedorNumeroEstrellas);
	        
	        
	        
	        /*
			
			TextView txtNivel = new TextView(getApplicationContext());
			txtNivel.setText(Integer.toString(num));
			txtNivel.setTextSize(100);
			txtNivel.setTextColor(Color.BLACK);
			txtNivel.setGravity(Gravity.CENTER);
			txtNivel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			TextView txtRecord = new TextView(getApplicationContext());
			txtRecord.setText("Record:"+this.listadoNiveles.get(num-1).getRecord());
			txtRecord.setTextSize(30);
			txtRecord.setTextColor(Color.BLACK);
			txtRecord.setGravity(Gravity.CENTER);
			//txtRecord.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
//DESACTIVO BLOQUEO DE NIVELES				
			//if(num==1 || listadoNiveles.get(num-2).getResultadosJugador()!=null){
				TextView txtMarcaActual = new TextView(getApplicationContext());
				if(listadoNiveles.get(num-1).getResultadosJugador()==null){
					txtMarcaActual.setText("Tu Marca:");
				}else{
					txtMarcaActual.setText("Tu Marca:"+listadoNiveles.get(num-1).getResultadosJugador().getScore());
				}
				txtMarcaActual.setTextSize(37);
				txtMarcaActual.setTextColor(Color.RED);
				txtMarcaActual.setGravity(Gravity.CENTER);
				
				
				final Button btn = new Button(getApplicationContext());
				btn.setText("Jugar");
				btn.setTextSize(50);
				btn.setGravity(Gravity.CENTER);
				//btn.setMaxWidth(550);
				btn.setBackgroundColor(Color.TRANSPARENT);
				//Asignar fuente 
		        Typeface tf = Typeface.createFromAsset(this.getAssets(),"font/fuente1.ttf");
		        btn.setTypeface(tf);
		        //asignar estilos boton
				btn.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
				btn.setTextColor(Color.WHITE);
				
				final Button btnMenu = new Button(getApplicationContext());
				btnMenu.setText("Menu");
				btnMenu.setTextSize(30);
				btnMenu.setGravity(Gravity.CENTER);
				btnMenu.setBackgroundColor(Color.TRANSPARENT);
				//Asignar fuente 
				btnMenu.setTypeface(tf);
		        //asignar estilos boton
				btnMenu.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
				btnMenu.setTextColor(Color.WHITE);
				
				btnMenu.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						sonidos.ejecutarPulsacionBoton();
						//cambiar de actividad
						Intent i = new Intent(ActividadLevel.this, ActividadMenu.class);
						startActivity(i);
						finish();
					}
				}); 
				
				
				//numero = num;
				LinearLayout layoutAux = new LinearLayout(this);
				layoutAux.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins((int)(25*this.getResources().getDisplayMetrics().density), 0, 
										(int)(25*this.getResources().getDisplayMetrics().density), 20);
				layoutAux.addView(btn, layoutParams);
				layoutAux.addView(btnMenu, layoutParams);
				
				
				btn.setId(num);
				btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							sonidos.ejecutarPulsacionBoton();
							//cambiar de actividad
							Intent i = new Intent(ActividadLevel.this, MainActivity.class);
							i.putExtra("nivel", String.valueOf(btn.getId()));
							//i.putExtra("com.chocodroid.labyrinthGame.ConfiguracionDeUsuario", configUsuario);
							startActivity(i);
							//finish();
						}
			    });
				//fly.setBackgroundResource(R.drawable.fondo_menu);
				fly.addView(txtNivel);
				fly.addView(txtRecord);
				fly.addView(txtMarcaActual);
				fly.addView(layoutAux);
				*/
//DESACTIVO BLOQUEO DE NIVELES	
			/*}else{
				fly.setBackgroundResource(R.drawable.fondo_menu);
				fly.addView(txtNivel);
				fly.addView(txtRecord);
			
				ImageView imgBloqueado = new ImageView(getApplicationContext());
				imgBloqueado.setBackgroundResource(R.drawable.nivel_bloqueado);
				fly.addView(imgBloqueado);
			}*/
			
			
			
			realViewSwitcher.addView(fly);	
		}
		//PROBAR
		//realViewSwitcher.setBackgroundResource(R.drawable.fondo_menu_triple);
		
		realViewSwitcher.setCurrentScreen(mundoActual);
		
		
		//transacion entre dos actividades
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setContentView(realViewSwitcher);
		
		
		
		
		
		// OPTIONAL: listen for screen changes
		realViewSwitcher.setOnScreenSwitchListener(onScreenSwitchListener);
		realViewSwitcher.setSistemaMovimientoActivado(true);
	}
	
	
	public int cargarFondoSegunMundo(int mundo){
		if(mundo==1){
			return R.drawable.mundos01;
		}else if(mundo==2){
			return R.drawable.mundos02;
		}else if(mundo==3){
			return R.drawable.mundos03;
		}else if(mundo==4){
			return R.drawable.mundos04;
		}else if(mundo==5){
			return R.drawable.mundos05;
		}
		return 0;
	}
	
	public int cargarVentanaSegunMundo(int mundo){
		if(mundo==1){
			return R.drawable.mundo1;
		}else if(mundo==2){
			return R.drawable.mundo2;
		}else if(mundo==3){
			return R.drawable.mundo3;
		}else if(mundo==4){
			return R.drawable.mundo4;
		}else if(mundo==5){
			return R.drawable.mundo5;
		}
		return 0;
	}
	
	
	public void cargarGridNivelesDeMundo(int mundo){
		//tapa transparencia
		 
		final ImageView tapaTransparencia = new ImageView(this);
		tapaTransparencia.setBackgroundResource(R.drawable.fondo_pause);        
        final FrameLayout fly = (FrameLayout)listaFramesLayOut.get(mundo); 
		//Grid de iconos
		 final GridView gridview = new GridView(this);
		 FrameLayout.LayoutParams layoutParamsGrid = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
		 layoutParamsGrid.setMargins((int)(15*getResources().getDisplayMetrics().density), (int)(150*getResources().getDisplayMetrics().density), 0, 0);
		 gridview.setLayoutParams(layoutParamsGrid); 
		 this.mundoActual = mundo;
		 gridview.setAdapter(new AdapterGridLevel(this,listaResultados,this.mundoActual));
		 gridview.setNumColumns(4);
		 gridview.setVerticalSpacing((int)(20*getResources().getDisplayMetrics().density));
		 
		

		 /*gridview.setOnItemClickListener(new OnItemClickListener() {
			 public void onItemClick(AdapterView parent,View v, int position, long id){
				 	System.out.println("onItemClick:ActividadLevel");
				 	Intent i = new Intent(ActividadLevel.this, MainActivity.class);
					i.putExtra("nivel", String.valueOf((position +1)));
					i.putExtra("mundo", String.valueOf(mundoActual));
					startActivity(i);
					finish();
			 }
		});*/
        //boton para volver
        final Button btnVolver = new Button(this);
		btnVolver.setText(R.string.botonOpciones_Volver);
		btnVolver.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.botones_gr3_off));
		btnVolver.setTextSize(40);
		btnVolver.setGravity(Gravity.CENTER);
		btnVolver.setWidth((int)(250*this.getResources().getDisplayMetrics().density));
		btnVolver.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamsMundo = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
		layoutParamsMundo.setMargins(0, (int)(70*getResources().getDisplayMetrics().density), 0, 0);
		btnVolver.setLayoutParams(layoutParamsMundo); 
		Typeface tf = Typeface.createFromAsset(this.getAssets(),"font/damnarc.ttf");
		btnVolver.setTypeface(tf);
		btnVolver.setShadowLayer(3, 0, 0, Color.BLACK); 
		btnVolver.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fly.removeView(tapaTransparencia);
				fly.removeView(btnVolver);
				fly.removeView(gridview);
				realViewSwitcher.setSistemaMovimientoActivado(true);
				mundoActual=0;
			}
		});
		
		
        fly.addView(tapaTransparencia);
		fly.addView(btnVolver); 
		fly.addView(gridview);
		
		realViewSwitcher.setSistemaMovimientoActivado(false);
	}
	
	private void cargarResultados() {
		// TODO Auto-generated method stub
		listaResultados = sv.readAllScore();
		for(Score s:listaResultados){
			System.out.println("Nivel:" + s.getLevel() + "  Resultado:" +s.getScore());
			DatosNivel dn = listadoNiveles.get(Integer.valueOf(s.getLevel())-1);
			dn.setResultadosJugador(s);
			s.setMovMin(dn.getRecord());
			
			 if(Integer.valueOf(s.getLevel())<21){
				 this.numeroEstrellasMundo1 = this.numeroEstrellasMundo1 + s.devolverNumeroEstrellas();
			 }else if(Integer.valueOf(s.getLevel())>20 && Integer.valueOf(s.getLevel())<41){
			    	this.numeroEstrellasMundo2 = this.numeroEstrellasMundo2 + s.devolverNumeroEstrellas();
			 }else if(Integer.valueOf(s.getLevel())>40 && Integer.valueOf(s.getLevel())<61){
			    	this.numeroEstrellasMundo3 = this.numeroEstrellasMundo3 + s.devolverNumeroEstrellas();
			 }else if(Integer.valueOf(s.getLevel())>61 && Integer.valueOf(s.getLevel())<81){
			    	this.numeroEstrellasMundo4 = this.numeroEstrellasMundo4 + s.devolverNumeroEstrellas();
		     }else if(Integer.valueOf(s.getLevel())>80 && Integer.valueOf(s.getLevel())<101){
		        	this.numeroEstrellasMundo5 = this.numeroEstrellasMundo5 + s.devolverNumeroEstrellas();
		     }
		}
		
		
	}
	
	

	public void cargarDatosNiveles(String nombreFichero){
		InputStream is = this.getClass().getResourceAsStream(nombreFichero);
		String txt = this.convertStreamToString(is);
		this.leerDatosCompletos(txt);	
	}
	
	
	
	public String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public void leerDatosCompletos(String txt) {

		StringBuffer fileBuffer;
		String fileString = null;
		String line;
		try {
			String nivel = "";
			String record = "";
			String numBolas = "";
			String casillasRojas = "";
			String parametro="";
			while (!(line = txt.substring(0, txt.indexOf("\n"))).trim().equals("")) {
				//eliminar txtNivel
				line = line.substring(line.indexOf(",") + 1);
				//recuperarNivel
				nivel = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				//eliminar txtRecord
				line = line.substring(line.indexOf(",") + 1);
				//recuperarRecord
				record = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				//eliminar TxtBolas
				line = line.substring(line.indexOf(",") + 1);
				//recuperarNumBolas
				numBolas = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				//eliminar TxtCasillasRojas
				line = line.substring(line.indexOf(",") + 1);
				//recuperar CasillasRojas
				casillasRojas = line.trim();
				txt = txt.substring(txt.indexOf("\n") + 1);
				
				//System.out.println(nivel+","+record+","+numBolas+","+casillasRojas);
				DatosNivel dn = new DatosNivel(Integer.valueOf(nivel),Integer.valueOf(record),Integer.valueOf(numBolas),Integer.valueOf(casillasRojas));
				//this.flay.addView(c);
				listadoNiveles.add(dn);
				
				if (txt.startsWith("FIN")){
					break;
				}
			}
			
			

		} catch (Exception e) {
			e.getStackTrace();
		}
	} // readFile
	
	private final RealViewSwitcher.OnScreenSwitchListener onScreenSwitchListener = new RealViewSwitcher.OnScreenSwitchListener() {
		
		@Override
		public void onScreenSwitched(int screen) {
			// this method is executed if a screen has been activated, i.e. the screen is completely visible
			//  and the animation has stopped (might be useful for removing / adding new views)
			Log.d("RealViewSwitcher", "switched to screen: " + screen);
		}
		
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	sonidos.ejecutarPulsacionBoton();
	    	Intent i = new Intent(ActividadLevel.this, ActividadMenu.class);
			startActivity(i);
			finish();
	        return true;
	    }else if(keyCode == KeyEvent.KEYCODE_HOME){
	    	sonidos.ejecutarPulsacionBoton();
	    	sonidos.pararMusicaJuego();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	 @Override
	 protected void onDestroy() {
	    super.onDestroy();
	    this.liberarMemoria();
	    System.out.println("******* Vistas Eliminadas:ActividadLevel ********");
	 }
	 
	 public void liberarMemoria(){
		 Iterator it = listaFramesLayOut.entrySet().iterator();
		    while (it.hasNext()) {
			    Map.Entry e = (Map.Entry)it.next();
			    FrameLayout fly = (FrameLayout)listaFramesLayOut.get(e.getKey()); 
			    if(fly!=null)fly.removeAllViews();
			    fly = null;
		    }
		    if(realViewSwitcher!=null)this.unbindDrawables(realViewSwitcher);
		    
		    realViewSwitcher = null;
		    System.gc();
	 }

	 private void unbindDrawables(View view) {
		    if (view.getBackground() != null) {
		        view.getBackground().setCallback(null);
		    }
		    if (view instanceof ViewGroup) {
		        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
		            unbindDrawables(((ViewGroup) view).getChildAt(i));
		        }
		        ((ViewGroup) view).removeAllViews();
		    }
		}
	 
	 public void cambiarAJuego(int nivel){
		 sv.cerrarBD();
		 this.liberarMemoria();
		 System.out.println("Nivel:"+nivel + "   Mundo:"+mundoActual);
		 Intent i = new Intent(ActividadLevel.this, MainActivity.class);
		 i.putExtra("nivel", String.valueOf(nivel));
		 i.putExtra("mundo", String.valueOf(mundoActual));
		 startActivity(i);
		 finish();
	 }
}




