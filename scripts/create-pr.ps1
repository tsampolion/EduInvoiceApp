# Simple script to create a PR from Cursor changes
# This script is designed to be beginner-friendly

Write-Host "🚀 Creating Pull Request from Cursor changes..." -ForegroundColor Green

# Ask for a simple description of what you changed
$Description = Read-Host "What did you change? (e.g., 'Fixed login bug', 'Added new feature')"

# Create a branch name with today's date
$Date = Get-Date -Format "MMdd"
$BranchName = "cursor-changes-$Date"

Write-Host "�� Creating branch: $BranchName" -ForegroundColor Yellow

# Create and switch to new branch
git checkout -b $BranchName

# Add all your changes
git add .

# Commit with your description
git commit -m "Cursor changes: $Description"

# Push to GitHub
git push -u origin $BranchName

# Create the Pull Request
Write-Host "🔗 Creating Pull Request..." -ForegroundColor Yellow

gh pr create --title "Cursor Changes: $Description" --body "Changes made in Cursor IDE: $Description" --base main

Write-Host "✅ Done! Your Pull Request has been created!" -ForegroundColor Green
Write-Host "🌿 Branch: $BranchName" -ForegroundColor Cyan
Write-Host "�� You can now review and merge your changes on GitHub" -ForegroundColor Cyan 