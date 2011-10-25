package com.chocodroid.labyrinthGame;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.Gravity;
import android.widget.FrameLayout;


public class GeneradorNiveles {
	
	
	
	private Activity mActivity;
	private FrameLayout flay;
	public HashMap listaCuadradosCompleta= new HashMap();
	
	
	public ArrayList<Cuadrado> listadoElementos = new ArrayList();
	public List<Pelota> listadoPelotas = new ArrayList();
	public int recordMovimientos;
	public String tema;
	
	public GeneradorNiveles(FrameLayout _flay,Activity _mActivity, int nivel,String tema){
		this.mActivity = _mActivity;		
		this.flay = _flay;
		this.tema=tema;
		this.inicio(nivel);
	}
	
	public GeneradorNiveles(FrameLayout _flay,Activity _mActivity,boolean esTutorial){
		if(esTutorial){
			this.mActivity = _mActivity;		
			this.flay = _flay;
			this.cargarTutorial();
		}
	}

	
	
	private void inicio(int nivel) {
		this.cargarTodosDesactivados("posiciones_tema_madera.txt");	
		//this.activarNivel("nivel_probar_posiciones.txt");
		this.activarNivel("nivel"+nivel+".txt");
	}
	
	private void cargarTutorial() {
		this.cargarTodosDesactivados("posiciones_tema_madera.txt");	
		this.activarNivel("tutorial.txt");
	}
	
	
	public void cargarTodosDesactivados(String posiciones) {
		InputStream is = this.getClass().getResourceAsStream(posiciones);
		String txt = this.convertStreamToString(is);
		this.leerElementosCompletos(txt);
	}
	
