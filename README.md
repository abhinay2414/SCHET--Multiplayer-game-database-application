# SCHET--Multiplayer-game-database-application
To input basic data of an multiplayer game result to make analysis easy.

## Overview

SCHET is a multiplayer game database application developed using Java, JavaFX, JDBC, MySQL, and Maven. It is designed to simplify the management of multiplayer game data by providing an intuitive desktop interface for storing and maintaining player information, match records, and leaderboard rankings. The application integrates a JavaFX-based frontend with a MySQL backend, ensuring efficient data management and a seamless user experience.

The system allows users to add and manage player profiles, record match results, and automatically update leaderboard rankings through MySQL triggers. By combining object-oriented programming principles with relational database management, SCHET demonstrates how desktop applications can efficiently interact with databases to automate game data processing while maintaining data integrity and consistency.

# What Does SCHET Do?
SCHET provides a centralized system for managing multiplayer game data. Instead of manually maintaining player records and scores, the application automates these tasks through a graphical desktop interface connected to a relational database.

The application allows users to:
- Register and manage player profiles.
- Record match results.
- Maintain player inventories and achievements.
- Track player experience and levels.
- Automatically calculate leaderboard rankings.
- Store and retrieve game data efficiently from MySQL.

# How Does It Work?
SCHET follows a three-layer architecture consisting of the presentation layer, application layer, and database layer.

The JavaFX interface allows users to interact with the application through forms and tables. User actions such as adding a player or recording a match are processed by the Java backend, which communicates with the MySQL database using JDBC. All player information, match records, inventories, achievements, and leaderboard data are stored within the database.

Whenever a new match result is inserted, a MySQL trigger automatically recalculates the player's total score and updates the leaderboard. This removes the need for manual score calculations while ensuring rankings remain accurate at all times.

# Technologies Used
| Technology | Purpose |
|------------|---------|
| Java 17 | Core application development |
| JavaFX | Desktop graphical user interface |
| JDBC | Database connectivity |
| MySQL | Relational database management |
| Maven | Dependency management and project build |
| SQL | Database queries, procedures, views and triggers |

# Project Architecture

```
User
   │
   ▼
JavaFX User Interface
   │
   ▼
Controllers
   │
   ▼
JDBC
   │
   ▼
MySQL Database
```
# Database Components
The application stores data across multiple relational tables.

- Players
- Matches
- MatchResults
- Leaderboard
- Items
- Inventory
- Achievements
- PlayerAchievements

The database also includes:

- Primary Keys
- Foreign Keys
- Views
- Stored Procedures
- Triggers
- Indexes

These components ensure data integrity while improving query performance and automating leaderboard updates.

---

# Project Structure

```
SCHET
│
├── src
│   ├── main
│   │   ├── java
│   │   │
│   │   ├── controller
│   │   │      Handles user interactions
│   │   │
│   │   ├── dao
│   │   │      Performs database operations
│   │   │
│   │   ├── model
│   │   │      Represents application entities
│   │   │
│   │   ├── util
│   │   │      Database connection utilities
│   │   │
│   │   └── Main.java
│   │
│   └── resources
│       ├── fxml
│       │      JavaFX layouts
│       ├── css
│       │      Styling
│       └── images
│              Application assets
│
├── database
│   └── MULTIGAMEDB.sql
│
├── pom.xml
│
└── README.md
```
# Key Features

- Player Management
- Match Management
- Achievement Tracking
- Inventory Management
- Automatic Leaderboard Updates
- Database Automation using Triggers
- Modern JavaFX Desktop Interface
- Efficient MySQL Data Storage

# How to Run

1. Clone this repository.
2. Import the project as a Maven project.
3. Create the MySQL database.
4. Import the provided SQL file.
5. Update the JDBC database credentials.
6. Build the project using Maven.
7. Run the JavaFX application.

# Future Improvements
These improvements can be made but since this project has single aim to make understanding of JDBC and DBMS ,thus the already present features are enough. But still the following are the best improvements for the project.
- User authentication
- Match analytics dashboard
- Charts and statistics
- Cloud database support
- REST API integration
- Online multiplayer synchronization

# License

This project was developed for educational purposes and demonstrates the integration of Java desktop development with relational database management.
