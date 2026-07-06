const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seedDatabase() {
    console.log("Seeding database...");

    // 1. Add Companies
    const starkRef = await db.collection("companies").add({
        name: "Stark Industries",
        descriptor: "Global Defense & Clean Energy",
        funding: "$50B Public",
        employees: 50000,
        openDeals: 12,
        color: "accent"
    });

    const piedPiperRef = await db.collection("companies").add({
        name: "Pied Piper",
        descriptor: "Middle-Out Compression",
        funding: "$15M Series B",
        employees: 14,
        openDeals: 1,
        color: "pro"
    });

    // 2. Add Contacts
    await db.collection("contacts").add({
        name: "Tony Stark",
        initials: "TS",
        title: "CEO & Lead Innovator",
        company: "Stark Industries",
        company_id: starkRef.id,
        email: "tony@stark.com",
        score: 99
    });

    await db.collection("contacts").add({
        name: "Richard Hendricks",
        initials: "RH",
        title: "Founder & CEO",
        company: "Pied Piper",
        company_id: piedPiperRef.id,
        email: "richard@piedpiper.com",
        score: 72
    });

    // 3. Add Events
    await db.collection("events").add({
        title: "AI Horizon Summit 2026",
        date: "Oct 15 - 17, 2026",
        location: "Moscone Center, SF",
        attendees: 5000,
        color: "primary"
    });

    console.log("Database seeded successfully!");
}

seedDatabase().catch(console.error);
