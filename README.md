# Planet Simulation Project

## Project Overview

This project is a JavaFX-based 3D planetary simulation that models a solar system using real physics concepts instead of simple circular, “on-the-rails” movement.

The goal of the project is to create an interactive space simulation where planets and moons are not just animated along fixed paths, but instead move using gravitational physics, orbital data, and real-time updates. Users can view the solar system, focus on specific bodies, adjust the camera, change simulation speed, dynamically add, edit, or remove celestial bodies, save and load custom systems through Firebase, change planet rotation speed, and use custom JPG textures for planets.

## Problem This Project Solves

Many simple solar system simulations show planets moving in perfect circles at fixed speeds. That is easy to visualize, but it does not represent how real orbital motion works.

This project solves that by moving toward a physics-based simulation that uses:

- Real planetary masses
- Real radius values
- Orbital elements
- Newtonian gravity
- Velocity Verlet integration
- Adjustable simulation speed
- Dynamic body creation
- Dynamic body editing
- Parent-child moon relationships
- Visual planet rotation
- Editable rotation speed
- Time-scale-based spin animation
- Built-in planet textures
- Custom local JPG texture uploads
- Firebase authentication
- Firestore save/load support
- Interactive camera controls

Instead of being a static demo, the simulation becomes an interactive sandbox where users can experiment with planets, moons, stars, orbits, gravitational behavior, saved custom systems, visual planetary rotation, and custom planet appearances.

---

## Current Features

### 1. 3D Solar System Visualization

The simulation displays a 3D solar system using JavaFX.

Currently included:

- Sun
- Mercury
- Venus
- Earth
- Moon
- Mars
- Jupiter
- Saturn
- Uranus
- Neptune

Each body has its own:

- Name
- Mass
- Radius
- Color
- 3D sphere model
- Position
- Velocity
- Acceleration
- Body type
- Optional parent body
- Optional orbital elements
- Rotation speed
- Optional texture path

The project converts real-world distances into scene units so large astronomical values can be displayed inside the JavaFX scene.

The project also supports texture-based rendering for known solar system bodies. Default planet textures are stored in the project’s resources folder under:

```text
src/main/resources/textures/
```

At runtime, those textures are loaded using Java resource paths such as:

```text
/textures/earth.jpg
/textures/mars.jpg
/textures/moon.jpg
```

---

### 2. Real Physics-Based Motion

The simulation now uses physics instead of only fixed circular paths.

Implemented physics features:

- Newtonian gravity between bodies
- Acceleration updates from gravitational force
- Position updates
- Velocity updates
- Velocity Verlet integration
- Time-step based simulation updates
- Adjustable time scaling

Each body stores:

- Position on X, Y, and Z axes
- Velocity on X, Y, and Z axes
- Acceleration on X, Y, and Z axes

This allows planets and moons to move based on gravitational interactions rather than being manually animated.

---

### 3. Orbital Elements Support

Bodies can be created using orbital elements instead of only simple distance and angle values.

Supported orbital values:

- Semi-major axis
- Eccentricity
- Inclination
- Ascending node
- Argument of periapsis
- True anomaly

These values allow the simulation to create more realistic elliptical and inclined orbits.

This improves the original version because planets no longer need to spawn in a simple straight line or circular path. Their starting positions and velocities can be calculated from orbital data.

---

### 4. Focused Body Camera System

The camera can focus on a selected celestial body.

Added camera features:

- Focus body dropdown
- Camera follows the selected body
- Camera orbits around the focused body
- Yaw-style orbit movement
- Pitch-style orbit movement
- Distance adjustment
- Body details display for selected objects

This makes the simulation easier to explore because the user can lock onto a planet, moon, or star instead of manually searching for it in space.

---

### 5. Mouse Camera Controls

Mouse controls were added for smoother camera movement.

| Mouse Input | Action |
|---|---|
| Hold Left Click + Drag | Adjust camera yaw and pitch |
| Hold Right Click + Drag Up/Down | Adjust camera distance |

The mouse controls allow the user to orbit around the selected focus body and zoom in or out without needing to manually edit camera values.

---

### 6. Simulation Speed Control

The project includes a simulation speed slider.

The speed can scale from real-time movement up to much faster time steps, allowing users to observe orbital motion more quickly.

