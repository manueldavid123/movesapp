package com.example.movesapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movesclass.Places;
import com.example.movesclass.Resultados;
import com.example.movesclass.Usuarios;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import android.support.v7.app.ActionBarActivity;

public class Preguntas extends ActionBarActivity {
	private TextView pregunta;
	private TextView tituloPreguntas;
	private RadioButton r1,r2,r3;
	private Button button;
	private String access_token;
	private String expires_in;
	private static String str_respuesta;
    public static int date ;
	private static Date fecha;
	private static Date inicio;
	private static Date fin;
    private static Calendar comienzoActual;
	private static int contador = 1;
	private int contadorAtras = 0;
	private static int aleat=0,aseg=0,af1=0,af2=0,asegf1=0,asegf2=0,salir=0;
	private static String verdadero = null,falso1 = null,falso2 = null;
	private static List<Resultados> lista = new ArrayList<Resultados>();
	private static ArrayList<Places> segments = new ArrayList<Places>();
    private Boolean actualizar;
	@Override
	public void onBackPressed(){
		contadorAtras++;
		if(contadorAtras == 1) Toast.makeText(this, "Pulse de nuevo para cerrar la aplicación", Toast.LENGTH_LONG).show();
		if(contadorAtras == 2){
			contadorAtras =0;
			Intent salida=new Intent( Intent.ACTION_MAIN); 
			finish();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preguntas);
		final Context contexto = this;
		access_token = getIntent().getStringExtra("accessToken");
		expires_in = getIntent().getStringExtra("expires_in");
        actualizar = getIntent().getBooleanExtra("update",false);
		
		pregunta = (TextView) findViewById(R.id.pregunta);
		tituloPreguntas = (TextView) findViewById(R.id.tituloPreguntas);
		r1 = (RadioButton) findViewById(R.id.radio0);
		r2 = (RadioButton) findViewById(R.id.radio1);
		r3 = (RadioButton) findViewById(R.id.radio2);
		button = (Button) findViewById(R.id.siguiente);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				contador++;
				if(contador > 10){
					contador = 1;
					String terminado = "";
					terminado = new Gson().toJson(lista);
					lista.clear();
					Intent i = new Intent(contexto, Results.class);
					i.putExtra("JSON", terminado);
                    i.putExtra("Datos", str_respuesta);
					startActivity(i);
				}else{
				
					try {
						ejecutar(str_respuesta);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
            });
		
        Usuarios u = new Usuarios(this);
        //Los datos se guardan en la Base de Datos. Si existen datos en la base de datos, se obtienen, sino...
        if(!u.checkDatos()){
            str_respuesta = u.obtieneDatos();
            Log.i("Datos","He entrado en CheckDatos");
        }else if(u.checkDatos() || actualizar == true){             //... Si hay datos y el botón de actualizar ha sido pulsado, entonces los datos se obtienen de la API
            HttpClient client = new DefaultHttpClient();
            comienzoActual = new GregorianCalendar();
            String dateAux1 = Integer.toString(comienzoActual.get(Calendar.YEAR));
            String dateAux2 = Integer.toString(comienzoActual.get(Calendar.MONTH));
            String mesConvertido = convierteMes(dateAux2);
            String dateString = dateAux1+mesConvertido;

            //Obtenemos la fecha Actual
            date = Integer.parseInt(dateString);

            Boolean cent = true;
            int cont = 0;

            //Se obtienen los datos de los ultimos 6 meses
            do {
                HttpGet request = new HttpGet("https://api.moves-app.com/api/1.1/user/places/daily/" + date + "?access_token=" + access_token);
                HttpResponse respuesta;
                String recorre = "";
                try {
                    respuesta = client.execute(request);
                    recorre = EntityUtils.toString(respuesta.getEntity());

                    //  str_respuesta = str_respuesta.substring(0, str_respuesta.length()-1)+",";
                    //   String cadenaNueva2 = r.substring(1, str_respuesta2.length());
                    //  str_respuesta = "";
                    //   str_respuesta = cadenaNueva + cadenaNueva2;
                    //  Log.d("JSON",cadenaNueva);
                    //  Log.d("JSON2",cadenaNueva2);
                } catch (Exception e) {
                    Toast.makeText(this, "Ocurrio un error llamando a la API: " + e.toString(), Toast.LENGTH_LONG).show();
                    //pregunta.setText(e.toString());
                }

                String error = recorre.substring(2, 7);
                Log.d("Cadena error", error);
                if (error.equals("error") || recorre == "" || cont == 5) { //Solamente cuenta 5 meses
                    cent = false;
                } else {
                    if (str_respuesta == "" || str_respuesta == null) {
                        str_respuesta = recorre;
                    } else {
                        str_respuesta = str_respuesta.substring(0, str_respuesta.length() - 1) + ",";
                        String cadenaNueva2 = recorre.substring(1, recorre.length());
                        str_respuesta += cadenaNueva2;
                    }
                    Log.e("text",str_respuesta);
                    int auxfecha = restaFecha(date);
                    date = auxfecha;
                    Log.d("Fecha", Integer.toString(date));
                    cont++;
                }

            } while (cent);

            String usuario = u.obtieneUsuario();

            u.insertaDatos(str_respuesta);
        }
        Log.e("Datos",str_respuesta.toString());
		try {
			ejecutar(str_respuesta);

		} catch (IOException e) {
			
			e.printStackTrace();
		}
        

		
	}

