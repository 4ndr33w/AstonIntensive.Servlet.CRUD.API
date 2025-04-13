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

CREATE TABLE IF NOT EXISTS servlets.projects
(
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        text not null,
    description text,
    created_at  timestamptz default now(),
    updated_at  timestamptz,
    image       bytea default null,
    admin_id    uuid,
    project_status    integer     default 0,
    FOREIGN KEY (admin_id) REFERENCES servlets.users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS servlets.project_users (
    project_id UUID,
    user_id UUID,
	created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (project_id, user_id),
	FOREIGN KEY (project_id) REFERENCES servlets.projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES servlets.users(id) ON DELETE CASCADE
);

/*-------------------------------------------------------*/
CREATE OR REPLACE FUNCTION servlets.update_projects_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_projects_timestamp
BEFORE UPDATE ON servlets.projects
FOR EACH ROW
EXECUTE FUNCTION servlets.update_projects_timestamp();

/*-------------------------------------------------------*/

CREATE OR REPLACE FUNCTION servlets.update_users_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_timestamp
BEFORE UPDATE ON servlets.users
FOR EACH ROW
EXECUTE FUNCTION servlets.update_users_timestamp();

/*-------------------------------------------------------*/
CREATE OR REPLACE FUNCTION servlets.update_project_users_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_projects_timestamp
BEFORE UPDATE ON servlets.project_users
FOR EACH ROW
EXECUTE FUNCTION servlets.update_project_users_timestamp();