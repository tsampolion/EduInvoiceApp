# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.28.3] - 2025-08-16

### Fixed
- Drawer: wrapped drawer content with `ModalDrawerSheet` to render correctly and be interactive; disabled redundant global app bar.
- Global Top Bar: removed redundant `TopAppBar` from `MainActivity` to avoid double bars and follow per-screen bars.
- Lesson form: fixed student selection not populating by passing `userId` to `getStudentById` and wiring group loading with `userId`.
- Groups: added inline validation for required group name with error state on Save.

### UX
- Search/Filter: unified bottom sheet implemented on Students, Lessons, Groups, and Archived Students; inline search bars removed.
- Headers: replaced bar-style headers with slim in-content headers across major screens; added top-left drawer FAB overlay to avoid FAB collisions.
- Home: clarified Students button color logic; distinct success vs error container colors.

### DevOps
- Version bumped to 0.28.3.

## [0.28.4] - 2025-08-16

### Fixed
- Student and Group lesson forms respect per-user lookups for students/groups; selection fields populate reliably.

### UX
- Drawer: top-left FAB overlay on Students, Lessons, Groups, Classes, Revenue so it won’t conflict with primary FABs.
- Headers: slim in-content headers adopted for Student, Group, Privacy, Profile, Login, Register, Reset Password.
- Archived Students: migrated to bottom sheet for search/sort.

### DevOps
- Version bumped to 0.28.4.

## [0.28.2] - 2025-08-15

### Added
- User account deletion: settings now includes a Delete Account action with confirmation. Deleting an account permanently removes the user and all owned data (students, lessons, groups, cross-refs) on the device, executed transactionally.

### DevOps
- Version bumped to 0.28.2.

## [0.28.1] - 2025-08-14

### Fixed
- Auth gating: removed drawer access on Welcome and blocked drawer interactions while unauthenticated. Removed `WelcomeScreen` menu icon and parameter.
- Drawer: added Classes and Revenue menu items and routing; ensured drawer respects scaffold insets across screens.
- Home: removed redundant top app bar as universal topbar is provided by `MainActivity`.
- Search bars: added `modifier` to `ModernSearchBar`, standardized spacing; applied correct padding in Students/Lessons/Groups to avoid overlap.
- FAB visibility: hide FABs on Students/Lessons/Groups when lists are empty.
- Lists refresh: clear `DataCache` after saving a student so lists update immediately.
- Groups: implemented delete flow with confirmation.
- Date/time pickers: made entire field area clickable.
- PDF printing: use actual file for printing; keep `FileProvider` `Uri` for sharing.

### DevOps
- CI: deduplicated workflows; standardized JDK 17; added assembleRelease sanity step; instrumented tests on API 35.
- Dependencies: keep SQLCipher `net.zetetic:android-database-sqlcipher` at 4.5.4 (latest available in repo). Add runtime fallback to unencrypted Room DB for debug builds when SQLCipher native libs are incompatible (e.g., 16KB page size devices/emulators).

## [0.28.0] - 2025-08-14

### Added
- Workspace-friendly Android SDK bootstrap; automatic license acceptance; `local.properties` handling.
- Comprehensive domain tests; runnable performance test suite.

### Enhanced
- Coverage (Kover), architecture checks (Konsist), static analysis (Detekt) integrated and enforced.

### Fixed
- Build stability across environments by removing privileged SDK setup requirements.

### Technical
- Gradle/Kotlin alignment; scripted test execution via `scripts/run_comprehensive_tests.ps1`.

## [0.25.0] - 2024-12-19

### Added
- **Phase 2: Modern UI & User Experience Enhancements**
  - Modern skeleton loading screens with shimmer effects
  - Enhanced progress indicators with modern design
  - Comprehensive empty states with actionable content
  - Edge-to-edge design implementation
  - Modern search bar with voice input support
  - Advanced filtering with modern UI components
  - Modern PDF generation system with Material Design 3
  - Haptic feedback system for enhanced user interactions
  - Smooth animations and micro-interactions
  - Visual feedback components for better UX
  - Comprehensive analytics and performance monitoring
  - Business metrics tracking system
  - Accessibility testing framework
  - Performance testing suite

### Enhanced
- **Search & Filtering**
  - Persistent search history using DataStore
  - Advanced date range filtering
  - Modern search suggestions UI
  - Improved search performance (< 500ms)
  - Fuzzy search capabilities

