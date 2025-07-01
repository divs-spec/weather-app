import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherServerbackend {
    private static final String API_KEY = "879b77ce2c2f52921547e52c3b0bb8f7"; // OpenWeatherMap API key

    public static void main(String[] args) throws IOException {
        int port = 8082; // Use 8082 or change if port is busy
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/weather", new WeatherHandler());
        server.setExecutor(null);

        System.out.println("Server running on http://localhost:" + port);
        server.start();
    }

    static class WeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers for all responses
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");

            // Handle preflight request (CORS)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); // No Content
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && "/weather".equals(exchange.getRequestURI().getPath())) {
                try {
                    // Read request body
                    int contentLength = Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length"));
                    InputStream inputStream = exchange.getRequestBody();
                    byte[] requestData = inputStream.readNBytes(contentLength);
                    String requestBody = new String(requestData, StandardCharsets.UTF_8);

                    JSONObject data = new JSONObject(requestBody);
                    String city = data.optString("city", "");

                    // Fetch weather data from OpenWeatherMap API
                    String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city +
                            "&appid=" + API_KEY + "&units=metric";

                    HttpURLConnection apiConnection = (HttpURLConnection) new URL(urlString).openConnection();
                    apiConnection.setRequestMethod("GET");

                    int responseCode = apiConnection.getResponseCode();
                    InputStream apiResponseStream = (responseCode == 200)
                            ? apiConnection.getInputStream()
                            : apiConnection.getErrorStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(apiResponseStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    JSONObject weatherData = new JSONObject(responseBuilder.toString());

                    if (responseCode == 200) {
                        JSONObject mainData = weatherData.getJSONObject("main");
                        double temperature = mainData.getDouble("temp");
                        int humidity = mainData.getInt("humidity");
                        String description = weatherData.getJSONArray("weather").getJSONObject(0).getString("description");

                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("City", city);
                        jsonResponse.put("Temperature", String.format("%.2f", temperature) + "°C");
                        jsonResponse.put("Humidity", humidity + "%");
                        jsonResponse.put("Description", description);

                        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                    } else {
                        String errorMessage = weatherData.optString("message", "Invalid city");
                        byte[] responseBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(400, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                    }
                } catch (Exception e) {
                    String errorMessage = "Internal Server Error: " + e.getMessage();
                    byte[] responseBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                } finally {
                    exchange.getResponseBody().close();
                }
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        }
    }
}
