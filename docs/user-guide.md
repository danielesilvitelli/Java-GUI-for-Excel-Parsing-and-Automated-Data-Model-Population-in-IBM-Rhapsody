# User Guide - Rhapsody Excel Sync

## Prerequisites
1. **Java Development Kit (JDK 8+)** installed on your system.
2. **IBM Rhapsody** (tested with v9.0.2) installed locally.
3. An active license and local environment configuration supporting Rhapsody Java API (`rhapsody.jar`).

## Getting Started
1. Launch **IBM Rhapsody** and open your target project workspace.
2. Start the Java application (via IDE or executable JAR).
3. The GUI will automatically detect the active Rhapsody session.

## Step-by-Step Workflow
1. **Setup Connection:** Select the project loading option from the initial combo box (e.g., *"Obtain the current active project"*) and click **Set up**.
2. **Import Excel Data:** Click **Read data from Excel**, select your structured `.xlsx` requirements file (ICD template).
3. **Automated Population:** The tool parses the file row-by-row, populating packages, message types, composite/elementary structures, and engineering tags automatically within the Rhapsody model tree.
4. **Manual Adjustments (Optional):** Use the GUI tree selection to add, modify, delete elements, or update specific tags (`Byte_Offset`, `Start_Bit`, etc.) on the fly.