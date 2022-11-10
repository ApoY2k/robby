# 🤖 robby

# ▶ Run Setup

* Create an empty file named `robby.db`
* Run `docker compose up --build`
* Navigate to `<server-ip>:63378`

## 🛠 Development Setup

### Requirements

* Kotlin
* JVM 17
* Maven

Remaining required libraries and toolings can be installed through maven with `maven install`.

### 🗃 Database

* Run `database_schema.sql` with sqlite to create a local databse file
* Set `DATABASE_PATH` env-var to the file path when running pointing to this file
