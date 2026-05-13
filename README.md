# 🌳 Arbor - Forest Mapping & Inventory API

> A robust backend API for digital geographic mapping and forest inventory, developed as an interdisciplinary project between the Forest Engineering and Computer Science departments.

## 📖 1. Introduction
**Arbor** aims to create a web platform for the geographic mapping and inventory of trees located on the campus of the Federal Rural University of Pernambuco. This initiative integrates forest measurement data, bole quality assessment, and injury identification, among other factors, into an accessible and visually intuitive digital platform.

## 🎯 2. Project Objectives

### 2.1 General Objective
Develop a web platform for the visualization and management of georeferenced tree data, promoting the integration of knowledge between Forest Measurement and Computer Science.

### 2.2 Specific Objectives
* **Geographic Mapping:** Display the precise location of each tree on an interactive web map.
* **Forest Attributes Inventory:** Associate each mapped tree with detailed attributes.
* **Accessibility & Visualization:** Allow users to view detailed information by interacting with points on the map.
* **Educational & Scientific Support:** Create a tool to enrich the teaching of forestry disciplines and serve as a basis for future research.

## 📋 3. Attributes to be Collected
The Forest Engineering students will collect and standardize the following data:
* **Geographic Location:** Precise coordinates (latitude and longitude) obtained via GNSS (high-precision GPS) receivers.
* **Dendrometric Measurement:** Diameter at Breast Height (DBH), total height, commercial bole height, and crown projection.
* **Bole Quality:** Visual assessment of trunk straightness, bifurcations, knots, and thick branches.
* **Injuries & Health:** Identification and description of any damages or health issues present in the tree.

## ✅ 4. Expected Results
* An interactive web page displaying the geographic mapping of trees and their detailed attributes.
* A digital database with georeferenced and dendrometric information.
* An innovative educational tool for teaching Forest Measurement and Inventory.
* A solid foundation for future project expansions, such as new analytical features or mapping area enlargement.

---

## 🚀 Technologies Used
* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL + **PostGIS** (for spatial data)
* **Migration:** Flyway
* **ORM:** Hibernate + Hibernate Spatial
* **Docs:** Springdoc OpenAPI (Swagger)
* **Infra:** Docker & Docker Compose

## 🛠️ Getting Started (Installation & Setup)

1. **Clone the repository:**
```bash
git clone [https://github.com/Monitoramento-Florestal/monitoramento-florestal-backend.git](https://github.com/Monitoramento-Florestal/monitoramento-florestal-backend.git)
cd monitoramento-florestal-backend
```

2. **Start the Database Infrastructure:**
```bash
docker-compose up -d
```

3. **Run the Application (local dev is default):**
```bash
./mvnw spring-boot:run
```

**Windows PowerShell:**
```powershell
.\mvnw.cmd spring-boot:run
```

4. **Production-like run (explicit profile + env var):**
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="troque-por-uma-chave-com-32-caracteres-ou-mais"
./mvnw spring-boot:run
```

**Windows PowerShell:**
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:JWT_SECRET="troque-por-uma-chave-com-32-caracteres-ou-mais"
.\mvnw.cmd spring-boot:run
```

## 🐳 Infrastructure Access (pgAdmin)
If you want to visually inspect the PostGIS database:
1. Go to `http://localhost:5050` in your browser.
2. Login with `admin@arbor.com` / `admin`.
3. Connect to `localhost` (or the container IP) on port `5432`.
