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

#### Restore schema

`schema.sql` contains the current DB schema. Restore it into a local development postgres instance
and make sure you update the environment variables on the application run config accordingly.

#### Update generated database classes

If you made changes to the schema, make sure to run

    maven -f pom.xml -DjooqJdbcUrl=jdbc:postgresql://<dbserver>/<dbname> jooq-codegen:generate

so the generated classes are updated.