### Optimized
- **Performance Improvements**
  - Fixed deprecated API usage in progress indicators
  - Optimized component rendering
  - Enhanced accessibility with proper semantics
  - Improved memory usage patterns
  - Better error handling and recovery

### Technical
- **Code Quality**
  - Added comprehensive test coverage
  - Implemented modern architecture patterns
  - Enhanced error boundaries and recovery
  - Improved dependency injection
  - Better separation of concerns

### Security
- **Enhanced Security**
  - Improved data validation
  - Better error reporting
  - Enhanced backup/restore security
  - Improved user authentication flow

## [0.24.9] - 2024-12-18

### Added
- **Modern Search System**
  - Implemented `ModernSearchRepository` with fuzzy search
  - Added `ModernFilterManager` for advanced filtering
  - Created `SearchHistoryRepository` for persistent search history
  - Added date range filtering capabilities
  - Integrated search suggestions in Lessons/Groups screens

### Enhanced
- **PDF Generation**
  - Modern PDF theme system with Material Design 3
  - Enhanced PDF components with professional typography
  - Improved PDF generator with better visual hierarchy
  - Added PDF theme manager for customization
  - Settings integration for PDF theme selection

### Technical
- **Performance Optimizations**
  - Optimized search performance
  - Improved database query efficiency
  - Enhanced UI component rendering
  - Better memory management

### Fixed
- **Bug Fixes**
  - Fixed search history persistence
  - Resolved PDF generation issues
  - Improved error handling in search operations
  - Enhanced UI responsiveness

## [0.24.8] - 2024-12-17

### Added
- **Enhanced Error Handling**
  - Comprehensive error boundaries throughout the app
  - Automatic retry mechanisms for failed operations
  - User-friendly error messages and recovery options
  - Error reporting and analytics integration

### Enhanced
- **Data Management**
  - Improved backup and restore functionality
  - Enhanced data validation and integrity checks
  - Better conflict resolution for data synchronization
  - Optimized database operations

### Fixed
- **Bug Fixes**
  - Fixed search history persistence
  - Resolved PDF generation issues
  - Improved error handling in search operations
  - Enhanced UI responsiveness

## [0.24.7] - 2024-12-16

### Added
- **Advanced Search Features**
  - Real-time search with debouncing
  - Search history and suggestions
  - Advanced filtering options
  - Search result highlighting

### Enhanced
- **User Interface**
  - Modern Material Design 3 components
  - Improved navigation patterns
  - Enhanced accessibility features
  - Better responsive design

### Technical
- **Architecture Improvements**
  - Better separation of concerns
  - Enhanced dependency injection
  - Improved state management
  - Better error handling

### Performance
- **Optimizations**
  - Reduced app startup time
  - Improved search performance
  - Enhanced database operations
  - Better memory management

## [0.24.6] - 2024-12-15

### Added
- **Modern UI Components**
  - Material Design 3 implementation
  - Enhanced typography system
  - Improved color schemes
  - Better visual hierarchy

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Improved rendering performance
  - Better memory usage
  - Enhanced database operations

## [0.24.5] - 2024-12-14

### Added
- **Enhanced Security Features**
  - Improved password hashing with BCrypt
  - Enhanced data encryption
  - Better authentication flow
  - Improved backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.24.4] - 2024-12-13

### Added
- **Advanced Features**
  - Enhanced search functionality
  - Improved filtering options
  - Better data visualization
  - Enhanced reporting features

### Enhanced
- **User Interface**
  - Modern design improvements
  - Better navigation
  - Enhanced accessibility
  - Improved responsiveness

### Technical
- **Code Quality**
  - Better error handling
  - Enhanced test coverage
  - Improved documentation
  - Better code organization

### Performance
- **Optimizations**
  - Faster app performance
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.24.3] - 2024-12-12

### Added
- **New Features**
  - Enhanced search capabilities
  - Improved filtering system
  - Better data management
  - Enhanced user experience

### Enhanced
- **Performance**
  - Faster app startup
  - Better memory management
  - Improved database operations
  - Enhanced UI responsiveness

### Technical
- **Improvements**
  - Better error handling
  - Enhanced security
  - Improved code quality
  - Better documentation

### Fixed
- **Bug Fixes**
  - Various UI improvements
  - Better error handling
  - Enhanced stability
  - Improved performance

