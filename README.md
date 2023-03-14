# runningdiary
Běžecký deník s GUI

## Sestavení

Pro sestavení projektu je používán nástroj `gradle`.

V kořenovém adresáři projektu zadejte příkazy:

`gradle`

`gradle jar`

V adresáři `build/libs` vznikne spustitelný `runningdiary.jar` .jar soubor.

## Spuštění

Pro spuštění aplikace je potřeba mít staženou knihovnu JavaFX, např. z https://openjfx.io.

Aplikace se spustí po zadání příkazu:

`java --module-path <module-path> --add-modules=javafx.controls -jar <jar-path>`

    <module-path> cesta k .jar souborům knihovny JavaFX
    <jar-path>    cesta ke spustitelnému .jar souboru aplikace
