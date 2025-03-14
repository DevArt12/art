# Art Gallery App

A fully offline Android Art Gallery application built with Kotlin and Android Architecture Components.

## Features

1. Art Gallery
   - Display artworks in a grid layout
   - Add, edit, and delete artworks
   - View artwork details

2. Artist Profiles
   - Manage artist profiles
   - View artist portfolios
   - Add, edit, and delete artists

3. Art Market
   - List artworks for sale
   - Include pricing and contact information
   - Browse available artworks

4. Art News
   - View local art news
   - Manage news articles
   - Add, edit, and delete news items

## Technical Details

- Language: Kotlin
- Architecture: MVVM
- Database: Room
- UI Components: Material Design, RecyclerView
- Image Loading: Glide
- Navigation: Navigation Component
- ViewBinding for view access

## Project Structure

```
app/
├── data/
│   ├── dao/         # Data Access Objects
│   ├── entity/      # Database Entities
│   └── AppDatabase  # Room Database
├── repository/      # Data Repositories
├── ui/             # UI Components
│   ├── gallery/
│   ├── artists/
│   ├── market/
│   └── news/
├── util/           # Utility Classes
├── viewmodel/      # ViewModels
└── adapter/        # RecyclerView Adapters
```

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an emulator or physical device

## Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- Room Database
- ViewModel & LiveData
- Coroutines
- Glide for image loading
- Navigation Component

## Running Tests

The project includes unit tests for:
- Database operations
- Repository layer
- ViewModels

Run tests using:
```bash
./gradlew test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