The display automatically formats speed as:

- Real-time multiplier
- Minutes per second
- Hours per second
- Days per second

This makes it easier to understand how fast the simulation is running.

---

### 7. Body Scale Control

A body scale slider allows the visual size of planets, moons, and stars to be adjusted.

This is important because real planetary sizes are extremely small compared to orbital distances. Without scaling, most planets would be too small to see clearly.

The simulation keeps the physical values separate from the visual scale, so changing the body scale does not change the actual physics values.

The scaling system has also been improved so changing the scale updates existing sphere radii instead of rebuilding every planet object. This prevents the app from repeatedly reloading texture images while the slider is moving.

---

### 8. Texture and Material Caching

The simulation avoids reloading planet textures every time the bodies are rebuilt or scaled.

Texture performance improvements include:

- Cached `PhongMaterial` objects
- Cached texture-based materials for known planets
- Cached solid-color materials for custom bodies
- Radius-only updates when body scale changes
- Prevention of duplicate point lights after rebuilding bodies

This improves performance when adjusting the body scale slider because the app no longer reloads every image repeatedly.

---

### 9. Custom Local JPG Texture Uploads

The project supports custom local JPG textures for added or edited bodies.

When a user chooses an image:

1. The app opens a file chooser.
2. The user selects a `.jpg` or `.jpeg` file.
3. The app validates the selected image.
4. The app copies the image into a local application texture folder.
5. The copied file path is saved with the body metadata.
6. The renderer loads the custom texture from that saved path.

Custom texture validation includes:

- File must be `.jpg` or `.jpeg`
- File must be 2 MB or smaller
- Image must be 2048x2048 or smaller
- Image must successfully load as an image

The custom texture folder is:

```text
~/.solar-system-simulation/textures/
```

This makes the app keep its own stable copy of the selected image instead of depending on the original file staying in Downloads, Desktop, or another temporary location.

Custom uploaded images are saved locally, not uploaded to Firebase Storage.

---

### 10. Planet Axis Rotation Animation

Planets now visually spin on their axes.

Implemented rotation features:

- Each rendered body has a rotation transform.
- Rotation angles are tracked per body.
- Each planet can have its own spin speed.
- Some planets can rotate in the opposite direction.
- Rotation is visual only and does not affect gravity or orbital calculations.

This gives textured planets more realistic motion because they no longer only move through space; they also rotate while orbiting.

---

### 11. Editable Rotation Speed

When adding or editing a body, the user can change the body’s rotation speed.

Rotation speed is stored as:

```text
rotationSpeedDegPerSecond
```

This value controls how fast the body visually spins on its axis.

Positive values spin in one direction, and negative values spin in the opposite direction.

---

### 12. Time-Scale-Based Rotation

Planet spin speed increases as the simulation time scale increases.

When the user increases simulation speed, planet rotation also speeds up visually. This keeps the spin animation consistent with the idea that simulated time is passing faster.

The rotation system can use either:

- Direct scaled time using `dt * timeScale`
- A softer visual scale using `dt * Math.sqrt(timeScale)`

The softer scale is useful because extremely high time scales can make planets spin so fast that the rotation becomes visually unreadable.

---

### 13. Add / Edit / Remove Body System

The project includes a dynamic body management system.

Users can:

- Add new bodies
- Edit existing bodies
- Remove bodies
- Select existing bodies from a list
- Refresh body lists automatically after changes
- Change body color
- Change orbital values
- Change body type
- Assign a parent planet for moons
- Change rotation speed
- Choose a custom JPG texture

Supported fields:

- Name
- Mass
- Radius
- Semi-major axis
- Eccentricity
- Inclination
- Ascending node
- Argument of periapsis
- True anomaly
- Type
- Parent planet
- Color
- Rotation speed
- Texture path

This means bodies are no longer locked into hardcoded definitions only. Users can create and modify their own stars, planets, and moons during runtime.

---

### 14. Star / Planet / Moon Type System

The simulation supports different body types:

- Star
- Planet
- Moon

Type-specific behavior:

- Stars do not require orbital values.
- Planets use orbital values.
- Moons require a parent planet.
- Moons inherit motion relative to their parent planet.
- A moon cannot be created without a valid parent.
- A planet with moons cannot be removed until its moons are removed first.
- A body cannot be created with invalid mass or radius values.

