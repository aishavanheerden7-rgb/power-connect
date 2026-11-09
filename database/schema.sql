-- PowerConnect database (MySQL 8)
-- Run in MySQL Workbench, or: mysql -ustudent -p < database/schema.sql
-- Running it again wipes everything and starts fresh.

DROP DATABASE IF EXISTS powerconnect;
CREATE DATABASE powerconnect;
USE powerconnect;

-- ---------- Lookup tables (3NF) ----------
-- Each allowed value is stored once here. The other tables reference these codes with foreign keys,
-- so a misspelt value such as 'ELECTRICTY' is rejected by the database.

CREATE TABLE roles (
    code VARCHAR(20)  PRIMARY KEY,                  -- ADMIN, TECHNICIAN, COUNCILLOR, COMMUNITY
    name VARCHAR(50)  NOT NULL
);

CREATE TABLE service_types (
    code VARCHAR(20)  PRIMARY KEY,                  -- WATER, ELECTRICITY, SANITATION
    name VARCHAR(50)  NOT NULL
);

CREATE TABLE incident_statuses (
    code       VARCHAR(20) PRIMARY KEY,             -- REPORTED, ASSIGNED, ...
    name       VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL                         -- order in the lifecycle
);

CREATE TABLE areas (
    name VARCHAR(50) PRIMARY KEY                    -- Lusaka areas
);

-- The problems people can report for each service. The key has two columns because the same name
-- ('Other') exists under several services, so a category name alone does not decide the service.
CREATE TABLE categories (
    service_type VARCHAR(20) NOT NULL,
    name         VARCHAR(50) NOT NULL,
    PRIMARY KEY (service_type, name),
    FOREIGN KEY (service_type) REFERENCES service_types(code)
);

INSERT INTO roles (code, name) VALUES
('ADMIN', 'Admin'), ('TECHNICIAN', 'Technician'), ('COUNCILLOR', 'Councillor'), ('COMMUNITY', 'Community member');

INSERT INTO service_types (code, name) VALUES
('WATER', 'Water'), ('ELECTRICITY', 'Electricity'), ('SANITATION', 'Sanitation');

INSERT INTO incident_statuses (code, name, sort_order) VALUES
('REPORTED', 'Reported', 1), ('ASSIGNED', 'Technician assigned', 2), ('DISPATCHED', 'Technician on the way', 3),
('IN_PROGRESS', 'Work in progress', 4), ('COMPLETED', 'Work completed', 5), ('NOT_COMPLETED', 'Not completed', 6),
('REOPENED', 'Reopened', 7), ('CLOSED', 'Closed', 8);

INSERT INTO areas (name) VALUES
('Matero'), ('Kabulonga'), ('Chilenje'), ('Kabwata'), ('Chawama'), ('Kanyama'),
('Mtendere'), ('Woodlands'), ('Garden'), ('Chelston'), ('Kalingalinga'), ('Northmead');

INSERT INTO categories (service_type, name) VALUES
('WATER', 'No water'), ('WATER', 'Burst pipe'), ('WATER', 'Dirty water'), ('WATER', 'Low pressure'), ('WATER', 'Other'),
('ELECTRICITY', 'Power outage'), ('ELECTRICITY', 'Fallen cable'), ('ELECTRICITY', 'Faulty meter'), ('ELECTRICITY', 'Street light'), ('ELECTRICITY', 'Other'),
('SANITATION', 'Blocked sewer'), ('SANITATION', 'Rubbish not collected'), ('SANITATION', 'Public toilet'), ('SANITATION', 'Other');

-- ---------- Main tables ----------

-- Everyone who can log in. "role" decides which screens they see.
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_code     VARCHAR(10)  NOT NULL UNIQUE,     -- shown on screen, e.g. ADM-001, TEC-001, CLR-001, CM-001
    role          VARCHAR(20)  NOT NULL,            -- ADMIN, TECHNICIAN, COUNCILLOR or COMMUNITY
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    area          VARCHAR(50),                      -- Lusaka area they live or work in (a councillor's ward)
    service_type  VARCHAR(20),                      -- technicians only: WATER, ELECTRICITY or SANITATION
    language      VARCHAR(3)   NOT NULL DEFAULT 'en', -- en (English), bem (Bemba), ny (Nyanja)
    password_hash VARCHAR(255) NOT NULL,            -- BCrypt hash, never the plain password
    token         VARCHAR(64),                      -- set at login, cleared at logout
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role)         REFERENCES roles(code),
    FOREIGN KEY (area)         REFERENCES areas(name),
    FOREIGN KEY (service_type) REFERENCES service_types(code)
);

