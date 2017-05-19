package ro.pub.cs.systems.eim.practicaltest02var03.practicaltest02var03;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02Var03MainActivity extends AppCompatActivity {
    // Server widgets
    private EditText serverPortEditText = null;
    private Button connectButton = null;

    private TextView translatedWord = null;

    // Client widgets
    private EditText clientWordEditText = null;
    private Button getWordButton = null;

    private ServerThread serverThread = null;
    private ClientThread clientThread = null;


    private ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();
    private class ConnectButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String serverPort = serverPortEditText.getText().toString();

            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));

            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }

            serverThread.start();
        }

    }

    private GetWordButtonClickListener getWordButtonClickListener = new GetWordButtonClickListener();
    private class GetWordButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String clientAddress = "localhost";
            String clientPort = serverPortEditText.getText().toString();

            if (clientAddress == null || clientAddress.isEmpty()
                    || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            String word = clientWordEditText.getText().toString();

            if (word == null || word.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (word) should be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            translatedWord.setText(Constants.EMPTY_STRING);

            clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), word, translatedWord);
            clientThread.start();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_practical_test02_var03_main);

        serverPortEditText = (EditText)findViewById(R.id.server_port_edit_text);

        connectButton = (Button)findViewById(R.id.connect_button);
        connectButton.setOnClickListener(connectButtonClickListener);

        clientWordEditText = (EditText)findViewById(R.id.client_word_edit_text);

        getWordButton = (Button)findViewById(R.id.get_word_button);
        getWordButton.setOnClickListener(getWordButtonClickListener);

        translatedWord = (TextView)findViewById(R.id.translated_word);

    }



    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}
