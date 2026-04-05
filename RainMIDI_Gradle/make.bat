call .\gradlew.bat :RainMIDI_launcher:fatJar

mkdir RainMIDI_vX.XX

copy .\RainMIDI_launcher\build\libs\RainMIDI_launcher-all-1.0.jar .\RainMIDI_vX.XX\RainMIDI.jar

xcopy .\RainMIDI_launcher\ext\res .\RainMIDI_vX.XX\res /S /E /I /H /Y
xcopy .\RainMIDI_launcher\ext\jre .\RainMIDI_vX.XX\jre /S /E /I /H /Y
copy .\RainMIDI_launcher\ext\*.bat .\RainMIDI_vX.XX
copy .\RainMIDI_launcher\ext\*.exe .\RainMIDI_vX.XX
copy .\RainMIDI_launcher\ext\*.txt .\RainMIDI_vX.XX
copy ..\docs\index.html .\RainMIDI_vX.XX\manual.html
copy ..\docs\index.en.html .\RainMIDI_vX.XX\manual.en.html

pause