const fs = require('fs'); 
const express = require('express');
const cors = require('cors'); // Allows frontend to communicate with backend
const app = express();

app.use(express.json()); // Enable JSON parsing
app.use(cors()); // Enable cross-origin requests (for frontend access)

// Path to your JSON file
const JSON_FILE_PATH = './UserPokemon.json';

// API to read JSON file
app.get('/get-json', (req, res) => {
    fs.readFile(JSON_FILE_PATH, 'utf8', (err, data) => {
        if (err) return res.status(500).json({ error: "Error reading file" });

        res.json(JSON.parse(data));
    });
});

// API to update JSON file
app.post('/update-json', (req, res) => {
    const newData = req.body;

    fs.readFile(JSON_FILE_PATH, 'utf8', (err, data) => {
        if (err) return res.status(500).json({ error: "Error reading file" });

        let jsonData = JSON.parse(data);
        Object.assign(jsonData, newData);

        fs.writeFile(JSON_FILE_PATH, JSON.stringify(jsonData, null, 2), 'utf8', (err) => {
            if (err) return res.status(500).json({ error: "Error writing file" });

            res.json({ message: "JSON file updated successfully!", jsonData });
        });
    });
});

// Start the server
const PORT = 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
