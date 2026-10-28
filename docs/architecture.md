# Architecture Overview - Rhapsody Excel Sync

## 1. Introduction
The application is designed following a modular architecture that bridges Excel-based Interface Control Documents (ICD) with IBM Rhapsody data models via Java. It decouples the Presentation Layer (Swing GUI), the Business/Tree Manipulation Logic, and the Integration/Persistence Layer (Apache POI & Rhapsody API).

## 2. Component Breakdown

*   **Presentation Layer (`com.rhapsody.gui.GUI`)**
    *   Implements a custom Java Swing desktop interface.
    *   Provides interactive components: a hierarchical component tree (`JTree`), dynamic metadata forms, tag configuration panels, and a real-time console logger.
*   **Business & Tree Logic (`TreeInsertion`, `TreeModification`, `TreeDeletion`, `TagManager`, `OperationsOnJTree`)**
    *   Manages node validation, sorting algorithms (alphabetical and numerical based on tags like `Byte_Offset` or `Start_Bit`), and structural constraints mapping (Packages, Types, Attributes, and custom stereotypes).
*   **Integration Layer (`RhapsodySide`, `LoadingDataFromExcel`, `LoadingDataFromRhapsody`)**
    *   **Excel Engine:** Uses Apache POI to parse `.xlsx` workbooks row-by-row, validating sheets, extracting version metadata, and mapping signals/messages.
    *   **Rhapsody Connector:** Interacts directly with the active IBM Rhapsody instance using the proprietary COM/Java API (`RhapsodyAppServer`).