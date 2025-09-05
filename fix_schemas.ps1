# fix_schemas.ps1
# PS 5.1 compatible script that structurally patches Room schema JSON files so that
# ownerId, className, isActive have defaultValue at their first appearance.
# It also updates embedded CREATE TABLE SQL in createSql/setupQueries when needed.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "Fixing Room schema files..."

# Adjust if your exported schema path differs
$schemaDir = "data/schemas/gr.eduinvoice.data.database.EduInvoiceDatabase"
if (!(Test-Path -LiteralPath $schemaDir)) {
  throw "Schema directory not found: $schemaDir"
}

function Set-DefaultValueIfMissing {
  param(
    [Parameter(Mandatory=$true)][object]$field,
    [Parameter(Mandatory=$true)][string]$name,
    [Parameter(Mandatory=$true)][string]$defaultValue
  )
  if ($field.fieldPath -eq $name -and $field.notNull -eq $true) {
    $hasDefault = $false
    if ($null -ne $field.PSObject.Properties['defaultValue']) {
      $hasDefault = -not [string]::IsNullOrEmpty([string]$field.defaultValue)
    }
    if (-not $hasDefault) {
      $field | Add-Member -NotePropertyName defaultValue -NotePropertyValue $defaultValue -Force
      return $true
    }
  }
  return $false
}

$files = Get-ChildItem -LiteralPath $schemaDir -Filter "*.json" | Sort-Object Name
$patchedCount = 0

foreach ($file in $files) {
  Write-Host ("Processing " + $file.Name + "...")

  $jsonText = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
  $json = $jsonText | ConvertFrom-Json

  $filePatched = $false

  # 1) Patch field metadata defaults in entities[*].fields[*]
  if ($null -ne $json -and $null -ne $json.database -and $null -ne $json.database.entities) {
    foreach ($entity in $json.database.entities) {
      if ($null -ne $entity.fields) {
        foreach ($field in $entity.fields) {
          if (Set-DefaultValueIfMissing -field $field -name "ownerId"  -defaultValue "0")  { $filePatched = $true }
          if (Set-DefaultValueIfMissing -field $field -name "className" -defaultValue "''") { $filePatched = $true }
          if (Set-DefaultValueIfMissing -field $field -name "isActive"  -defaultValue "1")  { $filePatched = $true }
        }
      }

      # 2) Patch per-entity createSql (string-based, avoid regex backtick issues)
      if ($null -ne $entity.createSql -and ($entity.createSql -is [string]) -and $entity.createSql.Length -gt 0) {
        $sql = $entity.createSql

        if ($sql -like '*`ownerId` INTEGER NOT NULL*' -and $sql -notlike '*`ownerId` INTEGER NOT NULL DEFAULT*') {
          $sql = $sql.Replace('`ownerId` INTEGER NOT NULL', '`ownerId` INTEGER NOT NULL DEFAULT 0')
          $filePatched = $true
        }
        if ($sql -like '*`className` TEXT NOT NULL*' -and $sql -notlike '*`className` TEXT NOT NULL DEFAULT*') {
          $sql = $sql.Replace('`className` TEXT NOT NULL', '`className` TEXT NOT NULL DEFAULT ''''')
          $filePatched = $true
        }
        if ($sql -like '*`isActive` INTEGER NOT NULL*' -and $sql -notlike '*`isActive` INTEGER NOT NULL DEFAULT*') {
          $sql = $sql.Replace('`isActive` INTEGER NOT NULL', '`isActive` INTEGER NOT NULL DEFAULT 1')
          $filePatched = $true
        }

        $entity.createSql = $sql
      }
    }
  }

  # 3) Patch database.setupQueries[] (some schemas also include CREATE TABLE there)
  if ($null -ne $json -and $null -ne $json.database -and $null -ne $json.database.setupQueries) {
    for ($i = 0; $i -lt $json.database.setupQueries.Count; $i++) {
      $sql = $json.database.setupQueries[$i]
      if (-not ($sql -is [string])) { continue }
      $orig = $sql

      if ($sql -like '*`ownerId` INTEGER NOT NULL*' -and $sql -notlike '*`ownerId` INTEGER NOT NULL DEFAULT*') {
        $sql = $sql.Replace('`ownerId` INTEGER NOT NULL', '`ownerId` INTEGER NOT NULL DEFAULT 0')
      }
      if ($sql -like '*`className` TEXT NOT NULL*' -and $sql -notlike '*`className` TEXT NOT NULL DEFAULT*') {
        $sql = $sql.Replace('`className` TEXT NOT NULL', '`className` TEXT NOT NULL DEFAULT ''''')
      }
      if ($sql -like '*`isActive` INTEGER NOT NULL*' -and $sql -notlike '*`isActive` INTEGER NOT NULL DEFAULT*') {
        $sql = $sql.Replace('`isActive` INTEGER NOT NULL', '`isActive` INTEGER NOT NULL DEFAULT 1')
      }

      if ($sql -ne $orig) {
        $json.database.setupQueries[$i] = $sql
        $filePatched = $true
      }
    }
  }

  if ($filePatched) {
    $json | ConvertTo-Json -Depth 100 | Out-File -FilePath $file.FullName -Encoding UTF8 -NoNewline
    Write-Host ("Fixed " + $file.Name)
    $patchedCount++
  } else {
    Write-Host ("No changes in " + $file.Name)
  }
}

Write-Host ("Patched " + $patchedCount + " file(s).")
Write-Host "Next:  ./gradlew :data:clean :app:clean ; ./gradlew :data:assemblePlainDebug"
