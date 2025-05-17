# Self Progress Tracking Service

A backend service for tracking personal learning progress through structured syllabi and daily activities. The application enables users to create hierarchical learning plans and track their progress over time.

## Technologies

- Spring Boot 3.x (Java 17+)
- PostgreSQL 14+
- JWT Authentication
- Spring Security
- Maven

## Features

- Syllabus management with hierarchical structure (Subject → Topic → SubTopic)
- Daily progress tracking and status updates
- Analytics and reporting (completion rates, time distribution, streaks)
- Resource management for learning materials
- Syllabus sharing with public/private visibility

## Setup Instructions

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 14+
- Docker (optional, for containerized setup)

### Database Configuration

1. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE progress_tracking;
   ```

2. **Default database configuration**
   - URL: `jdbc:postgresql://localhost:5432/progress_tracking`
   - Username: `postgres`
   - Password: `root`

   These settings can be modified in `src/main/resources/application.yml`

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/self-progress-tracking-service.git
   cd self-progress-tracking-service
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Docker Setup

1. **Build the Docker image**
   ```bash
   docker build -t progress-tracking-service .
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

   This will:
   - Use the built Spring Boot application image
   - Start a PostgreSQL database
   - Start pgAdmin for database management
   - Configure all necessary connections

3. **Access the services**
   - API: `http://localhost:8080`
   - pgAdmin: `http://localhost:5050` (Email: `admin@admin.com`, Password: `admin`)

4. **Rebuild the image after changes**
   ```bash
   docker build -t progress-tracking-service .
   docker-compose down
   docker-compose up -d
   ```

### Environment Variables

For production deployment, configure these environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL for database | jdbc:postgresql://localhost:5432/progress_tracking |
| `SPRING_DATASOURCE_USERNAME` | Database username | postgres |
| `SPRING_DATASOURCE_PASSWORD` | Database password | postgres |
| `JWT_SECRET` | Secret key for JWT tokens (32+ chars) | verySecretKeyThatShouldBeAtLeast32CharactersLong |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | dev |

## API Documentation

The application provides a RESTful API with the following main endpoints:

- **Authentication**: `/auth` - Register, login, refresh tokens, verify email
- **Syllabus Management**: `/syllabi`, `/subjects`, `/topics`, `/subtopics`
- **Progress Tracking**: `/progress` - Create, update, and view progress entries
- **Analytics**: `/progress/analytics` - Completion summaries, time distribution, streaks
- **Resources**: `/resources` - Attach and manage learning materials

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the application is running.

## Authentication

The application uses JWT for secure authentication:

1. **Register a new user**
   ```
   POST /auth/register
   {
     "username": "user123",
     "email": "user@example.com",
     "password": "securePassword123"
   }
   ```

2. **Verify email** (required before login)
   ```
   GET /auth/verify-email?token=verification_token_from_email
   ```

3. **Login to get JWT tokens**
   ```
   POST /auth/login
   {
     "username": "user123",
     "password": "securePassword123"
   }
   ```
   Response includes `accessToken` and `refreshToken`

4. **Include token in requests**
   ```
   Authorization: Bearer your_access_token
   ```

5. **Refresh expired access token**
   ```
   POST /auth/refresh-token
   {
     "refreshToken": "your_refresh_token"
   }
   ```

## Testing the API

### Postman Collections

The project includes two Postman collections for testing:

1. **Basic API Collection** - `docs/Self_Progress_Tracking_Service.postman_collection.json`
   - Contains all API endpoints with example requests
   - Includes test scripts to automatically set tokens

2. **30-Day Learning Journey** - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_collection.json`
   - Simulates a realistic 30-day learning journey
   - Demonstrates varying activity levels and progress patterns
   - Organized by weeks with realistic usage scenarios

### Using the Postman Collections

1. Import the collections and environment files into Postman
   - `docs/Self_Progress_Tracking_Service.postman_collection.json`
   - `docs/Self_Progress_Tracking_Service.postman_environment.json`
   - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_collection.json`
   - `docs/Self_Progress_Tracking_Service_30Day_Journey.postman_environment.json`

2. Set the environment variables:
   - `baseUrl`: `http://localhost:8080`

3. Run the "Register" and "Login" requests first to get authentication tokens

4. Test other endpoints which will automatically use the stored tokens

### Testing Syllabus Sharing

1. Create a syllabus with `isPublic: true`
2. Generate a shareable link with `POST /syllabi/{id}/share`
3. Access the shared syllabus without authentication using `GET /syllabi/public/{shareableLink}`
4. Revoke sharing with `DELETE /syllabi/{id}/share`

## Database Schema

The application uses a PostgreSQL database with the following entity relationships:

```
User (1) ----< Syllabus (1) ----< Subject (1) ----< Topic (1) ----< SubTopic
  |                |                 |                |                |
  |                |                 |                |                |
  +----< ProgressEntry              Resource >-------+----------------+
```

### Entity Descriptions

- **User**: Central entity for authentication and ownership
- **Syllabus**: Top-level container owned by a user
- **Subject, Topic, SubTopic**: Hierarchical learning structure
- **ProgressEntry**: Records learning progress for any item
- **Resource**: Learning materials attached to any level

### Database Tables

The schema is automatically created by Hibernate when the application starts with `spring.jpa.hibernate.ddl-auto=update`.

For reference, here's the SQL schema:

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255)
);

CREATE TABLE syllabi (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    shareable_link VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    syllabus_id BIGINT NOT NULL REFERENCES syllabi(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

CREATE TABLE subtopics (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topics(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

CREATE TABLE progress_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    item_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    time_spent_minutes INTEGER,
    notes TEXT
);

CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT REFERENCES subjects(id),
    topic_id BIGINT REFERENCES topics(id),
    subtopic_id BIGINT REFERENCES subtopics(id),
    item_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

## Additional Documentation

- **Database Schema**: See [docs/database_schema.md](docs/database_schema.md) for a detailed description of the database structure
- **Postman Testing Guide**: See [docs/postman_guide.md](docs/postman_guide.md) for instructions on using the Postman collections
- **SQL Schema**: See [schema.sql](schema.sql) for the complete database schema in SQL format

## License

This project is licensed under the MIT License.
