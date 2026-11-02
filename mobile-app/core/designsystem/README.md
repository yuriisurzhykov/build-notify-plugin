# Graphite — дизайн-система Build Notifier

Отдельный модуль `:core:designsystem` на чистом Compose Multiplatform: только
`compose.runtime / ui / foundation / resources`, **без Material**. Поэтому одинаково
собирается и выглядит на Android, iOS, Desktop (JVM) и Web (Wasm). Шрифты Inter и
JetBrains Mono лежат внутри модуля.

Принцип: **тихий интерфейс, громкие сигналы.** Цвет появляется только там, где есть
статус сборки; всё, что пришло из Gradle (задачи, длительности, логи), набрано моноширинным.

## Подключение

```kotlin
// build.gradle.kts модуля-потребителя
commonMain.dependencies {
    implementation(project(":core:designsystem"))
}
```

```kotlin
GraphiteTheme {                      // darkTheme = isSystemInDarkTheme() по умолчанию
    ScreenSurface(Modifier.fillMaxSize()) {
        HeroPanel(BuildTone.Failure) {
            StatusLabel(BuildTone.Failure, "Сборка упала · 2 мин назад")
            Text("payment-sdk не собрался", style = GraphiteTheme.typography.display)
            PrimaryButton("Передать агенту", onClick = { /* … */ }, trailingIcon = GraphiteIcons.ArrowRight)
        }
    }
}
```

Витрина всех токенов и компонентов: `GraphiteCatalog()` — работает на любой платформе
(например, `Window { GraphiteTheme { GraphiteCatalog() } }` на десктопе).

## Токены — `GraphiteTheme.*`

| Аксессор     | Что внутри |
|--------------|------------|
| `colors`     | `background`, `surface`, `surfaceRaised`, `line`, `content*`, `inverse`, консоль (`console*` — тёмная в обеих темах) и статусы `running / success / failure / warning / neutral`, каждый — `ToneColors(main, container, hero, onMain)`. `colors.tone(BuildTone)` выбирает нужный. Схемы: `GraphiteDarkColors`, `GraphiteLightColors`. |
| `typography` | `display 30`, `headline 22`, `title 17`, `body 15`, `bodyStrong`, `bodySmall 14`, `meta 13`, `label 13`, `caption 12`, `button`, `buttonLarge`; моно: `timer 64`, `code 14`, `mono 13`, `log 12`. |
| `spacing`    | Сетка 4 pt: `xxs 2 … xxxl 32`, `screen 20`, `heroInset 8`, `section 28`. |
| `shapes`     | `log 6`, `tag 6`, `control 14`, `inset 16`, `card 18`, `hero 28`, `sheet`, `pill`. |
| `sizes`      | `touchTarget 44`, кнопки `48 / 54`, `chip 36`, `listRow 52`, иконки `14 / 18 / 22`. |

## Компоненты

| Группа        | Компоненты |
|---------------|------------|
| Основа        | `Text`, `Icon`, `GraphiteIcons`, `ProvideTextStyle` |
| Поверхности   | `ScreenSurface`, `HeroPanel(tone)`, `InsetPanel`, `Card`, `SheetSurface`, `SectionHeader`, `Divider` |
| Кнопки        | `PrimaryButton` (одна на экран), `SecondaryButton`, `OutlineButton`, `DangerButton`, `Button(variant, size)`, `IconButton` |
| Статусы       | `StatusLabel`, `StatusPill`, `StatusBadge`, `StatusDot`, `Tag`, `ConnectionBadge` |
| Списки        | `HistoryRow`, `RunTaskRow`, `SettingRow` |
| Лог           | `Console`, `LogLine(tone: LogTone, offset)`, `IssueText` |
| Управление    | `ProgressBar` (`null` — неопределённый), `FilterChip`, `Switch`, `ChoiceCard` |

## Правила

- Статус передаётся через `BuildTone`, а не через цвет: так оттенок, яркость и иконка всегда согласованы.
- На экране одна `PrimaryButton`. Разрушающие действия — только `DangerButton`, никогда не залитые.
- Всё, что пришло из Gradle, — `mono`/`code`/`log`/`timer`.
- Касаемые элементы не меньше 44 dp; иконкам без подписи нужен `contentDescription`.
- Обратная связь при нажатии своя (лёгкое сжатие и затемнение), без Material ripple — одинаково на всех платформах.

Прототип экранов: https://claude.ai/artifact/DZzSEyzU8ri9phJq9JTxtT

## Движение — `motion/`

| API | Где используется |
|-----|------------------|
| `GraphiteMotion` | общие длительности (`Fast 150`, `Medium 280`, `Slow 450`), шаг каскада `Stagger 55`, пружины `bouncy()` / `settle()` |
| `Reveal(index)` | каскадное появление элементов экрана (fade + подъём) |
| `PopIn` | пружинное «выпрыгивание» — бейдж успеха, цифры PIN |
| `Modifier.shakeOn(trigger)` | встряска при ошибке |
| `PulseRings` | кольца-радар вокруг иконки, пока идёт ожидание |
| `BouncingDots` | «ещё работаем» |
| `Modifier.breathing()` | мягкое «дыхание» элемента, который ждёт пользователя |
