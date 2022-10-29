drop table if exists "games";
create table if not exists "games" (
	"id"	integer,
	"currentRegister"	integer not null,
	"state"	text not null,
	"startedAt"	integer,
	"finishedAt"	integer,
	primary key("id" AUTOINCREMENT)
);
drop table if exists "robots";
create table if not exists "robots" (
	"id"	integer,
	"game_id"	integer not null,
	"session"	text,
	"name"	text not null,
	"ready"	integer not null,
	"model"	text not null,
	"facing"	text not null,
	"damage"	integer not null,
	"poweredDown"	integer not null,
	"powerDownScheduled"	integer not null,
	"passedCheckpoints"	integer not null,
	foreign key("game_id") references "games"("id"),
	primary key("id" AUTOINCREMENT)
);
drop table if exists "movementCards";
create table if not exists "movementCards" (
	"id"	integer,
	"game_id"	integer not null,
	"robot_id"	integer,
	"movement"	text not null,
	"priority"	integer not null,
	"register"	integer,
	foreign key("game_id") references "games"("id"),
	primary key("id" AUTOINCREMENT)
);
drop table if exists "fields";
create table if not exists "fields" (
	"id"	integer,
	"game_id"	integer not null,
	"robot_id"	integer,
	"type"	integer not null,
	"conditions"	integer not null,
	"positionX"	integer not null,
	"positionY"	integer not null,
	"outgoingDirection"	text not null,
	"incomingDirections"	text not null,
	foreign key("game_id") references "games"("id"),
	foreign key("robot_id") references "robots"("id"),
	primary key("id" AUTOINCREMENT)
);
