import os

file_path = "/home/randomx/Videos/mitron/backend/index.js"
with open(file_path, "r") as f:
    content = f.read()

# Add path require
if "const path = require('path');" not in content:
    content = content.replace("const express = require('express');", "const express = require('express');\nconst path = require('path');")

# Fix static middleware and add root route
old_static = "app.use(express.static('public')); // Serve the Admin Dashboard!"
new_static = """app.use(express.static(path.join(__dirname, 'public')));

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});"""

if old_static in content:
    content = content.replace(old_static, new_static)

with open(file_path, "w") as f:
    f.write(content)

print("Updated index.js for Vercel")
