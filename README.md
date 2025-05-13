# ReCHor - Swiss Public Transport Route Finder 🇨🇭

## Overview
ReCHor (Recherche d'Horaire) is a Java application that helps users find optimal public transportation routes in Switzerland. Similar to websites like cff.ch, search.ch, and bahn.de, ReCHor determines the best way to travel from one stop to another at a given date and time, optimizing for departure/arrival times and minimizing the number of transfers.

## Demo 

<video src="https://github.com/user-attachments/assets/3fc22231-730f-447a-b75e-bc112b056ec7" controls></video>

## Features
- **Offline functionality**: Works with pre-downloaded Swiss public transport schedule data, no internet connection required
- **Multi-criteria optimization**: Simultaneously optimizes departure and arrival times while minimizing transfers
- **Complete Swiss coverage**: Includes all public transportation options in Switzerland
- **User-friendly interface**: Simple input of departure, destination, and time preferences, with ride visualization and calendar event export

## How It Works
ReCHor leverages the official Swiss public transportation timetable data to find optimal routes between any two stops in the Swiss transport network. The application implements advanced search algorithms to analyze possible connections and determine the most efficient routes based on:

- Departure and arrival times
- Number of transfers required
- Total journey duration

## Requirements
- Java Runtime Environment (JRE) 8 or higher
- Pre-downloaded Swiss public transport schedule data
- Minimum 2GB of RAM recommended

## Installation
1. Clone this repository
```
git clone https://github.com/seyl3/ReCHor.git
```
2. Download the required transportation data (see documentation)
3. Build the project using your preferred Java IDE or with the included build script
4. Run the application

## Usage
1. Launch the application
2. Enter your departure location
3. Enter your destination
4. Select your preferred departure date and time
5. Review the suggested routes
6. Select your preferred route to view detailed information
7. Add this route to your calendar by downloading an .ical


## Development
This project was developed as part of an object-oriented programming course using Java, CS-108, at EPFL. It demonstrates the practical application of:
- Object-oriented design principles
- Efficient algorithms
- Data processing and management
- User interface design

## License
[License information]

---

*Note: This project is for educational purposes and is not affiliated with the official Swiss Federal Railways (SBB/CFF/FFS) or other commercial route planning services.*