-- A problem reported by a community member
CREATE TABLE incidents (
    incident_id   INT AUTO_INCREMENT PRIMARY KEY,
    reported_by   INT NOT NULL,                     -- the community member
    technician_id INT NULL,                         -- NULL until the admin assigns someone
    service_type  VARCHAR(20)  NOT NULL,            -- WATER, ELECTRICITY or SANITATION
    category      VARCHAR(50)  NOT NULL,            -- e.g. Power outage, Burst pipe, Blocked sewer
    area          VARCHAR(50)  NOT NULL,            -- Lusaka area, e.g. Matero
    address       VARCHAR(255),                     -- street / landmark
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'REPORTED',
        -- REPORTED -> ASSIGNED -> DISPATCHED -> IN_PROGRESS -> COMPLETED or NOT_COMPLETED -> CLOSED
        -- (COMPLETED but still broken -> REOPENED -> the admin assigns again)
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at  TIMESTAMP NULL,                   -- when the technician finished the work
    closed_at     TIMESTAMP NULL,                   -- when the incident was closed
    rating        INT NULL,                         -- 1 to 5 stars from the community member when they close it
    FOREIGN KEY (reported_by)   REFERENCES users(user_id),
    FOREIGN KEY (technician_id) REFERENCES users(user_id),
    FOREIGN KEY (service_type, category) REFERENCES categories(service_type, name),
    FOREIGN KEY (area)          REFERENCES areas(name),
    FOREIGN KEY (status)        REFERENCES incident_statuses(code)
);

-- History: one row every time an incident's status changes (this gives us the timestamps)
CREATE TABLE incident_updates (
    update_id     INT AUTO_INCREMENT PRIMARY KEY,
    incident_id   INT NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    note          VARCHAR(255),
    updated_by    INT NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id),
    FOREIGN KEY (updated_by)  REFERENCES users(user_id),
    FOREIGN KEY (status)      REFERENCES incident_statuses(code)
);

-- In-app alerts: community members (updates), technicians (new jobs), admins (new, reopened, finished,
-- closed incidents and messages), councillors (new incidents in their ward)
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,                   -- who receives it
    incident_id     INT NOT NULL,
    status          VARCHAR(20)  NOT NULL,          -- the app shows this in the user's language
    message         VARCHAR(255) NOT NULL,          -- English text, e.g. for Swagger / the database
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(user_id),
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id)
);

-- ---------- Phase 2 ----------

-- Messages written on an incident by the community member, technician, admin or councillor
CREATE TABLE messages (
    message_id  INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    sender_id   INT NOT NULL,
    body        VARCHAR(500) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id),
    FOREIGN KEY (sender_id)   REFERENCES users(user_id)
);

-- Duplicate detection: other community members who said "yes, I have the same problem".
-- They follow the incident and get the same alerts as the person who reported it.
CREATE TABLE incident_followers (
    follower_id INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    user_id     INT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (incident_id, user_id),                  -- you can only follow an incident once
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id),
    FOREIGN KEY (user_id)     REFERENCES users(user_id)
);

-- Photos of the problem (or of the finished work), stored as base64 text:
-- "data:image/jpeg;base64,/9j/4AAQ..." - the app can show this text directly as an image.
-- A separate table so that listing incidents does not load all the photos.
CREATE TABLE incident_photos (
    photo_id    INT AUTO_INCREMENT PRIMARY KEY,
    incident_id INT NOT NULL,
    uploaded_by INT NOT NULL,
    image       LONGTEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incidents(incident_id),
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

-- Who to contact: service hotlines (area is NULL) and the councillor for each Lusaka area
CREATE TABLE contacts (
    contact_id   INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    title        VARCHAR(100) NOT NULL,             -- e.g. 'Water and sewer hotline', 'Ward councillor'
    service_type VARCHAR(20),                       -- WATER, ELECTRICITY, SANITATION, or NULL
    area         VARCHAR(50),                       -- the councillor's area, NULL for hotlines
    phone        VARCHAR(20) NOT NULL,
    email        VARCHAR(100),
    FOREIGN KEY (service_type) REFERENCES service_types(code),
    FOREIGN KEY (area)         REFERENCES areas(name)
);

-- Questions and answers for the chat bot
CREATE TABLE faqs (
    faq_id     INT AUTO_INCREMENT PRIMARY KEY,
    keywords   VARCHAR(255) NOT NULL,               -- e.g. 'pipe,burst,leak': if the message contains one of these...
    answer_en  TEXT NOT NULL,                       -- ...reply with the answer in their language
    answer_bem TEXT NOT NULL,
    answer_ny  TEXT NOT NULL
);