This adds structure to the simulation and prevents invalid solar system setups.

---

### 15. Parent-Child Moon System

Moons are connected to parent planets.

Implemented moon logic:

- Moons must have a parent planet.
- Moons are positioned relative to their parent.
- Moons inherit the parent planet’s velocity.
- Editing a planet rebuilds its child moons correctly.
- Removing a planet is blocked if moons still orbit it.
- The default solar system includes Earth’s Moon.

This allows the project to support nested orbital systems instead of only planets orbiting the Sun.

---

### 16. Firebase Authentication

The project includes Firebase authentication.

Users can:

- Register an account
- Sign in with email and password
- Sign out
- Access save/load controls after signing in

Authentication is handled through the Firebase Authentication REST API.

This is a major improvement because saved systems can be tied to specific users instead of being shared globally or lost when the application closes.

---

### 17. Firestore Save / Load System

The project supports persistent saved solar systems using Firestore.

Users can:

- Enter a save name
- Save the current solar system state
- View saved systems
- Load a selected saved system
- Delete a selected saved system

Saved systems store body snapshots, including:

- Name
- Type
- Parent body
- Mass
- Radius
- Color
- Rotation speed
- Texture path
- Position
- Velocity
- Acceleration
- Orbital elements

This means custom solar systems can be saved and restored later instead of resetting every time the application closes.

For custom uploaded textures, Firestore saves the local texture path string. The actual image file remains stored on the same computer.

---

### 18. Data Snapshot and JSON Mapping System

The project includes a snapshot system for converting the current simulation state into saveable data.

The `SolarSystemState` class can create body snapshots that include physics values and metadata.

The `FirestoreJsonMapper` class converts those snapshots into Firestore-compatible JSON and converts loaded Firestore data back into a working `SolarSystemState`.

This helps separate the simulation logic from the persistence logic and makes the save/load system easier to maintain.

---

### 19. Data and Metadata Management

The `SolarSystemState` class stores both physics data and display metadata.

Stored body data includes:

- Body object
- Radius in kilometers
- Body type
- Parent body
- Color
- Rotation speed
- Texture path
- Orbital elements
- Position
- Velocity
- Acceleration

Useful methods include:

- `getBody`
- `getBodyNames`
- `getPlanetNames`
- `getBodyRadiusKm`
- `getBodyType`
- `getOrbitParent`
- `getBodyColor`
- `getBodyRotationSpeedDegPerSecond`
- `getBodyTexturePath`
- `getOrbitElements`
- `addNewBody`
- `updateBody`
- `removeBody`
- `updatePhysics`
- `toInitialConditions`
- `toSnapshots`
- `fromSnapshots`

This makes the project easier to expand because the data is organized instead of scattered throughout the code.

---

### 20. Updated JavaFX User Interface

The project uses a side-panel JavaFX interface with multiple sections.

UI sections include:

- Account
- View
- Details
- Add
- Edit

The Account section handles login, registration, saving, loading, and deleting saved systems.

The View section handles focus selection, simulation speed, body scale, and selected body details.

The Add section handles creation of new stars, planets, and moons, including rotation speed and custom texture selection.

The Edit section handles selecting existing bodies, changing values, updating bodies, deleting bodies, changing rotation speed, and changing texture images.

This makes the application easier to use because the controls are grouped by purpose instead of being scattered.

---

## Major Improvements From Original Version

The original project started as a hardcoded 3D solar system where bodies moved in a fixed way.

The current version improves that by adding:

- All 8 planets
- Earth’s Moon
- Sun
- Real mass constants
- Real radius values
- Real orbital element inputs
- Gravity-based movement
- Velocity Verlet integration
- Dynamic body creation
- Dynamic editing
- Dynamic removal
- Body type validation
- Parent planet support for moons
- Camera focus system
- Mouse camera controls
- Simulation speed control
- Visual scale control
- Texture support for known bodies
- Custom local JPG texture uploads
- JPG validation
- Texture/material caching
- Faster body scaling
- Planet axis rotation animation
- Editable rotation speed
- Rotation speed linked to simulation time scale
- Firebase user authentication
- Firestore save/load/delete system
- Saved system list
- Body snapshot serialization
- Firestore JSON mapping
- Cleaner backend structure
- Updated JavaFX side-panel UI

