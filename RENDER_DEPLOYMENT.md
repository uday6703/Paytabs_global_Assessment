# 🚀 Render Deployment Guide - Banking System POC

This guide provides step-by-step instructions to deploy the Banking System POC to [Render](https://render.com).

## 📋 Prerequisites

1. A [GitHub](https://github.com) account
2. A [Render](https://render.com) account (free tier available)
3. Git installed on your local machine
4. (Optional for local testing) Docker and Docker Compose installed

---

## 🐳 Docker Files Overview

This project includes the following Docker-related files:

| File | Purpose |
|------|---------|
| `system1-gateway/Dockerfile` | Docker image for Gateway Service (Java 17) |
| `system2-corebank/Dockerfile` | Docker image for Core Banking Service (Java 17) |
| `banking-ui/Dockerfile` | Docker image for React Frontend (Nginx) |
| `banking-ui/nginx.conf` | Nginx configuration for SPA routing |
| `docker-compose.yml` | Local development with all services |
| `*/.dockerignore` | Files to exclude from Docker builds |

---

## 🧪 Local Testing with Docker

Before deploying to Render, you can test locally:

```bash
# Build and run all services
docker-compose up --build

# Access the application
# - Banking UI: http://localhost:80
# - Gateway API: http://localhost:8081
# - Core Bank API: http://localhost:8082

# Stop all services
docker-compose down

# Stop and remove volumes (clean start)
docker-compose down -v
```

---

## 🎯 Deployment Options

Choose one of the following deployment methods:

### Option A: Blueprint Deployment (Recommended - One Click)
### Option B: Manual Deployment (Step by Step)

---

## Option A: Blueprint Deployment (Recommended)

This project includes a `render.yaml` file that defines all services. Render can automatically deploy everything at once.

### Step 1: Push Code to GitHub

```bash
# Initialize git repository (if not already done)
cd banking-system-poc
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit - Banking System POC"

# Create a new repository on GitHub, then push
git remote add origin https://github.com/YOUR_USERNAME/banking-system-poc.git
git branch -M main
git push -u origin main
```

### Step 2: Deploy via Render Blueprint

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New"** → **"Blueprint"**
3. Connect your GitHub account (if not already connected)
4. Select the `banking-system-poc` repository
5. Render will detect the `render.yaml` file
6. Click **"Apply"** to deploy all services

### Step 3: Wait for Deployment

- Render will create:
  - 🗄️ PostgreSQL database (`banking-postgres`)
  - ⚙️ System 2 - Core Banking Service (`system2-corebank`)
  - 🌐 System 1 - Gateway Service (`system1-gateway`)
  - 💻 Banking UI (`banking-ui`)

- **Note:** First deployment may take 10-15 minutes as Docker images are built.

### Step 4: Get Service URLs

After deployment, note down the URLs from your Render dashboard:
- `system1-gateway`: `https://system1-gateway-xxxx.onrender.com`
- `system2-corebank`: `https://system2-corebank-xxxx.onrender.com`
- `banking-ui`: `https://banking-ui-xxxx.onrender.com`

---

## Option B: Manual Deployment

If you prefer to deploy services individually:

### Step 1: Create PostgreSQL Database

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **"New"** → **"PostgreSQL"**
3. Configure:
   - **Name:** `banking-postgres`
   - **Database:** `corebankdb`
   - **User:** `bankuser`
   - **Region:** Oregon (or closest to you)
   - **Plan:** Free
4. Click **"Create Database"**
5. **Save the connection details** (Internal Database URL)

### Step 2: Deploy System 2 - Core Banking

1. Click **"New"** → **"Web Service"**
2. Connect your GitHub repository
3. Configure:
   - **Name:** `system2-corebank`
   - **Root Directory:** `system2-corebank`
   - **Runtime:** Docker
   - **Region:** Oregon
   - **Plan:** Free
4. Add **Environment Variables:**

   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | `jdbc:postgresql://[INTERNAL_HOST]:5432/corebankdb` |
   | `DATABASE_USERNAME` | `bankuser` |
   | `DATABASE_PASSWORD` | `[From PostgreSQL settings]` |
   | `CORS_ORIGINS` | `*` |

5. Click **"Create Web Service"**

### Step 3: Deploy System 1 - Gateway

1. Click **"New"** → **"Web Service"**
2. Connect your GitHub repository
3. Configure:
   - **Name:** `system1-gateway`
   - **Root Directory:** `system1-gateway`
   - **Runtime:** Docker
   - **Region:** Oregon
   - **Plan:** Free
4. Add **Environment Variables:**

   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `SYSTEM2_URL` | `https://system2-corebank-xxxx.onrender.com` |
   | `CORS_ORIGINS` | `*` |

5. Click **"Create Web Service"**

### Step 4: Deploy Banking UI

1. Click **"New"** → **"Static Site"**
2. Connect your GitHub repository
3. Configure:
   - **Name:** `banking-ui`
   - **Root Directory:** `banking-ui`
   - **Build Command:** `npm install && npm run build`
   - **Publish Directory:** `dist`
4. Add **Environment Variables:**

   | Key | Value |
   |-----|-------|
   | `VITE_GATEWAY_URL` | `https://system1-gateway-xxxx.onrender.com` |
   | `VITE_CORE_URL` | `https://system2-corebank-xxxx.onrender.com` |

5. Add **Rewrite Rule** (for React Router):
   - Source: `/*`
   - Destination: `/index.html`
   - Action: Rewrite

6. Click **"Create Static Site"**

---

## 🔧 Post-Deployment Configuration

### Update Environment Variables

After all services are deployed, you may need to update the environment variables with actual URLs:

1. Go to each service in Render Dashboard
2. Navigate to **"Environment"** tab
3. Update the URLs with the actual Render service URLs
4. Click **"Save Changes"** (triggers redeploy)

### Test the Deployment

1. **Health Check - Gateway:**
   ```
   https://system1-gateway-xxxx.onrender.com/health
   ```
   Expected: `System 1 - Gateway is running`

2. **Health Check - Core Banking:**
   ```
   https://system2-corebank-xxxx.onrender.com/health
   ```
   Expected: `System 2 - Core Banking is running`

3. **Open Banking UI:**
   ```
   https://banking-ui-xxxx.onrender.com
   ```

---

## 🔐 Test Credentials

| Role | Username | Password | Card Number | PIN |
|------|----------|----------|-------------|-----|
| Customer 1 | `cust1` | `pass` | 4123456789012345 | 1234 |
| Customer 2 | `cust2` | `pass` | 4987654321098765 | 5678 |
| Super Admin | `admin` | `admin` | - | - |

---

## ⚠️ Important Notes

### Free Tier Limitations

- **Spin Down:** Free tier services spin down after 15 minutes of inactivity
- **Cold Starts:** First request after spin-down may take 30-60 seconds
- **Database:** Free PostgreSQL databases expire after 90 days

### Production Considerations

For production deployment, consider:

1. **Upgrade Plan:** Use paid plans for persistent services
2. **Custom Domain:** Add custom domain for professional URLs
3. **SSL:** Render provides free SSL certificates automatically
4. **Secrets Management:** Use Render's secret environment variables for sensitive data
5. **Monitoring:** Set up health checks and alerts

---

## 🔄 Updating the Application

### Automatic Deploys

By default, Render auto-deploys on every push to the main branch:

```bash
git add .
git commit -m "Update feature"
git push origin main
```

### Manual Deploys

You can also trigger manual deploys from the Render Dashboard:
1. Go to your service
2. Click **"Manual Deploy"** → **"Deploy latest commit"**

---

## 🐛 Troubleshooting

### Service Won't Start

1. Check **"Logs"** tab in Render Dashboard
2. Verify environment variables are correct
3. Ensure database connection string is valid

### CORS Errors

1. Verify `CORS_ORIGINS` is set to `*` or specific frontend URL
2. Restart backend services after changing CORS settings

### Database Connection Failed

1. Use **Internal Database URL** (not External)
2. Verify database is in the same region
3. Check database credentials

### Build Failures

1. Check build logs in Render Dashboard
2. Ensure all dependencies are in `pom.xml` / `package.json`
3. Test build locally first: `./mvnw package -DskipTests`

---

## 📊 Architecture on Render

```
┌─────────────────────────────────────────────────────────────┐
│                        RENDER CLOUD                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────────────┐                                      │
│   │   banking-ui     │ ← React Static Site                  │
│   │  (Static Site)   │                                      │
│   └────────┬─────────┘                                      │
│            │                                                 │
│            ▼                                                 │
│   ┌──────────────────┐        ┌──────────────────┐         │
│   │ system1-gateway  │───────▶│ system2-corebank │         │
│   │  (Web Service)   │        │  (Web Service)   │         │
│   │    Port 8081     │        │    Port 8082     │         │
│   └──────────────────┘        └────────┬─────────┘         │
│                                        │                    │
│                                        ▼                    │
│                               ┌──────────────────┐         │
│                               │ banking-postgres │         │
│                               │   (PostgreSQL)   │         │
│                               └──────────────────┘         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📞 Support

If you encounter issues:
1. Check [Render Documentation](https://render.com/docs)
2. Review service logs in Render Dashboard
3. Verify all environment variables are correctly set
