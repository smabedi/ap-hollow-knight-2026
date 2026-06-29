# Hollow Knight 2D Clone (LibGDX & Box2D)

A 2D side-scroller inspired by *Hollow Knight*, developed using the **LibGDX framework and Box2D physics engine**. This project was built as the Graphics Assignment for the Advanced Programming (AP) course.

## 📌 Project Overview
Built strictly using the **MVC (Model-View-Controller)** architecture, this game separates rendering, physics, and game logic. By leveraging LibGDX's OpenGL wrappers, Tiled maps (`.tmx`), and Box2D for rigid body physics, it features a robust game loop, dynamic camera tracking, and complex entity AI.

## 🚀 Core Features
* **LibGDX & Scene2D Rendering:** Custom sprite rendering, UI stages for menus and HUDs, and dynamic orthographic camera tracking.
* **Box2D Physics Engine:** Handles deterministic gravity, collision detection (via sensors), and advanced platforming mechanics including Dashing, Double Jumping, and Pogo-Jumping off hazards.
* **Tiled Map Parsing:** Dynamically loads environments like Forgotten Crossroads and Greenpath, automatically converting map logic layers into Box2D static bodies.
* **Complex AI State Machines:** Features environment-specific enemies (e.g., Crawlids, Mosquitoes) and a multiphase False Knight boss fight equipped with distance-based decision logic, anti-spam move generation, and dynamic phase scaling.
* **Charm & Inventory System:** An interactive Scene2D pause menu allowing players to equip gameplay modifiers (Charms) using a Notch limitation system.
* **Dynamic Spell & Soul System:** Combat interactions fill a Soul Vessel, which can be spent dynamically on healing (Focus) or casting unique spells (Vengeful Spirit, Howling Wraiths).
* **SQLite State Persistence:** Complete game state saving and loading (Player HP, Location, Soul, Playtime, and Achievements) utilizing a local, serverless SQLite relational database for robust, portable data management.

## 🛠️ Technical Stack
* **Language:** Java
* **Architecture:** MVC (Model-View-Controller)
* **Game Framework:** LibGDX
* **Physics Engine:** Box2D
* **Level Design:** Tiled Map Editor
* **Build Tool:** Gradle
* **Database:** SQLite (JDBC)

## 🎮 Controls
* **Movement:** Arrow Keys
* **Jump:** `Z` (Press again in midair for Double Jump)
* **Dash:** `C`
* **Nail Attack:** `X` (Press `Down + X` while in air to Pogo)
* **Focus (Heal):** Hold `A`
* **Menus:** `Escape` (Pause/Settings), `I` (Inventory), `Tab` (Map)

## 📁 Getting Started
1. Clone the repository:
   https://github.com/smabedi/ap-hollow-knight-2026.git
2. Open the project folder inside **IntelliJ IDEA**.
3. Allow the IDE to resolve the project structure and sync the **Gradle** dependencies.
4. Open the Gradle tab and run the `lwjgl3:run` task to launch the game.

## 🎓 Academic Context
* **Institution:** Sharif University of Technology
* **Course:** Advanced Programming (AP)
* **Semester:** Spring 2026
* **Assignment:** Exercise 2 (Graphics Assignment)

## 📝 License & Copyright
**Code:** All original Java source code within this repository is licensed under the MIT License.

**Assets & IP Disclaimer:** This project was created strictly for educational purposes as a university assignment. All *Hollow Knight* intellectual property, characters, environmental art, audio assets, and original game concepts are the exclusive property of **Team Cherry**.

The game assets (images, sounds, fonts) included in this repository are **NOT** covered by the MIT License and are used under the assumption of Fair Use for non-commercial, educational purposes. No copyright infringement is intended.