The project has moved from a basic visual demo toward a real interactive simulation with persistent user-created systems.

---

## Firebase Integration

Firebase is implemented for authentication and persistent storage.

Implemented Firebase features:

- User registration
- User sign in
- User sign out
- Save current solar system state
- Load saved solar systems
- List saved systems
- Delete saved systems
- Store custom planet data
- Store custom moon data
- Store star configurations
- Save edited body values between sessions
- Save rotation speed values
- Save texture path values

Firebase allows the simulation state to persist instead of resetting every time the application closes.

Custom image files are not uploaded to Firebase Storage. Firestore stores the local texture path, and the actual image remains on the same computer.

---

## Current Limitations

Some features are still incomplete or planned for future improvement.

Current limitations include:

- Collision handling is not implemented yet.
- Custom uploaded textures only reload correctly on the same computer where the image was uploaded.
- Custom uploaded textures are not transferred between computers because Firebase Storage is not being used.
- The simulation is an educational sandbox, not a precision astronomy tool.
- Very high simulation speeds can make unstable systems behave unpredictably.
- Very high visual rotation speeds can make planet spin hard to see, so a softened visual time scale may be better.
- Firebase configuration is currently stored directly in the project configuration class.
- Keyboard camera controls were part of the earlier planned control system, but the current active camera controls are mouse-based and focus-dropdown based.

---

## More Future Improvements

Possible future features include:

- Collision handling
- Saturn rings
- Click-to-focus body selection
- Better visual scaling
- Preset solar systems
- User-created system templates
- Better UI layout for editing bodies
- Import/export system data
- Optional Firebase Storage support for cloud-synced images
- Better Firebase configuration handling
- More visual effects
- Orbit trails
- Labels for bodies
- More moons for gas giants
- More accurate planet rotation periods
- Tilted rotation axes for planets

---

## Sprint Backlog

### Completed

- Added all 8 planets
- Added Sun
- Added Earth’s Moon
- Added kilometers-to-scene conversion
- Added real mass constants
- Added radius values
- Added orbital elements
- Implemented physics-based movement
- Added Velocity Verlet integration
- Added camera focus system
- Added mouse camera controls
- Added simulation speed slider
- Added body scale slider
- Added Add / Edit / Remove body system
- Added Star / Planet / Moon type system
- Added parent planet support for moons
- Added validation for invalid body setups
- Added JavaFX side-panel UI
- Added Firebase authentication
- Added user registration
- Added user sign in
- Added user sign out
- Added Firestore save system
- Added Firestore load system
- Added Firestore delete system
- Added saved systems list
- Added body snapshot system
- Added Firestore JSON mapper
- Added texture rendering for known bodies
- Added custom local JPG texture uploads
- Added JPG file validation
- Added texture/material caching
- Improved body scaling performance
- Added planet axis rotation animation
- Added editable rotation speed
- Added time-scale-based rotation speed
- Added texture path saving/loading

### Still To Do

- Add collision handling
- Improve UI organization
- Add more visual features
- Add Saturn rings
- Add click-to-focus selection
- Add orbit trails
- Add body labels
- Add import/export support
- Improve Firebase configuration security
- Add realistic axial tilts
- Add more accurate spin speeds for each body
- Add optional cloud image syncing if Firebase Storage becomes available

---

## How to Run

This project uses Maven and JavaFX.

Requirements:

- Java 23 or newer
- Maven
- Internet connection for Firebase login/save/load features

Clone the project:

```bash
git clone https://github.com/EvilWizard910/Solar-System-Simulation.git
cd Solar-System-Simulation
```

Run the project:

```bash
mvn clean javafx:run
```

The JavaFX Maven plugin launches the main class:

```text
com.example.planetsimdemo/com.example.planetsimdemo.Main
```

---

## Summary

This project is building an interactive, physics-based solar system simulator.

It solves the problem of simple, unrealistic planet simulations by using real orbital concepts, gravity, dynamic body management, parent-child moon relationships, interactive camera controls, visual planet rotation, editable spin speed, optimized texture scaling, local JPG texture uploads, and Firebase persistence. Instead of only watching planets move on fixed circular tracks, users can explore, edit, save, load, and continue building their own solar systems.
