# Comprehensive Test Execution Script
# This script runs all test categories and generates detailed reports

param(
    [string]$TestCategory = "all",
    [string]$OutputDir = "test-reports",
    [switch]$GenerateReport = $true,
    [switch]$Verbose = $false
)

Write-Host "Starting Comprehensive Test Suite Execution" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Set environment variables for testing
$env:ANDROID_HOME = "C:\Users\dimit\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\dimit\AppData\Local\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Add Android SDK tools to PATH
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
$env:PATH += ";$env:ANDROID_HOME\tools"
$env:PATH += ";$env:ANDROID_HOME\tools\bin"
$env:PATH += ";$env:JAVA_HOME\bin"

# Create output directory
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    Write-Host "Created output directory: $OutputDir" -ForegroundColor Yellow
}

# Get current timestamp for report naming
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$reportFile = "$OutputDir\test-execution-report-$timestamp.txt"
$htmlReportFile = "$OutputDir\test-execution-report-$timestamp.html"

# Initialize test results
$testResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    Skipped = 0
    StartTime = Get-Date
    EndTime = $null
    Categories = @{}
}

function Write-TestLog {
    param([string]$Message, [string]$Level = "INFO")
    
    $timestamp = Get-Date -Format "HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    switch ($Level) {
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        "WARN" { Write-Host $logMessage -ForegroundColor Yellow }
        "SUCCESS" { Write-Host $logMessage -ForegroundColor Green }
        default { Write-Host $logMessage -ForegroundColor White }
    }
    
    # Write to report file
    Add-Content -Path $reportFile -Value $logMessage
}

function Run-TestCategory {
    param(
        [string]$Category,
        [string]$Description,
        [string]$Command
    )
    
    Write-TestLog "Starting $Description..." "INFO"
    $categoryStartTime = Get-Date
    
    try {
        $testResults.Categories[$Category] = @{
            Status = "RUNNING"
            StartTime = $categoryStartTime
            EndTime = $null
            Duration = $null
            Passed = 0
            Failed = 0
            Skipped = 0
            Errors = @()
        }
        
        if ($Verbose) {
            Write-TestLog "Executing command: $Command" "INFO"
        }
        
        # Execute the test command
        $result = Invoke-Expression $Command 2>&1
        
        $categoryEndTime = Get-Date
        $duration = $categoryEndTime - $categoryStartTime
        
        $testResults.Categories[$Category].EndTime = $categoryEndTime
        $testResults.Categories[$Category].Duration = $duration
        
        # Parse results (this is a simplified version - in practice, you'd parse the actual test output)
        if ($LASTEXITCODE -eq 0) {
            $testResults.Categories[$Category].Status = "PASSED"
            Write-TestLog "$Description completed successfully in $($duration.TotalSeconds.ToString('F2')) seconds" "SUCCESS"
        } else {
            $testResults.Categories[$Category].Status = "FAILED"
            Write-TestLog "$Description failed with exit code $LASTEXITCODE" "ERROR"
            $testResults.Categories[$Category].Errors += $result
        }
        
    } catch {
        $categoryEndTime = Get-Date
        $duration = $categoryEndTime - $categoryStartTime
        
        $testResults.Categories[$Category].EndTime = $categoryEndTime
        $testResults.Categories[$Category].Duration = $duration
        $testResults.Categories[$Category].Status = "ERROR"
        $testResults.Categories[$Category].Errors += $_.Exception.Message
        
        Write-TestLog "$Description failed with exception: $($_.Exception.Message)" "ERROR"
    }
}