## [0.24.2] - 2024-12-11

### Added
- **Security Enhancements**
  - Improved authentication
  - Enhanced data protection
  - Better password handling
  - Enhanced backup security

### Enhanced
- **User Experience**
  - Better navigation
  - Improved accessibility
  - Enhanced responsiveness
  - Better error handling

### Technical
- **Performance**
  - Faster app startup
  - Better memory usage
  - Improved database operations
  - Enhanced UI performance

### Fixed
- **Bug Fixes**
  - Various stability improvements
  - Better error handling
  - Enhanced security
  - Improved performance

## [0.24.1] - 2024-12-10

### Added
- **New Features**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Performance**
  - Faster app startup
  - Better memory management
  - Improved database operations
  - Enhanced UI responsiveness

### Technical
- **Improvements**
  - Better error handling
  - Enhanced security
  - Improved code quality
  - Better documentation

### Fixed
- **Bug Fixes**
  - Various UI improvements
  - Better error handling
  - Enhanced stability
  - Improved performance

## [0.24.0] - 2024-12-09

### Added
- **Major Release**
  - Complete app redesign
  - Enhanced security features
  - Improved performance
  - Better user experience

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.23.0] - 2024-12-08

### Added
- **Feature Release**
  - Enhanced search capabilities
  - Improved filtering system
  - Better data visualization
  - Enhanced reporting features

### Enhanced
- **User Interface**
  - Modern design improvements
  - Better navigation
  - Enhanced accessibility
  - Improved responsiveness

### Technical
- **Code Quality**
  - Better error handling
  - Enhanced test coverage
  - Improved documentation
  - Better code organization

### Performance
- **Optimizations**
  - Faster app performance
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.22.0] - 2024-12-07

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.21.0] - 2024-12-06

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.20.0] - 2024-12-05

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.19.0] - 2024-12-04

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.18.0] - 2024-12-03

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.17.0] - 2024-12-02

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.16.0] - 2024-12-01

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.15.0] - 2024-11-30

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.14.0] - 2024-11-29

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.13.0] - 2024-11-28

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.12.0] - 2024-11-27

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.11.0] - 2024-11-26

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.10.0] - 2024-11-25

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.9.0] - 2024-11-24

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.8.0] - 2024-11-23

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.7.0] - 2024-11-22

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.6.0] - 2024-11-21

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.5.0] - 2024-11-20

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.4.0] - 2024-11-19

### Added
- **Security Release**
  - Enhanced authentication system
  - Improved data encryption
  - Better password handling
  - Enhanced backup security

### Enhanced
- **Data Management**
  - Better backup and restore functionality
  - Improved data validation
  - Enhanced error handling
  - Better conflict resolution

### Technical
- **Performance Improvements**
  - Optimized database operations
  - Enhanced UI performance
  - Better memory management
  - Improved app responsiveness

### Security
- **Security Enhancements**
  - Enhanced data protection
  - Improved user privacy
  - Better security practices
  - Enhanced authentication

## [0.3.0] - 2024-11-18

### Added
- **Performance Release**
  - Enhanced app performance
  - Improved memory management
  - Better database operations
  - Enhanced UI responsiveness

### Enhanced
- **User Experience**
  - Smoother animations
  - Better loading states
  - Improved error handling
  - Enhanced accessibility

### Technical
- **Code Quality**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI performance

## [0.2.0] - 2024-11-17

### Added
- **Feature Release**
  - Enhanced search functionality
  - Improved filtering options
  - Better data management
  - Enhanced user interface

### Enhanced
- **Core Features**
  - Modern UI design
  - Enhanced data management
  - Improved authentication
  - Better error handling

### Technical
- **Architecture**
  - Better code organization
  - Enhanced test coverage
  - Improved documentation
  - Better error handling

### Performance
- **Optimizations**
  - Faster app startup
  - Better memory usage
  - Enhanced database operations
  - Improved UI responsiveness

## [0.1.0] - 2024-11-16

### Added
- **Initial Release**
  - Basic app functionality
  - User authentication
  - Student management
  - Lesson tracking
  - Invoice generation
  - Basic UI components

### Technical
- **Foundation**
  - Android app architecture
  - Database setup
  - Basic security features
  - Error handling
  - Testing framework

### Performance
- **Baseline**
  - Basic app performance
  - Memory management
  - Database operations
  - UI responsiveness
