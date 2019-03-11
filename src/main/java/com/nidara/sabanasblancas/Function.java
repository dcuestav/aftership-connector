package com.nidara.sabanasblancas;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.nidara.sabanasblancas.model.AfterShipTrackingUpdate;
import com.nidara.sabanasblancas.model.AfterShipTrackingUpdateMessage;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java&code={your function key}
     * 2. curl "{your host}/api/HttpTrigger-Java?name=HTTP%20Query&code={your function key}"
     * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
     * More details: https://aka.ms/functions_authorization_keys
     */
    @FunctionName("hello")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }

    @FunctionName("tracking-notification")
    public HttpResponseMessage receiveTrackingNotificationAndUpdateOrderState(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
                    final ExecutionContext context) {

        context.getLogger().info("Recibida notificación de cambio de estado de un tracking");

        // Parse query parameter
        Optional<String> requestBody = request.getBody();

        if (requestBody.isPresent()) {

            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

                AfterShipTrackingUpdate update = mapper.readValue(requestBody.get(), AfterShipTrackingUpdate.class);
                AfterShipTrackingUpdateMessage message = update.getMsg();

                Optional<String> orderId = message.getNumericOrderId();

                if (message.isDelivered()) {
                    if (orderId.isPresent()) {
                        addDeliveredOrderHistory(orderId.get());
                    } else {
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Falta order id " +
                                "numérico").build();
                    }
                }

                return request.createResponseBuilder(HttpStatus.OK).body(message.toString()).build();

            } catch (IOException e) {
                context.getLogger().warning(e.getMessage());
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Fallo al deserializar el cuerpo del mensaje").build();
            }

        } else {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Body vacío").build();
        }

    }

    private void addDeliveredOrderHistory(String orderId) throws IOException {

        String apiKey = System.getenv("PRESTAKEY");
        String apiUrl = System.getenv("PRESTAURL") + "/order_histories";

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(apiKey, "");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        HttpPost httpPost = new HttpPost(apiUrl);

        StringEntity newOrderHistory = new StringEntity("<prestashop>" +
                "<order_history>" +
                "<id_order_state>5</id_order_state>" +
                "<id_order>" + orderId + "</id_order>" +
                "</order_history>" +
                "</prestashop>",
                ContentType.create("text/xml", "UTF-8"));

        httpPost.setEntity(newOrderHistory);
        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            System.out.println(response.getStatusLine().toString());
        } finally {
            response.close();
        }

    }

}
