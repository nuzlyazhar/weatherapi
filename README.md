# Weather API Service Design and Implementation

## Introduction

## Potential Improvements and Future Enhancements
Given more time, the following additions and improvements could be made to further enhance the application:

### Enhanced Test Coverage:
- Add env specific properties and use spring profiles

### Enhanced Test Coverage:
- Add integration tests, using SpringBoot support for Testcontainers (Use @ServiceConnection, @GenericContainers)
- Add more edge case scenarios in unit tests, mainly for rate limiting and API key validation.
- Add Performence test

### Caching Mechanism:
- use something like Redis for cache
### API Documentation:
- Use OpenAPI for docs

### Logging and Monitoring:
- Implement comprehensive logging throughout the application for better debugging and monitoring @slf4j.
- Instrument with open telemetry to publish metrics, traces and logs.

### Security Enhancements:
- Implement HTTPS to ensure secure communication.
- Add  authentication

### Containerization:
- Dockerize the application for easier deployment and scaling. (JIB)

### Make more Configurable :
- Make rate limiting parameters configurable without code changes. 
- Make RestTemplate configurable, even explore the latest RestClient in spring.

### Better Exception handling:
- Add exception handling

### Historical Data:
- Implement functionality to retrieve and store historical weather data.