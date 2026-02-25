#!/bin/sh

# Maven wrapper script for Windows (PowerShell version)
# This script automatically downloads and runs Maven

$MAVEN_VERSION = "3.9.6"
$MAVEN_HOME = "$PSScriptRoot\.mvn\wrapper\maven-$MAVEN_VERSION"
$MAVEN_URL = "https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"

if (-not (Test-Path $MAVEN_HOME)) {
    Write-Host "Downloading Maven $MAVEN_VERSION..."
    $TempZip = "$env:TEMP\maven.zip"
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile $TempZip
    
    Write-Host "Extracting Maven..."
    New-Item -ItemType Directory -Path "$PSScriptRoot\.mvn\wrapper" -Force | Out-Null
    Expand-Archive -Path $TempZip -DestinationPath "$PSScriptRoot\.mvn\wrapper\"
    Remove-Item $TempZip
}

& "$MAVEN_HOME\bin\mvn.cmd" @args
