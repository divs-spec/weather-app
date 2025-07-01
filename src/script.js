async function getWeather() {
    const city = document.getElementById('cityInput').value;
    if (!city) {
        alert("Please enter a city name!");
        return;
    }

    try {
        const api = axios.create({
            baseURL: "http://localhost:8082", // Local Java backend
            headers: {
                'Content-Type': 'application/json'
            },
        });

        const response = await api.post('/weather', { city });
        const data = response.data;

        if (data.error) {
            document.getElementById('weatherResult').innerHTML =
                `<p style="color: red;">${data.error}</p>`;
        } else {
            document.getElementById('weatherResult').innerHTML = `
                <h2>Weather in ${data.City}</h2>
                <p>Temperature: ${data.Temperature}</p>
                <p>Description: ${data.Description}</p>
                <p>Humidity: ${data.Humidity}</p>
            `;
        }
    } catch (error) {
        console.error("Error:", error);
        document.getElementById('weatherResult').innerHTML =
            `<p style="color: red;">Failed to fetch weather data.</p>`;
    }
}
