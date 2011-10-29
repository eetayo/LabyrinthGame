package com.chocodroid.labyrinthGame;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.chocodroid.labyrinthGame.save.SaveGame;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener,
		AnimationListener {
	/** Called when the activity is first created. */

	private AdView adView;
	private SaveGame sv;

	public static String ESTADO_JUEGO_EN_JUEGO = "enJuego";
	public static String ESTADO_JUEGO_PAUSE = "enPause";
	public static String ESTADO_JUEGO_NIVEL_COMPLETADO = "finNivel";

	// COMO JUGAR
	public static String ESTADO_JUEGO_COMO_JUGAR = "modoTutorial";
	public static String ESTADO_JUEGO_COMO_JUGAR_EN_FUNCIONAMIENTO = "modoTutorialFuncionando";

	private String estadoJuego = "";

	// Elementos Inicio
	public FrameLayout fbL;
	private ImageView fondoMarco;
	private ImageView fondoMarcoCabecera;
	private ImageView imgPulsadores;
	public GeneradorNiveles gN;
	public static int POSICION_INICIAL = 86;
	public static int POSICION_INICIAL_MARCO = 71;

	public static int nivel;
	public static int mundo;
	public static String TEMA_NIVEL;

	public ImageView tapaFinal;
	public ImageView tapaPause;
	public TextView txtNivelCompletado;
	public TextView txtNumeroNivel;
	// BOTONES PARA PAUSE
	public Button btnPulsadores;
	public Button btnMusica;
	public Button btnSonidos;
	public Button btnReiniciar;
	public Button btnReiniciar2;
	public Button btnReiniciar3;
	public Button btnSalir;
	public Button btnContinuar;
	public Button btnVolver;
	public Button btnMenuPrincipal;
	public Button btnSeleccionarNivel;
	public Button btnOpciones;
	public Button btnSiguiente;
	public Button btnPause;

	// Para el modo tutorial
	public TextView txtTutorial;
	public ImageView imgDedo;
	public int pasoTutorial;

	public boolean activitySwitchFlag = false;

	public static Marcador marcadorActual;
	Marcador marcadorRecord;

	public Sonidos sonidos;
	public ConfiguracionDeUsuario configUsuario;

	// Animaciones
	public Animation animationPause;
	public Animation animationReiniciar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Transacion entre dos actividades
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setRequestedOrientation(1);

		setContentView(R.layout.loading_game);
		
        // Inicializaci—n de la actividad, layout, etc
        TareaCarga task = new TareaCarga(this);
        task.execute("http://blog.fr4gus.com/api/test.json");

	}

	static class TareaCarga extends AsyncTask<String, Void, Void> {
		WeakReference<MainActivity> context;

		public TareaCarga(MainActivity activity) {
			context = new WeakReference<MainActivity>(activity);
		}

		@Override
		protected void onPreExecute() {
			// Av’sele al usuario que estamos trabajando
		}

		@Override
		protected Void doInBackground(String... params) {
			// Aqu’ hacemos una tarea laaarga
			MainActivity activity = context.get();
			activity.cargarPreInicial();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MainActivity activity = context.get();
			if (activity != null && !activity.isFinishing()) {
				// Aqu’ actualizamos la UI con el resultado
				activity.cargarPantalla();

			}
			
		}
	}

	public void cargarPantalla(){
		setContentView(this.fbL);
	}
	
	public void cargarPreInicial() {
		sv = new SaveGame(getApplicationContext());

		final Bundle extras = getIntent().getExtras();
		// ASIGNAR CONFIG
		// 0 activado
		// 1 desactivado
		configUsuario = new ConfiguracionDeUsuario();
		if (sv.readInt("pulsadores") == 0)
			configUsuario.setPulsadoresMostrar(true);
		if (sv.readInt("sonido") == 0)
			configUsuario.setSonidoActivado(true);
		if (sv.readInt("musica") == 0)
			configUsuario.setMusicaActivada(true);
		// FIN ASIGNACION DE CONFIG

		sonidos = new Sonidos(this.getApplicationContext(), configUsuario);
		sonidos.ejecutarMusicaJuego();
		// PUBLICIDAD ADMOB
		//adView = (AdView) this.findViewById(R.id.adView);
		//AdRequest r = new AdRequest();
		//adView.loadAd(r);
		// FIN PUBLICDAD ADMOB

		String esPantallaTutorial = getIntent().getExtras().getString("comoJugar");
		if (esPantallaTutorial != null && esPantallaTutorial.equals("Si")) {
			fbL = (FrameLayout) findViewById(R.id.FrameLayout01);
			fbL.setOnTouchListener((OnTouchListener) this);
			this.cargarFondo();
			this.cargaInicialTutorial();
		} else {
			int nivel = Integer.valueOf(getIntent().getExtras().getString("nivel"));

			if (nivel < 21) {
				this.TEMA_NIVEL = Constantes.TEMA_MADERA;
			} else if (nivel > 20 && nivel < 41) {
				this.TEMA_NIVEL = Constantes.TEMA_VERDE;
			} else if (nivel > 40 && nivel < 61) {
				this.TEMA_NIVEL = Constantes.TEMA_PIEDRA;
			} else if (nivel > 60 && nivel < 81) {
				this.TEMA_NIVEL = Constantes.TEMA_AGUA;
			} else if (nivel > 80 && nivel < 101) {
				this.TEMA_NIVEL = Constantes.TEMA_MERCADO;
			}

			System.out.println("---NIVEL:" + nivel);
			this.nivel = nivel;
			if (this.nivel == 0)
				this.nivel = 1;
			fbL= new FrameLayout(getApplicationContext());
			//fbL = (FrameLayout) findViewById(R.id.FrameLayout01);
			fbL.setOnTouchListener((OnTouchListener) this);
			this.cargarFondo();
			this.cargaInicial(this.nivel);
			
			
		}

	}

	public void cargarPorEncima() {
		ImageView imgLoading = new ImageView(this);
		imgLoading.setBackgroundResource(R.drawable.madera_marco_juego);

		fbL.addView(imgLoading);
	}

	public void cargarFondoMarco() {
		if (this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)) {
			fondoMarco.setBackgroundResource(R.drawable.madera_marco_juego);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)) {
			fondoMarco.setBackgroundResource(R.drawable.mercado_marco_juego);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)) {
			fondoMarco.setBackgroundResource(R.drawable.agua_marco_juego);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)) {
			fondoMarco.setBackgroundResource(R.drawable.piedra_marco_juego);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)) {
			fondoMarco.setBackgroundResource(R.drawable.verde_marco_juego);
		}

	}

	public void cargarFondo() {
		if (this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)) {
			fbL.setBackgroundResource(R.drawable.madera_fondo);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)) {
			fbL.setBackgroundResource(R.drawable.mercado_fondo);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)) {
			fbL.setBackgroundResource(R.drawable.agua_fondo);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)) {
			fbL.setBackgroundResource(R.drawable.piedra_fondo);
		} else if (this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)) {
			fbL.setBackgroundResource(R.drawable.verde_fondo);
		}

	}

	private void cargaInicial(int nivel) {
		fbL.removeAllViews();
		this.estadoJuego = this.ESTADO_JUEGO_EN_JUEGO;
		// Cargar cuadricula
		fondoMarco = new ImageView(this);
		this.cargarFondoMarco();
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParams.setMargins(0,
				(int) (POSICION_INICIAL_MARCO * getResources()
						.getDisplayMetrics().density), 0, 0);
		fondoMarco.setLayoutParams(layoutParams);
		fbL.addView(fondoMarco);

		// Generar Nivel
		gN = new GeneradorNiveles(fbL, this, nivel, TEMA_NIVEL);

		// Cargar fondo cabecera
		fondoMarcoCabecera = new ImageView(this);
		fondoMarcoCabecera
				.setBackgroundResource(R.drawable.cabecera_juego_fondo);
		FrameLayout.LayoutParams layoutParamsFondoCabecera = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParamsFondoCabecera.setMargins((int) (2.5 * getResources()
				.getDisplayMetrics().density), (int) (5 * getResources()
				.getDisplayMetrics().density), 0, 0);
		fondoMarcoCabecera.setLayoutParams(layoutParamsFondoCabecera);
		fbL.addView(fondoMarcoCabecera);

		// Cargar Pause
		btnPause = new Button(this);
		btnPause.setBackgroundResource(R.drawable.cabecera_juego_pausa_off);
		FrameLayout.LayoutParams layoutParamsPause = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		layoutParamsPause.setMargins((int) (10 * getResources()
				.getDisplayMetrics().density), (int) (12.5 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnPause.setLayoutParams(layoutParamsPause);
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				handlerPause.sendMessage(msg);
			}
		});
		this.fbL.addView(btnPause);

		// Cargar Reiniciar
		btnReiniciar = new Button(this);
		btnReiniciar.setBackgroundResource(R.drawable.cabecera_juego_reiniciar);
		FrameLayout.LayoutParams layoutParamsReiniciar = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		layoutParamsReiniciar.setMargins((int) (55 * getResources()
				.getDisplayMetrics().density), (int) (12.5 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnReiniciar.setLayoutParams(layoutParamsReiniciar);
		btnReiniciar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				handlerReiniciar.sendMessage(msg);
			}
		});
		this.fbL.addView(btnReiniciar);

		// Crear Textos Movimientos y Record
		marcadorActual = new Marcador(this, 110, 10, getResources().getString(
				R.string.txtMarcador_numMovimientos), this,
				Marcador.TIPO_MARCADOR);
		this.fbL.addView(marcadorActual);

		marcadorRecord = new Marcador(this, 110, 30, getResources().getString(
				R.string.txtMarcador_numMovimientosMinimos)
				+ this.gN.recordMovimientos, this, Marcador.TIPO_RECORD);
		this.fbL.addView(marcadorRecord);

		// Cargar Reiniciar
		btnSalir = new Button(this);
		btnSalir.setBackgroundResource(R.drawable.cabecera_juego_salir);
		FrameLayout.LayoutParams layoutParamsSalir = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
		layoutParamsSalir.setMargins((int) (270 * getResources()
				.getDisplayMetrics().density), (int) (12.5 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnSalir.setLayoutParams(layoutParamsSalir);
		btnSalir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				handlerSalir.sendMessage(msg);
			}
		});
		this.fbL.addView(btnSalir);

		// Cargar Pulsadores
		imgPulsadores = new ImageView(this);
		imgPulsadores.setBackgroundResource(R.drawable.pulsadores_version3);
		FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParams2.setMargins(0,
				(int) ((POSICION_INICIAL - 10) * getResources()
						.getDisplayMetrics().density), 0, 0);
		imgPulsadores.setLayoutParams(layoutParams2);
		fbL.addView(imgPulsadores);
		if (configUsuario.isPulsadoresMostrar()) {
			imgPulsadores.setVisibility(0);
		} else {
			imgPulsadores.setVisibility(4);
		}
		
		//Cargar Texto nivel
		txtNumeroNivel = new TextView(this);
		Typeface tf = Typeface.createFromAsset(this.getAssets(),"font/damnarc.ttf");
		FrameLayout.LayoutParams layoutParamsTxtNivel = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsTxtNivel.setMargins(0, (int) (Constantes.POSICION_INICIAL * getResources()
				.getDisplayMetrics().density), 0, 0);
		txtNumeroNivel.setLayoutParams(layoutParamsTxtNivel);
		txtNumeroNivel.setTextColor(Color.WHITE);
		txtNumeroNivel.setTypeface(tf);
		txtNumeroNivel.setText(getResources().getString(R.string.txtJuego_Nivel)+this.nivel);
		txtNumeroNivel.setTextSize(30);
		Animation animationTexto = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.transparente_en_2seg);
		//animationTexto.setFillEnabled(true);
		animationTexto.setFillAfter(true);
		txtNumeroNivel.startAnimation(animationTexto);
		txtNumeroNivel.setShadowLayer(3, 0, 0, Color.BLACK);
		fbL.addView(txtNumeroNivel);

	}

	public void ejecutarEfectoBotonReiniciar() {
		Animation myFadeInAnimation = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.efecto_boton);
		btnReiniciar.startAnimation(myFadeInAnimation);
	}

	public Handler handlerScores = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			cargaMenuFinal();
		}
	};

	protected void miHilo() {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
					// cargaMenuFinal();
					Message msg = new Message();
					handlerScores.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// System.out.println("-------------Click onTouch-------------"+
		// event.getX() +","+event.getY());
		if (this.estadoJuego.equals(ESTADO_JUEGO_EN_JUEGO)
				|| this.estadoJuego
						.equals(ESTADO_JUEGO_COMO_JUGAR_EN_FUNCIONAMIENTO)) {
			// Ordenar segun cual se va a mover primero
			String movimiento = this.devolverZonaImpacto(event.getX(),
					event.getY());
			boolean comprobarSiEjecutarTutorial = comprobarSiHaPulsadoLaOrdenElUsuarioEnElTutorial(movimiento);
			if ((movimiento != null)
					&& ((this.estadoJuego.equals(ESTADO_JUEGO_EN_JUEGO)) || (this.estadoJuego
							.equals(ESTADO_JUEGO_COMO_JUGAR_EN_FUNCIONAMIENTO) && comprobarSiEjecutarTutorial))) {

				// lanzar sonido
				sonidos.ejecutarSonidoChoque();
				Pelota.tipoOrdenacion = movimiento;
				Collections.sort(gN.listadoPelotas);

				boolean seHaMovidoAlgunaPelota = false;
				Iterator iterPelotas = gN.listadoPelotas.iterator();
				while (iterPelotas.hasNext()) {
					Pelota p = (Pelota) iterPelotas.next();
					// segun la zona de impacto, mover a un lugar u otro
					boolean rtdo = p.moverPelota(movimiento);
					if (rtdo == true) {
						seHaMovidoAlgunaPelota = true;
					}
				}

				// comprobar si es necesario cambiar imagen de pelota por
				// colision
				for (Pelota p : gN.listadoPelotas) {
					for (Pelota p2 : gN.listadoPelotas) {
						if (!p.equals(p2)) {
							if (p.getMatrizY() == p2.getMatrizY()) {
								boolean p2alladoizq = false;
								boolean p2alladodrch = false;
								if ((p.getMatrizX() == p2.getMatrizX() - 1)) {
									p2alladodrch = true;
								}
								if ((p.getMatrizX() == p2.getMatrizX() + 1)) {
									p2alladoizq = true;
								}
								// comprobar si existe pared de separacion entre
								// las pelotas
								Cuadrado c1 = (Cuadrado) this.gN.listaCuadradosCompleta
										.get(((int) p.getMatrizX() + 5) + ","
												+ p.getMatrizY());
								if (p2alladodrch == true && c1 != null
										&& !c1.isActivado()) {
									p.cambiarImagenyPosicionPelotaEnCasoDeColision();
								}
								Cuadrado c2 = (Cuadrado) this.gN.listaCuadradosCompleta
										.get(((int) p.getMatrizX() + 4) + ","
												+ p.getMatrizY());
								if (p2alladoizq == true && c2 != null
										&& !c2.isActivado()) {
									p.cambiarImagenyPosicionPelotaEnCasoDeColision();
								}
							}

							if (p.getMatrizX() == p2.getMatrizX()) {
								boolean p2arriba = false;
								boolean p2abajo = false;
								if ((p.getMatrizY() == p2.getMatrizY() - 2)) {
									p2abajo = true;
								}
								if ((p.getMatrizY() == p2.getMatrizY() + 2)) {
									p2arriba = true;
								}
								// comprobar si existe pared de separacion entre
								// las pelotas
								Cuadrado c1 = (Cuadrado) this.gN.listaCuadradosCompleta
										.get(p.getMatrizX() + ","
												+ ((int) p.getMatrizY() - 1));
								Cuadrado c2 = (Cuadrado) this.gN.listaCuadradosCompleta
										.get(p.getMatrizX() + ","
												+ ((int) p.getMatrizY() + 1));
								if (p2arriba == true && c1 != null
										&& !c1.isActivado()) {
									p.cambiarImagenyPosicionPelotaEnCasoDeColision();
								}
								if (p2abajo == true && c2 != null
										&& !c2.isActivado()) {
									p.cambiarImagenyPosicionPelotaEnCasoDeColision();
								}
							}
						}

					}
				}

				if (this.estadoJuego.equals(ESTADO_JUEGO_EN_JUEGO)) {
					// Solo sumar en caso de que haya existido movimiento de las
					// pelotas
					if (seHaMovidoAlgunaPelota) {
						marcadorActual.sumarMovimiento();
					}

					// COMPROBAR SI HAS GANADO
					if (comprobarSiHasCompletadoElNivel()) {
						// this.cargaMenuFinal();
						this.estadoJuego = this.ESTADO_JUEGO_NIVEL_COMPLETADO;
						miHilo();
					}
				}
			}
		}

		return false;
	}

	public boolean comprobarSiHasCompletadoElNivel() {
		for (Pelota p : gN.listadoPelotas) {
			for (Cuadrado c : gN.listadoElementos) {
				if (c.getTipo().equals(c.CUADRADO)
						&& p.getMatrizX() == c.getMatrizX()
						&& p.getMatrizY() == c.getMatrizY()) {

					return true;
				}
			}
		}

		return false;
	}

	public void cargaMenuFinal() {

		// Colocar cortina Inicial
		tapaFinal = new ImageView(this);
		tapaFinal.setBackgroundResource(R.drawable.tapa_fin);
		// FrameLayout.LayoutParams layoutParams = new
		// FrameLayout.LayoutParams(465,465);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParams.setMargins(0, (int) (87 * getResources()
				.getDisplayMetrics().density), 0, 0);
		tapaFinal.setLayoutParams(layoutParams);
		this.fbL.addView(tapaFinal);

		// PONGO EL MARCADOR EN ROJO
		this.marcadorActual.setTextColor(Color.RED);

		// NIVEL COMPLETADO
		TextView txtNivelCompletado = new TextView(this);
		txtNivelCompletado.setText("ÁSuperado!");
		txtNivelCompletado.setTextSize(37);
		txtNivelCompletado.setTextColor(Color.BLACK);
		Typeface tf = Typeface.createFromAsset(this.getAssets(),"font/fuente1.ttf");
		txtNivelCompletado.setTypeface(tf);
		FrameLayout.LayoutParams layoutParamsNC = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsNC.setMargins(0, (int) (95 * getResources()
				.getDisplayMetrics().density), 0, 0);
		txtNivelCompletado.setLayoutParams(layoutParamsNC);
		this.fbL.addView(txtNivelCompletado);

		// BOTO

		btnReiniciar3 = new Button(this);
		btnReiniciar3.setText("Reiniciar");
		btnReiniciar3.setTextSize(30);
		btnReiniciar3.setGravity(Gravity.CENTER);
		btnReiniciar3.setWidth((int) (250 * this.getResources()
				.getDisplayMetrics().density));
		btnReiniciar3.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.boton_estrellas_amarilla_on));
		btnReiniciar3.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamsReiniciar = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsReiniciar.setMargins(0, (int) (140 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnReiniciar3.setLayoutParams(layoutParamsReiniciar);
		btnReiniciar3.setTypeface(tf);
		btnReiniciar3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.pararMusicaJuego();
				sonidos.ejecutarPulsacionBoton();
				fbL.removeView(tapaFinal);
				// fbL.removeView(txtNivelCompletado);

				cargaInicial(MainActivity.nivel);
			}
		});
		this.fbL.addView(btnReiniciar3);

		Button btnSalir = new Button(this);
		btnSalir.setText("Salir");
		btnSalir.setTextSize(30);
		btnSalir.setGravity(Gravity.CENTER);
		btnSalir.setWidth((int) (250 * this.getResources().getDisplayMetrics().density));
		btnSalir.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.boton_estrellas_amarilla_on));
		btnSalir.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamsSalir = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsSalir.setMargins(0, (int) (200 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnSalir.setLayoutParams(layoutParamsSalir);
		btnSalir.setTypeface(tf);
		btnSalir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.pararMusicaJuego();
				sonidos.ejecutarPulsacionBoton();
				// cambiar de actividad
				finish();
			}
		});
		this.fbL.addView(btnSalir);

		Button btnContinuar = new Button(this);
		btnContinuar.setText("Continuar");
		btnContinuar.setTextSize(40);
		btnContinuar.setGravity(Gravity.CENTER);
		btnContinuar.setWidth((int) (250 * this.getResources()
				.getDisplayMetrics().density));
		btnContinuar.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.boton_estrellas_amarilla_on));
		btnContinuar.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamsContinuar = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsContinuar.setMargins(0, (int) (290 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnContinuar.setLayoutParams(layoutParamsContinuar);
		btnContinuar.setTypeface(tf);
		btnContinuar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.pararMusicaJuego();
				sonidos.ejecutarPulsacionBoton();
				// SaveGame sv = new SaveGame(getApplicationContext());
				// Cargar resultado Actual, en caso de ser mejor que el que ya
				// existia
				if(sv==null) sv = new SaveGame(getApplicationContext());
				sv.writeScore(String.valueOf(MainActivity.nivel),
						MainActivity.marcadorActual.getValor(), "f", "N");
				MainActivity.nivel = nivel + 1;
				if(nivel==21 || nivel==41 || nivel==61 || nivel==81){
					setContentView(R.layout.loading_game);
					//Saltar a mundo2
					//cambiar de actividad
					Intent i = new Intent(MainActivity.this, ActividadLevel.class);
					i.putExtra("nivel_ultimo", String.valueOf(nivel));
					startActivity(i);
					finish();
				}else{
					//si es cualquier otro mundo cargarlo
					cargaInicial(MainActivity.nivel);
				}
				// cambiar de actividad
				// Intent i = new Intent(MainActivity.this,
				// ActividadLevel.class);
				// MainActivity.nivel = MainActivity.nivel+1;
				// System.out.println(MainActivity.nivel);
				// i.putExtra("nivel", String.valueOf(MainActivity.nivel));
				// Cargar nivel en SQLite
				// sv.writeInt("nivelActual", MainActivity.nivel);
				// i.putExtra("com.chocodroid.labyrinthGame.ConfiguracionDeUsuario",
				// configUsuario);
				// startActivity(i);
				// finish();
			}
		});
		this.fbL.addView(btnContinuar);
	}

	public void cargaMenuPause() {
		if (this.estadoJuego.equals(ESTADO_JUEGO_EN_JUEGO)) {
			this.estadoJuego = ESTADO_JUEGO_PAUSE;
			// Colocar cortina Pause
			tapaPause = new ImageView(this);
			tapaPause.setBackgroundResource(R.drawable.fondo_pause);
			this.fbL.addView(tapaPause);

			Typeface tf = Typeface.createFromAsset(this.getAssets(),
					"font/damnarc.ttf");
			// PAUSE [CONTINUAR, REINICIAR, OPCIONES, SELECCIONAR NIVEL, MENU
			// PRINCIPAL]
			// PULSAR EN OPCIONES Y ENTONCES MUESTRA [VOLVER, PULSADORES,
			// MUSICA, SONIDO]
			btnContinuar = new Button(this);
			btnContinuar.setText(R.string.botonOpciones_continuar);
			btnContinuar.setTextSize(30);
			btnContinuar.setGravity(Gravity.CENTER);
			btnContinuar.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			// btnContinuar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
			btnContinuar.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.botones_gr3_off));
			btnContinuar.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsContinuar = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsContinuar.setMargins(0, (int) (40 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnContinuar.setLayoutParams(layoutParamsContinuar);
			btnContinuar.setTypeface(tf);
			btnContinuar.setShadowLayer(3, 0, 0, Color.BLACK);
			btnContinuar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					estadoJuego = ESTADO_JUEGO_EN_JUEGO;
					fbL.removeView(tapaPause);
					// fbL.removeView(txtNivelCompletado);
					fbL.removeView(btnReiniciar2);
					fbL.removeView(btnSalir);
					fbL.removeView(btnContinuar);
					fbL.removeView(btnMusica);
					fbL.removeView(btnPulsadores);
					fbL.removeView(btnSonidos);
					fbL.removeView(btnOpciones);
					fbL.removeView(btnSeleccionarNivel);
					fbL.removeView(btnMenuPrincipal);
					tapaPause = null;
				}
			});
			this.fbL.addView(btnContinuar);

			btnReiniciar2 = new Button(this);
			btnReiniciar2.setText(R.string.botonOpciones_reiniciar);
			btnReiniciar2.setTextSize(30);
			btnReiniciar2.setGravity(Gravity.CENTER);
			btnReiniciar2.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			// btnReiniciar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
			btnReiniciar2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.botones_gr_off));
			btnReiniciar2.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsReiniciar = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsReiniciar.setMargins(0, (int) (100 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnReiniciar2.setLayoutParams(layoutParamsReiniciar);
			btnReiniciar2.setTypeface(tf);
			btnReiniciar2.setShadowLayer(3, 0, 0, Color.BLACK);
			btnReiniciar2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					cargaInicial(MainActivity.nivel);
				}
			});
			this.fbL.addView(btnReiniciar2);

			btnOpciones = new Button(this);
			btnOpciones.setText(R.string.botonOpciones_opciones);
			btnOpciones.setTextSize(30);
			btnOpciones.setGravity(Gravity.CENTER);
			btnOpciones.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			btnOpciones.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.botones_gr_off));
			btnOpciones.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsOpciones = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsOpciones.setMargins(0, (int) (160 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnOpciones.setLayoutParams(layoutParamsOpciones);
			btnOpciones.setTypeface(tf);
			btnOpciones.setShadowLayer(3, 0, 0, Color.BLACK);
			btnOpciones.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					// cargar siguiente pantalla de opciones
					btnContinuar.setVisibility(4);
					btnReiniciar2.setVisibility(4);
					btnOpciones.setVisibility(4);
					btnSeleccionarNivel.setVisibility(4);
					btnMenuPrincipal.setVisibility(4);
					// hacer invisibles
					btnVolver.setVisibility(1);
					btnSonidos.setVisibility(1);
					btnMusica.setVisibility(1);
					btnPulsadores.setVisibility(1);
				}
			});
			this.fbL.addView(btnOpciones);

			btnSeleccionarNivel = new Button(this);
			btnSeleccionarNivel
					.setText(R.string.botonOpciones_seleccionar_nivel);
			btnSeleccionarNivel.setTextSize(30);
			btnSeleccionarNivel.setGravity(Gravity.CENTER);
			btnSeleccionarNivel.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			btnSeleccionarNivel.setBackgroundDrawable(getResources()
					.getDrawable(R.drawable.botones_gr_off));
			btnSeleccionarNivel.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsSeleccionarNivel = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsSeleccionarNivel.setMargins(0,
					(int) (220 * getResources().getDisplayMetrics().density),
					0, 0);
			btnSeleccionarNivel.setLayoutParams(layoutParamsSeleccionarNivel);
			btnSeleccionarNivel.setTypeface(tf);
			btnSeleccionarNivel.setShadowLayer(3, 0, 0, Color.BLACK);
			btnSeleccionarNivel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					Intent i = new Intent(MainActivity.this,
							ActividadLevel.class);
					startActivity(i);
					finish();
				}
			});
			this.fbL.addView(btnSeleccionarNivel);

			btnMenuPrincipal = new Button(this);
			btnMenuPrincipal.setText(R.string.botonOpciones_menu_principal);
			btnMenuPrincipal.setTextSize(30);
			btnMenuPrincipal.setGravity(Gravity.CENTER);
			btnMenuPrincipal.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			btnMenuPrincipal.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.botones_gr_off));
			btnMenuPrincipal.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsMenuPrincipal = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsMenuPrincipal.setMargins(0, (int) (280 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnMenuPrincipal.setLayoutParams(layoutParamsMenuPrincipal);
			btnMenuPrincipal.setTypeface(tf);
			btnMenuPrincipal.setShadowLayer(3, 0, 0, Color.BLACK);
			btnMenuPrincipal.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					Intent i = new Intent(MainActivity.this,
							ActividadMenu.class);
					startActivity(i);
					finish();
				}
			});
			this.fbL.addView(btnMenuPrincipal);

			btnVolver = new Button(this);
			btnVolver.setText(R.string.botonOpciones_Volver);
			btnVolver.setTextSize(30);
			btnVolver.setGravity(Gravity.CENTER);
			btnVolver.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			btnVolver.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.botones_gr3_off));
			btnVolver.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsVolver = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsVolver.setMargins(0, (int) (40 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnVolver.setLayoutParams(layoutParamsVolver);
			btnVolver.setTypeface(tf);
			btnVolver.setShadowLayer(3, 0, 0, Color.BLACK);
			btnVolver.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					// cargar siguiente pantalla de opciones
					btnContinuar.setVisibility(1);
					btnReiniciar2.setVisibility(1);
					btnOpciones.setVisibility(1);
					btnSeleccionarNivel.setVisibility(1);
					btnMenuPrincipal.setVisibility(1);
					// hacer invisibles
					btnVolver.setVisibility(4);
					btnSonidos.setVisibility(4);
					btnMusica.setVisibility(4);
					btnPulsadores.setVisibility(4);
				}
			});
			this.fbL.addView(btnVolver);

			// btnPulsadores
			btnPulsadores = new Button(this);
			if (configUsuario.isPulsadoresMostrar()) {
				btnPulsadores.setText(R.string.botonOpciones_pulsadores_ON);
				btnPulsadores.setBackgroundDrawable(this.getResources()
						.getDrawable(R.drawable.botones_gr_off));
			} else {
				btnPulsadores.setText(R.string.botonOpciones_pulsadores_OFF);
				btnPulsadores.setBackgroundDrawable(this.getResources()
						.getDrawable(R.drawable.botones_gr2_on));
			}
			btnPulsadores.setTextSize(30);
			btnPulsadores.setGravity(Gravity.CENTER);
			btnPulsadores.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			btnPulsadores.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsPulsadores = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsPulsadores.setMargins(0, (int) (100 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnPulsadores.setLayoutParams(layoutParamsPulsadores);
			btnPulsadores.setTypeface(tf);
			btnPulsadores.setShadowLayer(3, 0, 0, Color.BLACK);
			btnPulsadores.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					// pulsadores on/off
					if (!configUsuario.isPulsadoresMostrar()) {
						btnPulsadores
								.setText(R.string.botonOpciones_pulsadores_ON);
						btnPulsadores.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr_off));
						sv.writeInt("pulsadores", 0);
						configUsuario.setPulsadoresMostrar(true);
						imgPulsadores.setVisibility(0);

					} else {
						btnPulsadores
								.setText(R.string.botonOpciones_pulsadores_OFF);
						btnPulsadores.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr2_on));
						sv.writeInt("pulsadores", 1);
						configUsuario.setPulsadoresMostrar(false);
						imgPulsadores.setVisibility(4);
					}
				}
			});
			this.fbL.addView(btnPulsadores);

			// btnMusica
			btnMusica = new Button(this);
			if (configUsuario.isMusicaActivada()) {
				btnMusica.setText(R.string.botonOpciones_musica_ON);
				btnMusica.setBackgroundDrawable(this.getResources()
						.getDrawable(R.drawable.botones_gr_off));
			} else {
				btnMusica.setText(R.string.botonOpciones_musica_OFF);
				btnMusica.setBackgroundDrawable(this.getResources()
						.getDrawable(R.drawable.botones_gr2_on));
			}
			btnMusica.setTextSize(30);
			btnMusica.setGravity(Gravity.CENTER);
			btnMusica.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			// btnMusica.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
			btnMusica.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsMusica = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsMusica.setMargins(0, (int) (160 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnMusica.setLayoutParams(layoutParamsMusica);
			btnMusica.setTypeface(tf);
			btnMusica.setShadowLayer(3, 0, 0, Color.BLACK);
			btnMusica.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					if (!configUsuario.isMusicaActivada()) {
						// sonidos on/off
						btnMusica.setText(R.string.botonOpciones_musica_ON);
						btnMusica.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr_off));
						sv.writeInt("musica", 0);
						configUsuario.setMusicaActivada(true);
						sonidos.ejecutarMusicaJuego();
					} else {
						sv.writeInt("musica", 1);
						btnMusica.setText(R.string.botonOpciones_musica_OFF);
						btnMusica.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr2_on));
						sonidos.pararMusicaJuego();
						configUsuario.setMusicaActivada(false);
					}

				}
			});
			this.fbL.addView(btnMusica);

			// btnMusica
			btnSonidos = new Button(this);
			if (configUsuario.isSonidoActivado()) {
				btnSonidos.setText(R.string.botonOpciones_sonidos_ON);
				btnSonidos.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.botones_gr_off));
			} else {
				btnSonidos.setText(R.string.botonOpciones_sonidos_OFF);
				btnSonidos.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.botones_gr2_on));
			}
			btnSonidos.setTextSize(30);
			btnSonidos.setGravity(Gravity.CENTER);
			btnSonidos.setWidth((int) (250 * this.getResources()
					.getDisplayMetrics().density));
			// btnSonidos.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.estilos_botones_menu));
			btnSonidos.setTextColor(Color.WHITE);
			FrameLayout.LayoutParams layoutParamsSonidos = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL);
			layoutParamsSonidos.setMargins(0, (int) (220 * getResources()
					.getDisplayMetrics().density), 0, 0);
			btnSonidos.setLayoutParams(layoutParamsSonidos);
			btnSonidos.setTypeface(tf);
			btnSonidos.setShadowLayer(3, 0, 0, Color.BLACK);
			btnSonidos.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sonidos.ejecutarPulsacionBoton();
					if (!configUsuario.isSonidoActivado()) {
						// sonidos on/off
						btnSonidos.setText(R.string.botonOpciones_sonidos_ON);
						btnSonidos.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr_off));
						sv.writeInt("sonido", 0);
						configUsuario.setSonidoActivado(true);
					} else {
						sv.writeInt("sonido", 1);
						btnSonidos.setText(R.string.botonOpciones_sonidos_OFF);
						btnSonidos.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.botones_gr2_on));
						configUsuario.setSonidoActivado(false);
					}

				}
			});
			this.fbL.addView(btnSonidos);

			/*
			 * btnSalir = new Button(this);
			 * btnSalir.setText(R.string.botonOpciones_salir);
			 * btnSalir.setTextSize(30); btnSalir.setGravity(Gravity.CENTER);
			 * btnSalir
			 * .setWidth((int)(250*this.getResources().getDisplayMetrics(
			 * ).density));
			 * //btnSalir.setBackgroundDrawable(this.getResources().
			 * getDrawable(R.drawable.estilos_botones_menu));
			 * btnSalir.setBackgroundDrawable
			 * (getResources().getDrawable(R.drawable.botones_gr_on));
			 * btnSalir.setTextColor(Color.WHITE); FrameLayout.LayoutParams
			 * layoutParamsSalir = new
			 * FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
			 * FrameLayout.LayoutParams.WRAP_CONTENT,
			 * Gravity.CENTER_HORIZONTAL); layoutParamsSalir.setMargins(0,
			 * (int)(280*getResources().getDisplayMetrics().density), 0, 0);
			 * btnSalir.setLayoutParams(layoutParamsSalir);
			 * btnSalir.setTypeface(tf); btnSalir.setShadowLayer(3, 0, 0,
			 * Color.BLACK); btnSalir.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) {
			 * sonidos.pararMusicaJuego(); sonidos.ejecutarPulsacionBoton();
			 * //cambiar de actividad finish(); } });
			 * this.fbL.addView(btnSalir);
			 */

			// hacer invisibles
			btnSonidos.setVisibility(4);
			btnMusica.setVisibility(4);
			btnPulsadores.setVisibility(4);
			btnVolver.setVisibility(4);
		}
	}

	public void limpiarAnimaciones() {
		btnPause.clearAnimation();
		btnReiniciar.clearAnimation();
	}

	// EFECTO BOTON REINICIAR
	public Handler handlerReiniciar = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationReiniciar == null) {
				animationReiniciar = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton);
				animationReiniciar
						.setAnimationListener(new AnimationListener() {
							public void onAnimationEnd(Animation animation) {
								System.out.println("cargarInicial()");
								cargaInicial(MainActivity.nivel);
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub

							}
						});
			}
			System.out.println("Arrancar Animacion");
			btnReiniciar.startAnimation(animationReiniciar);

		}
	};

	public Handler handlerPause = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationPause == null) {
				animationPause = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton);
				animationPause.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						System.out.println("cargarMenuPause()");
						cargaMenuPause();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}
				});
			}
			System.out.println("Arrancar Animacion");
			btnPause.startAnimation(animationPause);
		}
	};

	// EFECTO BOTON SALIR
	public Handler handlerSalir = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.efecto_boton);
			hyperspaceJumpAnimation
					.setAnimationListener(new AnimationListener() {
						public void onAnimationEnd(Animation animation) {
							sonidos.pararMusicaJuego();
							sonidos.ejecutarPulsacionBoton();
							// cambiar de actividad
							finish();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub

						}
					});
			// btnSalir.clearAnimation();
			btnSalir.startAnimation(hyperspaceJumpAnimation);
		}
	};

	public String devolverZonaImpacto(float x, float y) {
		float escala = this.getResources().getDisplayMetrics().density;
		int Posicion = (int) (POSICION_INICIAL * getResources()
				.getDisplayMetrics().density);

		// this.esArriba(x, y);
		// this.in_or_out_of_polygon(x, y);
		boolean arriba = this.pntInTriangle(x, y, 0, Posicion, 320 * escala,
				Posicion, 160 * escala, 160 * escala + Posicion);
		boolean abajo = this.pntInTriangle(x, y, 0, 320 * escala + Posicion,
				160 * escala, 160 * escala + Posicion, 320 * escala, 320
						* escala + Posicion);
		boolean izq = this.pntInTriangle(x, y, 0, Posicion, 160 * escala, 160
				* escala + Posicion, 0, 320 * escala + Posicion);
		boolean drcha = this.pntInTriangle(x, y, 320 * escala, Posicion,
				160 * escala, 160 * escala + Posicion, 320 * escala, 320
						* escala + Posicion);
		if (arriba) {
			System.out.println("--Arriba--");
			return Pelota.MOVER_ARRIBA;
		} else if (abajo) {
			System.out.println("--Abajo--");
			return Pelota.MOVER_ABAJO;
		} else if (izq) {
			System.out.println("--Izq--");
			return Pelota.MOVER_IZQUIERDA;
		} else if (drcha) {
			System.out.println("--Drcha--");
			return Pelota.MOVER_DERECHA;
		}

		// ARRIBA=66,0 HASTA 254,66
		// System.out.println("x:"+x+" y:"+y);

		/*
		 * if(x>=66*escala && x<=254*escala){ if(y>=0+Posicion &&
		 * y<=66*escala+Posicion){ return Pelota.MOVER_ARRIBA; } }
		 * 
		 * 
		 * //ABAJO=66,254 HASTA 254,320 if(x>=66*escala && x<=254*escala){
		 * if(y>=254*escala+Posicion && y<=320*escala+Posicion){ return
		 * Pelota.MOVER_ABAJO; } }
		 * 
		 * //IZQUIERDA=0,66 HASTA 66,254 if(x>=0*escala && x<66*escala){
		 * if(y>66*escala+Posicion && y<254*escala+Posicion){ return
		 * Pelota.MOVER_IZQUIERDA; } } //DERECHA if(x>=254*escala &&
		 * x<320*escala){ if(y>66*escala+Posicion && y<254*escala+Posicion){
		 * return Pelota.MOVER_DERECHA; } }
		 */

		return null;
	}

	public boolean pntInTriangle(double px, double py, double x1, double y1,
			double x2, double y2, double x3, double y3) {

		double o1 = getOrientationResult(x1, y1, x2, y2, px, py);
		double o2 = getOrientationResult(x2, y2, x3, y3, px, py);
		double o3 = getOrientationResult(x3, y3, x1, y1, px, py);

		return (o1 == o2) && (o2 == o3);
	}

	private int getOrientationResult(double x1, double y1, double x2,
			double y2, double px, double py) {
		double orientation = ((x2 - x1) * (py - y1)) - ((px - x1) * (y2 - y1));
		if (orientation > 0) {
			return 1;
		} else if (orientation < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public void onDestroy() {
		if(adView!=null)adView.destroy();
		super.onDestroy();
		limpiarMemoria();
	}

	public void limpiarMemoria() {
		unbindDrawables(this.fbL);
		fbL = null;
		fondoMarco = null;
		fondoMarcoCabecera = null;
		imgPulsadores = null;
		tapaFinal = null;
		tapaPause = null;
		txtNivelCompletado = null;
		btnPulsadores = null;
		btnMusica = null;
		btnSonidos = null;
		btnReiniciar = null;
		btnReiniciar2 = null;
		btnSalir = null;
		btnContinuar = null;
		btnSiguiente = null;
		btnPause = null;
		btnOpciones = null;
		btnSeleccionarNivel = null;
		btnMenuPrincipal = null;
		txtTutorial = null;
		imgDedo = null;
		System.gc();
		System.out.println("******* Vistas Eliminadas:MainActivity ********");
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			activitySwitchFlag = true;
			sv.cerrarBD();
			sonidos.ejecutarPulsacionBoton();
			sonidos.pararMusicaJuego();
			finish();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			sv.cerrarBD();
			sonidos.ejecutarPulsacionBoton();
			sonidos.pararMusicaJuego();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v("TAG", "onPause");
		sonidos.pararMusicaJuego();
		if (activitySwitchFlag) {
			Log.v("TAG", "activity switch");
		} else {
			Log.v("TAG", "home button");
			// sv.cerrarBD();
			sonidos.pararMusicaJuego();
		}
		activitySwitchFlag = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		sv = new SaveGame(getApplicationContext());
		if(configUsuario!=null){
			if (configUsuario.isMusicaActivada()) {
				if(sonidos!=null){
					sonidos.ejecutarMusicaJuego();
				}else{
					sonidos = new Sonidos(this.getApplicationContext(), configUsuario);
					sonidos.ejecutarMusicaJuego();
				}
			}
		}else{
			configUsuario = new ConfiguracionDeUsuario();
			if (sv.readInt("pulsadores") == 0)
				configUsuario.setPulsadoresMostrar(true);
			if (sv.readInt("sonido") == 0)
				configUsuario.setSonidoActivado(true);
			if (sv.readInt("musica") == 0)
				configUsuario.setMusicaActivada(true);
			// FIN ASIGNACION DE CONFIG

			sonidos = new Sonidos(this.getApplicationContext(), configUsuario);
			sonidos.ejecutarMusicaJuego();
		}
		
	}

	// --------------------------------------------------------------------------------------
	// MODO TUTORIAL
	// --------------------------------------------------------------------------------------

	private void cargaInicialTutorial() {
		fbL.removeAllViews();
		this.estadoJuego = this.ESTADO_JUEGO_COMO_JUGAR;

		Typeface tf = Typeface.createFromAsset(this.getAssets(),
				"font/fuente1.ttf");

		// Cargar cuadricula
		fondoMarco = new ImageView(this);
		fondoMarco.setBackgroundResource(R.drawable.madera_marco_juego);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParams.setMargins(0,
				(int) (POSICION_INICIAL_MARCO * getResources()
						.getDisplayMetrics().density), 0, 0);
		fondoMarco.setLayoutParams(layoutParams);
		fbL.addView(fondoMarco);
		pasoTutorial = 1;

		gN = new GeneradorNiveles(fbL, this, true);

		// TextField
		TextView txtNombreTutorial = new TextView(this);
		txtNombreTutorial.setText("Tutorial");
		txtNombreTutorial.setTextColor(Color.WHITE);
		txtNombreTutorial.setTextSize(25);
		txtNombreTutorial.setTypeface(tf);
		FrameLayout.LayoutParams layoutParamsNombreTutorial = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParamsNombreTutorial.setMargins((int) (10 * getResources()
				.getDisplayMetrics().density), (int) (10 * getResources()
				.getDisplayMetrics().density), 0, 0);
		txtNombreTutorial.setLayoutParams(layoutParamsNombreTutorial);
		this.fbL.addView(txtNombreTutorial);

		TextView txtPasoAPaso = new TextView(this);
		txtPasoAPaso.setText("Paso a Paso");
		txtPasoAPaso.setTextColor(Color.WHITE);
		txtPasoAPaso.setTextSize(25);
		txtPasoAPaso.setTypeface(tf);
		FrameLayout.LayoutParams layoutParamsPasoAPaso = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParamsPasoAPaso.setMargins((int) (10 * getResources()
				.getDisplayMetrics().density), (int) (40 * getResources()
				.getDisplayMetrics().density), 0, 0);
		txtPasoAPaso.setLayoutParams(layoutParamsPasoAPaso);
		this.fbL.addView(txtPasoAPaso);

		// Cargar Pause -> deberia ser el boton Salir
		Button btnPause = new Button(this);
		btnPause.setText("");
		btnPause.setBackgroundResource(R.drawable.cabecera_juego_pausa_off);// -->
																			// cambiar
																			// imagen
		FrameLayout.LayoutParams layoutParamsPause = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsPause.setMargins((int) (110 * getResources()
				.getDisplayMetrics().density), (int) (40 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnPause.setLayoutParams(layoutParamsPause);
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		this.fbL.addView(btnPause);

		// Cargar Pulsadores
		imgPulsadores = new ImageView(this);
		imgPulsadores.setBackgroundResource(R.drawable.pulsadores_version3);
		FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParams2.setMargins(0, (int) (POSICION_INICIAL * getResources()
				.getDisplayMetrics().density), 0, 0);
		imgPulsadores.setLayoutParams(layoutParams2);
		fbL.addView(imgPulsadores);
		imgPulsadores.setVisibility(0);

		// flecha y texto por debajo
		// ImageView imgFlecha = new ImageView(this);
		// imgFlecha.setBackgroundResource(R.drawable.flecha_ayuda);
		// FrameLayout.LayoutParams layoutParamsFlecha = new
		// FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
		// AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		// layoutParamsFlecha.setMargins(0,
		// (int)(POSICION_INICIAL*getResources().getDisplayMetrics().density),
		// 0, 0);
		// imgFlecha.setLayoutParams(layoutParamsFlecha);
		// fbL.addView(imgFlecha);

		txtTutorial = new TextView(this);
		txtTutorial.setText("Hay que llevar a una de estas pelotas al agujero");
		txtTutorial.setTextSize(15);
		txtTutorial.setGravity(Gravity.CENTER);
		txtTutorial.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamstxtTutorial1 = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParamstxtTutorial1.setMargins(0,
				(int) ((POSICION_INICIAL + 100) * getResources()
						.getDisplayMetrics().density), 0, 0);
		txtTutorial.setLayoutParams(layoutParamstxtTutorial1);
		fbL.addView(txtTutorial);

		// Un boton siguiente
		btnSiguiente = new Button(this);
		btnSiguiente.setText(R.string.botonOpciones_siguiente);
		btnSiguiente.setTextSize(30);
		btnSiguiente.setGravity(Gravity.CENTER);
		btnSiguiente.setWidth((int) (250 * this.getResources()
				.getDisplayMetrics().density));
		btnSiguiente.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.boton_estrellas_amarilla_on));
		btnSiguiente.setTextColor(Color.WHITE);
		FrameLayout.LayoutParams layoutParamsSiguiente = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL);
		layoutParamsSiguiente.setMargins(0, (int) (400 * getResources()
				.getDisplayMetrics().density), 0, 0);
		btnSiguiente.setLayoutParams(layoutParamsSiguiente);
		btnSiguiente.setTypeface(tf);
		btnSiguiente.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				cargarPaso();
			}
		});
		this.fbL.addView(btnSiguiente);

	}

	public void cargarPaso() {
		pasoTutorial++;
		if (pasoTutorial == 2) {
			cargarPaso2Tutorial();
		} else if (pasoTutorial == 3) {
			cargarPaso3Tutorial();
		} else if (pasoTutorial == 4) {
			cargarPaso4Tutorial();
		} else if (pasoTutorial == 5) {
			cargarPaso5Tutorial();
		} else if (pasoTutorial == 6) {
			cargarPaso6Tutorial();
		} else if (pasoTutorial == 7) {
			cargarPaso7Tutorial();
		}
	}

	public boolean comprobarSiHaPulsadoLaOrdenElUsuarioEnElTutorial(
			String movimiento) {
		if (pasoTutorial == 6 && movimiento.equals(Pelota.MOVER_ABAJO)) {
			cargarPaso();
			return true;
		} else if (pasoTutorial == 7
				&& movimiento.equals(Pelota.MOVER_IZQUIERDA)) {
			cargarPaso();
			return true;
		}
		return false;

	}

	public void cargarPaso2Tutorial() {
		txtTutorial.setText("Las pelotas se mueven de manera conjunta");
	}

	public void cargarPaso3Tutorial() {
		txtTutorial
				.setText("Las pelotas se pueden mover hacia la izquierda, derecha, arriba o abajo");
		imgDedo = new ImageView(this);
		imgDedo.setBackgroundResource(R.drawable.dedo);
		FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				AbsoluteLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
		layoutParams2.setMargins((int) (104 * getResources()
				.getDisplayMetrics().density),
				(int) ((POSICION_INICIAL + 30) * getResources()
						.getDisplayMetrics().density), 0, 0);
		imgDedo.setLayoutParams(layoutParams2);
		fbL.addView(imgDedo);
		// Animation hyperspaceJumpAnimation =
		// AnimationUtils.loadAnimation(this, R.anim.animation_dedo);
		// imgDedo.startAnimation(hyperspaceJumpAnimation);
		AnimationSet set = new AnimationSet(false);

		TranslateAnimation animation = new TranslateAnimation(0,
				(int) (125 * getResources().getDisplayMetrics().density), 0,
				(int) (125 * getResources().getDisplayMetrics().density));
		animation.setDuration(500);
		animation.setStartOffset(250);
		animation.setFillAfter(true);

		TranslateAnimation animation2 = new TranslateAnimation(0,
				(int) (-125 * getResources().getDisplayMetrics().density), 0,
				(int) (125 * getResources().getDisplayMetrics().density));
		animation2.setDuration(500);
		animation2.setStartOffset(1000);
		animation2.setFillAfter(true);

		TranslateAnimation animation3 = new TranslateAnimation(0,
				(int) (-125 * getResources().getDisplayMetrics().density), 0,
				(int) (-125 * getResources().getDisplayMetrics().density));
		animation3.setDuration(500);
		animation3.setStartOffset(1750);
		animation3.setFillAfter(true);

		TranslateAnimation animation4 = new TranslateAnimation(0,
				(int) (125 * getResources().getDisplayMetrics().density), 0,
				(int) (-125 * getResources().getDisplayMetrics().density));
		animation4.setDuration(500);
		animation4.setStartOffset(2500);
		animation4.setFillAfter(true);

		set.addAnimation(animation);
		set.addAnimation(animation2);
		set.addAnimation(animation3);
		set.addAnimation(animation4);
		imgDedo.startAnimation(set);

	}

	public void cargarPaso4Tutorial() {
		fbL.removeView(imgDedo);
		txtTutorial
				.setText("Cada pelota avanza hasta colisionar con una pared o colisionar con una pelota");
	}

	public void cargarPaso5Tutorial() {
		txtTutorial
				.setText("Ahora te ayudare a resolver el nivel que esta en pantalla");
		this.estadoJuego = ESTADO_JUEGO_COMO_JUGAR_EN_FUNCIONAMIENTO;
	}

	public void cargarPaso6Tutorial() {
		btnSiguiente.setVisibility(4);
		txtTutorial.setText("Pulsa Abajo");
	}

	public void cargarPaso7Tutorial() {
		txtTutorial.setText("Pulsa Izquierda");
	}

	public void cargarPaso8Tutorial() {
		txtTutorial.setText("Pulsa Abajo");
	}

	public void cargarPaso9Tutorial() {
		txtTutorial.setText("Pulsa Abajo");
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub

	}

}