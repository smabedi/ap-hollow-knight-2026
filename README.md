# Hollow Knight 2D Clone (Java GUI)

A native 2D side-scroller inspired by *Hollow Knight*, developed entirely from scratch using **Java Swing and AWT**. This project was built as homework for the Advanced Programming (AP) course, specifically focusing on native rendering without the use of external game engines.

## 📌 Project Overview
Built strictly using the **MVC (Model-View-Controller)** architecture, this game engine processes rendering, physics, and game logic independently. By leveraging Java Swing's `JPanel` and `Graphics2D` components, it features a custom render loop, dynamic camera tracking, and complex entity AI.

## 🚀 Core Features
* **Native Swing Graphics:** Custom sprite rendering, animation frames, and visual effects handled entirely through Java's native 2D graphics API.
* **Custom Physics Engine:** Handles gravity, collision detection, and advanced platforming mechanics including Dashing, Double Jumping, and Pogo-Jumping off hazards.
* **Complex AI State Machines:** Features 4 standard enemy types and a multiphase boss fight (False Knight) equipped with distance-based decision logic, anti-spam move generation, and dynamic phase scaling.
* **Charm & Inventory System:** An interactive pause menu allowing players to equip up to 5 different gameplay modifiers (Charms) using a 3-Notch limitation system.
* **Dynamic Spell & Soul System:** Combat interactions fill a Soul Vessel, which can be spent dynamically on healing (Focus) or casting unique spells (Vengeful Spirit, Howling Wraiths).
* **JSON State Persistence:** Complete game state saving and loading (Player HP, Location, Soul, and unlocked Achievements) via JSON serialization.

## 🛠️ Technical Stack
* **Language:** Java
* **Architecture:** MVC (Model-View-Controller)
* **GUI Framework:** Java Swing / AWT
* **IDE:** JetBrains IntelliJ IDEA
* **Data Format:** JSON

## 🎮 Controls
* **Movement:** Arrow Keys
* **Jump:** `Z` (Press again in midair for Double Jump)
* **Dash:** `C`
* **Nail Attack:** `X` (Press `Down + X` while in air to Pogo)
* **Focus (Heal):** Hold `A`
* **Menus:** `Escape` (Pause/Settings), `I` (Inventory)

## 📁 Getting Started
1. Clone the repository:
   https://github.com/smabedi/ap-hollow-knight-gui-2026.git
2. Open the project folder inside **IntelliJ IDEA**.
3. Allow the IDE to resolve the project structure and sync the JDK.
4. Run the main entry point to launch the Java Swing graphical interface.

## 🎓 Academic Context
* **Institution:** Sharif University of Technology
* **Course:** Advanced Programming (AP)
* **Semester:** Spring 2026
* **Assignment:** Homework #4 (Graphics in Java)
