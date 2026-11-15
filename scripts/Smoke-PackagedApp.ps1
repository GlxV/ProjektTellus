param([string]$JavaHome, [string]$ReportPath)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
if (-not $JavaHome) { $JavaHome = Join-Path $project '.tools/jdk-25.0.5+8' }
if (-not $ReportPath) { $ReportPath = Join-Path $project 'docs/evidencias/package-launch.json' }
$java = (Resolve-Path -LiteralPath (Join-Path $JavaHome 'bin/java.exe')).Path
$jar = (Resolve-Path -LiteralPath (Join-Path $project 'target/ProjektTellus.jar')).Path
$temporary = Join-Path ([IO.Path]::GetTempPath()) ('tellus-package-smoke-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $temporary
$database = Join-Path $temporary 'package.db'
$desktopName = 'TellusPackage_' + [guid]::NewGuid().ToString('N')
$expectedTitle = 'ProjektTellus — Simulador Planetario'
$report = [ordered]@{
    status='FAIL'; startedUtc=[DateTime]::UtcNow.ToString('o'); java=$java
    javaVersion=((& $java -version 2>&1 | ForEach-Object ToString) -join "`n")
    jar=$jar; jarSha256=(Get-FileHash -LiteralPath $jar).Hash.ToLowerInvariant()
    jarSha256After=$null; database=$database; databaseWasAbsent=(-not (Test-Path -LiteralPath $database))
    desktop=('winsta0\' + $desktopName); privateDesktop=$true; desktopActivated=$false
    pidCreated=$null; windowTitle=$null; windowHandle=$null; processAliveAtVerification=$false
    databaseHeaderVersion=$null; schema=$null; closeMethod=$null; exitCode=$null
    mavenExecuted=$false; error=$null
    limits='Abertura do JAR real e criação do schema local somente. Não equivale a testar PC/projetor da faculdade, fluidez/FPS ou fluxo completo de interação.'
}

if (-not ('TellusPackageNative' -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.Text;
using System.Collections.Generic;
using System.Runtime.InteropServices;
public static class TellusPackageNative {
 [StructLayout(LayoutKind.Sequential, CharSet=CharSet.Unicode)] public struct Startup {
  public int cb; public string reserved, desktop, title;
  public int x,y,w,h,xc,yc,fill,flags; public short show,reservedSize;
  public IntPtr reservedPtr,input,output,error;
 }
 [StructLayout(LayoutKind.Sequential)] public struct Info { public IntPtr process,thread; public uint pid,tid; }
 public sealed class WindowInfo { public IntPtr Handle; public string Title; public uint Pid; }
 public delegate bool EnumCallback(IntPtr window, IntPtr parameter);
 [DllImport("user32.dll", CharSet=CharSet.Unicode, SetLastError=true)] public static extern IntPtr CreateDesktop(string name,IntPtr device,IntPtr mode,int flags,uint access,IntPtr security);
 [DllImport("user32.dll")] public static extern bool CloseDesktop(IntPtr desktop);
 [DllImport("user32.dll", SetLastError=true)] static extern bool EnumDesktopWindows(IntPtr desktop,EnumCallback callback,IntPtr parameter);
 [DllImport("user32.dll", CharSet=CharSet.Unicode)] static extern int GetWindowText(IntPtr window,StringBuilder text,int max);
 [DllImport("user32.dll")] static extern uint GetWindowThreadProcessId(IntPtr window,out uint pid);
 [DllImport("user32.dll")] static extern bool IsWindowVisible(IntPtr window);
 [DllImport("user32.dll", CharSet=CharSet.Unicode, SetLastError=true)] public static extern bool PostMessage(IntPtr window,uint msg,IntPtr w,IntPtr l);
 [DllImport("kernel32.dll", CharSet=CharSet.Unicode, SetLastError=true)] public static extern bool CreateProcess(string app,StringBuilder command,IntPtr ps,IntPtr ts,bool inherit,uint flags,IntPtr env,string cwd,ref Startup startup,out Info info);
 [DllImport("kernel32.dll")] public static extern uint WaitForSingleObject(IntPtr handle,uint millis);
 [DllImport("kernel32.dll")] public static extern bool GetExitCodeProcess(IntPtr handle,out uint code);
 [DllImport("kernel32.dll")] public static extern bool TerminateProcess(IntPtr handle,uint code);
 [DllImport("kernel32.dll")] public static extern bool CloseHandle(IntPtr handle);
 public static WindowInfo[] Windows(IntPtr desktop,uint ownedPid) {
  var found=new List<WindowInfo>();
  EnumCallback callback=(window,parameter)=> {
   uint pid; GetWindowThreadProcessId(window,out pid);
   if(pid==ownedPid && IsWindowVisible(window)) {
    var title=new StringBuilder(1024); GetWindowText(window,title,title.Capacity);
    found.Add(new WindowInfo {Handle=window,Title=title.ToString(),Pid=pid});
   }
   return true;
  };
  if(!EnumDesktopWindows(desktop,callback,IntPtr.Zero)) {
   int error=Marshal.GetLastWin32Error();
   if(error!=0)throw new System.ComponentModel.Win32Exception(error);
  }
  GC.KeepAlive(callback); return found.ToArray();
 }
}
'@
}
$desktop = [IntPtr]::Zero
$info = New-Object TellusPackageNative+Info
$ownedWindow = $null
try {
    $desktop = [TellusPackageNative]::CreateDesktop($desktopName,[IntPtr]::Zero,[IntPtr]::Zero,0,0x1FF,[IntPtr]::Zero)
    if ($desktop -eq [IntPtr]::Zero) { throw 'CreateDesktop falhou.' }
    $startup = New-Object TellusPackageNative+Startup
    $startup.cb = [Runtime.InteropServices.Marshal]::SizeOf($startup)
    $startup.desktop = $report.desktop
    $command = '"' + $java + '" --enable-native-access=ALL-UNNAMED "-Dprojekttellus.db=' + $database + '" -jar "' + $jar + '"'
    $report.commandLine = $command
    if (-not [TellusPackageNative]::CreateProcess($java,[Text.StringBuilder]::new($command),[IntPtr]::Zero,[IntPtr]::Zero,$false,0x08000000,[IntPtr]::Zero,$project,[ref]$startup,[ref]$info)) {
        throw "CreateProcess falhou: $([Runtime.InteropServices.Marshal]::GetLastWin32Error())"
    }
    $report.pidCreated = $info.pid
    $deadline = [DateTime]::UtcNow.AddSeconds(30)
    while ([DateTime]::UtcNow -lt $deadline) {
        if ([TellusPackageNative]::WaitForSingleObject($info.process,0) -ne 258) { throw 'JAR encerrou antes da verificação.' }
        $ownedWindow = [TellusPackageNative]::Windows($desktop,$info.pid) | Where-Object Title -eq $expectedTitle | Select-Object -First 1
        if (Test-Path -LiteralPath $database) {
            $stream = [IO.File]::Open($database,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::ReadWrite)
            try {
                if ($stream.Length -ge 100) {
                    $header = New-Object byte[] 100; $null=$stream.Read($header,0,100)
                    $report.databaseHeaderVersion = [int]$header[60]*16777216 + [int]$header[61]*65536 + [int]$header[62]*256 + [int]$header[63]
                }
            } finally { $stream.Dispose() }
        }
        if ($ownedWindow -and $report.databaseHeaderVersion -eq 1) { break }
        Start-Sleep -Milliseconds 100
    }
    if (-not $ownedWindow) { throw 'Janela com título real não encontrada no desktop privado.' }
    $report.windowTitle = $ownedWindow.Title
    $report.windowHandle = ('0x{0:X}' -f $ownedWindow.Handle.ToInt64())
    if ($report.databaseHeaderVersion -ne 1) { throw 'Banco não chegou ao schema versão 1.' }
    # Source-file probe only opens an already-existing database READ ONLY. It never runs migrations.
    $probe = Join-Path $temporary 'PackageSchemaProbe.java'
    @'
import java.sql.*;
import java.util.*;
import org.sqlite.SQLiteConfig;
class PackageSchemaProbe {
 public static void main(String[] args) throws Exception {
  SQLiteConfig config=new SQLiteConfig(); config.setReadOnly(true);
  try(var c=DriverManager.getConnection("jdbc:sqlite:"+args[0],config.toProperties());var s=c.createStatement()) {
   var tables=new TreeSet<String>();
   try(var r=s.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {while(r.next())tables.add(r.getString(1));}
   if(!tables.containsAll(List.of("mundos","eventos","snapshots")))throw new AssertionError(tables);
   int version; try(var r=s.executeQuery("PRAGMA user_version")){r.next();version=r.getInt(1);}
   if(version!=1)throw new AssertionError("version "+version);
   try(var r=s.executeQuery("PRAGMA quick_check")){r.next();if(!r.getString(1).equals("ok"))throw new AssertionError("quick_check");}
   for(String table:List.of("mundos","eventos","snapshots"))try(var r=s.executeQuery("SELECT count(*) FROM "+table)){r.next();if(r.getLong(1)!=0)throw new AssertionError("not empty "+table);}
   System.out.println("{\"userVersion\":1,\"tables\":[\"eventos\",\"mundos\",\"snapshots\"],\"allTablesEmpty\":true,\"quickCheck\":\"ok\",\"probeReadOnly\":true}");
  }
 }
}
'@ | Set-Content -LiteralPath $probe -Encoding utf8
    $probeLines = @(& $java --enable-native-access=ALL-UNNAMED -cp $jar $probe $database 2> (Join-Path $temporary 'schema-probe.err.log'))
    if ($LASTEXITCODE -ne 0) { throw 'Verificação JDBC do schema falhou.' }
    $report.schema = ($probeLines -join "`n") | ConvertFrom-Json
    $report.processAliveAtVerification = [TellusPackageNative]::WaitForSingleObject($info.process,0) -eq 258
    if (-not $report.processAliveAtVerification) { throw 'Processo não permaneceu vivo até verificar schema.' }
    # Re-enumerate by owned PID before sending WM_CLOSE; never close another process's window.
    $ownedWindow = [TellusPackageNative]::Windows($desktop,$info.pid) | Where-Object { $_.Title -eq $expectedTitle -and $_.Handle.ToInt64() -eq [Convert]::ToInt64($report.windowHandle.Substring(2),16) } | Select-Object -First 1
    if (-not $ownedWindow) { throw 'A janela própria não está mais disponível para WM_CLOSE.' }
    if (-not [TellusPackageNative]::PostMessage($ownedWindow.Handle,0x0010,[IntPtr]::Zero,[IntPtr]::Zero)) { throw 'PostMessage WM_CLOSE falhou.' }
    $report.closeMethod = 'WM_CLOSE, janela do PID criado'
    $deadline = [DateTime]::UtcNow.AddSeconds(10)
    while ([TellusPackageNative]::WaitForSingleObject($info.process,250) -eq 258 -and [DateTime]::UtcNow -lt $deadline) { }
    if ([TellusPackageNative]::WaitForSingleObject($info.process,0) -eq 258) { throw 'Aplicativo não encerrou após WM_CLOSE.' }
    [uint32]$exitCode = 1
    $null = [TellusPackageNative]::GetExitCodeProcess($info.process,[ref]$exitCode)
    $report.exitCode = $exitCode
    $report.jarSha256After = (Get-FileHash -LiteralPath $jar).Hash.ToLowerInvariant()
    if ($exitCode -ne 0 -or $report.jarSha256After -ne $report.jarSha256) { throw 'Saída não zero ou JAR mudou durante smoke.' }
    $report.status = 'PASS'
} catch {
    $report.error = $_.Exception.Message
} finally {
    if ($info.process -ne [IntPtr]::Zero) {
        if ([TellusPackageNative]::WaitForSingleObject($info.process,0) -eq 258) {
            $null = [TellusPackageNative]::TerminateProcess($info.process,90)
            $null = [TellusPackageNative]::WaitForSingleObject($info.process,5000)
            $report.closeMethod = 'TerminateProcess no HANDLE do processo criado; smoke FAIL'
        }
        [uint32]$finalExit = 1
        $null=[TellusPackageNative]::GetExitCodeProcess($info.process,[ref]$finalExit)
        $report.exitCode=$finalExit
        $null=[TellusPackageNative]::CloseHandle($info.thread)
        $null=[TellusPackageNative]::CloseHandle($info.process)
    }
    if ($desktop -ne [IntPtr]::Zero) { $null=[TellusPackageNative]::CloseDesktop($desktop) }
    $report.finishedUtc=[DateTime]::UtcNow.ToString('o')
    $report.databaseBytes=if (Test-Path -LiteralPath $database) { (Get-Item -LiteralPath $database).Length } else { 0 }
    $report.jarSha256After=(Get-FileHash -LiteralPath $jar).Hash.ToLowerInvariant()
    $null=New-Item -ItemType Directory -Path (Split-Path -Parent $ReportPath) -Force
    $report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $ReportPath -Encoding utf8
}
$report | ConvertTo-Json -Depth 6
if ($report.status -ne 'PASS') { throw "Smoke do JAR falhou: $($report.error)" }
