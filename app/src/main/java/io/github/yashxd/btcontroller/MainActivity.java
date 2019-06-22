package io.github.yashxd.btcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_ENABLE_BT = 10;
    private boolean connection = false;

    BluetoothAdapter bluetoothAdapter;

    private final String DEVICE_ADDRESS = "98:D3:32:20:E3:E9"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    Button connect;// switchState;
    android.widget.ImageButton speak;
    TextView txtSpeechInput;

    String command;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //switchState = (Button) findViewById(R.id.button_switch);
        connect = (Button) findViewById(R.id.button_connect);
        speak = (android.widget.ImageButton) findViewById(R.id.button_speechinput);
        txtSpeechInput = (TextView) findViewById(R.id.textview_speechout);

        /*switchState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command = "1";
                try {
                    outputStream.write(command.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/



        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTinit()) {
                    BTconnect();
                }
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!connection)
                    Toast.makeText(getApplicationContext(), "Connect to Arduino first!", Toast.LENGTH_SHORT).show();
                else
                    promptSpeechInput();
            }
        });
    }

    public boolean BTinit()
    {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) { //Checks if the device supports bluetooth
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if(!bluetoothAdapter.isEnabled()) { //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);

            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if(bondedDevices.isEmpty()) {//Checks for paired bluetooth devices
            Toast.makeText(getApplicationContext(), "Please pair the device first", Toast.LENGTH_SHORT).show();
        }
        else {
            for(BluetoothDevice iterator : bondedDevices) {
                if(iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect()
    {
        boolean connected = true;

        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            Toast.makeText(getApplicationContext(),
                    "Connection to Arduino successful", Toast.LENGTH_LONG).show();
            connection = true;
        }
        catch(IOException e) {
            e.printStackTrace();
            connected = false;
            connection = false;
            Toast.makeText(getApplicationContext(),
                    "Connection failed!\nPlease try again.", Toast.LENGTH_LONG).show();
        }

        if(connected) {
            try {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    signalArduino(result.get(0));
                }
                break;
            }

        }
    }

    private void signalArduino(String input) {
        //Compares the input as processed by the Speech recognition API and identifies the command by Hit-and-Trial.
        //The command is stored in the String command. To add a custom message, store it in the same variable and skip the if-else-if tree.

        if(input.equals(getString(R.string.switch_state)))
            command = "1";
        else if(input.equals(getString(R.string.blink)))
            command = "2";
        else if(input.equals(getString(R.string.glow)))
            command = "3";
        else if(input.compareToIgnoreCase(getString(R.string.led1_on_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_on_2))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_on_3))==0) {
            command = "A";
            txtSpeechInput.setText(getString(R.string.led1_on));
        }
        else if(input.compareToIgnoreCase(getString(R.string.led2_on_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_on_2))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_on_3))==0) {
            command = "B";
            txtSpeechInput.setText(getString(R.string.led2_on));
        }
        else if(input.compareToIgnoreCase(getString(R.string.led3_on_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led3_on_2))==0) {
            command = "C";
            txtSpeechInput.setText(getString(R.string.led3_on));
        }
        else if(input.compareToIgnoreCase(getString(R.string.led1_off_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_off_2))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_off_3))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_off_4))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_off_5))==0 ||
                input.compareToIgnoreCase(getString(R.string.led1_off_6))==0) {
            command = "a";
            txtSpeechInput.setText(getString(R.string.led1_off));
        }
        else if(input.compareToIgnoreCase(getString(R.string.led2_off_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_off_2))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_off_3))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_off_4))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_off_5))==0 ||
                input.compareToIgnoreCase(getString(R.string.led2_off_6))==0) {
            command = "b";
            txtSpeechInput.setText(getString(R.string.led2_off));
        }
        else if(input.compareToIgnoreCase(getString(R.string.led3_off_1))==0 ||
                input.compareToIgnoreCase(getString(R.string.led3_off_2))==0 ||
                input.compareToIgnoreCase(getString(R.string.led3_off_3))==0 ||
                input.compareToIgnoreCase(getString(R.string.led3_off_4))==0) {
            command = "c";
            txtSpeechInput.setText(getString(R.string.led3_off));
        }
        else {
            command = "!";
            txtSpeechInput.setText(getString(R.string.try_again));
            Toast.makeText(this, "Please input a valid command!", Toast.LENGTH_SHORT).show();
        }

        //Send data to Arduino.
        try {
            outputStream.write(command.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        connection = false;
    }

    /*@Override
    protected void onPostResume() {
        super.onPostResume();
        connection = false;
    }*/

}
