# 🤖 robby

A game about programming battle bots.

## 🛠 Development Setup

Running `docker compose up -d` boots up both an app server Dockerfile and an adminer to view the database.
[Adminer](http://127.0.0.1:8335/?sqlite=&username=admin&db=%2Frobby.db) credentials: `admin/admin`.

Both the local run configuration and the docker compose setup point to the same database file (`/robby.db`).

Note that for the local server you need to first build the shadow jar file with `gradle shadowJar`.
