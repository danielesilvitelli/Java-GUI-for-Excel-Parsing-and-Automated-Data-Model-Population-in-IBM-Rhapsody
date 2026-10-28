# Rhapsody Excel Sync 🚀

A production-grade Java desktop application designed to bridge Excel-based Interface Control Documents (ICD) with **IBM Engineering Systems Design Rhapsody**.

This tool automates the extraction of structured data from Excel workbooks (using **Apache POI**) and dynamically populates/synchronizes the model tree and tag metadata in IBM Rhapsody via its native Java API. It effectively eliminates manual data entry, cuts down integration errors and boosts efficiency in Systems Engineering workflows.

---

## 🌟 Key Features

*   **Excel Requirements Parser:** Reads and validates `.xlsx` files row-by-row, extracting signal definitions, data types, offsets, bit fields and metadata.
*   **Automated Rhapsody Model Population:** Programmatically creates and structures Packages (`ICD_`), Types (`Composite`, `Elementary`, `Message`), Attributes and Stereotypes inside the active Rhapsody instance.
*   **Interactive Swing GUI:** Custom desktop interface (`JTree`) providing visual feedback, real-time logging console and manual override capabilities (Add, Edit, Delete, Tag update).
*   **Smart Rescaling & Ordering Algorithms:** Automatically calculates and orders attributes based on `Byte_Offset`, `Start_Bit` and payload sizes ($O(n \log n)$ binary search indexing).
*   **Bidirectional Version Control:** Reads data model versions from Excel and updates workbook metadata post-import.

---

## 🛠️ Architecture & Tech Stack

*   **Language:** Java 8+
*   **GUI Framework:** Java Swing
*   **Excel Parsing Engine:** Apache POI 5.2.3
*   **SysML/UML Modeling Integration:** IBM Rhapsody Java API (`com.telelogic.rhapsody.core`)
*   **Build & Dependency Management:** Maven

```text
Java-GUI-for-Excel-Parsing-and-Automated-Data-Model-Population-in-IBM-Rhapsody/
├── docs/
│   ├── architecture.md    # System architecture details
│   └── user-guide.md      # Setup and usage guide
├── src/
│   └── com/
│       └── rhapsody/
│           └── gui/       # Application source code
├── .gitignore
├── pom.xml                # Maven configuration
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

1. **Java Development Kit (JDK 8+)** installed.
2. **IBM Rhapsody** (v9.0.2 or compatible) installed locally.
3. Target project open in your running IBM Rhapsody instance.

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/danielesilvitelli/Java-GUI-for-Excel-Parsing-and-Automated-Data-Model-Population-in-IBM-Rhapsody.git
   cd Java-GUI-for-Excel-Parsing-and-Automated-Data-Model-Population-in-IBM-Rhapsody
   ```

2. **Verify Local Dependencies:**
   Ensure `rhapsody.jar` is available in your local IBM Rhapsody installation path:
   `C:/Program Files/IBM/Rhapsody/9.0.2/Share/JavaAPI/rhapsody.jar`

3. **Build with Maven:**
   ```bash
   mvn clean compile
   ```

---

## 💻 Usage

1. Launch IBM Rhapsody and open your workspace/project.
2. Execute the `GUI.java` main class.
3. Select **"Obtain the current active project"** and click **Set up**.
4. Click **Read data from Excel** to load your `.xlsx` ICD file.
5. Monitor real-time logs in the console panel as the Rhapsody tree populates automatically.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.