package com.chocodroid.labyrinthGame;

import java.util.ArrayList;
import java.util.HashMap;

import com.chocodroid.labyrinthGame.save.Score;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;


	public class AdapterGridLevel extends BaseAdapter {
	    private ActividadLevel mContext;
	    private Integer[] mThumbIds=new Integer[20];
	    private int mundo;
	    private ArrayList<Score> listaResultados;
	    private ArrayList<String> listaNivelesBloqueados= new ArrayList();
	    private  Typeface tfBerlin;

	    public AdapterGridLevel(ActividadLevel c,ArrayList<Score> listaResultados, int mundo) {
	        this.mundo= mundo;
	        this.listaResultados = listaResultados;
	    	this.cargarImagenes();
	        mContext = c;
	    }

	    public int getCount() {
	        return mThumbIds.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        final Button buttonView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	        	buttonView = new Button(mContext);
	        	buttonView.setLayoutParams(new GridView.LayoutParams(85, 85));
	        	//buttonView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	        	buttonView.setPadding(8, 0, 8, 18);
	        } else {
	        	buttonView = (Button) convertView;
	        }

	        buttonView.setBackgroundResource(mThumbIds[position]);
	        if(!this.estaNivelBloqueado(String.valueOf(position+1)) || mContext.activarTodosLosNiveles){
		        if(mundo==1){
			        buttonView.setText(String.valueOf(position+1));
					buttonView.setId(position+1);
		        }else if(mundo==2){
			        buttonView.setText(String.valueOf(position+1+20));
			        buttonView.setId(position+21);
		        }else if(mundo==3){
		        	buttonView.setText(String.valueOf(position+1+40));
		        	buttonView.setId(position+41);
		        }else if(mundo==4){
		        	buttonView.setText(String.valueOf(position+1+60));
		        	buttonView.setId(position+61);
		        }else if(mundo==5){
		        	buttonView.setText(String.valueOf(position+1+80));
		        	buttonView.setId(position+81);
		        }
		        
		        buttonView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//Intent i = new Intent(ActividadLevel.class, MainActivity.class);
						//i.putExtra("nivel", String.valueOf(v.getId()));
						//i.putExtra("mundo", String.valueOf(mundo));
						//startActivity(i);
						//finish();
						mContext.cambiarAJuego(buttonView.getId());
						System.out.println("ClickBoton:AdapterGridLevel");
					}
				});
	        }
	       buttonView.setTypeface(this.recuperarFuenteBerlin());
			buttonView.setTextSize(30);
			buttonView.setTextColor(Color.WHITE);
			
	        return buttonView;
	    }
	    
	    public Typeface recuperarFuenteBerlin(){
			if(tfBerlin==null){
				tfBerlin = Typeface.createFromAsset(mContext.getAssets(),"font/berlin_sans_bold.ttf");
			}
			return tfBerlin;
		}
	    
	    
	    public boolean estaNivelBloqueado(String nivelBloqueado){
	    	for(String n:listaNivelesBloqueados){
	    		if(n==nivelBloqueado){
	    			return true;
	    		}
	    	}
	    	return false;
	    	
	    }
	    
	    public void cargarImagenes(){
	    	//System.out.println("NumeroResultados:"+listaResultados.size());
	    	int ultimoNivelAlcanzado = 0;
	    	if(listaResultados!=null){
		    	for(Score s:listaResultados){
		    		if(Integer.valueOf(s.getLevel())>ultimoNivelAlcanzado){
	    				ultimoNivelAlcanzado = Integer.valueOf(s.getLevel());
	    			}
		    		
		    		if(mundo==1 && Integer.parseInt(s.getLevel())<21){
		    			//comprobar el numero de movimientos y poner una u otra imagen dependiendo del resultado
		    			if(s.devolverNumeroEstrellas()==0){
		    				mThumbIds[Integer.valueOf(s.getLevel())-1] = R.drawable.boton_nivel0estrellas;
		    			}else if(s.devolverNumeroEstrellas()==1){
		    				mThumbIds[Integer.valueOf(s.getLevel())-1] = R.drawable.boton_nivel1estrellas;
		    			}else if(s.devolverNumeroEstrellas()==2){
		    				mThumbIds[Integer.valueOf(s.getLevel())-1] = R.drawable.boton_nivel2estrellas;
		    			}else if(s.devolverNumeroEstrellas()==3){
		    				mThumbIds[Integer.valueOf(s.getLevel())-1] = R.drawable.boton_nivel3estrellas;
		    			}			    		
		    		}else if(mundo==2 && Integer.parseInt(s.getLevel())>20 && Integer.parseInt(s.getLevel())<41){
		    			if(s.devolverNumeroEstrellas()==0){
		    				mThumbIds[Integer.valueOf(s.getLevel())-21] = R.drawable.boton_nivel0estrellas;
		    			}else if(s.devolverNumeroEstrellas()==1){
		    				mThumbIds[Integer.valueOf(s.getLevel())-21] = R.drawable.boton_nivel1estrellas;
		    			}else if(s.devolverNumeroEstrellas()==2){
		    				mThumbIds[Integer.valueOf(s.getLevel())-21] = R.drawable.boton_nivel2estrellas;
		    			}else if(s.devolverNumeroEstrellas()==3){
		    				mThumbIds[Integer.valueOf(s.getLevel())-21] = R.drawable.boton_nivel3estrellas;
		    			}
		    		}else if(mundo==3 && Integer.parseInt(s.getLevel())>40 && Integer.parseInt(s.getLevel())<61){
		    			if(s.devolverNumeroEstrellas()==0){
		    				mThumbIds[Integer.valueOf(s.getLevel())-41] = R.drawable.boton_nivel0estrellas;
		    			}else if(s.devolverNumeroEstrellas()==1){
		    				mThumbIds[Integer.valueOf(s.getLevel())-41] = R.drawable.boton_nivel1estrellas;
		    			}else if(s.devolverNumeroEstrellas()==2){
		    				mThumbIds[Integer.valueOf(s.getLevel())-41] = R.drawable.boton_nivel2estrellas;
		    			}else if(s.devolverNumeroEstrellas()==3){
		    				mThumbIds[Integer.valueOf(s.getLevel())-41] = R.drawable.boton_nivel3estrellas;
		    			}
		    		}else if(mundo==4 && Integer.parseInt(s.getLevel())>60 && Integer.parseInt(s.getLevel())<81){
		    			if(s.devolverNumeroEstrellas()==0){
		    				mThumbIds[Integer.valueOf(s.getLevel())-61] = R.drawable.boton_nivel0estrellas;
		    			}else if(s.devolverNumeroEstrellas()==1){
		    				mThumbIds[Integer.valueOf(s.getLevel())-61] = R.drawable.boton_nivel1estrellas;
		    			}else if(s.devolverNumeroEstrellas()==2){
		    				mThumbIds[Integer.valueOf(s.getLevel())-61] = R.drawable.boton_nivel2estrellas;
		    			}else if(s.devolverNumeroEstrellas()==3){
		    				mThumbIds[Integer.valueOf(s.getLevel())-61] = R.drawable.boton_nivel3estrellas;
		    			}
		    		}else if(mundo==5 && Integer.parseInt(s.getLevel())>80 && Integer.parseInt(s.getLevel())<101){
		    			if(s.devolverNumeroEstrellas()==0){
		    				mThumbIds[Integer.valueOf(s.getLevel())-81] = R.drawable.boton_nivel0estrellas;
		    			}else if(s.devolverNumeroEstrellas()==1){
		    				mThumbIds[Integer.valueOf(s.getLevel())-81] = R.drawable.boton_nivel1estrellas;
		    			}else if(s.devolverNumeroEstrellas()==2){
		    				mThumbIds[Integer.valueOf(s.getLevel())-81] = R.drawable.boton_nivel2estrellas;
		    			}else if(s.devolverNumeroEstrellas()==3){
		    				mThumbIds[Integer.valueOf(s.getLevel())-81] = R.drawable.boton_nivel3estrellas;
		    			}
		    		}
		    	}
		    }
	    	
	    	System.out.println("----AdapterGridLevel.java: ultimoNivelAlcanzado: " +ultimoNivelAlcanzado);
	    	
	    	for(int i=0;i<mThumbIds.length;i++){
	    		if(mThumbIds[i]==null){
	    			if((ultimoNivelAlcanzado-((mundo-1)*20))==i && ultimoNivelAlcanzado!=0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else if(mundo==1 && i==0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else if(mundo==2 && i==0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else if(mundo==3 && i==0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else if(mundo==4 && i==0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else if(mundo==5 && i==0){
	    				mThumbIds[i] = R.drawable.boton_nivel0estrellas;
	    			}else{
	    				mThumbIds[i] = R.drawable.boton_nivel_bloqueado_estrellas;
	    				listaNivelesBloqueados.add(String.valueOf(i+1));
	    			}
	    		}
	    	}
		    
	    	
	    }
	    
	    
	  

	}
