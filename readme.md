# Dijkstra Demo
**Készítette: Kovács Dániel (WQU2KS), Gazd.info. MSc, SZTE TTIK**
---
Ez a projekt az *Optimalizálás Alkalmazásai* kurzus teljesítése végett jött létre. A feladat a legrövidebb út feladat megoldása a Dijkstra algoritmussal, JAVA nyelven, Androidos eszközökre.

A programhoz készült egy [használati útmutató]() is.
## Követelmények
Kötelező elemek, amelyek az [előleadás]() során kerültek rögzítésre:
- Tetszőleges, irányítatlan, súlyozott gráf
- Gráf létrehozása csúcsok és élek meghatározásával
- Gráf véletlenszerű generálása
- Csúcsok és élek utólag szerkeszthetőek
- Csúcsok mozgathatóak
- Google Guava Graph csomag használata
- Dijkstra algoritmus lefutása után minden csúcsra látható az összköltség és az oda vezető út

## Funkciók
Csúcs (Node)
- felvétele
- átnevezése
- mozgatása
- törlése

Él (Edge)
- felvétele
- költségének módosítása
- törlése

Gráf (Graph)
- generálása
- törlése
- (Bővítése az előbbi funkciók segítségével)

Dijkstra algoritmus
- lefuttatás egy kijelölt kezdőcsúcsból
- útvonal és költség megjelenítése minden csúcsra vonatkozóan

További funkciók
- Csúcsra vagy élre kattintva megjelenik róla az információ
- Dijkstra algoritmus futtatása után egy csúcsra kattintva látható az útvonal és megjelenik az arra vonatkozó információ

## Felhasznált irodalom
- Farkas Richárd: Algoritmusok és adatszerkezetek I. Előadás jegyzet (2019.)
- [Wikipédia](https://hu.wikipedia.org/wiki/Dijkstra-algoritmus) (2022.)
- [Guava Graph csomag](https://www.javadoc.io/doc/com.google.guava/guava/23.5-android/com/google/common/graph/Graphs.html) (2022.)
