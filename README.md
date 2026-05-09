# Solar System Simulation

A JavaFX 3D solar system simulator that models celestial bodies with gravitational physics, orbital elements, interactive camera controls, editable planets/moons/stars, and Firebase-backed save files.

The project started as a basic planetary visualization, but the current version is closer to an interactive solar-system sandbox. Bodies are not just moved along fixed circular paths. They are initialized from orbital data and updated with Newtonian gravity and Velocity Verlet integration.

---

## Features

### 3D JavaFX Solar System

The application renders a 3D solar system using JavaFX spheres, lighting, textures, and a perspective camera.

Default bodies include:

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

Known default bodies use bundled texture images when available. Custom body colors are supported through the UI.

---

### Physics-Based Motion

Planet movement is handled by a physics engine instead of simple fixed-path animation.

Implemented physics features:

- Newtonian gravity between all bodies
- Acceleration calculations from gravitational force
- Position and velocity updates
- Velocity Verlet integration
- Real-time animation loop
- Adjustable simulation time scale

Each body stores:

- Name
- Mass
- Position: `x`, `y`, `z`
- Velocity: `vx`, `vy`, `vz`
- Acceleration: `ax`, `ay`, `az`

---

### Orbital Elements

Bodies can be initialized from orbital elements instead of hardcoded circular paths.

Supported orbital values:

- Semi-major axis
- Eccentricity
- Inclination
- Ascending node
- Argument of periapsis
- True anomaly

This allows planets and moons to start from more realistic elliptical and inclined orbital states.

---

### Star / Planet / Moon System

The simulator supports three body types:

- `Star`
- `Planet`
- `Moon`

Validation rules are built in:

- Stars can exist without orbital values.
- Planets orbit the central star setup.
- Moons require a parent planet.
- Moons inherit their parent planet's position and velocity.
- A moon cannot be created without a valid parent planet.
- A planet with child moons cannot be deleted until its moons are removed first.

---

### Add, Edit, and Delete Bodies

The side panel allows users to create and manage celestial bodies at runtime.

Editable fields include:

- Name
- Type
- Parent planet for moons
- Mass
- Radius in kilometers
- Semi-major axis in AU
- Eccentricity
- Inclination
- Ascending node
- Argument of periapsis
- True anomaly
- Color

The edit panel also includes sliders for changing major orbital/body values on selected bodies.

---

### Interactive Camera Controls

The camera can focus on any body in the simulation.

View controls include:

- Focus dropdown
- Body details panel
- Camera orbit around selected body
- Mouse drag camera rotation
- Right-click drag zoom adjustment
- Time scale slider
- Size scale slider

The size scale changes only the rendered body size. It does not change the physics values.

---

### Firebase Authentication and Save Files

The project includes Firebase support for saving and loading user-created solar systems.

Current Firebase features:

- Register account
- Sign in
- Sign out
- Save current solar system state
- List saved systems
- Load selected save
- Delete selected save

The app uses Firebase Authentication for accounts and Firestore for saved simulation states.

Saved systems store body snapshots, including:

- Body metadata
- Mass
- Radius
- Color
- Position
- Velocity
- Acceleration
- Orbit data
- Parent-child body relationships

---

## Tech Stack

- Java 23
- JavaFX 21.0.6
- Maven
- Firebase Authentication REST API
- Firestore REST API
- Jackson Databind
- JUnit 5

---

## Project Structure

```text
src/main/java/com/example/planetsimdemo/
├── Main.java                         # Application entry point
├── SimulationScreen.java             # 3D scene, camera, rendering, animation loop
├── SolarSystem.java                  # High-level simulation wrapper
├── SolarSystemState.java             # Body storage, metadata, orbital setup, snapshots
├── Body.java                         # Physics body model
├── PhysicsEngine.java                # Gravity and Verlet integration
├── BodyEditorViewModel.java          # Add/edit/delete body state and validation bridge
├── Design2Controller.java            # JavaFX UI controller
├── AuthViewModel.java                # Authentication and save/load UI state
├── FirebaseAuthenticationService.java# Firebase sign-in/sign-up service
├── InitialConditionsRepository.java  # Firestore save/load/delete repository
├── FirestoreJsonMapper.java          # Firestore JSON serialization/deserialization
├── FirebaseClientConfig.java         # Firebase project configuration
└── Conversions.java                  # Constants and unit conversion helpers

src/main/resources/
├── Design2.fxml                      # Main side-panel UI
├── style.css                         # UI styling
└── textures/                         # Planet/moon/sun texture assets
```

---

## Requirements

Before running the project, install:

- Java 23 or newer
- Maven
- Internet access for Firebase login/save/load features

JavaFX dependencies are handled through Maven.

---

## How to Run

Clone the repository:

```bash
git clone https://github.com/EvilWizard910/Solar-System-Simulation.git
cd Solar-System-Simulation
```

Run the application with Maven:

```bash
mvn clean javafx:run
```

The Maven JavaFX plugin launches:

```text
com.example.planetsimdemo/com.example.planetsimdemo.Main
```

---

## How to Use

### View the Solar System

Open the app and use the `View` section to:

- Select a body to focus on
- View body details
- Adjust simulation speed
- Adjust rendered body size

### Move the Camera

- Hold left click and drag to rotate around the focused body.
- Hold right click and drag up/down to adjust camera distance.
- Select another body from the focus dropdown to follow it.

### Add a Body

Open the `Add` panel and enter the body values.

For planets and moons, orbital values are required. For moons, a parent planet must be selected.

### Edit or Delete a Body

Open the `Edit` panel, select a body, change values, then update it. Delete is blocked when removing a planet that still has child moons.

### Save or Load a System

Open the `Account` panel.

1. Register or sign in.
2. Enter a save name.
3. Click `Save`.
4. Select saved systems from the list to load or delete them.

---

## Current Limitations

- Collision detection is not implemented yet.
- Custom texture upload buttons exist in the UI, but custom texture selection is not wired up yet.
- Firebase configuration is currently stored in `FirebaseClientConfig.java`.
- The simulation uses simplified orbital/physics assumptions and is intended as an educational sandbox, not a precision astronomy tool.
- Very large time scales can make unstable or unusual systems behave unpredictably.

---

## Future Improvements

Planned or possible improvements:

- Collision handling
- Click-to-focus body selection directly from the 3D scene
- Custom texture upload support
- More preset solar systems
- Import/export save files
- Improved UI layout
- Better camera controls
- More advanced lighting and visual effects
- Saturn ring rendering
- Stronger Firebase configuration handling

---

## Summary

Solar System Simulation is an interactive JavaFX space sandbox. It combines 3D rendering, real gravitational calculations, orbital elements, runtime body editing, and Firebase persistence so users can build, modify, save, and reload custom solar systems.
