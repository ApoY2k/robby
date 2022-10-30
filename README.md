# 🤖 robby

## 🛠 Development Setup

### Requirements

* Kotlin
* JVM 17
* Maven

Remaining required libraries and toolings can be installed through maven with `maven install`.

### Running in dev mode

Run `ApplicationKt` module and adjust environment variables where necessary.

To auto-redeploy, use `-Dio.ktor.development=true` as VM option.

### 🗃 Database

* Run `database_schema.sql` with sqlite to create a local databse file
* Set `DATABASE_PATH` env-var to the file path when running pointing to this file
