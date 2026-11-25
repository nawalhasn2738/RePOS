$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$outputRoot = Join-Path $tempRoot ('RePOS-refactoring-build-' + [guid]::NewGuid().ToString('N'))
$resolvedOutput = [IO.Path]::GetFullPath($outputRoot)
if (-not $resolvedOutput.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe build output path' }
[IO.Directory]::CreateDirectory($resolvedOutput) | Out-Null
try {
    $classes = Join-Path $resolvedOutput 'classes'
    [IO.Directory]::CreateDirectory($classes) | Out-Null
    $sourceRoots = @((Join-Path $projectRoot 'src/main/java'), (Join-Path $projectRoot 'src/test/java'))
    $sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoots -Recurse -File -Filter '*.java' | Sort-Object FullName)
    $argumentsFile = Join-Path $resolvedOutput 'sources.txt'
    $quotedPaths = @($sourceFiles | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
    [IO.File]::WriteAllLines($argumentsFile, [string[]]$quotedPaths, (New-Object Text.UTF8Encoding($false)))
    & javac -version
    if ($LASTEXITCODE -ne 0) { throw 'javac unavailable' }
    & javac --release 8 -encoding UTF-8 -d $classes "@$argumentsFile"
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed: exit $LASTEXITCODE" }
    Write-Output "Compiled $($sourceFiles.Count) source files with --release 8"
    & java '-Djava.awt.headless=true' -cp $classes repos.refactoring.RefactoringTests
    if ($LASTEXITCODE -ne 0) { throw "Tests failed: exit $LASTEXITCODE" }
} finally {
    if ([IO.Path]::GetFullPath($resolvedOutput).StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase) -and
        (Split-Path -Leaf $resolvedOutput).StartsWith('RePOS-refactoring-build-')) {
        Remove-Item -LiteralPath $resolvedOutput -Recurse -Force
    } else { throw 'Refusing unsafe build cleanup' }
}
