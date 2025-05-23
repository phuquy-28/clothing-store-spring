# Fashion E-commerce Backend

## Introduction
This is the backend for a fashion e-commerce project, built with Spring Boot. It provides APIs to support the functionalities of a fashion e-commerce website.

## Technologies Used
- Spring Boot
- MySQL
- Docker
- Swagger (API Documentation)

## System Requirements
- Java Development Kit (JDK) 17 or later
- Docker
- MySQL

## Installation and Running

### Using Docker
1. Clone the repository:
   ```
   git clone [repository URL]
   ```
2. Navigate to the project directory:
   ```
   cd [project directory name]
   ```
3. Build and run Docker containers:
   ```
   docker-compose up --build
   ```

The application will be accessible at `http://localhost:8080`.

Note: The Docker setup uses environment variables defined in the docker-compose.yml file.

### Running Locally
1. Ensure MySQL is installed and running locally
2. Clone the repository:
   ```
   git clone [repository URL]
   ```
3. Navigate to the project directory:
   ```
   cd [project directory name]
   ```
4. Run the application:
   ```
   ./gradlew bootRun
   ```

The application will now be accessible at `http://localhost:8080`.

Note: When running locally, make sure to configure your database connection settings in the application.properties or application.yml file.

## API Documentation
API documentation is automatically generated using Swagger. After running the application, access:
```
http://localhost:8080/swagger-ui.html
```

## Third-party Integrations
- VNPay: Online payment
- Google: Login and email sending

## Testing
The project includes unit tests and integration tests. To run tests:
```
./gradlew test
```