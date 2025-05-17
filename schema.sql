-- Self Progress Tracking Service Database Schema
-- This file can be used to manually initialize the database structure

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS progress_entries;
DROP TABLE IF EXISTS subtopics;
DROP TABLE IF EXISTS topics;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS syllabi;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255)
);

-- Create syllabi table
CREATE TABLE syllabi (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    shareable_link VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create subjects table
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    syllabus_id BIGINT NOT NULL REFERENCES syllabi(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

-- Create topics table
CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

-- Create subtopics table
CREATE TABLE subtopics (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL,
    target_completion_date DATE
);

-- Create progress_entries table
CREATE TABLE progress_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    time_spent_minutes INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create resources table
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT REFERENCES subjects(id) ON DELETE CASCADE,
    topic_id BIGINT REFERENCES topics(id) ON DELETE CASCADE,
    subtopic_id BIGINT REFERENCES subtopics(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_syllabi_user_id ON syllabi(user_id);
CREATE INDEX idx_subjects_syllabus_id ON subjects(syllabus_id);
CREATE INDEX idx_topics_subject_id ON topics(subject_id);
CREATE INDEX idx_subtopics_topic_id ON subtopics(topic_id);
CREATE INDEX idx_progress_entries_user_id ON progress_entries(user_id);
CREATE INDEX idx_progress_entries_item_id_type ON progress_entries(item_id, item_type);
CREATE INDEX idx_resources_item_types ON resources(subject_id, topic_id, subtopic_id);

-- Add constraints to ensure only one parent is set for resources
ALTER TABLE resources ADD CONSTRAINT check_resource_parent 
CHECK (
    (subject_id IS NOT NULL AND topic_id IS NULL AND subtopic_id IS NULL) OR
    (subject_id IS NULL AND topic_id IS NOT NULL AND subtopic_id IS NULL) OR
    (subject_id IS NULL AND topic_id IS NULL AND subtopic_id IS NOT NULL)
);

-- Sample admin user (password: admin123)
-- INSERT INTO users (username, email, password, role, email_verified)
-- VALUES ('admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', true);
