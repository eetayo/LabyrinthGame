package com.chocodroid.labyrinthGame;



import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Cuadrado extends ImageView{
	
	public static String CUADRADO = "cuadrado";
	public static String BORDE_VERTICAL1 = "bordeVertical1";
	public static String BORDE_VERTICAL234 = "bordeVertical";
	public static String BORDE_VERTICAL5 = "bordeVertical5";
	public static String BORDE_HORIZONTAL1 = "bordeHorizontal1";
	public static String BORDE_HORIZONTAL234 = "bordeHorizontal";
	public static String BORDE_HORIZONTAL5 = "bordeHorizontal5";
	
	
	public static String TIPO_PARED="pared";
	public static String TIPO_CUADRADO_FINAL="cuadrado";
	
	
	
	public final float scale = getContext().getResources().getDisplayMetrics().density;

	private int matrizX;
	private int matrizY;
	private int posicionX;
	private int posicionY;
	//se utilizan para centrar el cuadro final
	private int posicionXCuadradoFinal;
	private int posicionYCuadradoFinal;
	private String tipo;
	private String tipoPared;
	private boolean activado;
	private Activity mActivity;
	private static String TEMA_NIVEL = "";
	
	
	public Cuadrado(Context context,int x, int y,int matrizX,int matrizY,String tipo,Activity mActivity,String tema){		
		super(context);
		this.setMatrizX(matrizX);
        this.setMatrizY(matrizY);
        this.setPosicionX((int)(x*scale));
        this.setPosicionY((int)(y*scale));
        
        //TEMPORAL
        this.TEMA_NIVEL= tema;
        
        this.setActivado(false);
        this.setmActivity(mActivity);
        if(TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
        	this.cargarImagenSegunTipoMadera(tipo);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
        	this.cargarImagenSegunTipoVerde(tipo);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
        	this.cargarImagenSegunTipoPiedra(tipo);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
        	this.cargarImagenSegunTipoHielo(tipo);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
        	this.cargarImagenSegunTipoMercado(tipo);
        }
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        layoutParams.setMargins(this.getPosicionX(), (int)(this.getPosicionY()+Constantes.POSICION_INICIAL*scale), 0, 0);
        this.setLayoutParams(layoutParams);  		
        System.out.println("Escala:" + scale);
	}
	//Tema basico
	/*public void cargarImagenSegunTipo(String tipo){
		if(tipo.equals(CUADRADO)){
			this.setBackgroundResource(R.drawable.cuadrado);
	        this.setTipo(TIPO_CUADRADO_FINAL);
		}else if(tipo.equals(BORDE_VERTICAL1)){
			this.setBackgroundResource(R.drawable.vertical_fila1);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_VERTICAL234)){
			this.setBackgroundResource(R.drawable.vertical_fila2_3_4);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_VERTICAL5)){
			this.setBackgroundResource(R.drawable.vertical_fila5);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL1)){
			this.setBackgroundResource(R.drawable.horizontal_col1);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL234)){
			this.setBackgroundResource(R.drawable.horizontal_col2_3_4);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL5)){
			this.setBackgroundResource(R.drawable.horizontal_col5);
			this.setTipo(TIPO_PARED);
		}		
	}*/
	//Tema Madera
	public void cargarImagenSegunTipoMadera(String tipo){
		if(tipo.equals(CUADRADO)){
			this.setBackgroundResource(R.drawable.casilla_final);
	        this.setTipo(TIPO_CUADRADO_FINAL);
		}else if(tipo.equals(BORDE_VERTICAL1)){
			this.setBackgroundResource(R.drawable.madera_vertical_fila_1);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_VERTICAL234)){
			this.setBackgroundResource(R.drawable.madera_vertical_fila_2_3_4);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_VERTICAL5)){
			this.setBackgroundResource(R.drawable.madera_vertical_fila_5);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL1)){
			this.setBackgroundResource(R.drawable.madera_horizontal_columna1);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL234)){
			this.setBackgroundResource(R.drawable.madera_horizontal_columna2_3_4);
			this.setTipo(TIPO_PARED);
		}else if(tipo.equals(BORDE_HORIZONTAL5)){
			this.setBackgroundResource(R.drawable.madera_horizontal_columna5);
			this.setTipo(TIPO_PARED);
		}		
		this.setTipoPared(tipo);
	}
	
	
	//Tema Verde
		public void cargarImagenSegunTipoVerde(String tipo){
			if(tipo.equals(CUADRADO)){
				this.setBackgroundResource(R.drawable.casilla_final);
		        this.setTipo(TIPO_CUADRADO_FINAL);
			}else if(tipo.equals(BORDE_VERTICAL1)){
				this.setBackgroundResource(R.drawable.verde_vertical_fila_1);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_VERTICAL234)){
				this.setBackgroundResource(R.drawable.verde_vertical_fila_2_3_4);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_VERTICAL5)){
				this.setBackgroundResource(R.drawable.verde_vertical_fila_5);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL1)){
				this.setBackgroundResource(R.drawable.verde_horizontal_columna1);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL234)){
				this.setBackgroundResource(R.drawable.verde_horizontal_columna2_3_4);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL5)){
				this.setBackgroundResource(R.drawable.verde_horizontal_columna5);
				this.setTipo(TIPO_PARED);
			}		
			this.setTipoPared(tipo);
		}
		
		//Tema Verde
	   public void cargarImagenSegunTipoPiedra(String tipo){
					if(tipo.equals(CUADRADO)){
						this.setBackgroundResource(R.drawable.casilla_final);
				        this.setTipo(TIPO_CUADRADO_FINAL);
					}else if(tipo.equals(BORDE_VERTICAL1)){
						this.setBackgroundResource(R.drawable.piedra_vertical_fila_1);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_VERTICAL234)){
						this.setBackgroundResource(R.drawable.piedra_vertical_fila_2_3_4);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_VERTICAL5)){
						this.setBackgroundResource(R.drawable.piedra_vertical_fila_5);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL1)){
						this.setBackgroundResource(R.drawable.piedra_horizontal_columna1);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL234)){
						this.setBackgroundResource(R.drawable.piedra_horizontal_columna2_3_4);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL5)){
						this.setBackgroundResource(R.drawable.piedra_horizontal_columna5);
						this.setTipo(TIPO_PARED);
					}		
					this.setTipoPared(tipo);
		}
	   
	 //Tema Agua/Hielo
	   public void cargarImagenSegunTipoHielo(String tipo){
					if(tipo.equals(CUADRADO)){
						this.setBackgroundResource(R.drawable.casilla_final);
				        this.setTipo(TIPO_CUADRADO_FINAL);
					}else if(tipo.equals(BORDE_VERTICAL1)){
						this.setBackgroundResource(R.drawable.ice_vertical_fila_1);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_VERTICAL234)){
						this.setBackgroundResource(R.drawable.ice_vertical_fila_2_3_4);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_VERTICAL5)){
						this.setBackgroundResource(R.drawable.ice_vertical_fila_5);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL1)){
						this.setBackgroundResource(R.drawable.ice_horizontal_columna1);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL234)){
						this.setBackgroundResource(R.drawable.ice_horizontal_columna2_3_4);
						this.setTipo(TIPO_PARED);
					}else if(tipo.equals(BORDE_HORIZONTAL5)){
						this.setBackgroundResource(R.drawable.ice_horizontal_columna5);
						this.setTipo(TIPO_PARED);
					}		
					this.setTipoPared(tipo);
		}
	   
	   public void cargarImagenSegunTipoMercado(String tipo){
			if(tipo.equals(CUADRADO)){
				this.setBackgroundResource(R.drawable.casilla_final);
		        this.setTipo(TIPO_CUADRADO_FINAL);
			}else if(tipo.equals(BORDE_VERTICAL1)){
				this.setBackgroundResource(R.drawable.mercado_vertical_fila_1);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_VERTICAL234)){
				this.setBackgroundResource(R.drawable.mercado_vertical_fila_2_3_4);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_VERTICAL5)){
				this.setBackgroundResource(R.drawable.mercado_vertical_fila_5);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL1)){
				this.setBackgroundResource(R.drawable.mercado_horizontal_columna1);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL234)){
				this.setBackgroundResource(R.drawable.mercado_horizontal_columna2_3_4);
				this.setTipo(TIPO_PARED);
			}else if(tipo.equals(BORDE_HORIZONTAL5)){
				this.setBackgroundResource(R.drawable.mercado_horizontal_columna5);
				this.setTipo(TIPO_PARED);
			}		
			this.setTipoPared(tipo);
}
	
	public void cambiarImagenSiEsNecesarioVerticales(HashMap listadoCuadradosCompleta){
		if(tipoPared.equals(BORDE_VERTICAL234)||tipoPared.equals(BORDE_VERTICAL5)){
			if(this.getMatrizY()>1){				
				if(((Cuadrado)listadoCuadradosCompleta.get(""+this.getMatrizX() +","+(this.getMatrizY()-2)+"")).isActivado()){
					if(this.getMatrizY()==9){
						if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
							this.setBackgroundResource(R.drawable.madera_vertical_fila_5_continuacion);	
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
							this.setBackgroundResource(R.drawable.verde_vertical_fila_5_continuacion);	
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
							this.setBackgroundResource(R.drawable.piedra_vertical_fila_5_continuacion);	
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
							this.setBackgroundResource(R.drawable.ice_vertical_fila_5_continuacion);	
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
							this.setBackgroundResource(R.drawable.mercado_vertical_fila_5_continuacion);	
						}
										
					}else{
						if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
							this.setBackgroundResource(R.drawable.madera_vertical_fila_2_3_4_continuacion);	
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
							this.setBackgroundResource(R.drawable.verde_vertical_fila_2_3_4_continuacion);
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
							this.setBackgroundResource(R.drawable.piedra_vertical_fila_2_3_4_continuacion);
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
							this.setBackgroundResource(R.drawable.ice_vertical_fila_2_3_4_continuacion);
						}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
							this.setBackgroundResource(R.drawable.mercado_vertical_fila_2_3_4_continuacion);	
						}
					}
				}
			}
		}
	}
	
	public void cambiarImagenSiEsNecesarioHorizontales(HashMap listadoCuadradosCompleta){
		if(tipoPared.equals(BORDE_HORIZONTAL1)){
			if(!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()-1)+"")).isActivado()&&
			   !((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()+1)+"")).isActivado()&&
			   ((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+1) +","+this.getMatrizY()+"")).isActivado()){
				if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
					this.setBackgroundResource(R.drawable.madera_horizontal_columna1_continuacion);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
					this.setBackgroundResource(R.drawable.verde_horizontal_columna1_continuacion);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
					this.setBackgroundResource(R.drawable.piedra_horizontal_columna1_continuacion);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
					this.setBackgroundResource(R.drawable.ice_horizontal_columna1_continuacion);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
					this.setBackgroundResource(R.drawable.mercado_horizontal_columna1_continuacion);	
				}
			}		
		}else if(tipoPared.equals(BORDE_HORIZONTAL234)){
			if(!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()-1)+"")).isActivado()&&
			   !((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()+1)+"")).isActivado()&&
				!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()-1)+"")).isActivado()&&
				!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()+1)+"")).isActivado()&&
				((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()-1) +","+this.getMatrizY()+"")).isActivado()&&
				((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+1) +","+this.getMatrizY()+"")).isActivado()){
				//continuacion por ambos lados
				if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
					this.setBackgroundResource(R.drawable.madera_horizontal_columna2_3_4_continuacion_izq_y_drcha);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
					this.setBackgroundResource(R.drawable.verde_horizontal_columna2_3_4_continuacion_izq_y_drcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
					this.setBackgroundResource(R.drawable.piedra_horizontal_columna2_3_4_continuacion_izq_y_drcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
					this.setBackgroundResource(R.drawable.ice_horizontal_columna2_3_4_continuacion_izq_y_drcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
					this.setBackgroundResource(R.drawable.mercado_horizontal_columna2_3_4_continuacion_izq_y_drcha);
				}
			}else if(!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()-1)+"")).isActivado()&&
					   !((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()+1)+"")).isActivado()&&
						((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()-1) +","+this.getMatrizY()+"")).isActivado()){
				//continuacion por la izquierda
				if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
					this.setBackgroundResource(R.drawable.madera_horizontal_columna2_3_4_continuacion_izq);	
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
					this.setBackgroundResource(R.drawable.verde_horizontal_columna2_3_4_continuacion_izq);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
					this.setBackgroundResource(R.drawable.piedra_horizontal_columna2_3_4_continuacion_izq);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
					this.setBackgroundResource(R.drawable.ice_horizontal_columna2_3_4_continuacion_izq);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
					this.setBackgroundResource(R.drawable.mercado_horizontal_columna2_3_4_continuacion_izq);
				}
				
			}else if(!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()-1)+"")).isActivado()&&
			   !((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+5) +","+(this.getMatrizY()+1)+"")).isActivado()&&
				((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+1) +","+this.getMatrizY()+"")).isActivado()){
					//continuacion por la derecha
				if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
					this.setBackgroundResource(R.drawable.madera_horizontal_columna2_3_4_continuacion_dcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
					this.setBackgroundResource(R.drawable.verde_horizontal_columna2_3_4_continuacion_dcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
					this.setBackgroundResource(R.drawable.piedra_horizontal_columna2_3_4_continuacion_dcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
					this.setBackgroundResource(R.drawable.ice_horizontal_columna2_3_4_continuacion_dcha);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
					this.setBackgroundResource(R.drawable.mercado_horizontal_columna2_3_4_continuacion_dcha);
				}
						
						
			}
		}else if(tipoPared.equals(BORDE_HORIZONTAL5)){
			if(!((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()-1)+"")).isActivado()&&
			   !((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()+4) +","+(this.getMatrizY()+1)+"")).isActivado()&&
				((Cuadrado)listadoCuadradosCompleta.get(""+(this.getMatrizX()-1) +","+this.getMatrizY()+"")).isActivado()){
							//continuacion por la derecha
				if(this.TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
					this.setBackgroundResource(R.drawable.madera_horizontal_columna5_continuacion);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
					this.setBackgroundResource(R.drawable.verde_horizontal_columna5_continuacion);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
					this.setBackgroundResource(R.drawable.piedra_horizontal_columna5_continuacion);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
					this.setBackgroundResource(R.drawable.ice_horizontal_columna5_continuacion);
				}else if(this.TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
					this.setBackgroundResource(R.drawable.mercado_horizontal_columna5_continuacion);
				}
								
			}
		}
		
	}


	public void setMatrizX(int matrizX) {
		this.matrizX = matrizX;
	}

	public int getMatrizX() {
		return matrizX;
	}

	public void setMatrizY(int matrizY) {
		this.matrizY = matrizY;
	}

	public int getMatrizY() {
		return matrizY;
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

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setActivado(boolean activado) {
		this.activado = activado;
	}

	public boolean isActivado() {
		return activado;
	}

	public void setmActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	public Activity getmActivity() {
		return mActivity;
	}
	public void setTipoPared(String tipoPared) {
		this.tipoPared = tipoPared;
	}
	public String getTipoPared() {
		return tipoPared;
	}
	public int getPosicionXCuadradoFinal() {
		return posicionXCuadradoFinal;
	}
	public void setPosicionXCuadradoFinal(int posicionXCuadradoFinal) {
		this.posicionXCuadradoFinal = posicionXCuadradoFinal;
	}
	public int getPosicionYCuadradoFinal() {
		return posicionYCuadradoFinal;
	}
	public void setPosicionYCuadradoFinal(int posicionYCuadradoFinal) {
		this.posicionYCuadradoFinal = posicionYCuadradoFinal;
	}
	
	
}
