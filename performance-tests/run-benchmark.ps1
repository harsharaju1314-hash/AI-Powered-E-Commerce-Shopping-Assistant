# ==============================================================================
# AI-Powered E-Commerce Shopping Assistant — Windows Concurrency Benchmark
# ==============================================================================
param (
    [string]$HostName = "localhost",
    [int]$Port = 8080,
    [int]$Threads = 500,
    [int]$RampUp = 30,
    [int]$Loops = 10
)

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " Starting Concurrency Benchmark (500+ Concurrent Requests)" -ForegroundColor Cyan
Write-Host " Target: http://${HostName}:${Port} | Threads: $Threads | Loops: $Loops" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan

$resultsDir = "performance-tests/results"
if (-not (Test-Path $resultsDir)) { New-Item -ItemType Directory -Path $resultsDir -Force | Out-Null }

$jmeterExe = Get-Command "jmeter" -ErrorAction SilentlyContinue

if ($jmeterExe) {
    Write-Host "[INFO] Executing Apache JMeter test plan..." -ForegroundColor Green
    & jmeter -n -t performance-tests/ecommerce-benchmark.jmx -Jhost=$HostName -Jport=$Port -Jthreads=$Threads -Jrampup=$RampUp -Jloops=$Loops -l "$resultsDir/results.jtl" -e -o "$resultsDir/report"
} else {
    Write-Host "[INFO] JMeter not in PATH. Running concurrent PowerShell workload simulation..." -ForegroundColor Yellow
    $url = "http://${HostName}:${Port}/api/products?page=0&size=20"
    Write-Host "Testing endpoint: $url"
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $tasks = 1..$Threads | ForEach-Object {
        [System.Threading.Tasks.Task]::Run([Action]{
            try {
                $sw = [System.Diagnostics.Stopwatch]::StartNew()
                $req = [System.Net.WebRequest]::Create("http://${HostName}:${Port}/api/products?page=0&size=20")
                $req.Timeout = 5000
                $resp = $req.GetResponse()
                $sw.Stop()
                $resp.Close()
                [PSCustomObject]@{ Success = $true; LatencyMs = $sw.ElapsedMilliseconds }
            } catch {
                [PSCustomObject]@{ Success = $false; LatencyMs = 500 }
            }
        })
    }
    
    $completed = [System.Threading.Tasks.Task]::WhenAll($tasks)
    $completed.Wait()
    $stopwatch.Stop()
    
    Write-Host "Benchmark Completed in $($stopwatch.ElapsedMilliseconds) ms" -ForegroundColor Green
}
