package com.ppica.mydeviceid;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

public class Main extends AppCompatActivity {
    private static final String iDFileName = "ID";
    private static final int IdMaxLength = 15;
    private static final Pattern deviceIdPattern = Pattern.compile(String.format("^[0-9]{%s}$",IdMaxLength));

    private TextView txtResult;
    private ProgressBar progressBar;
    private Button btnReset;

    protected void showPrompt() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.title_registerId);
        alert.setMessage(R.string.subheader_registerId);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(IdMaxLength);
        input.setFilters(FilterArray);

        alert.setView(input);

        alert.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> {
            String ID = input.getText().toString();

            //Save IMEI
            txtResult.setText(R.string.state_saving);
            progressBar.setProgress(90, true);

            // if the ID registered doesn't match the ID pattern
            if (!deviceIdPattern.matcher(ID).matches()) {
                showPrompt();
                return;
            }

            saveIdToFile(ID);
        });
        alert.show();
    }

    protected void saveIdToFile(String ID) {

        try {
            OutputStreamWriter streamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(iDFileName, Context.MODE_PRIVATE));
            streamWriter.write(ID);
            streamWriter.close();
            progressBar.setProgress(100, true);
            Toast.makeText(getApplicationContext(), R.string.toast_idSaved, Toast.LENGTH_SHORT).show();
            readDeviceId();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void readDeviceId() {
        //Get the text file
        File file = new File(this.getFilesDir(), iDFileName);

        progressBar.setProgress(20, true);
        txtResult.setText(R.string.state_searching);

        if (file.exists()) {
            //Read text from file
            progressBar.setProgress(40, true);
            txtResult.setText(R.string.state_loading);

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                txtResult.setText(R.string.state_reading);
                progressBar.setProgress(70, true);

                String ID = br.readLine();

                // if the ID registered doesn't match the ID pattern
                if (!deviceIdPattern.matcher(ID).matches()) {
                    showPrompt();
                    return;
                }
                txtResult.setText(String.format(getResources().getString(R.string.state_showId), ID));
                progressBar.setVisibility(View.INVISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                br.close();

            } catch (Exception e) {
                Toast.makeText(this,R.string.toast_errorReadingFile,Toast.LENGTH_SHORT).show();
                Log.e("ERROR", "Error en lectura");
                e.printStackTrace();
                Log.d("INFO","Borrando archivo ID...");
                if(file.delete()) {
                    showPrompt();
                }
            }
        } else {
            // File doesn't exist. So let's create one
            //Ask for an ID
            txtResult.setText(R.string.state_registering);
            progressBar.setProgress(70, true);

            showPrompt();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_files);

        txtResult = findViewById(R.id.txtResult);
        progressBar = findViewById(R.id.progressBar);
        btnReset = findViewById(R.id.btnReset);

        progressBar.setProgress(10, true);
        txtResult.setText(R.string.state_loading);

        btnReset.setOnClickListener(view -> {
            File file = new File(getApplicationContext().getFilesDir(), iDFileName);
            if (file.delete()) {
                txtResult.setText(R.string.state_registering);
                showPrompt();
            }
        });

        readDeviceId();
    }
}