# Project Plan

VantInk: Scraper-Based Webtoon & Comic Reader.
Transitioning from placeholder API to a modular Jsoup-based scraping system.
Tasks involve setting up the Source interface, implementing a specific scraper (e.g., for Webtoons.com), and integrating this into the existing MVVM architecture.

## Project Brief

# VantInk: Scraper-Based Webtoon Reader (Tachiyomi Style)

VantInk will now evolve to include a modular scraping engine. This allows the app to extract webtoon data directly from public websites by parsing their HTML structure.

## New Features
*   **Modular Scraper Engine**: An interface-based system to add multiple content sources.
*   **HTML Parsing (Jsoup)**: Using Jsoup to extract metadata (titles, covers) and image URLs from chapter pages.
*   **Source Management**: A way to select or toggle different web sources within the app.

## Updated Tech Stack
*   **HTML Parser**: Jsoup
*   **Architecture**: Repository pattern remains, but will now delegate to `Source` implementations.
*   **Dynamic Loading**: Images and lists will be fetched in real-time via scraping.

## Implementation Steps
**Total Duration:** 35m 35s

### Task_1_Data_Domain_Layer: Setup Domain and Data layers: Define models (Webtoon, Chapter), Room DB (History/Favorites), Retrofit service for FastAPI backend, and Repository pattern implementation.
- **Status:** COMPLETED
- **Updates:** Implemented Domain and Data layers.
- **Acceptance Criteria:**
  - Data models (Webtoon, Chapter) defined
  - Room DB with DAOs for local persistence implemented
  - Retrofit service for scraper backend created
  - Repository pattern implemented to bridge API and Room
- **Duration:** 10m 11s

### Task_2_Browsing_UI: Implement Home, Search, and Details screens using MVVM and Navigation 3. Create ViewModels to manage discovery and detail states.
- **Status:** COMPLETED
- **Updates:** Implemented Home, Search, and Details screens using MVVM and Navigation 3.
- **Acceptance Criteria:**
  - Home screen with genres/trending implemented
  - Search screen functional
  - Details screen displaying webtoon info and chapters implemented
  - Navigation between browsing screens working
- **Duration:** 3m 5s

### Task_3_Reader_And_Library: Create the Immersive Vertical Reader and Library management (Favorites/History). Implement scroll position synchronization using Room and high-res image loading with Coil.
- **Status:** COMPLETED
- **Updates:** Implemented Immersive Vertical Reader and Library management (Favorites/History).
- **Acceptance Criteria:**
  - Vertical reader with Coil image loading implemented
  - Scroll position synchronization with Room working
  - Favorites and History screens functional
  - Immersive mode active during reading
- **Duration:** 6m 1s

### Task_4_Theming_And_Verification: Apply AMOLED 'True Black' theme, Material 3 styling, Edge-to-Edge display, Adaptive layouts, and Adaptive app icon. Perform final build, run, and verification for stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** Finalized AMOLED "True Black" theme, Material 3 styling, and Edge-to-Edge display.
- **Acceptance Criteria:**
  - True Black AMOLED theme and M3 applied
  - Edge-to-Edge and Adaptive layouts functional
  - Adaptive app icon created
  - App builds and runs without crashes
  - All features verified and stable
- **Duration:** 11m 8s

### Task_5_Scraper_Engine_Implementation: Implement a modular scraper engine using Jsoup. Define a 'Source' interface for extensible content fetching and create a concrete implementation for a target site (e.g., Webtoons.com) to extract titles, chapters, and image URLs.
- **Status:** COMPLETED
- **Updates:** Jsoup library integrated into the project.
Defined 'Source' interface for search, details, and chapter parsing.
Implemented WebtoonsDotComSource for extracting data from Webtoons.com.
Verified build success.
- **Acceptance Criteria:**
  - Jsoup library integrated into the project
  - 'Source' interface defined for search, details, and chapter parsing
  - Webtoons.com scraper implemented and successfully extracting data
- **Duration:** 5m 10s

### Task_6_Scraper_Integration_And_Final_Verification: Integrate the scraper engine into the Repository layer, replacing or augmenting the existing API-based data flow. Update UI components to handle dynamic source data and perform a final verification of the full reading experience.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Repository delegates data fetching to the Scraper Engine
  - App successfully displays real-time scraped content in Home, Search, and Details
  - Vertical reader loads images from scraped URLs
  - Final build passes, all existing tests pass, and app does not crash
- **StartTime:** 2026-06-02 21:14:51 COT