    /**
     * Funcion que proporciona las preguntas y las respuestas para que sean contestadas.
     * */
	@SuppressLint("ShowToast")
	public void ejecutar(String respuesta) throws IOException{
		tituloPreguntas.setText("Pregunta "+ contador +" de 10");
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray jArray = parser.parse(respuesta).getAsJsonArray();

        

        for(JsonElement obj : jArray )
        {
            Places cse = gson.fromJson( obj , Places.class);
            segments.add(cse);
        }
        SimpleDateFormat ffecha =  new SimpleDateFormat("dd 'de' MMMM", new Locale("es_ES"));
		SimpleDateFormat fHora =  new SimpleDateFormat("HH:mm", new Locale("es_ES"));

		
		//Obtenemos los lugares
		getLugares();
		

		
		try{
	        //fecha de la pregunta, la hora de inicio y la hora de final
			fecha = new SimpleDateFormat("yyyyMMdd",Locale.FRANCE).parse( segments.get(aleat).getDate());
			inicio = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ",Locale.FRANCE).parse( segments.get(aleat).getSegments().get(aseg).getStartTime());
			fin = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ",Locale.FRANCE).parse( segments.get(aleat).getSegments().get(aseg).getEndTime());
		}catch(Exception e){
			Log.e("Fecha","Ocurrio un error con la fecha");
			pregunta.setText(e.toString());
		}
		
		//Toast.makeText(this, ffecha.format(fecha) + " "+ fHora.format(inicio), Toast.LENGTH_LONG);
		pregunta.setText("¿Dónde estuvo el día "+ffecha.format(fecha)+" "+getDiaSemana(fecha)+"\n desde las "+fHora.format(inicio)+" hasta las "+fHora.format(fin)+"?");
		

		int preguntas = (int)Math.round(Math.random()*3);
		int verdadera = 0;
        //Se pone la pregunta verdadera en un lugar diferente
		switch(preguntas){
            case 0:
                r1.setText(verdadero);
                r2.setText(falso1);
                r3.setText(falso2);
                verdadera = 1;
                break;
            case 1:
                r1.setText(falso1);
                r2.setText(verdadero);
                r3.setText(falso2);
                verdadera = 2;
                break;
            case 2:
                r1.setText(falso2);
                r2.setText(falso1);
                r3.setText(verdadero);
                verdadera = 3;
                break;
            case 3:
                r1.setText(verdadero);
                r2.setText(falso1);
                r3.setText(falso2);
                verdadera = 1;
                break;
		}
        // Se inserta en una lista la pregunta con la respuesta contestada
		if(r1.isChecked()){
			Resultados res = new Resultados(r1.getText().toString(),verdadero);
			lista.add(res);
		}
		if(r2.isChecked()){
			Resultados res = new Resultados(r2.getText().toString(),verdadero);
			lista.add(res);
		}
		if(r3.isChecked()){
			Resultados res = new Resultados(r3.getText().toString(),verdadero);
			lista.add(res);
		}
		
	}
	
