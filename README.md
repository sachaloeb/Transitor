# Transitor: Public Transport Routing Engine for Maastricht 🚍
This repository contains the implementation of Transitor, a Java-based project aimed at analyzing and enhancing socio-economic accessibility across Maastricht using public transport data. Developed as part of a project for the Department of Advanced Computing Sciences at Maastricht University, this project combines routing algorithms, geographic data, and socio-economic metrics.

## 📖 Overview
Transitor seeks to address accessibility challenges in urban environments by developing a tool to calculate and visualize public transport routes and assess socio-economic accessibility. The project is divided into three phases:

1. Distance and Time Calculators: Implement basic distance and time calculations for walking and cycling.
2. Public Transport Routing: Create a routing engine for public transport (direct routes).
3. Advanced Routing + Accessibility Analysis: Add support for transfers and compute accessibility metrics for all postal codes.

## 🎯 Objectives
1. Routing: Calculate feasible routes (walking, cycling, and bus) between any two postal codes in Maastricht.
2. Accessibility Analysis: Quantify socio-economic accessibility for every postal code, highlighting disparities.
3. Visualization: Develop user-friendly visualizations for routes and accessibility metrics.

## 🔍 Features
1. Routing Engine: Efficient algorithms for calculating distances and travel times.
2. Public Transport Data: Integration of GTFS datasets for real-time transit information.
3. Accessibility Metrics: Analyze and rank areas based on socio-economic connectivity.
4. Graphical User Interface (GUI): Visualize routes and accessibility data interactively.

## ⚙️ Setup and Usage
### Prerequisites
- Java Development Kit (JDK 11 or higher)
- MySQL for relational database storage
- Libraries: JUnit (for testing), GeoTools (for geographic data processing)
### Steps
1. Clone the repository:
   ```bash
     git clone https://github.com/username/transitor.git
     cd transitor
   ```
2. Set up the database:
   - Import the GTFS dataset into MySQL.

## 🧪 Testing and Evaluation
- JUnit Tests: Comprehensive unit tests with code coverage reports.
- Performance Metrics: Evaluate routing efficiency and database query optimization.
- Visualization: Visualize routes and accessibility metrics on an interactive map.

## 🤝 Contributors
This project was collaboratively developed by students of the Computer Science Year 1 program at Maastricht University. Special thanks to the Department of Advanced Computing Sciences for their guidance.
