package com.chocodroid.labyrinthGame;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.chocodroid.labyrinthGame.MainActivity.TareaCarga;
import com.chocodroid.labyrinthGame.save.SaveGame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActividadMenu extends Activity implements OnTouchListener {
	/** Called when the activity is first created. */

	private Typeface tf;
	
	public ImageView tapaPause;
	public Button btnPulsadores;
	public Button btnMusica;
	public Button btnReiniciar;
	public Button btnContinuar;
	public Button btnCreditos;
	public Button btnResetGame;
	public Button btnVolver;
	public Button btnSonidos;

	// Elementos Inicio
	public LinearLayout fbL;
	private ImageView fondoMarco;
	public GeneradorNiveles gN;
	public static int POSICION_INICIAL = 86;
	public SaveGame sv;
	public int nivelActual;
	public Sonidos sonidos;
	public ConfiguracionDeUsuario configUsuario;
	public Button imgMiniPelota;
	public Button imgMiniPelota2;
	public Button btnInicio;
	public Button btnComoJugar;
	public Button btnOpciones;
	public Button btnSalir;

	// Animaciones
	public Animation animationInicio;
	public Animation animationComoJugar;
	public Animation animationOpciones;
	public Animation animationSalir;
	//2 Animaciones
	public Animation animationVolver;
	public Animation animationSonidos;
	public Animation animationMusica;
	public Animation animationCreditos;
	public Animation animationResetGame;
	//AnimationPelota
	public Animation animationPelota;
	public Animation animationPelota2;

	// Botones
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		setRequestedOrientation(1);

		sv = new SaveGame(this);
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

		this.nivelActual = sv.readInt("nivelActual");
		System.out.println("NivelActual:" + nivelActual);

		this.cargarVistaInicial();
	}

	public void cargarVistaInicial() {
		setContentView(R.layout.menu);

		fbL = (LinearLayout) findViewById(R.id.layout_menu);
		fbL.setBackgroundResource(R.drawable.fondo_menu);
		
		//Drawable d= Imagenes.recuperaImagen("");
		imgMiniPelota = (android.widget.Button) findViewById(R.id.pelota_mini);
		animationPelota = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.efecto_boton2);
		animationPelota.setStartOffset(500);
		animationPelota.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				//ejecutar nuevamente
				imgMiniPelota.startAnimation(animationPelota);
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
		imgMiniPelota.startAnimation(animationPelota);
		imgMiniPelota.setId(1);
		imgMiniPelota.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("Click en imgMiniPelota");
				if(imgMiniPelota.getId()==1){
					imgMiniPelota.setBackgroundResource(R.drawable.mini_madera_pelota);
					imgMiniPelota.setId(2);
				}else if(imgMiniPelota.getId()==2){
					imgMiniPelota.setBackgroundResource(R.drawable.mini_piedra_pelota);
					imgMiniPelota.setId(1);
				}
			}
		});
		

		btnInicio = (android.widget.Button) findViewById(R.id.inicio);
		btnInicio.setTextSize(30);
		btnInicio.setShadowLayer(3, 0, 0, Color.BLACK);
		btnInicio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerInicio.sendMessage(msg);
			}
		});

		btnComoJugar = (android.widget.Button) findViewById(R.id.comoJugar);
		btnComoJugar.setTextSize(30);
		btnComoJugar.setShadowLayer(3, 0, 0, Color.BLACK);
		btnComoJugar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerComoJugar.sendMessage(msg);

			}
		});

		btnOpciones = (android.widget.Button) findViewById(R.id.opciones);
		btnOpciones.setTextSize(30);
		btnOpciones.setShadowLayer(3, 0, 0, Color.BLACK);
		btnOpciones.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerOpciones.sendMessage(msg);
			}
		});

		btnSalir = (android.widget.Button) findViewById(R.id.salir);
		btnSalir.setTextSize(30);
		btnSalir.setShadowLayer(3, 0, 0, Color.BLACK);
		btnSalir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerSalir.sendMessage(msg);
			}
		});

		// Asignar fuente
		btnInicio.setTypeface(this.recuperarFuente());
		btnComoJugar.setTypeface(this.recuperarFuente());
		btnOpciones.setTypeface(this.recuperarFuente());
		btnSalir.setTypeface(this.recuperarFuente());		
	}

	
	public Handler handlerInicio = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationInicio == null) {
				animationInicio = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationInicio.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						// cambiar de actividad
						Intent i = new Intent(ActividadMenu.this,
								ActividadLevel.class);
						i.putExtra("nivel", String.valueOf(nivelActual));
						startActivity(i);
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
			}

			btnInicio.setBackgroundResource(R.drawable.botones_gr3_on);
			btnInicio.startAnimation(animationInicio);
		}
	};

	public Handler handlerComoJugar = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationComoJugar == null) {
				animationComoJugar = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationComoJugar
						.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								// cambiar de actividad
								Intent i = new Intent(ActividadMenu.this,
										MainActivity.class);
								i.putExtra("comoJugar", "Si");
								startActivity(i);
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
			}

			btnComoJugar.setBackgroundResource(R.drawable.botones_gr_on);
			btnComoJugar.startAnimation(animationComoJugar);
		}
	};

	public Handler handlerOpciones = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationOpciones == null) {
				animationOpciones = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationOpciones.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
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

			btnOpciones.setBackgroundResource(R.drawable.botones_gr_on);
			btnOpciones.startAnimation(animationOpciones);
		}
	};

	public Handler handlerSalir = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationSalir == null) {
				animationSalir = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.efecto_boton2);
				animationSalir.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
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
			}
			btnSalir.startAnimation(animationSalir);
			//btnSalir.setBackgroundResource(R.drawable.botones_gr_on);
		}
	};


	public void cargaMenuPause() {
		// Colocar cortina Pause
		setContentView(R.layout.menu_opciones);
		fbL = (LinearLayout) findViewById(R.id.menu_opciones);
		fbL.setBackgroundResource(R.drawable.fondo_menu);

		if (sv == null)
			sv = new SaveGame(this);

		if (sv.readInt("pulsadores") == 0)
			configUsuario.setPulsadoresMostrar(true);
		if (sv.readInt("sonido") == 0)
			configUsuario.setSonidoActivado(true);
		if (sv.readInt("musica") == 0)
			configUsuario.setMusicaActivada(true);

		int numPelota = imgMiniPelota.getId();
		imgMiniPelota2 = (android.widget.Button) findViewById(R.id.pelota_mini2);
		if(numPelota==1){
			imgMiniPelota2.setBackgroundResource(R.drawable.mini_piedra_pelota);
		}else if(numPelota==2){
			imgMiniPelota2.setBackgroundResource(R.drawable.mini_madera_pelota);
		}
		animationPelota2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.efecto_boton2);
		animationPelota2.setStartOffset(100);
		animationPelota2.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				//ejecutar nuevamente
				imgMiniPelota2.startAnimation(animationPelota);
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
		imgMiniPelota2.startAnimation(animationPelota2);
		imgMiniPelota2.setId(numPelota);
		imgMiniPelota2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("Click en imgMiniPelota");
				if(imgMiniPelota2.getId()==1){
					imgMiniPelota2.setBackgroundResource(R.drawable.mini_madera_pelota);
					imgMiniPelota2.setId(2);
				}else if(imgMiniPelota2.getId()==2){
					imgMiniPelota2.setBackgroundResource(R.drawable.mini_piedra_pelota);
					imgMiniPelota2.setId(1);
				}
			}
		});
		
		// boton sonidos
		btnSonidos = (android.widget.Button) findViewById(R.id.btnSonidos);
		btnSonidos.setTextSize(30);
		btnSonidos.setShadowLayer(3, 0, 0, Color.BLACK);
		if (configUsuario.isSonidoActivado()) {
			btnSonidos.setText(R.string.botonOpciones_sonidos_ON);
			btnSonidos.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr_off));
		} else {
			btnSonidos.setText(R.string.botonOpciones_sonidos_OFF);
			btnSonidos.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr2_on));
		}
		btnSonidos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerSonidos.sendMessage(msg);

			}
		});

		// boton musica
		btnMusica = (android.widget.Button) findViewById(R.id.btnMusica);
		btnMusica.setTextSize(30);
		btnMusica.setShadowLayer(3, 0, 0, Color.BLACK);
		if (configUsuario.isMusicaActivada()) {
			btnMusica.setText(R.string.botonOpciones_musica_ON);
			btnMusica.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr_off));
		} else {
			btnMusica.setText(R.string.botonOpciones_musica_OFF);
			btnMusica.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr2_on));
		}

		btnMusica.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerMusica.sendMessage(msg);
			}
		});

		// boton creditos
		btnCreditos = (android.widget.Button) findViewById(R.id.btnCreditos);
		btnCreditos.setTextSize(30);
		btnCreditos.setShadowLayer(3, 0, 0, Color.BLACK);
		btnCreditos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerCreditos.sendMessage(msg);
			}
		});

		// boton reset game
		btnResetGame = (android.widget.Button) findViewById(R.id.btnResetGame);
		btnResetGame.setTextSize(30);
		btnResetGame.setShadowLayer(3, 0, 0, Color.BLACK);
		btnResetGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerResetGame.sendMessage(msg);
			}
		});

		// boton reset game
		btnVolver = (android.widget.Button) findViewById(R.id.btnVolver);
		btnVolver.setTextSize(30);
		btnVolver.setShadowLayer(3, 0, 0, Color.BLACK);
		btnVolver.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sonidos.ejecutarPulsacionBoton();
				Message msg = new Message();
				handlerVolver.sendMessage(msg);
			}
		});

		// Asignar fuente
		btnSonidos.setTypeface(this.recuperarFuente());
		btnMusica.setTypeface(this.recuperarFuente());
		btnCreditos.setTypeface(this.recuperarFuente());
		btnResetGame.setTypeface(this.recuperarFuente());
		btnVolver.setTypeface(this.recuperarFuente());

	}
	
	public Handler handlerSonidos = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationSonidos == null) {
				animationSonidos = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationSonidos.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						if (!configUsuario.isSonidoActivado()) {
							// sonidos on/off
							btnSonidos.setText(R.string.botonOpciones_sonidos_ON);
							btnSonidos.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr_off));
							sv.writeInt("sonido", 0);
							configUsuario.setSonidoActivado(true);
						} else {
							sv.writeInt("sonido", 1);
							btnSonidos.setText(R.string.botonOpciones_sonidos_OFF);
							btnSonidos.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr2_on));
							configUsuario.setSonidoActivado(false);
						}
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
			btnSonidos.startAnimation(animationSonidos);
		}
	};
	
	public Handler handlerMusica = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationMusica == null) {
				animationMusica = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationMusica.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						if (!configUsuario.isMusicaActivada()) {
							// sonidos on/off
							btnMusica.setText(R.string.botonOpciones_musica_ON);
							btnMusica.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr_off));
							sv.writeInt("musica", 0);
							configUsuario.setMusicaActivada(true);
						} else {
							sv.writeInt("musica", 1);
							btnMusica.setText(R.string.botonOpciones_musica_OFF);
							btnMusica.setBackgroundDrawable(getResources().getDrawable(R.drawable.botones_gr2_on));
							sonidos.pararMusicaJuego();
							configUsuario.setMusicaActivada(false);
						}
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
			btnMusica.startAnimation(animationMusica);
		}
	};
	
	public Handler handlerCreditos = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationCreditos == null) {
				animationCreditos = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationCreditos.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						//cargar pantalla de creditos
						btnCreditos.setBackgroundResource(R.drawable.botones_gr_off);
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

			btnCreditos.setBackgroundResource(R.drawable.botones_gr_on);
			btnCreditos.startAnimation(animationCreditos);
		}
	};
	
	public Handler handlerResetGame = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationResetGame == null) {
				animationResetGame = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationResetGame.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						//cargar nueva pantallaOpciones, ÀEstas seguro que deseas eliminar la BD actual con todo ...?
						btnResetGame.setBackgroundResource(R.drawable.botones_gr_off);
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

			btnResetGame.setBackgroundResource(R.drawable.botones_gr_on);
			btnResetGame.startAnimation(animationResetGame);
		}
	};
	
	public Handler handlerVolver = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			limpiarAnimaciones();
			if (animationVolver == null) {
				animationVolver = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.efecto_boton2);
				animationVolver.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						cargarVistaInicial();
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

			btnVolver.setBackgroundResource(R.drawable.botones_gr_on);
			btnVolver.startAnimation(animationVolver);
		}
	};
	
	public Typeface recuperarFuente(){
		if(tf==null){
			tf = Typeface.createFromAsset(this.getAssets(),"font/damnarc.ttf");
		}
		return tf;
	}
	
	public void limpiarAnimaciones() {
		btnInicio.clearAnimation();
		btnComoJugar.clearAnimation();
		btnOpciones.clearAnimation();
		btnSalir.clearAnimation();
		
		if(btnVolver!=null){
			btnVolver.clearAnimation();
		}
		
		if(btnSonidos!=null){
			btnSonidos.clearAnimation();
		}
		
		if(btnMusica!=null){
			btnMusica.clearAnimation();
		}
		
		if(btnCreditos!=null){
			btnCreditos.clearAnimation();
		}
		
		if(btnResetGame!=null){
			btnResetGame.clearAnimation();
		}
	}

	public void liberarMemoriaDeImagenes() {
		unbindDrawables(this.fbL);
		btnInicio = null;
		btnComoJugar = null;
		btnOpciones = null;
		btnSalir = null;
		btnVolver=null;
		btnSonidos=null;
		btnMusica=null;
		btnCreditos=null;
		btnResetGame=null;
		fbL = null;
		imgMiniPelota=null;
		imgMiniPelota2=null;
		animationPelota = null;
		animationPelota2 = null;
		animationVolver= null;
		animationSonidos= null;
		animationMusica= null;
		animationCreditos= null;
		animationResetGame= null;
		sv=null;
		System.gc();
		System.out.println("******* Vistas Eliminadas: ActividadMenu ********");

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

	public void onDestroy() {
		super.onDestroy();
		if (sv != null)
			sv.cerrarBD();
		liberarMemoriaDeImagenes();
		finish();
	}

	public static void closeAllBelowActivities(Activity current) {
		boolean flag = true;
		Activity below = current.getParent();
		if (below == null)
			return;
		System.out.println("Below Parent: " + below.getClass());
		while (flag) {
			Activity temp = below;
			try {
				below = temp.getParent();
				temp.finish();
			} catch (Exception e) {
				flag = false;
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		System.out.println("-------------Click onTouch-------------"
				+ event.getX() + "," + event.getY());

		return false;
	}


}