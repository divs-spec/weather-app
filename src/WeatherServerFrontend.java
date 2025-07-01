import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class WeatherServerFrontend {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/weather", new ProxyWeatherHandler()); // Proxy to backend
        server.setExecutor(null);

        System.out.println("Frontend server running on http://localhost:" + port);
        server.start();
    }

    // Serve static index.html
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && "/".equals(exchange.getRequestURI().getPath())) {
                File file = new File("index.html");
                if (file.exists()) {
                    byte[] response = java.nio.file.Files.readAllBytes(file.toPath());
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                }
            } else {
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
            }
        }
    }

    // Proxy POST request from frontend to backend at localhost:8082
    static class ProxyWeatherHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");

            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); // No content
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    // Read body
                    int contentLength = Integer.parseInt(exchange.getRequestHeaders().getFirst("Content-Length"));
                    byte[] requestBytes = exchange.getRequestBody().readNBytes(contentLength);
                    String requestBody = new String(requestBytes, StandardCharsets.UTF_8);

                    // Forward to backend server at port 8082
                    URL url = new URL("http://localhost:8082/weather");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");

                    OutputStream os = conn.getOutputStream();
                    os.write(requestBytes);
                    os.flush();
                    os.close();

                    // Read backend response
                    InputStream is = conn.getResponseCode() == 200
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder backendResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        backendResponse.append(line);
                    }

                    reader.close();
                    byte[] responseBytes = backendResponse.toString().getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(conn.getResponseCode(), responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                } catch (Exception e) {
                    String error = "{\"error\":\"Proxy failed: " + e.getMessage() + "\"}";
                    byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, errorBytes.length);
                    exchange.getResponseBody().write(errorBytes);
                } finally {
                    exchange.getResponseBody().close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
