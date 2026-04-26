# Planet Simulation Project

## Project Overview

This project is a JavaFX-based 3D planetary simulation that models a solar system using real physics concepts instead of simple circular, “on-the-rails” movement.

The goal of the project is to create an interactive space simulation where planets and moons are not just animated along fixed paths, but instead move using gravitational physics, orbital data, and real-time updates. Users can view the solar system, focus on specific bodies, adjust the camera, change simulation speed, and dynamically add, edit, or remove celestial bodies.

## Problem This Project Solves

Many simple solar system simulations show planets moving in perfect circles at fixed speeds. That is easy to visualize, but it does not represent how real orbital motion works.

This project solves that by moving toward a physics-based simulation that uses:

- Real planetary masses
- Orbital elements
- Newtonian gravity
- Velocity Verlet integration
- Adjustable simulation speed
- Dynamic body creation
- Interactive camera controls

Instead of being a static demo, the simulation becomes an interactive sandbox where users can experiment with planets, moons, stars, orbits, and gravitational behavior.

## Current Features

### 1. 3D Solar System Visualization

The simulation displays a 3D solar system using JavaFX.

Currently included:

- Sun
- Mercury
- Venus
- Earth
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

The project converts real-world distances into scene units so large astronomical values can be displayed inside the JavaFX scene.

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

Each body stores:

- Position on X, Y, and Z axes
- Velocity on X, Y, and Z axes
- Acceleration on X, Y, and Z axes

This allows planets to move based on gravitational interactions rather than being manually animated.

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
- Auto-distance scaling based on body size
- Reset camera button
- Yaw control
- Pitch control
- Distance control

This makes the simulation easier to explore because the user can lock onto a planet, moon, or star instead of manually searching for it in space.

---

### 5. Keyboard Camera Controls

The simulation supports keyboard controls for camera movement.

Keyboard controls:

| Key | Action |
|---|---|
| Left Arrow | Rotate camera left |
| Right Arrow | Rotate camera right |
| Up Arrow | Zoom in |
| Down Arrow | Zoom out |
| W | Pitch camera up |
| S | Pitch camera down |
| A | Fine yaw left |
| D | Fine yaw right |
| I | Fine zoom in |
| K | Fine zoom out |
| J | Fine pitch up |
| L | Fine pitch down |

Keyboard input ignores text fields so typing into forms does not accidentally move the camera.

---

### 6. Mouse Camera Controls

Mouse controls were added for smoother camera movement.

Mouse controls:

| Mouse Input | Action |
|---|---|
| Hold Left Click + Drag | Adjust yaw and pitch |
| Hold Right Click + Drag Up/Down | Adjust camera distance |
| Click Viewport | Refocus keyboard controls on the simulation |

The mouse and keyboard both control the same camera sliders, so they work together instead of conflicting.

---

### 7. Simulation Speed Control

The project includes a simulation speed slider.

The speed can scale from real-time movement up to much faster time steps, allowing users to observe orbital motion more quickly.

The display automatically formats speed as:

- Real-time multiplier
- Minutes per second
- Hours per second
- Days per second

This makes it easier to understand how fast the simulation is running.

---

### 8. Body Scale Control

A body scale slider allows the visual size of planets and stars to be adjusted.

This is important because real planetary sizes are extremely small compared to orbital distances. Without scaling, most planets would be too small to see clearly.

The simulation keeps the physical values separate from the visual scale, so changing the body scale does not change the actual physics values.

---

### 9. Add / Edit / Remove Body System

The project now includes a dynamic body management system.

Users can:

- Add new bodies
- Edit existing bodies
- Remove bodies
- Clear the current selection
- Refresh body lists
- Select existing bodies from a dropdown

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

This means bodies are no longer locked into hardcoded definitions only. Users can create and modify their own stars, planets, and moons during runtime.

---

### 10. Star / Planet / Moon Type System

The simulation supports different body types:

- Star
- Planet
- Moon

Type-specific behavior:

- Stars do not require orbital values.
- Planets orbit the Sun.
- Moons require a parent planet.
- Moons inherit motion relative to their parent planet.
- A moon cannot be created without a valid parent.
- A planet with moons cannot be removed until its moons are removed first.

This adds structure to the simulation and prevents invalid solar system setups.

---

### 11. Parent-Child Moon System

Moons are connected to parent planets.

Implemented moon logic:

- Moons must have a parent planet.
- Moons are positioned relative to their parent.
- Moons inherit the parent planet’s velocity.
- Editing a planet rebuilds its child moons correctly.
- Removing a planet is blocked if moons still orbit it.

This allows the project to support nested orbital systems instead of only planets orbiting the Sun.

---

### 12. Data and Metadata Management

The `SolarSystem` class stores both physics data and display metadata.

Stored body data includes:

- Body object
- Radius in kilometers
- Distance / semi-major axis
- Orbital angle / true anomaly
- Body type
- Parent body
- Color
- Orbital elements

Useful methods include:

- `getBody`
- `getBodyNames`
- `getPlanetNames`
- `getBodyRadiusKm`
- `getBodyDistanceAu`
- `getBodyAngleDeg`
- `getBodyType`
- `getOrbitParent`
- `getBodyColor`
- `getOrbitElements`
- `addNewBody`
- `updateBody`
- `removeBody`
- `updatePhysics`
- `setViewScale`

This makes the project easier to expand because the data is organized instead of scattered throughout the code.

---

## Major Improvements From Original Version

The original project started as a hardcoded 3D solar system where bodies moved in a fixed way.

The current version improves that by adding:

- All 8 planets
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
- Keyboard controls
- Mouse controls
- Simulation speed control
- Visual scale control
- Cleaner backend structure

The project has moved from a basic visual demo toward a real interactive simulation.

---

## Planned Features

### Firebase Integration

Firebase is planned for persistent storage.

Future Firebase features:

- Save the current universe state
- Load saved solar systems
- Store custom planet data
- Store custom moon data
- Store star configurations
- Allow users to restore previous simulations
- Save edited body values between sessions

This will allow the simulation to persist instead of resetting every time the application closes.

---

### More Future Improvements

Possible future features include:

- Save/load buttons in the UI
- Saturn rings
- Click-to-focus body selection
- Collision handling
- Better visual scaling
- Preset solar systems
- User-created systems
- Improved Firebase authentication
- Better UI layout for editing bodies
- Import/export system data

---

## Sprint Backlog

### Completed

- Added all 8 planets
- Added Sun
- Added kilometers-to-scene conversion
- Added real mass constants
- Added radius values
- Added orbital elements
- Implemented physics-based movement
- Added Velocity Verlet integration
- Added camera focus system
- Added yaw, pitch, and distance controls
- Added keyboard camera controls
- Added mouse camera controls
- Added simulation speed slider
- Added body scale slider
- Added Add / Edit / Remove body system
- Added Star / Planet / Moon type system
- Added parent planet support for moons
- Added validation for invalid body setups

### Still To Do

- Connect simulation state to Firebase
- Store planet variables in Firebase
- Store moon variables in Firebase
- Save current universe state
- Load previous universe state
- Improve UI organization
- Add more visual features

---

## Summary

This project is building an interactive, physics-based solar system simulator.

It solves the problem of simple, unrealistic planet simulations by using real orbital concepts, gravity, dynamic body management, and interactive camera controls. Instead of only watching planets move on fixed circular tracks, users can explore, edit, and eventually save their own solar systems.