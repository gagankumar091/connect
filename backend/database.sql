-- Connect App - Full Database Schema (v2)
-- Run migrate_v2.js to add missing columns to existing DBs.

CREATE TABLE IF NOT EXISTS companies (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    descriptor VARCHAR(255),
    avatar_url VARCHAR(1024),
    website VARCHAR(255),
    description TEXT,
    industry VARCHAR(100),
    founded VARCHAR(4),
    headquarters VARCHAR(255),
    employee_range VARCHAR(50),
    employees INT DEFAULT 0,
    funding VARCHAR(50),
    open_deals INT DEFAULT 0,
    recent_news TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    title VARCHAR(255),
    company VARCHAR(255),
    company_id VARCHAR(36),
    avatar_url VARCHAR(1024),
    linkedin VARCHAR(255),
    website VARCHAR(255),
    score INT DEFAULT 50,
    days_since_contact INT DEFAULT 0,
    lat DOUBLE,
    lng DOUBLE,
    fcm_token VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    date VARCHAR(50),
    lat DOUBLE,
    lng DOUBLE,
    attendee_ids TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chats (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    contact_id VARCHAR(36) NOT NULL,
    last_message TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    chat_id VARCHAR(36) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    text TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type ENUM('CONNECTION_REQUEST','NEW_MESSAGE','NEW_EVENT','REMINDER') DEFAULT 'NEW_MESSAGE',
    action_id VARCHAR(36),
    avatar_url VARCHAR(1024),
    due_date DATETIME,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS connections (
    id VARCHAR(36) PRIMARY KEY,
    sender_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS timeline_events (
    id VARCHAR(36) PRIMARY KEY,
    contact_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    subtitle VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(50),
    is_meeting BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample seed data
INSERT IGNORE INTO companies (id, name, descriptor, website, description, industry, founded, headquarters, employee_range, funding) VALUES 
('comp-abc', 'ABC Robotics', 'Robotics · Series A', 'abcrobotics.com', 'Building AI powered robotics for warehouse automation.', 'Robotics, AI', '2022', 'Bangalore, India', '25-50', 'Series A');
