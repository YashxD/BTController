package io.github.yashxd.btpixelstrip;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_ENABLE_BT = 10;
    private boolean connection = false;

    BluetoothAdapter bluetoothAdapter;

    private final String DEVICE_ADDRESS = "98:D3:32:20:E3:E9"; //MAC Address of Bluetooth Module
    private final String DEVICE_ADDRESS1 = "98:D3:35:00:CF:B3";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    Button submit, connect;
    EditText channelRed, channelGreen, channelBlue;

    String command;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        channelRed = findViewById(R.id.edittext_channelred);
        channelGreen = findViewById(R.id.edittext_channelgreen);
        channelBlue = findViewById(R.id.edittext_channelblue);
        submit = findViewById(R.id.button_submit);
        connect = findViewById(R.id.button_connect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BTinit()) {
                    BTconnect();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!connection)
                    Toast.makeText(getApplicationContext(), "Connect to Arduino first!", Toast.LENGTH_SHORT).show();
                else {
                    //command = channelRed.getText().toString() + channelGreen.getText().toString() + channelBlue.getText().toString();
                    command = channelRed.getText().toString();
                    try {
                        outputStream.write(command.getBytes());
                        Toast.makeText(getApplicationContext(),"Sent!",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
                if(iterator.getAddress().equals(DEVICE_ADDRESS1)) {
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
}
