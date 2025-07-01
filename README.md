
# ☀️ Weather App

A simple Java-based full-stack weather application that fetches real-time weather data using the OpenWeatherMap API and displays it on a clean web interface.

---

## 🔧 Technologies Used

| Component    | Stack                    |
| ------------ | ------------------------ |
| Language     | Java 17+                 |
| Backend      | Java HttpServer          |
| Frontend     | HTML, CSS, JavaScript    |
| HTTP Client  | HttpURLConnection, Axios |
| JSON Parsing | org.json (2023-02-27)    |
| API          | OpenWeatherMap API       |

---

## 📁 Project Structure

```
weather-app/
├── WeatherServerbackend.java      # Backend Java Server (port 8080)
├── WeatherServerFrontend.java     # Optional server to serve HTML
├── index.html                     # UI for user input
├── style.css                      # Styling for HTML
├── script.js                      # JavaScript for fetching weather
├── json-20230227.jar              # org.json parser
└── README.md
```

---

## ⚙️ How It Works

1. The user enters a city name in the input field.
2. JavaScript (`script.js`) sends a POST request to `/weather` with the city name.
3. `WeatherServerbackend.java` receives the request, makes a GET call to OpenWeatherMap, and parses the result.
4. Extracted weather data is sent back as a JSON response.
5. The browser displays temperature, humidity, and description.

---

## ⚡ How to Run This App

### Step 1: Compile the Backend

```bash
javac -cp ".;json-20230227.jar" WeatherServerbackend.java
```

### Step 2: Run the Backend Server

```bash
java -cp ".;json-20230227.jar" WeatherServerbackend
```

> Ensure port 8080 is free or change the port number in `WeatherServerbackend.java`

### Step 3: Open the Frontend

Open `index.html` in your browser directly:

```bash
file:///D:/java/weather%20app/src/index.html
```

Or use `WeatherServerFrontend.java` to serve it:

```bash
javac WeatherServerFrontend.java
java WeatherServerFrontend
```

Then open: [http://localhost:8080](http://localhost:8080)

---

## 📊 Sample API

OpenWeatherMap API:

```
http://api.openweathermap.org/data/2.5/weather?q={city}&appid={API_KEY}&units=metric
```

**Example Output:**

```json
{
  "City": "London",
  "Temperature": "18.23°C",
  "Humidity": "78%",
  "Description": "broken clouds"
}
```

---

## 📦 Requirements

* Java 17+
* org.json library (add json-20230227.jar to classpath)
* A valid [OpenWeatherMap API Key](https://openweathermap.org/api)

---

## 🔀 Future Enhancements

* Add automatic geolocation-based weather
* Show forecast data (5-day / hourly)
* Improve UI/UX with charts and animations
* Add loader and error handling on frontend

---

## 📜 License

This project is created for educational purposes and may be used as a learning reference.
