# Chat_App
A production-ready real-time one-to-one chat backend built with Spring Boot, WebSocket (STOMP), JWT authentication, and MySQL database.


# Real-Time Chat Backend - Spring Boot

A production-ready real-time one-to-one chat backend built with Spring Boot, WebSocket (STOMP), JWT authentication, and MySQL database.

## 🚀 Features

- ✅ User Registration & JWT Authentication
- ✅ Real-time messaging with WebSocket/STOMP
- ✅ Online/Offline user status tracking
- ✅ Message persistence in MySQL database
- ✅ Chat history retrieval
- ✅ Unread message tracking
- ✅ Message delivery & read receipts
- ✅ RESTful APIs for chat operations
- ✅ Swagger UI for API testing
- ✅ CORS enabled for frontend integration

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Postman (optional, for WebSocket testing)

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.0
- **WebSocket**: STOMP over WebSocket
- **Security**: Spring Security + JWT
- **Database**: MySQL with JPA/Hibernate
- **API Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd chat-backend
```

### 2. Configure MySQL Database

Create a database in MySQL:
```sql
CREATE DATABASE chat_db;
```

Update database credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chat_db?useSSL=false&serverTimezone=UTC
    username: your_mysql_username
    password: your_mysql_password
```

### 3. Update JWT Secret

Change the JWT secret in `application.yml`:
```yaml
jwt:
  secret: your-secret-key-minimum-256-bits-for-production-use
  expiration: 86400000 # 24 hours
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/chat-backend-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Base URL
```
http://localhost:8080/api
```

## 🔐 Authentication Flow

### 1. Register a New User
**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

### 2. Login
**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response**: Same as registration

### 3. Use JWT Token
Add the token to all subsequent requests:
```
Authorization: Bearer <your_jwt_token>
```

## 💬 REST API Endpoints

### Users

#### Get All Online Users
```
GET /api/users/online
Authorization: Bearer <token>
```

#### Get User by ID
```
GET /api/users/{userId}
Authorization: Bearer <token>
```

#### Get All Users
```
GET /api/users/all
Authorization: Bearer <token>
```

### Messages

#### Send Message
```
POST /api/messages/send
Authorization: Bearer <token>
Content-Type: application/json

{
  "receiverId": 2,
  "content": "Hello, how are you?"
}
```

#### Get Chat History
```
GET /api/messages/history/{userId}
Authorization: Bearer <token>
```

#### Get Unread Message Count
```
GET /api/messages/unread
Authorization: Bearer <token>
```

#### Mark Message as Read
```
PUT /api/messages/{messageId}/read
Authorization: Bearer <token>
```

## 🔌 WebSocket Connection

### Connection URL
```
ws://localhost:8080/ws
```

### Using SockJS (for browsers)
```
http://localhost:8080/ws
```

### Connection with STOMP Client

#### JavaScript Example
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  Authorization: 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to receive messages
  stompClient.subscribe('/user/queue/messages', function(message) {
    const messageData = JSON.parse(message.body);
    console.log('Received:', messageData);
  });
  
  // Subscribe to user status updates
  stompClient.subscribe('/topic/user-status', function(status) {
    const statusData = JSON.parse(status.body);
    console.log('Status update:', statusData);
  });
});

// Send a message
function sendMessage(receiverId, content) {
  stompClient.send('/app/chat', {}, JSON.stringify({
    receiverId: receiverId,
    content: content
  }));
}
```

### WebSocket Events

#### Subscribe Destinations

1. **Receive Personal Messages**
   - Destination: `/user/queue/messages`
   - Description: Receives messages sent to you

2. **User Status Updates**
   - Destination: `/topic/user-status`
   - Description: Broadcasts when users go online/offline

#### Send Destinations

1. **Send Message**
   - Destination: `/app/chat`
   - Payload:
   ```json
   {
     "receiverId": 2,
     "content": "Hello!"
   }
   ```

## 🧪 Testing

### Using Swagger UI

1. Open Swagger UI: `http://localhost:8080/swagger-ui.html`
2. Register a new user using `/api/auth/register`
3. Login using `/api/auth/login` and copy the JWT token
4. Click "Authorize" button at top-right
5. Enter: `Bearer <your_token>`
6. Now you can test all protected endpoints

### Using Postman

