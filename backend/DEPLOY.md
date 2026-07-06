# Backend Deployment to Vercel

## 1. Run DB Migration (one-time)

```bash
cd backend
npm install
node migrate_full.js
```

This creates all missing tables/columns.

## 2. Set Environment Variables in Vercel

Go to Vercel Dashboard → Project → Settings → Environment Variables:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `mysql://mitron_user:MitronSecure2026!@51.79.143.65:3306/mitron_db` |
| `FCM_SERVER_KEY` | *(your Firebase Legacy Server Key)* |

## 3. Deploy

The project auto-deploys when you push to the connected git branch.

```bash
git add .
git commit -m "backend: full migration + FCM push + AI briefing endpoints + message status"
git push
```

## 4. Verify

```bash
curl https://connect-mitron.vercel.app/api/companies
curl https://connect-mitron.vercel.app/api/briefings
```
