const mysql = require('mysql2/promise');
require('dotenv').config();

async function addCompanies() {
    try {
        const connection = await mysql.createConnection(process.env.DATABASE_URL);
        
        await connection.query(`DELETE FROM companies`);

        const companies = [
            {
                id: 'comp_1',
                name: 'TechNova Solutions',
                descriptor: 'AI & Machine Learning',
                funding: '$12M Series A',
                employees: 150
            },
            {
                id: 'comp_2',
                name: 'NextGen Fintech',
                descriptor: 'Decentralized Finance',
                funding: '$5M Seed',
                employees: 40
            }
        ];

        for (const c of companies) {
            await connection.query(
                'INSERT INTO companies (id, name, descriptor, funding, employees) VALUES (?, ?, ?, ?, ?)',
                [c.id, c.name, c.descriptor, c.funding, c.employees]
            );
        }
        
        console.log("Successfully added companies!");
        await connection.end();
    } catch (error) { console.error("Failed to add companies:", error); }
}
addCompanies();
