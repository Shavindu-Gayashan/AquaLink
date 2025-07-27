# AquaLink

**AquaLink** is an Android application developed in Java as a university project. Its goal is to provide real-time monitoring and control of water tank systems using **ESP32 microcontrollers**.

---

## 📱 Features

### ✅ Implemented

- **User Authentication (Firebase)**
    - Email/password login and registration
    - Password reset functionality
    - Automatic session management

- **Dashboard Interface**
    - Real-time water level monitoring (percentage)
    - Motor status display (On/Off indicators)
    - Firebase Realtime Database connection status
    - **IoT device connectivity status** (shows if tank is online/offline)
    - **Shake-to-support**: Shake device to quickly access technical support

- **Operation Modes**
    - **Auto Mode**: Automatically manages water level based on threshold values
    - **Manual Mode**: Allows direct control of the pump with a built-in **safety timeout**
    - **Custom Mode**: User-defined automation logic with configurable triggers

- **Advanced Mode Settings**
    - Configure ON/OFF water levels for Auto and Custom modes
    - Enable/disable auto triggers in Custom mode
    - Set safety timeout for Manual mode

- **Navigation & Menu**
    - User profile access
    - Map view (placeholder for multi-tank monitoring)
    - Technical support access
    - Settings and preferences
    - Device management
    - UI and notification customization
    - Access to documentation

- **Device Management Interface**
    - Add device using Device ID
    - Show added devices
    - Remove added devices

---

## 🚧 Under Development

The following features are currently in progress:

- 🔐 Google & Facebook authentication
- 🗺️ Multi-tank monitoring via map view
- 🛠️ Built-in technical support system
- ⚙️ User profile and settings management
- 🎨 UI/notification customization options
- 📘 In-app user manual and documentation
- 📊 Advanced water level prediction algorithms

---

## ⚠️ Disclaimer

> This project is intended for **educational purposes only** and is **not ready for production use**.  
> Integration with physical ESP32 devices for sensor input and pump control is being developed in parallel.

---

## 👨‍🎓 Developed as a university project by students of the **Open University of Sri Lanka**.
