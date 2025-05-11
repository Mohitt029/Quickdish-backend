Swiggy Backend 🍕🚀
Welcome to the Swiggy Backend, a Spring Boot-based API for a food delivery system. This project provides a robust backend for managing users, restaurants, orders, payments, deliveries, and more. Built with Java, Spring Boot, and MongoDB, it offers a scalable and efficient solution for a food delivery platform.
🌟 Features

User Management: Register, update, and delete users.
Restaurant & Menu: Browse restaurants, menus, and filter menu items by cuisine or meal type.
Cart & Orders: Add items to a cart, place orders, and track order status.
Payments: Record and validate payments for orders.
Delivery Management: Assign delivery boys to orders and fetch delivery details.
Coupons: Create, apply, and manage coupons for discounts.
Liked Items: Allow users to like menu items and retrieve their liked items.

🛠️ Tech Stack

Backend: Java, Spring Boot
Database: MongoDB
Dependency Management: Maven
Logging: SLF4J
Validation: Jakarta Validation
Version Control: Git

📦 Project Structure
Swiggy-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── swiggy_project/
│   │   │               ├── controller/    # REST controllers (e.g., UserController)
│   │   │               ├── exception/     # Custom exceptions (e.g., ResourceNotFoundException)
│   │   │               ├── model/         # Data models (e.g., Order, Payment)
│   │   │               ├── repository/    # MongoDB repositories (e.g., OrderRepository)
│   │   │               ├── service/       # Business logic (e.g., PaymentService)
│   │   │               └── SwiggyProjectApplication.java
│   │   └── resources/
│   │       └── application.properties    # Configuration (excluded from Git)
├── .gitignore                            # Git ignore file
├── pom.xml                               # Maven dependencies
└── README.md                             # Project documentation

