package com.example.ptmarketing04.pruebasdrive;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient apiClient;
    protected static final int REQ_CREATE_FILE = 1001;
    protected static final int REQ_OPEN_FILE = 1002;
    protected Button btEliminar,btActividad,btMeta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btEliminar = (Button)findViewById(R.id.btEliminar);
        btActividad = (Button)findViewById(R.id.btActividad);
        btMeta = (Button)findViewById(R.id.btMeta);

        btEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        deleteFile(DriveId.decodeFromString("DriveId:CAESABie8wIg6LjkrchRKAA="));
                    }
                }.start();
            }
        });

        btActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileWithActivity();
            }
        });

        btMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        getMetadata(DriveId.decodeFromString("DriveId:CAESABig8wIg6LjkrchRKAA="));
                    }
                }.start();
            }
        });


        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error de conexion!", Toast.LENGTH_SHORT).show();
        Log.e( "OnConnectionFailed: ",connectionResult+"");

    }


    //Crear directorio automático
    private void createFolder(final String foldername) {

        MetadataChangeSet changeSet =
                new MetadataChangeSet.Builder()
                        .setTitle(foldername)
                        .build();

        //Opción 1: Directorio raíz
        DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);

        //Opción 2: Otra carpeta distinta al directorio raiz
        //DriveFolder folder =
        //        DriveId.decodeFromString("DriveId:CAESABjKGSD6wKnM7lQoAQ==").asDriveFolder();

        folder.createFolder(apiClient, changeSet).setResultCallback(
                new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult result) {
                        if (result.getStatus().isSuccess())
                            Log.i("Carpeta creada con ID",result.getDriveFolder().getDriveId()+"");
                        else
                            Log.e("ERROR", "Error al crear carpeta");
                    }
                });
    }

    protected void crearCarpeta(View v){
        new Thread() {
            @Override
            public void run() {
                createFolder("Pruebas");
            }
        }.start();
    }


    //Escribir dentro del achivo automáticamente
    private void writeSampleText(DriveContents driveContents) {
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        try {
            writer.write("Esto es un texto de prueba!");
            writer.close();
        } catch (IOException e) {
            Log.e("Error al escribir:",e.getMessage());
        }
    }


    //Crear archivo automático
    private void createFile(final String filename) {

        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {

                            writeSampleText(result.getDriveContents());

                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                            .setTitle(filename)
                                            .setMimeType("text/plain")
                                            .build();

                            //Opción 1: Directorio raíz
                            DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);

                            //Opción 2: Otra carpeta distinta al directorio raiz
                            //DriveFolder folder =
                            //    DriveId.decodeFromString("DriveId:CAESABjKGSD6wKnM7lQoAQ==").asDriveFolder();

                            folder.createFile(apiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(DriveFolder.DriveFileResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                Log.i( "Fichero con ID = ", result.getDriveFile().getDriveId()+"");
                                            } else {
                                                Log.e("ERROR", "Error al crear el fichero");
                                            }
                                        }
                                    });
                        } else {
                            Log.e("ERROR", "Error al crear DriveContents");
                        }
                    }
                });
    }

    protected void crearFichero(View v){
        new Thread() {
            @Override
            public void run() {
                createFile("prueba1.txt");
            }
        }.start();
    }


    //////////////////////////////////////
    //  Para hacerlo desde el activity  //
    //////////////////////////////////////

    protected void createFileWithActivity(View v) {

        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        MetadataChangeSet changeSet =
                                new MetadataChangeSet.Builder()
                                        .setMimeType("text/plain")
                                        .build();

                        writeSampleText(result.getDriveContents());

                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(changeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(apiClient);

                        try {
                            startIntentSenderForResult(
                                    intentSender, REQ_CREATE_FILE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("Error al iniciar:", " Create File"+e);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CREATE_FILE:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    Log.i("Fichero con ID = " ,""+driveId);
                }
                break;
            case REQ_OPEN_FILE:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    Log.i("OK", "Fichero seleccionado ID = " + driveId);

                    readFile(driveId);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    //Eliminar
    private void deleteFile(DriveId fileDriveId) {
        DriveFile file = fileDriveId.asDriveFile();

        //Opción 1: Enviar a la papelera
        file.trash(apiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if(status.isSuccess())
                    Log.i("ELIMINADO", "Fichero eliminado correctamente.");
                else
                    Log.e("ERROR", "Error al eliminar el fichero");
            }
        });

        //Opción 2: Eliminar
        //file.delete(apiClient).setResultCallback(...)
    }

    //Lectura de ficheros
    private void readFile(DriveId fileDriveId) {

        DriveFile file = fileDriveId.asDriveFile();

        file.open(apiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e("ERROR","Error al abrir fichero (readFile)");
                            return;
                        }

                        DriveContents contents = result.getDriveContents();

                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(contents.getInputStream()));

                        StringBuilder builder = new StringBuilder();

                        try {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line);
                            }
                        } catch (IOException e) {
                            Log.e("ERROR","Error al leer fichero");
                        }

                        contents.discard(apiClient);

                        Log.i("OK", "Fichero leido: " + builder.toString());
                    }
                });
    }


    //Escritura de ficheros
    private void writeFile(DriveId fileDriveId) {

        DriveFile file = fileDriveId.asDriveFile();

        file.open(apiClient, DriveFile.MODE_WRITE_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e("ERROR","Error al abrir fichero (writeFile)");
                            return;
                        }

                        DriveContents contents = result.getDriveContents();

                        BufferedWriter writer =
                                new BufferedWriter(
                                        new OutputStreamWriter(contents.getOutputStream()));

                        try {
                            writer.write("Contenido del fichero modificado!");
                            writer.flush();
                        } catch (IOException e) {
                            Log.e("ERROR","Error al escribir fichero");
                        }

                        //Opcional: cambio de metadatos
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/plain")
                                .build();

                        contents.commit(apiClient, changeSet).setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status result) {
                                        if(result.getStatus().isSuccess())
                                            Log.i("OK", "Fichero escrito correctamente");
                                        else
                                            Log.e("ERROR", "Error al escribir fichero");
                                    }
                                });
                    }
                });
    }


    //Abrir ctividad para leer/escribir
    private void openFileWithActivity() {

        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain" })
                .build(apiClient);

        try {
            startIntentSenderForResult(
                    intentSender, REQ_OPEN_FILE, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e("ERROR", "Error al iniciar actividad: Open File", e);
        }
    }

    //Leer y tratar metadatos
    private void getMetadata(DriveId fileDriveId) {
        DriveFile file = fileDriveId.asDriveFile();

        file.getMetadata(apiClient).setResultCallback(
                new ResultCallback<DriveResource.MetadataResult>() {
                    @Override
                    public void onResult(DriveResource.MetadataResult metadataResult) {
                        if (metadataResult.getStatus().isSuccess()) {
                            Metadata metadata = metadataResult.getMetadata();
                            Log.i("RESULT", "Metadatos obtenidos correctamente." +
                                    " Title: " + metadata.getTitle() +
                                    " MIMETYPE: " + metadata.getMimeType() +
                                    " LastUpdated: " + metadata.getModifiedDate());
                        }
                        else {
                            Log.e("ERROR", "Error al obtener metadatos");
                        }
                    }
                });
    }


}
