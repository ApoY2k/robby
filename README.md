# 🤖 robby

## 🛠 Development Setup

### Requirements

* Kotlin
* JVM 17
* Maven

Remainging required libraries and toolings can be installed through maven with `maven install`.

### Running in dev mode

Run `ApplicationKt` module and adjust environment variables where necessary.

To auto-redeploy, use `-Dio.ktor.development=true` as VM option.

### 🗃 Database

`database_schema.sql` contains the current DB schema. Run it with sqlite to create a local development db. Set
the `DATABASE_PATH` env-var when running pointing to this file.
