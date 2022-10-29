BEGIN TRANSACTION;
DROP TABLE IF EXISTS "movementCards";
CREATE TABLE IF NOT EXISTS "movementCards" (
	"id"	INTEGER,
	"game_id"	INTEGER NOT NULL,
	"robot_id"	INTEGER,
	"type"	TEXT NOT NULL,
	"priority"	INTEGER NOT NULL,
	"register"	INTEGER,
	FOREIGN KEY("player_id") REFERENCES "players"("id"),
	FOREIGN KEY("game_id") REFERENCES "games"("id"),
	PRIMARY KEY("id" AUTOINCREMENT)
);
DROP TABLE IF EXISTS "robots";
CREATE TABLE IF NOT EXISTS "robots" (
	"id"	INTEGER,
	"game_id"	INTEGER NOT NULL,
	"session"	TEXT,
	"name"	TEXT NOT NULL,
	"ready"	INTEGER NOT NULL,
	"model"	TEXT NOT NULL,
	"facing"	TEXT NOT NULL,
	"damage"	INTEGER NOT NULL,
	"poweredDown"	INTEGER NOT NULL,
	"powerDownScheduled"	INTEGER NOT NULL,
	"passedCheckpoints"	INTEGER NOT NULL,
	FOREIGN KEY("game_id") REFERENCES "games"("id"),
	FOREIGN KEY("player_id") REFERENCES "players"("id"),
	FOREIGN KEY("field_id") REFERENCES "fields"("id"),
	PRIMARY KEY("id" AUTOINCREMENT)
);
DROP TABLE IF EXISTS "fields";
CREATE TABLE IF NOT EXISTS "fields" (
	"id"	INTEGER,
	"game_id"	INTEGER NOT NULL,
	"robot_id"	INTEGER,
	"type"	INTEGER NOT NULL,
	"conditions"	INTEGER NOT NULL,
	"positionX"	INTEGER NOT NULL,
	"positionY"	INTEGER NOT NULL,
	"outgoingDirection"	TEXT NOT NULL,
	"incomingDirections"	TEXT NOT NULL,
	FOREIGN KEY("game_id") REFERENCES "games"("id"),
	FOREIGN KEY("robot_id") REFERENCES "robots"("id"),
	PRIMARY KEY("id" AUTOINCREMENT)
);
DROP TABLE IF EXISTS "games";
CREATE TABLE IF NOT EXISTS "games" (
	"id"	INTEGER,
	"currentRegister"	INTEGER NOT NULL,
	"state"	TEXT NOT NULL,
	"startedAt"	INTEGER,
	"finishedAt"	INTEGER,
	PRIMARY KEY("id" AUTOINCREMENT)
);
COMMIT;
