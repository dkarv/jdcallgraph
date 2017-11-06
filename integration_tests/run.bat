echo Run %1

javac -version

rmdir /s /q target
rmdir /s /q result
mkdir target
mkdir result

for /F %%G in (src\com\dkarv\verifier\*.java) do (
    javac -d target %%G
)

for /F %%G in (src\com\dkarv\testcases\%1\*.java) do (
    javac -d target %%G
)
