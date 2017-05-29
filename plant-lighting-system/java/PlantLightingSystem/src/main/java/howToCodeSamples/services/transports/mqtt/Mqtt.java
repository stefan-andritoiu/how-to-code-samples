package howToCodeSamples.services.transports.mqtt;

import java.util.Properties;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class Mqtt implements MqttCallback {
	private final static String DEFAULT_TCP_PORT = "1883";
	private final static String DEFAULT_SSL_PORT = "8883";
	
	private String hostname;
	private String topic;
	private String clientId;
	private String authMethod;
	private String authToken;
	private boolean isSSL;
	private boolean configValid;
	
	private MqttClient client = null;

	public Mqtt(Properties config) {
		init(config);
	}

	/**
	 * Publish message
	 * 
	 * @param message
	 */
	public void publish(String message)
	{
		publish(topic, message, false, 2);
	}
	
	/**
	 * Publish message to a topic
	 * 
	 * @param topic
	 *            to publish the message to
	 * @param message
	 *            JSON object representation as a string
	 * @param retained
	 *            true if retained flag is required
	 * @param qos
	 *            quality of service (0, 1, 2)
	 */
	public void publish(String topic, String message, boolean retained, int qos) {
		connect(hostname, clientId, authMethod, authToken, isSSL);
		
		// create a new MqttMessage from the message string
		MqttMessage mqttMsg = new MqttMessage(message.getBytes());
		// set retained flag
		mqttMsg.setRetained(retained);
		// set quality of service
		mqttMsg.setQos(qos);
		try {
			client.publish(topic, mqttMsg);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		disconnect();
	}

	/**
	 * Received one subscribed message
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("MqttClient:");
		String payload = new String(message.getPayload());
		System.out.println("Message received on topic " + topic + ": message is " + payload);
	}

	@Override
	public void connectionLost(Throwable throwable) {
		System.err.println("MqttClient: connection to Mqtt server is lost.");
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}

	/**
	 * One message is successfully published
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		System.out.println("MqttClient: message sent successfully.");
	}

	private void connect(String serverHost, String clientId, String authmethod, String authtoken, boolean isSSL) {
		// check if client is already connected
			String connectionUri = null;

			// tcp://<org-id>.messaging.internetofthings.ibmcloud.com:1883
			// ssl://<org-id>.messaging.internetofthings.ibmcloud.com:8883
			if (isSSL) {
				connectionUri = "ssl://" + serverHost + ":" + DEFAULT_SSL_PORT;
			} else {
				connectionUri = "tcp://" + serverHost + ":" + DEFAULT_TCP_PORT;
			}

			if (client != null && client.isConnected()) {
				try {
					client.disconnect();
				} catch (MqttException e) {
					e.printStackTrace();
				}
				client = null;
			}

			try {
				client = new MqttClient(connectionUri, clientId);
			} catch (MqttException e) {
				e.printStackTrace();
			}

			client.setCallback(this);

			// create MqttConnectOptions and set the clean session flag
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setUserName(authmethod);
			options.setPassword(authtoken.toCharArray());

			// If SSL is used, do not forget to use TLSv1.2
			if (isSSL) {
				java.util.Properties sslClientProps = new java.util.Properties();
				sslClientProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
				options.setSSLProperties(sslClientProps);
			}

			try {
				// connect
				client.connect(options);
				System.out.println("MqttClient: connected to " + connectionUri);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

	/**
	 * Disconnect MqttClient from the MQTT server
	 */
	private void disconnect() {

		// check if client is actually connected
		if (isMqttConnected()) {
			try {
				// disconnect
				client.disconnect();
				System.out.println("MqttClient: successfully disconnected.");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Checks if the MQTT client has an active connection
	 * 
	 * @return True if client is connected, false if not.
	 */
	private boolean isMqttConnected() {
		boolean connected = false;
		try {
			if ((client != null) && (client.isConnected())) {
				connected = true;
			}
		} catch (Exception e) {
			// swallowing the exception as it means the client is not connected
		}
		return connected;
	}

	private void init(Properties config) {
		hostname = config.getProperty("MQTT_HOSTNAME");
		clientId = config.getProperty("MQTT_CLIENTID");
		topic = config.getProperty("MQTT_TOPIC");
		authMethod = config.getProperty("MQTT_AUTHMETHOD");
		authToken = config.getProperty("MQTT_AUTHTOKEN");
		String sslStr = config.getProperty("MQTT_SSL");

		if (hostname != null && clientId != null && topic != null && authMethod != null && authToken != null
				&& sslStr != null) {
			configValid = true;
		} else {
			configValid = false;
		}
		if (configValid && sslStr.equals("T")) {
			isSSL = true;
		} else {
			isSSL = false;
		}
	}

	public boolean getConfigResult() {
		return configValid;
	}
}
