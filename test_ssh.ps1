# PowerShell script to deploy to remote server
$ErrorActionPreference = "Continue"

$server = "49.235.161.106"
$port = 22
$user = "root"

Write-Host "Testing SSH connection to $server..." -ForegroundColor Green

# Test basic SSH connection
$testCmd = "echo 'SSH connection successful'"
$sshArgs = "-o StrictHostKeyChecking=no -o ConnectTimeout=10 -p $port $user@$server `"$testCmd`""

try {
    $output = & ssh $sshArgs.Split(' ') 2>&1
    Write-Host "SSH Test Result: $output" -ForegroundColor Green
} catch {
    Write-Host "SSH connection failed: $_" -ForegroundColor Red
}
