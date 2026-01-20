# SkyMaster Hub - AirLab API Integration

## Mô tả
Ứng dụng Spring Boot để tích hợp với AirLab API, lấy dữ liệu về sân bay, hãng hàng không và tuyến bay, sau đó lưu vào MySQL database.

## Cấu hình

### Database Configuration (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/platform
spring.datasource.username=root
spring.datasource.password=
```

### AirLab API Configuration
```properties
airlab.api.key=5243c38e-2c82-49dc-9886-967821bc7544
airlab.api.base-url=https://airlabs.co/api/v9/
```

## Cài đặt

1. Đảm bảo MySQL đang chạy trên localhost:3306
2. Tạo database tên `platform`:
```sql
CREATE DATABASE platform;
```

3. Chạy ứng dụng:
```bash
./mvnw spring-boot:run
```

## API Endpoints

### 1. Fetch Data từ AirLab API và Lưu vào Database

#### Fetch Airports
```
GET http://localhost:8081/api/airlab/fetch/airports
```
Lấy danh sách sân bay từ AirLab API và lưu vào database.

#### Fetch Airlines
```
GET http://localhost:8081/api/airlab/fetch/airlines
```
Lấy danh sách hãng hàng không từ AirLab API và lưu vào database.

#### Fetch Routes
```
GET http://localhost:8081/api/airlab/fetch/routes
```
Lấy danh sách tuyến bay từ AirLab API và lưu vào database.

#### Fetch All Data
```
POST http://localhost:8081/api/airlab/fetch/all
```
Lấy tất cả dữ liệu (airports, airlines, routes) từ AirLab API và lưu vào database.

### 2. Lấy Data từ Database

#### Get All Airports
```
GET http://localhost:8081/api/airlab/airports
```
Lấy tất cả sân bay từ database.

#### Get All Airlines
```
GET http://localhost:8081/api/airlab/airlines
```
Lấy tất cả hãng hàng không từ database.

#### Get All Routes
```
GET http://localhost:8081/api/airlab/routes
```
Lấy tất cả tuyến bay từ database.

#### Get Airport by ICAO Code
```
GET http://localhost:8081/api/airlab/airports/{icaoCode}
```
Ví dụ: `GET http://localhost:8081/api/airlab/airports/VVNB`

#### Get Airline by IATA Code
```
GET http://localhost:8081/api/airlab/airlines/{iataCode}
```
Ví dụ: `GET http://localhost:8081/api/airlab/airlines/VN`

#### Get Routes by Airline
```
GET http://localhost:8081/api/airlab/routes/airline/{airlineIata}
```
Ví dụ: `GET http://localhost:8081/api/airlab/routes/airline/VN`

#### Get Routes by Departure Airport
```
GET http://localhost:8081/api/airlab/routes/departure/{departureIcao}
```
Ví dụ: `GET http://localhost:8081/api/airlab/routes/departure/VVNB`

#### Get Routes by Arrival Airport
```
GET http://localhost:8081/api/airlab/routes/arrival/{arrivalIcao}
```
Ví dụ: `GET http://localhost:8081/api/airlab/routes/arrival/VVTS`

## Cấu trúc Database

### Table: airports
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `icao_code` (VARCHAR)
- `name` (VARCHAR)
- `lat` (DOUBLE)
- `lng` (DOUBLE)

### Table: airlines
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `iata_code` (VARCHAR)
- `name` (VARCHAR)
- `country` (VARCHAR)

### Table: routes
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `airline_iata` (VARCHAR)
- `departure_icao` (VARCHAR)
- `arrival_icao` (VARCHAR)

## Testing với Postman hoặc cURL

### Ví dụ 1: Fetch và lưu tất cả dữ liệu
```bash
curl -X POST http://localhost:8081/api/airlab/fetch/all
```

### Ví dụ 2: Lấy danh sách sân bay
```bash
curl http://localhost:8081/api/airlab/airports
```

### Ví dụ 3: Tìm kiếm sân bay theo ICAO code
```bash
curl http://localhost:8081/api/airlab/airports/VVNB
```

## Lưu ý
- Đảm bảo API key của AirLab còn hiệu lực
- Database sẽ tự động tạo các bảng khi chạy ứng dụng lần đầu (JPA auto-create)
- Security đã được tắt cho việc testing (thay đổi trong SecurityConfig.java nếu cần)
