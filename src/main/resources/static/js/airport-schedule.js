// API Configuration
const API_BASE = '/api/airlab';

// Cache for data
let countriesCache = [];
let citiesCache = {};
let selectedCountry = null;
let selectedAirport = null;

// DOM Elements
const countrySelect = document.getElementById('countrySelect');
const airportSelect = document.getElementById('airportSelect');
const scheduleType = document.getElementById('scheduleType');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const loadingSection = document.getElementById('loadingSection');
const resultsSection = document.getElementById('resultsSection');
const schedulesList = document.getElementById('schedulesList');
const scheduleCount = document.getElementById('scheduleCount');

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadCountries();
});

// Load countries
async function loadCountries() {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/countries`);
        
        if (!response.ok) {
            throw new Error('Failed to load countries');
        }

        countriesCache = await response.json();
        console.log('Countries loaded:', countriesCache);
        populateCountrySelect(countriesCache);
        showError('');
    } catch (error) {
        console.error('Error loading countries:', error);
        showError('Failed to load countries. Please refresh the page.');
    } finally {
        showLoading(false);
    }
}

// Populate country dropdown
function populateCountrySelect(countries) {
    if (!countries || countries.length === 0) {
        countrySelect.innerHTML = '<option value="">-- No Countries Available --</option>';
        return;
    }

    const options = countries.map(country => {
        const code = country.code || '';
        const name = country.name || 'Unknown';
        return `<option value="${code}">${name} (${code})</option>`;
    }).join('');
    
    countrySelect.innerHTML = '<option value="">-- Choose Country --</option>' + options;
}

// Handle country selection
countrySelect.addEventListener('change', async (e) => {
    selectedCountry = e.target.value;
    airportSelect.disabled = !selectedCountry;
    airportSelect.innerHTML = '<option value="">-- Loading Airports --</option>';
    resultsSection.classList.remove('active');
    
    if (!selectedCountry) {
        return;
    }

    await loadAirportsByCountry(selectedCountry);
});

// Load airports by country
async function loadAirportsByCountry(countryCode) {
    try {
        showLoading(true);
        const response = await fetch(`${API_BASE}/airports/by-country/${countryCode}`);
        
        if (!response.ok) {
            throw new Error('Failed to load airports');
        }

        const data = await response.json();
        console.log('Airports loaded:', data);
        const airports = data.data || [];
        populateAirportSelect(airports);
        showError('');
    } catch (error) {
        console.error('Error loading airports:', error);
        showError('Failed to load airports for selected country.');
        airportSelect.innerHTML = '<option value="">-- No Airports Available --</option>';
        airportSelect.disabled = true;
    } finally {
        showLoading(false);
    }
}

// Populate airport dropdown
function populateAirportSelect(airports) {
    if (!airports || airports.length === 0) {
        airportSelect.innerHTML = '<option value="">-- No Airports Available --</option>';
        airportSelect.disabled = true;
        return;
    }

    const options = airports.map(airport => {
        const iataCode = airport.iataCode || airport.icaoCode || 'N/A';
        const airportName = airport.name || 'Unknown';
        return `<option value="${iataCode}">${airportName} (${iataCode})</option>`;
    }).join('');
    
    airportSelect.innerHTML = '<option value="">-- Choose Airport --</option>' + options;
    airportSelect.disabled = false;
}

// Handle airport selection
airportSelect.addEventListener('change', (e) => {
    selectedAirport = e.target.value;
    resultsSection.classList.remove('active');
});

// Search schedules
async function searchSchedules() {
    if (!selectedAirport) {
        showError('Please select an airport first.');
        return;
    }

    try {
        showLoading(true);
        showError('');

        const type = scheduleType.value;
        let url;

        if (type === 'departure') {
            url = `${API_BASE}/schedules/departure/${selectedAirport}`;
        } else {
            url = `${API_BASE}/schedules/arrival/${selectedAirport}`;
        }

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('Failed to load schedules');
        }

        const data = await response.json();
        displaySchedules(data.data || [], type);
        showSuccess(`Found ${data.count} flights for ${selectedAirport}`);
    } catch (error) {
        console.error('Error loading schedules:', error);
        showError('Failed to load flight schedules. Please try again.');
        resultsSection.classList.remove('active');
    } finally {
        showLoading(false);
    }
}

// Display schedules
function displaySchedules(schedules, type) {
    scheduleCount.textContent = schedules.length;

    if (!schedules || schedules.length === 0) {
        schedulesList.innerHTML = `
            <div class="empty">
                <div class="empty-icon">📭</div>
                <p>No flights found for selected airport</p>
            </div>
        `;
        resultsSection.classList.add('active');
        return;
    }

    const tableHTML = `
    <div class="table-wrapper">
        <table>
            <thead>
                <tr>
                    <th>Flight Code</th>
                    <th>Airline</th>
                    <th>From</th>
                    <th>To</th>
                    <th>${type === 'departure' ? 'Departure' : 'Arrival'}</th>
                    <th>Duration</th>
                </tr>
            </thead>
            <tbody>
                ${schedules.map(flight => {
                // Sửa: Dùng tên trường snake_case như trong @JsonProperty
                const timeField = type === 'departure'
                    ? flight.dep_time   // Đã đúng
                    : flight.arr_time;  // Đã đúng

                return `
                    <tr>
                        <!-- SỬA CÁC DÒNG DƯỚI ĐÂY -->
                        <td><span class="flight-code">${flight.flight_iata || 'N/A'}</span></td>
                        <td>${flight.airline_iata || 'N/A'}</td>
                        <td><span class="airport-code">${flight.dep_iata || 'N/A'}</span></td>
                        <td><span class="airport-code">${flight.arr_iata || 'N/A'}</span></td>
                        <td><span class="time">${timeField || 'N/A'}</span></td>
                        <td>${flight.duration ? `<span class="duration">${flight.duration} min</span>` : 'N/A'}</td>
                    </tr>
                `;
            }).join('')}
            </tbody>
        </table>
    </div>
`;

    schedulesList.innerHTML = tableHTML;
    resultsSection.classList.add('active');
    resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Show/hide loading
function showLoading(show) {
    if (show) {
        loadingSection.classList.add('active');
    } else {
        loadingSection.classList.remove('active');
    }
}

// Show error message
function showError(message) {
    if (message) {
        errorMessage.textContent = '❌ ' + message;
        errorMessage.classList.add('active');
    } else {
        errorMessage.classList.remove('active');
    }
}

// Show success message
function showSuccess(message) {
    if (message) {
        successMessage.textContent = '✅ ' + message;
        successMessage.classList.add('active');
        setTimeout(() => {
            successMessage.classList.remove('active');
        }, 5000);
    }
}
