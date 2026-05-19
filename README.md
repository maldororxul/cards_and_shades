# Card trading game on Kotlin
Полная рекурсивная структура проекта (maldororxul/cards_and_shades @ main):
```agsl
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   │   └── java/
│   │   │       └── com/
│   │   │           └── example/
│   │   │               └── cardsandshades/
│   │   │                   └── ExampleInstrumentedTest.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/androidTest/java/com/example/cardsandshades/ExampleInstrumentedTest.kt
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           └── cardsandshades/
│   │   │   │               ├── catalog/
│   │   │   │               │   ├── CampaignCatalog.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/catalog/CampaignCatalog.kt
│   │   │   │               │   └── CardCatalog.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/catalog/CardCatalog.kt
│   │   │   │               ├── engine/
│   │   │   │               │   └── GameEngine.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/engine/GameEngine.kt
│   │   │   │               ├── model/
│   │   │   │               │   ├── CardModel.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/model/CardModel.kt
│   │   │   │               │   ├── GameState.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/model/GameState.kt
│   │   │   │               │   ├── LevelModel.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/model/LevelModel.kt
│   │   │   │               │   ├── PlayerModel.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/model/PlayerModel.kt
│   │   │   │               │   └── UserProfile.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/model/UserProfile.kt
│   │   │   │               ├── ui/
│   │   │   │               │   ├── booster/
│   │   │   │               │   │   └── BoosterScreen.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/booster/BoosterScreen.kt
│   │   │   │               │   ├── campaign/
│   │   │   │               │   │   └── CampaignScreen.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/campaign/CampaignScreen.kt
│   │   │   │               │   ├── components/
│   │   │   │               │   │   ├── CardComponent.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/components/CardComponent.kt
│   │   │   │               │   │   └── DragAndDropSpaces.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/components/DragAndDropSpaces.kt
│   │   │   │               │   ├── game/
│   │   │   │               │   │   ├── GameScreen.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/game/GameScreen.kt
│   │   │   │               │   │   └── GameViewModel.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/game/GameViewModel.kt
│   │   │   │               │   └── theme/
│   │   │   │               │       ├── Color.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/theme/Color.kt
│   │   │   │               │       ├── Theme.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/theme/Theme.kt
│   │   │   │               │       └── Type.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/ui/theme/Type.kt
│   │   │   │               └── MainActivity.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/java/com/example/cardsandshades/MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/drawable/ic_launcher_background.xml
│   │   │   │   │   └── ic_launcher_foreground.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/drawable/ic_launcher_foreground.xml
│   │   │   │   ├── mipmap-anydpi/
│   │   │   │   │   ├── ic_launcher.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-anydpi/ic_launcher.xml
│   │   │   │   │   └── ic_launcher_round.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-anydpi/ic_launcher_round.xml
│   │   │   │   ├── mipmap-hdpi/
│   │   │   │   │   ├── ic_launcher.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-hdpi/ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-hdpi/ic_launcher_round.webp
│   │   │   │   ├── mipmap-mdpi/
│   │   │   │   │   ├── ic_launcher.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-mdpi/ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-mdpi/ic_launcher_round.webp
│   │   │   │   ├── mipmap-xhdpi/
│   │   │   │   │   ├── ic_launcher.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xhdpi/ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxhdpi/
│   │   │   │   │   ├── ic_launcher.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp
│   │   │   │   ├── mipmap-xxxhdpi/
│   │   │   │   │   ├── ic_launcher.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/values/colors.xml
│   │   │   │   │   ├── strings.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/values/strings.xml
│   │   │   │   │   └── themes.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/values/themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/xml/backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/res/xml/data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/main/AndroidManifest.xml
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── example/
│   │                   └── cardsandshades/
│   │                       └── ExampleUnitTest.kt -> https://github.com/maldororxul/cards_and_shades/blob/main/app/src/test/java/com/example/cardsandshades/ExampleUnitTest.kt
│   ├── .gitignore -> https://github.com/maldororxul/cards_and_shades/blob/main/app/.gitignore
│   ├── build.gradle.kts -> https://github.com/maldororxul/cards_and_shades/blob/main/app/build.gradle.kts
│   └── proguard-rules.pro -> https://github.com/maldororxul/cards_and_shades/blob/main/app/proguard-rules.pro
├── .gitignore -> https://github.com/maldororxul/cards_and_shades/blob/main/.gitignore
├── build.gradle.kts -> https://github.com/maldororxul/cards_and_shades/blob/main/build.gradle.kts
├── gradle.properties -> https://github.com/maldororxul/cards_and_shades/blob/main/gradle.properties
├── gradlew.bat -> https://github.com/maldororxul/cards_and_shades/blob/main/gradlew.bat
├── README.md -> https://github.com/maldororxul/cards_and_shades/blob/main/README.md
└── settings.gradle.kts -> https://github.com/maldororxul/cards_and_shades/blob/main/settings.gradle.kts
```