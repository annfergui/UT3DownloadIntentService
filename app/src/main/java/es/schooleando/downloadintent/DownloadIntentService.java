package es.schooleando.downloadintent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DownloadIntentService extends IntentService {
    // definimos tipos de mensajes que utilizaremos en ResultReceiver
    public static final int PROGRESS = 0;
    public static final int FINSISHED = 1;
    public static final int ERROR = 2;


    private static final String TAG = "DownloadIntentService";
    private int codRespuesta;
    private  int tamRecurso;
    private String tipo;
    private Bitmap imgBmPDownload;


    public DownloadIntentService() {
        super("DownloadIntentService");





    }

    @Override
    protected void onHandleIntent(Intent intent) {


        ResultReceiver resultado;
        // Ejemplo de como logear
        Log.d(TAG, "Servicio arrancado!");
if(intent!=null) {
    //Inicializamos el Resultreceiver con el intent recibido
    resultado = intent.getParcelableExtra("receiverTag");

    //Obtenemos el URL del Intent
    String urlString = intent.getStringExtra("url");
    Log.d(TAG, "El URL del intent es: " + urlString);

    Bundle b = new Bundle();

    //creamos el objeto ConnectivityManager
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    if (ni != null && ni.isConnected()) {
        // Aquí deberás descargar el archivo y notificar a la Activity mediante un ResultReceiver que recibirás en el intent.
    URL url=null;
        try {
            url = new URL(urlString);
            HttpURLConnection conex = (HttpURLConnection) url.openConnection();
            //peticion de descarga y conexión
            conex.setRequestMethod("HEAD");
            conex.connect();

            //aquí vamos a obtener los datos de la descarga
            codRespuesta = conex.getResponseCode();
            tipo = conex.getContentType();
            tamRecurso = conex.getContentLength();

            //comprobamos si nos ha devuelto un codigo ok
            if (codRespuesta == 200) {
                if (tipo.startsWith("image/")) {
                    InputStream is = url.openStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int i;
                    int tot = 0;

                    //ponemos el log de descargando

                    Log.d(TAG, "La descarga esta en curso...");

                    while ((i = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, i);

                        if (tamRecurso>0) {


                            b.putInt("DownloadService",bos.size() * 100 /tamRecurso);

                        } else {
                            b.putInt("DownloadService",i*-1);

                        }
                        resultado.send(this.PROGRESS, b);
                    }
                    String[] datos = urlString.split("/");
                    String[] trozo = datos[datos.length-1].split("\\.");

                    if (trozo.length<2) {
                        trozo = new String[]{"unknown", "jpg"};
                    }

                    //Archivo temporal
                    File outputDir = getExternalCacheDir();

                    File outputFile = File.createTempFile(trozo[0], "." + trozo[1], outputDir);
                    outputFile.deleteOnExit();      //Eliminar archivo temporal al SALIR

                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(bos.toByteArray());

                    b.putString("urlString", outputFile.getPath());
                    resultado.send(FINSISHED, b);


                    //cerraremos los flujos de entrada/salida

                    bos.close();
                    is.close();

                    //byte[]arrayImagenes=bos.toByteArray();
                    //imgBmPDownload= BitmapFactory.decodeByteArray(arrayImagenes,0,arrayImagenes.length);

                    Log.d(TAG, "Descarga completada");
                    resultado.send(FINSISHED, b);
                    b.putString("DownloadService", "Finalizada la descarga");
                    b.putParcelable("bitmapImagen",imgBmPDownload);

                } else {
                    Log.d(TAG, "La URL no pertenece a ninguna imagen");
                    b.putString("DownloadService", "Error:La URL no corresponde a una imagen");
                    resultado.send(ERROR, b);
                }
            } else {
                Log.d(TAG, "Error al conectar:" + codRespuesta);
                b.putString("DownloadService", "Error al conectar");
                resultado.send(ERROR, b);
            }


        } catch (MalformedURLException e) {
            b = new Bundle();
            b.putString("DownloadService", "URL incorrecta");
            resultado.send(this.ERROR, b);
            e.printStackTrace();
        } catch (IOException e) {
            b = new Bundle();
            b.putString("DownloadService", "Error de descarga");
            resultado.send(this.ERROR, b);
            e.printStackTrace();
        }
//aqui hacemos el else de la conexión
    } else {

        b.putString("DownloadService", "Error de conexión");
        resultado.send(ERROR, b);
    }
}else {
  Log.d(TAG,"Error en el DownloadService");
}
        // Aquí deberás descargar el archivo y notificar a la Activity mediante un ResultReceiver que recibirás en el intent.

        // Deberamos obtener el ResultReceiver del intent
        // intent.getParcelableExtra("receiver");

        // Es importante que definas el contenido de los Intent.

        // Por ejemplo:
        //  - que enviarás al IntentService como parámetro inicial (url a descargar)
        //         intent.getgetStringExtra("url")
        //  - que enviarás a ResultReceiver para notificarle incrementos en el porcentaje de descarga (número de 0 a 100)
        //         receiver.send(PROGRESS, Bundle);
        //  - que enviarás a ResultReceiver cuando se haya finalizado la descarga (nombre del archivo temporal)
        //         receiver.send(FINISHED, Bundle);




    }
}