	public void activarNivel(String nivel){
		InputStream is = this.getClass().getResourceAsStream(nivel);
		String txt = this.convertStreamToString(is);
		this.activarElementos(txt);		
		this.cambiarImagenPelotaSiEstanUnidas();
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


	public void leerElementosCompletos(String txt) {

		StringBuffer fileBuffer;
		String fileString = null;
		String line;
		try {
			String matrizX = "";
			String matrizY = "";
			String tipo = "";
			String parteXpx = "";
			String parteYpx = "";
			
			while (!(line = txt.substring(0, txt.indexOf("\n"))).trim().equals("")) {
				matrizY = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				matrizX = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				tipo = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				parteXpx = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				parteYpx = line.trim();
				txt = txt.substring(txt.indexOf("\n") + 1);
				
				System.out.println(matrizX+","+matrizY+","+tipo+","+parteXpx+","+parteYpx);
				Cuadrado c = new Cuadrado(flay.getContext(),Integer.valueOf(parteXpx),Integer.valueOf(parteYpx),Integer.valueOf(matrizX),Integer.valueOf(matrizY),tipo,mActivity,tema);
				//this.flay.addView(c);
				listaCuadradosCompleta.put(matrizX+","+matrizY, c);
				
				if (txt.startsWith("FIN")){
					break;
				}
			}
			
			

		} catch (Exception e) {
			e.getStackTrace();
		}
	} // readFile
	
	
	public void activarElementos(String txt) {

		StringBuffer fileBuffer;
		String fileString = null;
		String line;
		try {
			String matrizX = "";
			String matrizY = "";
			String nombrePelota = "";
			
			while (!(line = txt.substring(0, txt.indexOf("\n"))).trim().equals("")) {

				matrizY = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				matrizX = line.trim();
				
				txt = txt.substring(txt.indexOf("\n") + 1);
				
				Cuadrado c = (Cuadrado)listaCuadradosCompleta.get(matrizX+","+matrizY);
				c.setActivado(true);
				//CAMBIAR IMAGEN SEGUN EL TIPO
				//SINO LO DEJO AQUI, TENGO QUE REORGANIZAR TODO OTRA VEZ, PROBABLE FALLO FUTURO
				c.cambiarImagenSiEsNecesarioVerticales(listaCuadradosCompleta);
				//c.cambiarImagenSiEsNecesarioHorizontales(listaCuadradosCompleta);
				listadoElementos.add(c);
				
				//Centrar cuadrado final
				if(c.getTipo().equals(Cuadrado.TIPO_CUADRADO_FINAL)){
					c.setPosicionXCuadradoFinal(c.getPosicionX() + (int)(10.3*this.mActivity.getResources().getDisplayMetrics().density));
			        c.setPosicionYCuadradoFinal(c.getPosicionY() + (int)(10.3*this.mActivity.getResources().getDisplayMetrics().density));
			        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
			        layoutParams.setMargins(c.getPosicionXCuadradoFinal(), (int)(c.getPosicionYCuadradoFinal()+Constantes.POSICION_INICIAL*this.mActivity.getResources().getDisplayMetrics().density), 0, 0);
			        c.setLayoutParams(layoutParams);  
				}
				
				this.flay.addView(c);
				
				if (txt.startsWith("FINACTIVAR")){
					txt = txt.substring(txt.indexOf("\n") + 1);
					break;
				}
			}
			
			//Cambiar imagenes si son necesarias
			for(Cuadrado c:listadoElementos){
				c.cambiarImagenSiEsNecesarioHorizontales(listaCuadradosCompleta);
			}
			/*Iterator it = listaCuadradosCompleta.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry)it.next();
				System.out.println(e.getKey() + " " + e.getValue());
				Cuadrado c = (Cuadrado)listaCuadradosCompleta.get(e.getKey());
				c.cambiarImagenSiEsNecesarioHorizontales(listaCuadradosCompleta);
			}*/
			
			
			
			//COLOCAR PELOTAS
			while (!(line = txt.substring(0, txt.indexOf("\n"))).trim().equals("")) {
				nombrePelota = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				matrizY = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				matrizX = line.trim();
				
				txt = txt.substring(txt.indexOf("\n") + 1);
				
				Cuadrado c = (Cuadrado)listaCuadradosCompleta.get(matrizX+","+matrizY);
				
				Pelota p = new Pelota(flay.getContext(),c.getPosicionX(),c.getPosicionY(),Integer.valueOf(matrizX),Integer.valueOf(matrizY),nombrePelota,mActivity,this.tema);
				listadoPelotas.add(p);
				this.flay.addView(p);
				
				if (txt.startsWith("FIN")){
					System.out.println(".....FIN......");
					break;
				}
			}
			
			//RECUPERAR RECORD
			txt = txt.substring(txt.indexOf("\n") + 1);
			this.recordMovimientos = Integer.valueOf(txt.substring(txt.indexOf("RECORD,") + 7,txt.indexOf("\n")));

		} catch (Exception e) {
			e.getStackTrace();
		}
	} // readFile
	
	public void cambiarImagenPelotaSiEstanUnidas(){
		//comprobar si es necesario cambiar imagen de pelota por colision
		for(Pelota p:listadoPelotas){
			for(Pelota p2:listadoPelotas){
				if(!p.equals(p2)){
					if(p.getMatrizY()==p2.getMatrizY()){
						boolean p2alladoizq=false;
						boolean p2alladodrch=false;
						if((p.getMatrizX()==p2.getMatrizX()-1)){
							p2alladodrch=true;
						}
						if((p.getMatrizX()==p2.getMatrizX()+1)){
							p2alladoizq=true;
						}
						//comprobar si existe pared de separacion entre las pelotas
						Cuadrado c1=(Cuadrado)this.listaCuadradosCompleta.get(((int)p.getMatrizX()+5)+","+p.getMatrizY());
						if(p2alladodrch==true && c1!=null && !c1.isActivado()){
							p.cambiarImagenyPosicionPelotaEnCasoDeColision();
						}
						Cuadrado c2=(Cuadrado)this.listaCuadradosCompleta.get(((int)p.getMatrizX()+4)+","+p.getMatrizY());
						if(p2alladoizq==true && c2!=null && !c2.isActivado()){
							p.cambiarImagenyPosicionPelotaEnCasoDeColision();
						}
					}
					
					if(p.getMatrizX()==p2.getMatrizX()){
						boolean p2arriba=false;
						boolean p2abajo=false;
						if((p.getMatrizY()==p2.getMatrizY()-2)){
							p2abajo=true;
						}
						if((p.getMatrizY()==p2.getMatrizY()+2)){
							p2arriba=true;
						}
						//comprobar si existe pared de separacion entre las pelotas
						Cuadrado c1=(Cuadrado)this.listaCuadradosCompleta.get(p.getMatrizX()+","+((int)p.getMatrizY()-1));
						Cuadrado c2=(Cuadrado)this.listaCuadradosCompleta.get(p.getMatrizX()+","+((int)p.getMatrizY()+1));
						if(p2arriba==true && c1!=null && !c1.isActivado()){
							p.cambiarImagenyPosicionPelotaEnCasoDeColision();
						}
						if(p2abajo==true && c2!=null && !c2.isActivado()){
							p.cambiarImagenyPosicionPelotaEnCasoDeColision();
						}
					}
				}
				
			}
		}
	}
	
 
}
