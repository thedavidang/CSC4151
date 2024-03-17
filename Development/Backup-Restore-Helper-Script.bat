echo "Enable Developer mode and USB debugging on your phone and plug it in, then press Enter."
pause
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" backup -d -noapk com.spendsages.walletwatch

echo "Launch WSL/Ubuntu and run the command in comment below to backup."
REM dd if=backup.ab bs=1 skip=24 | python3 -c "import zlib,sys;sys.stdout.buffer.write(zlib.decompress(sys.stdin.buffer.read()))" | tar -xvf -

echo "When ready, press Enter to restore the data back on to your phone, but may require that you uninstall WalletWatch from your phone first."
pause
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" restore backup.ab -d