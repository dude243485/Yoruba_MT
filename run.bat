@echo off
REM ============================================================
REM  run.bat — Compile and run the Yoruba NL Interpreter
REM  Usage: run.bat
REM  Run from the project root directory.
REM ============================================================

set SOURCES=
for /R src\main\java %%f in (*.java) do set SOURCES=!SOURCES! "%%f"
setlocal EnableDelayedExpansion

set CP=lib\jackson-databind-2.17.0.jar;lib\jackson-core-2.17.0.jar;lib\jackson-annotations-2.17.0.jar
set OUT=out\production\YorubaInterpreter

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling...
javac -encoding UTF-8 -cp "%CP%" -d "%OUT%" src\main\java\com\nlinterpreter\model\TokenType.java src\main\java\com\nlinterpreter\model\Token.java src\main\java\com\nlinterpreter\lexicon\Lexicon.java src\main\java\com\nlinterpreter\lexicon\JsonLexiconLoader.java src\main\java\com\nlinterpreter\lexer\Lexer.java src\main\java\com\nlinterpreter\parser\Parser.java src\main\java\com\nlinterpreter\phonetic\PhoneticEngine.java src\main\java\com\nlinterpreter\error\ErrorReporter.java src\main\java\com\nlinterpreter\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo COMPILATION FAILED.
    exit /b 1
)

echo Compilation successful. Running...
echo.
java -cp "%OUT%;%CP%" com.nlinterpreter.Main
