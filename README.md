Sprint Backlog 
To do:
Add all 8 planets
convert kilometers to pixels method
implement physics vs "on the rails" motion
connect to firebase
store planet variables in firebase

Update 1.1

1. Focused Bodies + Better Camera Controls

Added

Focus system (select a body to center camera on)
Camera orbit system instead of free-fly only
Yaw control (left/right rotation)
Pitch control (up/down angle)
Distance control (zoom relative to body)
Auto distance scaling based on body size

Changed

Camera now follows a body instead of staying static
Movement is relative to the focused object, not world origin
Reset camera now snaps back to Sun properly

Fixed

Keyboard controls no longer conflict with UI inputs
Camera doesn’t get stuck behind the Sun (angle control solves it)
2. Add / Edit / Remove Bodies System

Added UI

Body selection dropdown
Add / Edit / Remove buttons
Clear selection button
Autofill when selecting a body

Added Fields

Name
Mass
Radius
Distance (AU)
Orbital angle (deg)
Type (Star / Planet / Moon)
Parent planet (for moons)
Color

Added Logic

Create new bodies dynamically
Edit existing bodies (including renaming)
Remove bodies safely
Prevent deleting planets that still have moons
Prevent invalid moon setups (no parent, self-parent, etc.)

Added System Features

Body type system (Star / Planet / Moon)
Moon → parent planet relationship
Orbital angle support (no more straight-line spawning)
Color customization

Added Backend (SolarSystem)

Stores metadata:
radius
distance
angle
type
parent
color
New methods:
getBodyNames / getPlanetNames
getBodyRadius / Distance / Angle / Type / Parent / Color
addNewBody
updateBody
removeBody

Changed

Moons now orbit selected planets dynamically (not hardcoded only)
Editing a planet correctly rebuilds its moons
Bodies are no longer static definitions—they’re fully dynamic