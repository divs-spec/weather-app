import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class WeatherFetcher {

    // Replace with your own OpenWeatherMap API key
    static final String API_KEY = "879b77ce2c2f52921547e52c3b0bb8f7";

    public static JSONObject fetch_weather(String city) {
        /* Fetch weather data for a given city. */

        // Construct the API request URL
        String url = "http://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + API_KEY + "&units=metric";

        try {
            // Sending request
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                // Create a BufferedReader to read the response stream from the connection
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // StringBuilder to store the response data
                StringBuilder responseBuilder = new StringBuilder();


                String line;  // Variable to hold each line of the response

                // Read the response line by line and append it to responseBuilder
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                // Parse the JSON response
                JSONObject weather_data = new JSONObject(responseBuilder.toString());
                return weather_data;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    public static void print_weather_report(String city, JSONObject weather_data) {
        /* Print the weather report for a given city. */

        System.out.println("\n Weather Report for: " + city);
        System.out.println("Temperature: " + weather_data.getJSONObject("main").getDouble("temp") + " °C");
        System.out.println("Description: " + weather_data.getJSONArray("weather").getJSONObject(0).getString("description"));
        System.out.println("Humidity: " + weather_data.getJSONObject("main").getInt("humidity") + " %");
    }

    public static void main(String[] args) {
        // Ask the user for a city name
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a city name: ");
        String city = scanner.nextLine();
        scanner.close();

        // Fetch weather data
        JSONObject weather_data = fetch_weather(city);

        if (weather_data != null) {
            // Print the weather report
            print_weather_report(city, weather_data);
        } else {
            System.out.println("Please check if you are putting correct API key and city name.");
        }
    }
}