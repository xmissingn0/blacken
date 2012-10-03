@echo off

set FILES=Prereqs.txt Structure.txt HelloWorld.txt BogPeopleGame.txt

mkdir ..\target\book
mkdir ..\target\book\images
xcopy /y images ..\target\book\images
copy pandoc\html.css ..\target\book\html.css
pandoc --data-dir=pandoc --from=markdown --smart --indented-code-classes=java --normalize --no-highlight --epub-cover-image=images\cover.jpg --epub-metadata=metadata.xml -o ..\target\book\BuildingRoguelikesWithBlacken.epub title.txt %FILES%

pandoc --to=html --chapters --data-dir=pandoc --standalone --smart --highlight-style=tango --css=html.css --indented-code-classes=java,numberedLines --normalize --toc -o ..\target\book\BuildingRoguelikesWithBlacken.html title.txt %FILES%

pandoc --to=html --chapters --data-dir=pandoc --smart --highlight-style=tango --indented-code-classes=java,numberedLines --normalize --toc -o ..\target\book\partial.html title.txt %FILES%

REM pandoc --data-dir=pandoc --number-sections --chapters --smart --indented-code-classes=java,numberedLines --highlight-style=monochrome --normalize --toc -o BuildingRoguelikesWithBlacken.pdf %FILES%

cd ..\target\book
C:\opt\kindlegen\kindlegen BuildingRoguelikesWithBlacken.epub -c1 -o BuildingRoguelikesWithBlacken.mobi
cd ..\..\book
