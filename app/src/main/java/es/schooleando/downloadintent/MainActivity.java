package es.schooleando.downloadintent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ProgressBar pb;
    private TextView tv;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //recogemos las variables del layout
        pb = (ProgressBar) findViewById(R.id.progress);
        pb.setVisibility(View.INVISIBLE);
        tv = (TextView) findViewById(R.id.estadoTV);
        et = (EditText) findViewById(R.id.urlET);

        // Añade en el interfaz un botón y un TextView, como mínimo.

        // cuando pulsemos el botón deberemos crear un Intent que contendrá un Bundle con:
        // una clave "url" con la dirección de descarga asociada.
        // una clave "receiver" con un objeto ResultReceiver.
        //
        // El objeto ResultReceiver contendrá el callback que utilizaremos para recibir
        // mensajes del IntentService.

        // después deberás llamar al servicio con el intent.
    }
       public void descargURL(View v){
        if (!et.getText().toString().trim().equals("")) {
            pb.setVisibility(View.VISIBLE);

            Intent i = new Intent(this, DownloadIntentService.class);
            //llamamos al url para obtener la cadena introducida

            i.putExtra("url", et.getText().toString());

            ResultReceiver receiver = new ResultReceiver(new Handler()) {
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    //vamos a recibir los resultados y los pasaremos a un switch-case
                    switch (resultCode) {
                        case DownloadIntentService.PROGRESS:
                            int progreso = resultData.getInt("DownloadService");
                            pb.setIndeterminate(progreso < 0);
                            if (progreso > 0) {
                                tv.setText("" + progreso + "%");
                            }
                            pb.setProgress(progreso);
                            break;
                        case DownloadIntentService.FINSISHED:
                            String urlString = resultData.getString("urlString");
                            pb.setVisibility(View.INVISIBLE);
                            tv.setText("");
                            File fichero = new File(urlString);
                            if (fichero.exists()) {
                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(fichero).toString());
                                String tipo = mime.getMimeTypeFromExtension(ext);
                                try {

                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.your.package.fileProvider", fichero);
                                        intent.setDataAndType(contentUri, tipo);
                                    } else {
                                        intent.setDataAndType(Uri.fromFile(fichero), tipo);
                                    }
                                    startActivityForResult(intent, 0);
                                }catch (ActivityNotFoundException e) {
                                    Toast.makeText(MainActivity.this, "No se encuentra la Activity", Toast.LENGTH_LONG).show();
                                }

                            }else{
                                Toast.makeText(MainActivity.this,"No existe el fichero",Toast.LENGTH_LONG).show();


                    }
                            break;

                        case DownloadIntentService.ERROR:
                            String mensaje=resultData.getString("DownloadService");
                            pb.setVisibility(View.INVISIBLE);
                            tv.setText("");
                            Toast.makeText(MainActivity.this,mensaje,Toast.LENGTH_LONG).show();
                            break;

                }

            }
        };
        i.putExtra("receiverTag",receiver);
            startService(i);
        }else{
            Toast.makeText(MainActivity.this,"La URL no ha sido introducida",Toast.LENGTH_SHORT).show();
        }
    }
    }