function Generate-HtmlReport {
    param([string]$OutputPath)
    
    $html = @"
<!DOCTYPE html>
<html>
<head>
    <title>Comprehensive Test Execution Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 30px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .metric { background-color: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #007bff; }
        .metric.passed { border-left-color: #28a745; }
        .metric.failed { border-left-color: #dc3545; }
        .metric.skipped { border-left-color: #ffc107; }
        .category { margin-bottom: 20px; border: 1px solid #dee2e6; border-radius: 8px; overflow: hidden; }
        .category-header { background-color: #f8f9fa; padding: 15px; border-bottom: 1px solid #dee2e6; }
        .category-header.passed { background-color: #d4edda; border-color: #c3e6cb; }
        .category-header.failed { background-color: #f8d7da; border-color: #f5c6cb; }
        .category-header.error { background-color: #fff3cd; border-color: #ffeaa7; }
        .category-content { padding: 15px; }
        .error-list { background-color: #f8f9fa; padding: 10px; border-radius: 4px; margin-top: 10px; }
        .error-item { color: #dc3545; margin: 5px 0; }
        .footer { text-align: center; margin-top: 30px; color: #6c757d; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Comprehensive Test Execution Report</h1>
            <p>Generated on: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</p>
        </div>
        
        <div class="summary">
            <div class="metric">
                <h3>Total Categories</h3>
                <p>$($testResults.Categories.Count)</p>
            </div>
            <div class="metric passed">
                <h3>Passed</h3>
                <p>$($testResults.Categories.Values | Where-Object { $_.Status -eq "PASSED" } | Measure-Object | Select-Object -ExpandProperty Count)</p>
            </div>
            <div class="metric failed">
                <h3>Failed</h3>
                <p>$($testResults.Categories.Values | Where-Object { $_.Status -eq "FAILED" } | Measure-Object | Select-Object -ExpandProperty Count)</p>
            </div>
            <div class="metric error">
                <h3>Errors</h3>
                <p>$($testResults.Categories.Values | Where-Object { $_.Status -eq "ERROR" } | Measure-Object | Select-Object -ExpandProperty Count)</p>
            </div>
            <div class="metric">
                <h3>Total Duration</h3>
                <p>$($testResults.EndTime - $testResults.StartTime | ForEach-Object { $_.TotalSeconds.ToString('F2') + 's' })</p>
            </div>
        </div>
        
        <div class="categories">
"@

    foreach ($category in $testResults.Categories.Keys) {
        $categoryData = $testResults.Categories[$category]
        $statusClass = $categoryData.Status.ToLower()
        
        $html += @"
            <div class="category">
                <div class="category-header $statusClass">
                    <h3>$category</h3>
                    <p>Status: $($categoryData.Status) | Duration: $($categoryData.Duration.TotalSeconds.ToString('F2'))s</p>
                </div>
                <div class="category-content">
"@

                 if ($categoryData.Errors.Count -gt 0) {
             $html += '<div class="error-list">'
             $html += '<h4>Errors:</h4>'
             foreach ($errorItem in $categoryData.Errors) {
                 $html += "<div class='error-item'>$errorItem</div>"
             }
             $html += '</div>'
         }

        $html += @"
                </div>
            </div>
"@
    }

    $html += @"
        </div>
        
        <div class="footer">
            <p>Test execution completed at $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</p>
        </div>
    </div>
</body>
</html>
"@

    Set-Content -Path $OutputPath -Value $html
    Write-TestLog "HTML report generated: $OutputPath" "SUCCESS"
}

# Start test execution
Write-TestLog "Test execution started at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "INFO"

# Define test categories and their execution commands
$testCategories = @{
    "unit" = @{
        Description = "Unit Tests"
        Command = "cd $PSScriptRoot\.. && ./gradlew test --tests '*UnitTest*'"
    }
    "integration" = @{
        Description = "Integration Tests"
        Command = "cd $PSScriptRoot\.. && ./gradlew test --tests '*IntegrationTest*'"
    }
    "performance" = @{
        Description = "Performance Tests"
        Command = "cd $PSScriptRoot\.. && ./gradlew test --tests '*PerformanceTest*'"
    }
    "stress" = @{
        Description = "Stress Tests"
        Command = "cd $PSScriptRoot\.. && ./gradlew test --tests '*StressTest*'"
    }
    "ui" = @{
        Description = "UI Automation Tests"
        Command = "cd $PSScriptRoot\.. && ./gradlew connectedAndroidTest --tests '*UiAutomationTest*'"
    }
    "comprehensive" = @{
        Description = "Comprehensive Test Runner"
        Command = "cd $PSScriptRoot\.. && ./gradlew test --tests '*ComprehensiveTestRunner*'"
    }
}

# Execute tests based on category parameter
if ($TestCategory -eq "all") {
    Write-TestLog "Executing all test categories..." "INFO"
    foreach ($category in $testCategories.Keys) {
        $categoryInfo = $testCategories[$category]
        Run-TestCategory -Category $category -Description $categoryInfo.Description -Command $categoryInfo.Command
    }
} elseif ($testCategories.ContainsKey($TestCategory)) {
    Write-TestLog "Executing $TestCategory tests..." "INFO"
    $categoryInfo = $testCategories[$TestCategory]
    Run-TestCategory -Category $TestCategory -Description $categoryInfo.Description -Command $categoryInfo.Command
} else {
    Write-TestLog "Invalid test category: $TestCategory" "ERROR"
    Write-TestLog "Available categories: $($testCategories.Keys -join ', ')" "INFO"
    exit 1
}

# Calculate final results
$testResults.EndTime = Get-Date
$totalDuration = $testResults.EndTime - $testResults.StartTime

# Generate summary
Write-TestLog "Test execution completed at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "INFO"
Write-TestLog "Total duration: $($totalDuration.TotalSeconds.ToString('F2')) seconds" "INFO"

$passedCategories = ($testResults.Categories.Values | Where-Object { $_.Status -eq "PASSED" }).Count
$failedCategories = ($testResults.Categories.Values | Where-Object { $_.Status -eq "FAILED" }).Count
$errorCategories = ($testResults.Categories.Values | Where-Object { $_.Status -eq "ERROR" }).Count

Write-TestLog "Summary: $passedCategories passed, $failedCategories failed, $errorCategories errors" "INFO"

# Generate reports
if ($GenerateReport) {
    Write-TestLog "Generating reports..." "INFO"
    Generate-HtmlReport -OutputPath $htmlReportFile
}

# Display final status
if ($failedCategories -eq 0 -and $errorCategories -eq 0) {
    Write-TestLog "All test categories completed successfully!" "SUCCESS"
    exit 0
} else {
    Write-TestLog "Some test categories failed. Check the report for details." "WARN"
    exit 1
}
