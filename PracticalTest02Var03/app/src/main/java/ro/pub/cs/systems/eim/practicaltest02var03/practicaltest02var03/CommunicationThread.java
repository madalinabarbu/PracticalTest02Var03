package ro.pub.cs.systems.eim.practicaltest02var03.practicaltest02var03;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

import static java.net.Proxy.Type.HTTP;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (word");
            String word = bufferedReader.readLine();

            if (word == null || word.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (word)");
                return;
            }


            HashMap<String, String> data = serverThread.getData();
            String definition = null;

            if (data.containsKey(word)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                definition = data.get(word);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();

                HttpGet httpGet = new HttpGet("http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + word);

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }

                Document document = Jsoup.parse(pageSourceCode);

                Element element = document.child(0);

                Elements elements = element.getElementsByTag("WordDefinition");

                for (Element definitions : elements) {
                    definition = definitions.data();
                    if (definition != null) {
                        serverThread.setData(word, definition);
                        break;
                    }
                }
            }

            if (definition == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Translated word is null!");
                return;
            }

            printWriter.println(definition);
            printWriter.flush();

        } catch (
                IOException ioException
                )

        {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally

        {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }


}