param(
    [string]$OutputDirectory,
    [string]$Classpath,
    [string]$JavaHome
)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
if (-not $JavaHome) { $JavaHome = Join-Path $project '.tools/jdk-25.0.5+8' }
if (-not $OutputDirectory) { $OutputDirectory = Join-Path $project ('docs/capturas/run-' + (Get-Date -Format 'yyyyMMdd-HHmmss')) }
if (Test-Path -LiteralPath $OutputDirectory) { throw 'Escolha uma pasta nova: evidências existentes não são sobrescritas.' }
$null = New-Item -ItemType Directory -Path $OutputDirectory
$OutputDirectory = (Resolve-Path -LiteralPath $OutputDirectory).Path
$scratch = Join-Path ([IO.Path]::GetTempPath()) ('tellus-demo-' + [guid]::NewGuid().ToString('N'))
$classes = Join-Path $scratch 'classes'
$null = New-Item -ItemType Directory -Path $classes
if (-not $Classpath) {
    $m2 = Join-Path $env:USERPROFILE '.m2/repository'
    $dependencies = @(
        'org/openjfx/javafx-base/25.0.1/javafx-base-25.0.1-win.jar',
        'org/openjfx/javafx-graphics/25.0.1/javafx-graphics-25.0.1-win.jar',
        'org/openjfx/javafx-controls/25.0.1/javafx-controls-25.0.1-win.jar',
        'org/openjfx/javafx-fxml/25.0.1/javafx-fxml-25.0.1-win.jar',
        'io/github/mkpaz/atlantafx-base/2.0.1/atlantafx-base-2.0.1.jar',
        'org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar'
    ) | ForEach-Object { Join-Path $m2 $_ }
    foreach ($jar in $dependencies) { if (-not (Test-Path -LiteralPath $jar)) { throw "Dependência offline ausente: $jar" } }
    $Classpath = $dependencies -join ';'
    $sources = @(Get-ChildItem -LiteralPath (Join-Path $project 'src/main/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
} else { $sources = @() } # Classpath fornecido pelo integrador, por exemplo o JAR já pronto.
$source = Join-Path $project 'src/test/java/com/projekttellus/ui/AcademicDemoCapture.java'
$sources += $source
& (Join-Path $JavaHome 'bin/javac.exe') -encoding UTF-8 -cp $Classpath -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'javac falhou; nenhum Maven foi executado.' }
$runtime = $classes + ';' + (Join-Path $project 'src/main/resources') + ';' + $Classpath
@{ javaHome=$JavaHome; classpath=$Classpath; scratch=$scratch; sources=$sources; desktop='Windows privado, nunca ativado'; mavenExecuted=$false } |
    ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'invocation.json') -Encoding utf8

# O processo gráfico nasce em outro desktop Win32. Não há SwitchDesktop, input global,
# resolução global, Start-Process visível nem janela no desktop do usuário.
if (-not ('TellusPrivateDesktop' -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
public static class TellusPrivateDesktop {
 [StructLayout(LayoutKind.Sequential, CharSet=CharSet.Unicode)] public struct Startup {
  public int cb; public string reserved, desktop, title;
  public int x,y,w,h,xc,yc,fill,flags; public short show, reservedSize;
  public IntPtr reservedPtr, input, output, error;
 }
 [StructLayout(LayoutKind.Sequential)] public struct ProcessInfo { public IntPtr process, thread; public uint pid, tid; }
 [DllImport("user32.dll", CharSet=CharSet.Unicode, SetLastError=true)] public static extern IntPtr CreateDesktop(string name, IntPtr device, IntPtr mode, int flags, uint access, IntPtr security);
 [DllImport("user32.dll")] public static extern bool CloseDesktop(IntPtr desktop);
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode, SetLastError=true)] public static extern bool CreateProcess(string app, System.Text.StringBuilder command, IntPtr ps, IntPtr ts, bool inherit, uint flags, IntPtr env, string cwd, ref Startup startup, out ProcessInfo info);
 [DllImport("kernel32.dll")] public static extern uint WaitForSingleObject(IntPtr handle, uint millis);
 [DllImport("kernel32.dll")] public static extern bool GetExitCodeProcess(IntPtr handle, out uint code);
 [DllImport("kernel32.dll")] public static extern bool CloseHandle(IntPtr handle);
}
'@
}
$desktopName = 'TellusDemo_' + [guid]::NewGuid().ToString('N')
$desktop = [TellusPrivateDesktop]::CreateDesktop($desktopName, [IntPtr]::Zero, [IntPtr]::Zero, 0, 0x1FF, [IntPtr]::Zero)
if ($desktop -eq [IntPtr]::Zero) { throw "CreateDesktop falhou: $([Runtime.InteropServices.Marshal]::GetLastWin32Error())" }
$info = New-Object TellusPrivateDesktop+ProcessInfo
try {
    $startup = New-Object TellusPrivateDesktop+Startup
    $startup.cb = [Runtime.InteropServices.Marshal]::SizeOf($startup)
    $startup.desktop = 'winsta0\' + $desktopName
    $java = Join-Path $JavaHome 'bin/java.exe'
    $command = '"' + $java + '" --enable-native-access=ALL-UNNAMED -Dglass.win.uiScale=1.0 -Dprism.forceGPU=true -cp "' + $runtime + '" com.projekttellus.ui.AcademicDemoCapture "' + $OutputDirectory + '" "' + $scratch + '"'
    $ok = [TellusPrivateDesktop]::CreateProcess($java, [Text.StringBuilder]::new($command), [IntPtr]::Zero, [IntPtr]::Zero, $false, 0x08000000, [IntPtr]::Zero, $project, [ref]$startup, [ref]$info)
    if (-not $ok) { throw "CreateProcess falhou: $([Runtime.InteropServices.Marshal]::GetLastWin32Error())" }
    while ([TellusPrivateDesktop]::WaitForSingleObject($info.process, 1000) -eq 258) {
        # Harness tem watchdog próprio de 150 s; cada espera aqui dura somente 1 s.
    }
    [uint32]$exitCode = 1
    $null = [TellusPrivateDesktop]::GetExitCodeProcess($info.process, [ref]$exitCode)
    Get-Content -LiteralPath (Join-Path $OutputDirectory 'execution.log') -ErrorAction SilentlyContinue
    if ($exitCode -ne 0) { throw "Captura falhou ($exitCode): $OutputDirectory" }
    Write-Output "Evidências: $OutputDirectory"
} finally {
    if ($info.thread -ne [IntPtr]::Zero) { $null = [TellusPrivateDesktop]::CloseHandle($info.thread) }
    if ($info.process -ne [IntPtr]::Zero) { $null = [TellusPrivateDesktop]::CloseHandle($info.process) }
    $null = [TellusPrivateDesktop]::CloseDesktop($desktop)
}