#### REST API Testing
1. Create a new request
2. Set method and URL (e.g., `GET http://localhost:8080/api/users/online`)
3. Add header: `Authorization: Bearer <your_token>`
4. Send request

#### WebSocket Testing
1. Create new WebSocket request
2. URL: `ws://localhost:8080/ws`
3. Connect and send STOMP frames

### Using wscat (Command Line)
```bash
# Install wscat
npm install -g wscat

# Connect
wscat -c "ws://localhost:8080/ws"

# Send STOMP CONNECT frame
CONNECT
Authorization:Bearer <your_token>

# Send message
SEND
destination:/app/chat
content-type:application/json

{"receiverId":2,"content":"Hello"}
```

## 📁 Project Structure

```
src/main/java/com/chat/backend/
├── ChatBackendApplication.java       # Main application class
├── config/                           # Configuration classes
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── WebSocketConfig.java         # WebSocket configuration
│   ├── SwaggerConfig.java           # Swagger/OpenAPI configuration
│   └── JwtAuthenticationFilter.java # JWT filter
├── controller/                       # REST controllers
│   ├── AuthController.java          # Authentication endpoints
│   ├── ChatController.java          # Chat & message endpoints
│   └── UserController.java          # User management endpoints
├── model/                            # JPA entities
│   ├── User.java                    # User entity
│   ├── Message.java                 # Message entity
│   ├── ChatRoom.java                # ChatRoom entity
│   └── UserStatus.java              # User status enum
├── dto/                              # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   ├── MessageRequest.java
│   └── MessageResponse.java
├── repository/                       # JPA repositories
│   ├── UserRepository.java
│   ├── MessageRepository.java
│   └── ChatRoomRepository.java
├── service/                          # Business logic
│   ├── AuthService.java
│   ├── UserService.java
│   ├── MessageService.java
│   └── ChatRoomService.java
├── security/                         # Security components
│   ├── JwtUtil.java                 # JWT utility
│   └── CustomUserDetailsService.java
└── websocket/                        # WebSocket handlers
    └── WebSocketEventListener.java  # Connection/disconnection events
```

## 🔒 Security Features

- **Password Encryption**: BCrypt password hashing
- **JWT Authentication**: Stateless authentication with JWT tokens
- **CORS Configuration**: Configurable CORS for frontend integration
- **Session Management**: Stateless session policy
- **Authorization**: Endpoint-level security with Spring Security

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  full_name VARCHAR(255),
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  last_seen DATETIME
);
```

### Messages Table
```sql
CREATE TABLE messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  chat_room_id BIGINT NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  delivered_at DATETIME,
  read_at DATETIME,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (receiver_id) REFERENCES users(id),
  FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id)
);
```

### Chat Rooms Table
```sql
CREATE TABLE chat_rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  chat_id VARCHAR(255) UNIQUE NOT NULL,
  user1_id BIGINT NOT NULL,
  user2_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  FOREIGN KEY (user1_id) REFERENCES users(id),
  FOREIGN KEY (user2_id) REFERENCES users(id)
);
```

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.yml`
- Ensure database `chat_db` exists

### JWT Token Errors
- Ensure token is properly formatted: `Bearer <token>`
- Check if token has expired (24h by default)
- Verify JWT secret is set correctly

### WebSocket Connection Issues
- Check if port 8080 is available
- Verify firewall settings
- Try using SockJS fallback

## 🚀 Deployment

### Application Properties for Production
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create schema in production

jwt:
  secret: ${JWT_SECRET}  # Use environment variable
  expiration: 86400000

server:
  port: ${PORT:8080}
```

### Building for Production
```bash
# Build JAR
mvn clean package -DskipTests

# Run
java -jar target/chat-backend-1.0.0.jar
```

## 📝 API Response Examples

### Successful Message Send
```json
{
  "id": 15,
  "senderId": 1,
  "senderUsername": "john_doe",
  "receiverId": 2,
  "receiverUsername": "jane_smith",
  "content": "Hello! How are you?",
  "isRead": false,
  "createdAt": "2024-12-24T10:30:00",
  "deliveredAt": null,
  "readAt": null
}
```

### User Status Update (WebSocket)
```json
{
  "userId": 2,
  "status": "ONLINE"
}
```

## 📄 License

This project is open-source and available under the MIT License.

## 👥 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 Contact

For questions or support, please open an issue in the GitHub repository.

---

**Built with ❤️ using Spring Boot**
