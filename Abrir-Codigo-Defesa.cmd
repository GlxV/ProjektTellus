@echo off
rem Explicit user invocation only; -ListOnly never opens an editor.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\Open-DefenseSources.ps1" %*
exit /b %errorlevel%
