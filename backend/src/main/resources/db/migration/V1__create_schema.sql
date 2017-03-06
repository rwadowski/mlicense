-- USERS
CREATE TABLE "users"(
    "id" UUID NOT NULL,
    "login" VARCHAR NOT NULL,
    "login_lowercase" VARCHAR NOT NULL,
    "email" VARCHAR NOT NULL NOT NULL,
    "password" VARCHAR NOT NULL NOT NULL,
    "salt" VARCHAR NOT NULL NOT NULL,
    "created_on" TIMESTAMP NOT NULL
);
ALTER TABLE "users" ADD CONSTRAINT "users_id" PRIMARY KEY("id");