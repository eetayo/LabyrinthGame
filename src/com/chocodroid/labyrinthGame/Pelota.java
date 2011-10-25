package com.chocodroid.labyrinthGame;



import java.util.Comparator;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class Pelota extends ImageView implements Comparable  {
	
	public static String tipoOrdenacion;
	
	public static String MOVER_IZQUIERDA="izquierda";
	public static String MOVER_DERECHA="derecha";
	public static String MOVER_ARRIBA="arriba";
	public static String MOVER_ABAJO="abajo";
	
	final float scale = getContext().getResources().getDisplayMetrics().density;
	
	private int matrizX;
	private int matrizY;
	private int posicionX;
	private int posicionY;
	private String nombre;
	
	private Activity mActivity;	
	private static String TEMA_NIVEL = "";
	
	
	public Pelota(Context context,int x, int y,int matrizX,int matrizY,String nombre,Activity mActivity,String tema){
		super(context);
		this.setMatrizX(matrizX);
        this.setMatrizY(matrizY);
        this.setPosicionX(x);
        this.setPosicionY(y);
        this.setNombre(nombre);
        
        //TEMPORAL
        this.TEMA_NIVEL= tema;
        if(TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
        	this.setBackgroundResource(R.drawable.madera_pelota);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
        	this.setBackgroundResource(R.drawable.verde_pelota);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
        	this.setBackgroundResource(R.drawable.mercado_pelota);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
        	this.setBackgroundResource(R.drawable.ice_pelota);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
        	this.setBackgroundResource(R.drawable.piedra_pelota);
        }
        
        this.setmActivity(mActivity);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        layoutParams.setMargins(this.getPosicionX(), (int)(this.getPosicionY()+Constantes.POSICION_INICIAL*scale), 0, 0);
        this.setLayoutParams(layoutParams);  		
        
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

	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setmActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	public Activity getmActivity() {
		return mActivity;
	}
	
	public boolean moverPelota(String m){
		System.out.println("Mover pelota:" +m);
		int mX = this.getMatrizX();
		int mY = this.getMatrizY();
		System.out.println("Actual:" +this.getMatrizY() + ","+this.getMatrizX());
		boolean haColisionado=false;
		if(m.equals(MOVER_ARRIBA)){
			if(this.getMatrizY()>1){
				Iterator iterE = ((MainActivity)mActivity).gN.listadoElementos.iterator();
				Cuadrado cuadradoColision=null;
				while(iterE.hasNext()){
					Cuadrado c = (Cuadrado)iterE.next();
					//IGUAL EN X y es de tipo Pared (para tener en cuenta paredes horizontales)
					if(c.getMatrizX()==this.getMatrizX() && c.getTipo().equals(c.TIPO_PARED)){
						if(c.getMatrizY()<this.getMatrizY()){
							//moverPelota a la fila del cuadrado
							//comprobar si es el mas proximo, pq puede existir N colisiones
							haColisionado=true;		
							if(cuadradoColision==null){
								cuadradoColision = c;
							}else{
								if((this.getMatrizY()-c.getMatrizY())<(this.getMatrizY()-cuadradoColision.getMatrizY())){
									cuadradoColision = c;
								}
							}
						}
					}				
				}

				if(haColisionado==false){
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionY();
					this.setMatrizY(1);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
					//mover pelota a fila0
					//this.ejecutarAnimacion(0,this.getPosicionY()-posicionOld);
					
				}else{
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionY();
					this.setMatrizY(cuadradoColision.getMatrizY()+1);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
					//this.ejecutarAnimacion(0,cuadradoColision.getPosicionY()-posicionOld);
				}
			}
			if(hayPelotaEnLaMismaPosicion()){
				this.setMatrizY(this.getMatrizY()+2);
				this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
				if(hayPelotaEnLaMismaPosicion()){
					this.setMatrizY(this.getMatrizY()+2);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
				}
			}
			this.moverSinAnimacion();
		}else if(m.equals(MOVER_ABAJO)){
			if(this.getMatrizY()<9){
				Iterator iterE = ((MainActivity)mActivity).gN.listadoElementos.iterator();
				Cuadrado cuadradoColision=null;
				while(iterE.hasNext()){
					Cuadrado c = (Cuadrado)iterE.next();
					if(c.getMatrizX()==this.getMatrizX() && c.getTipo().equals(c.TIPO_PARED)){
						if(c.getMatrizY()>this.getMatrizY()){
							//moverPelota a la fila del cuadrado
							//comprobar si es el mas proximo, pq puede existir N colisiones
							haColisionado=true;		
							if(cuadradoColision==null){
								cuadradoColision = c;
							}else{
								if((c.getMatrizY()-this.getMatrizY())<(cuadradoColision.getMatrizY()-this.getMatrizY())){
									cuadradoColision = c;
								}
							}
						}
					}				
				}

				if(haColisionado==false){
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionY();
					this.setMatrizY(9);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
					//mover pelota a fila0
					//this.ejecutarAnimacion(0,this.getPosicionY()-posicionOld);
				}else{
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionY();
					this.setMatrizY(cuadradoColision.getMatrizY()-1);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
					//this.ejecutarAnimacion(0,cuadradoColision.getPosicionY()-posicionOld);
				}
			}
			if(hayPelotaEnLaMismaPosicion()){
				this.setMatrizY(this.getMatrizY()-2);
				this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
				if(hayPelotaEnLaMismaPosicion()){
					this.setMatrizY(this.getMatrizY()-2);
					this.setPosicionY(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionY());
				}
			}
			this.moverSinAnimacion();
		}else if(m.equals(MOVER_IZQUIERDA)){
			if(this.getMatrizX()>1){
				Iterator iterE = ((MainActivity)mActivity).gN.listadoElementos.iterator();
				Cuadrado cuadradoColision=null;
				while(iterE.hasNext()){
					Cuadrado c = (Cuadrado)iterE.next();
					//IGUAL EN X y es de tipo Pared (para tener en cuenta paredes horizontales)
					if(c.getMatrizY()==this.getMatrizY() && c.getTipo().equals(c.TIPO_PARED)){
						if(c.getMatrizX()<(this.getMatrizX()+5)){
							//moverPelota a la fila del cuadrado
							//comprobar si es el mas proximo, pq puede existir N colisiones
							haColisionado=true;		
							if(cuadradoColision==null){
								cuadradoColision = c;
							}else{
								if(((this.getMatrizX()+5)-c.getMatrizX())<((this.getMatrizX()+5)-cuadradoColision.getMatrizX())){
									cuadradoColision = c;
								}
							}
						}
					}				
				}

				if(haColisionado==false){
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionX();
					this.setMatrizX(1);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
					//mover pelota a fila0
					//this.ejecutarAnimacion(0,this.getPosicionX()-posicionOld);
				}else{
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionX();
					this.setMatrizX(cuadradoColision.getMatrizX()-4);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
					//this.ejecutarAnimacion(0,cuadradoColision.getPosicionX()-posicionOld);		
				}
			}
			if(hayPelotaEnLaMismaPosicion()){
				this.setMatrizX(this.getMatrizX()+1);
				this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
				if(hayPelotaEnLaMismaPosicion()){
					this.setMatrizX(this.getMatrizX()+1);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
				}
			}
			this.moverSinAnimacion();
		}else if(m.equals(MOVER_DERECHA)){
			if(this.getMatrizX()<5){
				Iterator iterE = ((MainActivity)mActivity).gN.listadoElementos.iterator();
				Cuadrado cuadradoColision=null;
				while(iterE.hasNext()){
					Cuadrado c = (Cuadrado)iterE.next();
					if(c.getMatrizY()==this.getMatrizY() && c.getTipo().equals(c.TIPO_PARED)){
						if(c.getMatrizX()>=(this.getMatrizX()+5)){
							//moverPelota a la fila del cuadrado
							//comprobar si es el mas proximo, pq puede existir N colisiones
							haColisionado=true;		
							if(cuadradoColision==null){
								cuadradoColision = c;
							}else{
								if((c.getMatrizX()-(this.getMatrizX()+5))<(cuadradoColision.getMatrizX()-(this.getMatrizX()+5))){
									cuadradoColision = c;
								}
							}
						}
					}				
				}

				if(haColisionado==false){
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionX();
					this.setMatrizX(5);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
					//mover pelota a fila0
					//this.ejecutarAnimacion(0,this.getPosicionX()-posicionOld);
				}else{
					//actualizar posY y matrizY
					int posicionOld = this.getPosicionX();
					this.setMatrizX(cuadradoColision.getMatrizX()-5);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
					//this.ejecutarAnimacion(0,cuadradoColision.getPosicionX()-posicionOld);
				}
			}
			if(hayPelotaEnLaMismaPosicion()){
				this.setMatrizX(this.getMatrizX()-1);
				this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
				if(hayPelotaEnLaMismaPosicion()){
					this.setMatrizX(this.getMatrizX()-1);
					this.setPosicionX(((Cuadrado)(((MainActivity)mActivity).gN.listaCuadradosCompleta.get(this.getMatrizX()+","+this.getMatrizY()))).getPosicionX());
				}
			}
			this.moverSinAnimacion();
		}
		
		if(mX==this.getMatrizX() && mY==this.getMatrizY()) return false;
		else return true;
		
		
	}
	
	
	public boolean hayPelotaEnLaMismaPosicion(){
		for(Pelota p:((MainActivity)mActivity).gN.listadoPelotas){
			if(!p.equals(this)){
				if((this.getMatrizX()==p.getMatrizX())&&(this.getMatrizY()==p.getMatrizY())){
					return true;
				}
			}
		}
		return false;
	}
	
	public void moverSinAnimacion(){
		if(TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
        	this.setBackgroundResource(R.drawable.madera_pelota);
        }else if(TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
        	this.setBackgroundResource(R.drawable.verde_pelota);
        }
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        layoutParams.setMargins(this.getPosicionX(), (int)(this.getPosicionY()+Constantes.POSICION_INICIAL*scale), 0, 0);
        this.setLayoutParams(layoutParams); 
		
	}
	
	public void ejecutarAnimacion(int moverEnX,int moverEnY){	
		
        
		//System.out.println("MoverX:"+moverEnX + "  MoverY:"+moverEnY);
		/*AnimationSet set = new AnimationSet(false);		
		TranslateAnimation animation = new TranslateAnimation 
        ( 
        		0,moverEnX, 
        		0,moverEnY
        ); 
        animation.setDuration(200); 
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener(){
        	@Override
        	public void onAnimationEnd(Animation anim){
        		System.out.println("--------onAnimationEnd---------");
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

        set.addAnimation(animation);
        set.setFillAfter(true);
        //Animation animationA = new AlphaAnimation(1.0f, 0.5f);
        //animationA.setDuration(300);
        //set.addAnimation(animationA);
        //set.setFillBefore(true);
        set.reset();
        this.clearAnimation();
        this.startAnimation(set); 
        //this.clearAnimation();
         * 
         */
	}


	/*@Override
	public int compare(Object object1, Object object2) {
		// TODO Auto-generated method stub
		Pelota p1 = (Pelota) object1;
	    Pelota p2 = (Pelota) object2;
	    return p1.getPosicionY() - p2.getPosicionY();
	    /*if(tipoOrdenacion.equals(MOVER_ARRIBA)){
	    	return p1.getPosicionY() - p2.getPosicionY();
	    }else if(tipoOrdenacion.equals(MOVER_ABAJO)){
	    	return p2.getPosicionY() - p1.getPosicionY();
	    }else if(tipoOrdenacion.equals(MOVER_IZQUIERDA)){
	    	return p1.getPosicionX() - p2.getPosicionX();
	    }else if(tipoOrdenacion.equals(MOVER_DERECHA)){
	    	return p2.getPosicionX() - p1.getPosicionX();
	    }
	    return 0;
	}*/


	


	@Override
	public int compareTo(Object obj) {
		// TODO Auto-generated method stub
	    Pelota p2 = (Pelota) obj;
	    if(tipoOrdenacion.equals(MOVER_ARRIBA)){
	    	return this.getPosicionY() - p2.getPosicionY();
	    }else if(tipoOrdenacion.equals(MOVER_ABAJO)){
	    	return p2.getPosicionY() - this.getPosicionY();
	    }else if(tipoOrdenacion.equals(MOVER_IZQUIERDA)){
	    	return this.getPosicionX() - p2.getPosicionX();
	    }else if(tipoOrdenacion.equals(MOVER_DERECHA)){
	    	return p2.getPosicionX() - this.getPosicionX();
	    }
	    return 0;
	}
	
	public void cambiarImagenyPosicionPelotaEnCasoDeColision(){
		 if(TEMA_NIVEL.equals(Constantes.TEMA_MADERA)){
	        	this.setBackgroundResource(R.drawable.madera_pelota_colision);
	        }else if(TEMA_NIVEL.equals(Constantes.TEMA_VERDE)){
	        	this.setBackgroundResource(R.drawable.verde_pelota_colision);
	        }else if(TEMA_NIVEL.equals(Constantes.TEMA_MERCADO)){
	        	this.setBackgroundResource(R.drawable.mercado_pelota_colision);
	        }else if(TEMA_NIVEL.equals(Constantes.TEMA_AGUA)){
	        	this.setBackgroundResource(R.drawable.ice_pelota_colision);
	        }else if(TEMA_NIVEL.equals(Constantes.TEMA_PIEDRA)){
	        	this.setBackgroundResource(R.drawable.piedra_pelota_colision);
	        }
		int posX = this.getPosicionX()-5;
		int posY = this.getPosicionY()-5;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        layoutParams.setMargins(posX, (int)(posY+Constantes.POSICION_INICIAL*scale), 0, 0);
        this.setLayoutParams(layoutParams);  		
	}
	
}



