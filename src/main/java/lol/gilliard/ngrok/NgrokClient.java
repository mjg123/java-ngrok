package lol.gilliard.ngrok;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.gilliard.ngrok.client.TunnelDefinition;
import lol.gilliard.ngrok.client.TunnelDetails;
import lol.gilliard.ngrok.client.TunnelDetailsList;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class NgrokClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NgrokClient.class);

    public static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Process process;
    private final OkHttpClient httpClient;

    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;

    protected NgrokClient(Process ngrokProcess, String ngrokWebServiceUrl) {
        this.process = ngrokProcess;
        this.baseUrl = "http://" + ngrokWebServiceUrl + "/api";
        httpClient = new OkHttpClient();
    }

    public TunnelDetails connect(TunnelDefinition definition) {
        try {
            String body = MAPPER.writeValueAsString(definition);

            Request request = new Request.Builder()
                .url(baseUrl + "/tunnels")
                .method("POST", RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();

            Response response = httpClient.newCall(request).execute();

            return MAPPER.readValue(response.body().string(), TunnelDetails.class);

        } catch (IOException e) {
            throw new NgrokException("Failed to create tunnel", e);
        }
    }

    public TunnelDetails connect(String name, TunnelProtocol protocol, int port) {
        TunnelDefinition definition = new TunnelDefinition(name, protocol.getName(), port);
        return connect(definition);
    }

    public List<TunnelDetails> listTunnels() {

        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/tunnels")
                .header("accept", "application/json")
                .build();

            Response response = httpClient.newCall(request).execute();

            return MAPPER.readValue(response.body().string(), TunnelDetailsList.class).tunnels;

        } catch (IOException e) {
            throw new NgrokException("Failed to list tunnels", e);
        }

    }

    public TunnelDetails getTunnel(String tunnelName) {
        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/tunnels/" + tunnelName)
                .header("accept", "application/json")
                .build();

            Response response = httpClient.newCall(request).execute();

            if (! response.isSuccessful()){
                return null;
            }

            return MAPPER.readValue(response.body().string(), TunnelDetails.class);

        } catch (IOException e) {
            throw new NgrokException("Failed to list tunnels", e);
        }
    }

    public void disconnect(String tunnelName) {

        try {
            Request request = new Request.Builder()
                .url(baseUrl + "/tunnels/" + tunnelName)
                .delete()
                .build();

            Response response = httpClient.newCall(request).execute();

            if (! response.isSuccessful()){
                throw new NgrokException(String.format("Failed to disconnect tunnel: %s", response.body().string()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectAll(){
        listTunnels().stream().forEach(tunnel -> disconnect(tunnel.name));
    }

    public void shutdown(){
        try {
            process.destroy(); // polite request
            process.waitFor(); // blocks until the process is gone

        } catch (InterruptedException e) {
            throw new NgrokException("Interrupted while waiting for ngrok to shut down", e);
        }
    }

    public TunnelBuilder build() {
        return new TunnelBuilder(this);
    }
}
