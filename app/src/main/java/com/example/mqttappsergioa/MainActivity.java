package com.example.mqttappsergioa;

import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String TOPIC = "test/topic";

    private TextView txtEstado;
    private TextView txtTopic;
    private TextView txtMensaje;
    private TextView txtContador;

    private int contadorMensajes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular elementos del XML
        txtEstado = findViewById(R.id.txtEstado);
        txtTopic = findViewById(R.id.txtTopic);
        txtMensaje = findViewById(R.id.txtMensaje);
        txtContador = findViewById(R.id.txtContador);

        actualizarEstado("Conectando...", "#D97706");

        conectarMQTT();
    }

    private void actualizarEstado(String estado, String color) {

        runOnUiThread(() -> {
            txtEstado.setText(estado);
            txtEstado.setTextColor(Color.parseColor(color));
        });
    }

    private void conectarMQTT() {

        String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(
                getApplicationContext(),
                BROKER,
                clientId
        );

        client.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {

                Log.e("MQTT", "Conexion perdida", cause);

                actualizarEstado("Desconectado", "#DC2626");

                runOnUiThread(() -> {
                    txtTopic.setText("Sin suscripción activa");
                });
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

                Log.d("MQTT", "Topic: " + topic);
                Log.d("MQTT", "Mensaje recibido: " + message.toString());

                // Actualizar interfaz
                runOnUiThread(() -> {

                    contadorMensajes++;

                    txtTopic.setText(topic);
                    txtMensaje.setText(message.toString());
                    txtContador.setText(String.valueOf(contadorMensajes));

                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

                Log.d("MQTT", "Mensaje entregado");

            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        try {

            client.connect(options, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("MQTT", "Conexion exitosa al broker");

                    actualizarEstado("Conectado", "#16A34A");

                    suscribirTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {

                    Log.e("MQTT", "Error de conexion", exception);

                    actualizarEstado("Error de conexión", "#DC2626");
                }
            });

        } catch (MqttException e) {

            Log.e("MQTT", "Error MQTT", e);

            actualizarEstado("Error MQTT", "#DC2626");
        }
    }

    private void suscribirTopic() {

        try {

            client.subscribe(TOPIC, 0, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d("MQTT", "Suscripcion exitosa: " + TOPIC);

                    runOnUiThread(() -> {
                        txtTopic.setText(TOPIC);
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {

                    Log.e("MQTT", "Error de suscripcion", exception);

                    runOnUiThread(() -> {
                        txtTopic.setText("Error de suscripción");
                    });
                }
            });

        } catch (MqttException e) {

            Log.e("MQTT", "Error al suscribirse", e);

            runOnUiThread(() -> {
                txtTopic.setText("Error de suscripción");
            });
        }
    }
}