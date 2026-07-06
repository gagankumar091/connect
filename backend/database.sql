-- Copy and paste this into your MySQL Database (e.g., Aiven) to set up the tables!

CREATE TABLE IF NOT EXISTS contacts (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    company VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO contacts (id, name, email, company) VALUES 
(UUID(), 'Tony Stark', 'tony@stark.com', 'Stark Industries'),
(UUID(), 'Richard Hendricks', 'richard@piedpiper.com', 'Pied Piper');
