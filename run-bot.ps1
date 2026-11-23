# -----------------------------------------------------------------------------
# Script de inicio rapido en PowerShell con el Bot de Telegram habilitado
# -----------------------------------------------------------------------------

if (Test-Path ".env") {
    Write-Host ">>> Cargando credenciales locales desde .env..."
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $line = $line -replace "^export\s+", ""
            $parts = $line.Split("=", 2)
            if ($parts.Length -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim().Trim('"').Trim("'")
                [System.Environment]::SetEnvironmentVariable($key, $val, "Process")
            }
        }
    }
} else {
    Write-Host ">>> AVISO: No se encontro el archivo .env. Puedes crearlo copiando .env.example"
}

Write-Host ">>> Iniciando Archivo Digital..."
.\mvnw.cmd spring-boot:run