	public int aleatorio(int tipo,ArrayList<Places> segmentos,int seleccionado, int anterior){
		int i = 0;
		switch(tipo){
		case 0: //aleatorio principal
		
				i = (int)Math.round(Math.random()*(segmentos.size()-1));
				if(i < 0) i = 0;
		
			break;
			
		case 1: //fechas de la pregunta
				if(segmentos.get(seleccionado).getSegments() != null){
					i = (int)Math.round(Math.random()*(segmentos.get(seleccionado).getSegments().size()-1));
					if(i < 0) i = 0;
			
				}
			break;
		
		case 2: //segmento aleatorio de falso 1
	
				i = (int)Math.round(Math.random()*(segmentos.size()-1));
				if(i < 0) i = 0;
		
			break;
			
		case 3: //fecha para pregunta falsa
			
				i = (int)Math.round(Math.random()*(segmentos.get(seleccionado).getSegments().size()-1));
				if(i < 0) i = 0;
			
			break;
		}
		
		return i;
	}
	
	public static String getDiaSemana(Date d){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(d);
		String dia = ""; 
		switch(cal.get(Calendar.DAY_OF_WEEK)){
		case 1:
			dia = "(Domingo)";
			break;
		case 2:
			dia = "(Lunes)";
			break;
		case 3:
			dia = "(Martes)";
			break;
		case 4:
			dia = "(Miercoles)";
			break;
		case 5:
			dia = "(Jueves)";
			break;
		case 6:
			dia = "(Viernes)";
			break;
		case 7:
			dia = "(Sabado)";
			break;
		
		}
		return dia;
	}

    /**
     * Función con la que obtenemos los lugares de la API de Moves
     * */

	public void getLugares(){
		
		do{
			
			aleat = aleatorio(0,segments,0,0); 
			aseg = aleatorio(1,segments,aleat,0);
			try{
				verdadero = segments.get(aleat).getSegments().get(aseg).getPlace().getName();
			}catch(Exception e){
				Log.e("Verdadero" ,"Fallo en verdadero");
			}
			if(verdadero == null || verdadero.isEmpty()) {
				Log.e("nullVerdadero","nulo");
				verdadero = "";
			}
		}while( verdadero.length() < 2);
		
		
		do{
			af1 = aleatorio(2,segments,0,0);
			asegf1 = aleatorio(1,segments,af1,0);
			try{
			falso1=segments.get(af1).getSegments().get(asegf1).getPlace().getName();
			}catch(Exception e){
				//Log.e("Falso1","nulo");
			}
			if(falso1 == null || falso1.isEmpty()){
				Log.e("nullFalso1","nulo"); 
				falso1 = "";
			}
		}while(falso1.length()<2 || falso1.equals(verdadero));
		
		do{
			af2 = aleatorio(2,segments,0,af1);	
			asegf2 = aleatorio(1,segments,af2,asegf1);	
			try{
			falso2 = segments.get(af2).getSegments().get(asegf2).getPlace().getName();
			}catch(Exception e){
				Log.e("Fallo","Fallo en falso2");
			}
			if(falso2 == null || falso2 == ""){
				Log.e("nullFalso2","nulo");
				falso2 = "";
			}
		}while(falso2.length()<2 || (falso2.equals(falso1) || falso2.equals(verdadero)));
		
	}
    public int restaFecha(int fecha){
        if(fecha%100 == 1) {
            fecha = fecha - 89;
        }else{
            fecha--;
        }
        return fecha;
    }

    /**
     * Función para convertir un mes en dato numérico tal y como lo realizar Moves. P.e. Enero = 01
     * */
    public String convierteMes(String mes){
        String res = "";
        switch (mes){
            case "0":
                res = "01";
                break;
            case "1":
                res = "02";
                break;
            case "2":
                res = "03";
                break;
            case "3":
                res = "04";
                break;
            case "4":
                res = "05";
                break;
            case "5":
                res = "06";
                break;
            case "6":
                res = "07";
                break;
            case "7":
                res = "08";
                break;
            case "8":
                res = "09";
                break;
            case "9":
                res = "10";
                break;
            case "10":
                res = "11";
                break;
            case "11":
                res = "12";
                break;

        }
        return res;
    }
}
