DROP SCHEMA IF EXISTS servlets CASCADE;
DROP TABLE IF EXISTS servlets.users CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS servlets;
CREATE TABLE IF NOT EXISTS servlets.users
(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_name     text unique not null,
    first_name    text,
    last_name     text,
    email         text unique not null,
    password      text        not null,
    phone         text,
    created_at    timestamptz default now(),
    updated_at    timestamptz,
    image         bytea default null,
    last_login_date timestamptz,
    userstatus    integer     default 0
);
