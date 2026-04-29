@echo off
cd /d "%~dp0"
java --module-path . --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing,javafx.web -jar VehicleID.jar
pause